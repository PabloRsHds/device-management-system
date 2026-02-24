package br.com.device_login.service;

import br.com.device_login.dtos.loginDto.RequestLoginDto;
import br.com.device_login.dtos.loginDto.ResponseUserForLogin;
import br.com.device_login.dtos.tokenDto.RequestTokensDto;
import br.com.device_login.dtos.tokenDto.ResponseTokens;
import br.com.device_login.infra.exceptions.InvalidCredentialsException;
import br.com.device_login.infra.exceptions.ServiceUnavailableException;
import br.com.device_login.metrics.login.LoginMetrics;
import br.com.device_login.microservice.UserClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);
    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final LoginMetrics loginMetrics;

    @Autowired
    public LoginService(
            UserClient userClient,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            LoginMetrics loginMetrics) {
        this.userClient = userClient;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.loginMetrics = loginMetrics;
    }

    // ========================================== LOGIN ==============================================================

    public ResponseTokens login(RequestLoginDto request) {

        // Inicio o Timer
        var timeSample = this.loginMetrics.startTimer();

        // Faço uma verificação para ver se o usuário existe, e também verifico se o e-mail e a senha estão corretos
        var user = this.verifyUser(request.email(), request.password(), timeSample);

        // Retorno os tokens caso o usuário exista
        var tokens = this.generateTokens(user.userId(), user.role());

        this.loginMetrics.loginSuccess();
        this.loginMetrics.stopSuccessLoginTimer(timeSample);
        return tokens;
    }

    public ResponseUserForLogin verifyUser(String email, String password, Timer.Sample timeSample) {
        log.debug("Verificando se o e-mail está cadastrado no banco de dados: {}", email);
        var user = this.getUser(email, null);

        if (user == null) {

            log.info("Usuário não encontrado no banco de dados!");
            this.loginMetrics.userNotFound();
            this.loginMetrics.stopFailedLoginTimer(timeSample);
            throw new InvalidCredentialsException("Email or Password is incorrect");
        }

        if (!this.passwordEncoder.matches(password, user.password())) {

            log.info("Usuário encontrado, mas a senha está incorreta!");
            this.loginMetrics.invalidCredentials();
            this.loginMetrics.stopFailedLoginTimer(timeSample);
            throw new InvalidCredentialsException("Email or Password is incorrect");
        }

        log.info("Usuário verificado com sucesso, retorno suas informações para assim gerar seu token");
        return user;
    }

    // Método de geração de tokens e refreshTokens
    public ResponseTokens generateTokens(String userId, String role) {

        log.debug("Gerando os tokens para o usuário: {}", userId);
        var expireToken = LocalDateTime.now().plusHours(1).toInstant(ZoneOffset.of("-03:00"));
        var now = Instant.now();

        var claims = JwtClaimsSet.builder()
                .issuer("DEVICE-LOGIN")
                .issuedAt(now)
                .subject(userId)
                .expiresAt(expireToken)
                .claim("SCOPE", role)
                .build();

        var expireRefreshToken = LocalDateTime.now().plusDays(30).toInstant(ZoneOffset.of("-03:00"));

        var claimsRefresh = JwtClaimsSet.builder()
                .issuer("DEVICE-LOGIN")
                .issuedAt(now)
                .subject(userId)
                .expiresAt(expireRefreshToken)
                .claim("SCOPE", role)
                .build();

        var accessToken = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        var accessRefreshToken = this.jwtEncoder.encode(JwtEncoderParameters.from(claimsRefresh)).getTokenValue();

        if (accessToken == null || accessRefreshToken == null) {
            log.info("Não foi possível gerar os tokens, devido a um erro inesperado");
            throw new JwtEncodingException("Não foi possível gerar os tokens");
        }

        log.info("Tokens gerados com sucesso!");
        return new ResponseTokens(accessToken, accessRefreshToken);
    }

    // Circuit breaker para o microservice de usuário
    @CircuitBreaker(name = "circuitbreaker_feign", fallbackMethod = "getUserFallback")
    public ResponseUserForLogin getUser(String email, String userId ) {

        if (email != null && !email.isBlank()) {
            return this.userClient.getResponseUserWithEmailOrUserId(email, null);
        }

        return this.userClient.getResponseUserWithEmailOrUserId(null, userId);
    }

    public ResponseUserForLogin getUserFallback(String email, String userId, Exception ex) {
        log.error("Service Unavailable, try again later.", ex);
        throw new ServiceUnavailableException("Service Unavailable, try again later.");
    }
    // ================================================================================================================



    // ======================================== REFRESH TOKENS ========================================================

    public ResponseTokens refreshTokens(RequestTokensDto request) {

        var timeSample = this.loginMetrics.startTimer();

        var accessToken = jwtDecoder.decode(request.accessToken());
        var refreshToken = jwtDecoder.decode(request.refreshToken());

        // 1. Refresh token deve expirar
        if (refreshToken.getExpiresAt() == null ||
                Instant.now().isAfter(refreshToken.getExpiresAt())) {

            log.info("Refresh token expirado ou inválido!");
            this.loginMetrics.failedRefreshTokens();
            this.loginMetrics.stopFailedRefreshTokensTimer(timeSample);
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        // 2. Subjects devem bater
        if (!Objects.equals(refreshToken.getSubject(), accessToken.getSubject())) {

            log.info("Subjects não batem!");
            this.loginMetrics.failedRefreshTokens();
            this.loginMetrics.stopFailedRefreshTokensTimer(timeSample);
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        // 3. Busca usuário confiável
        var user = this.getUser(null, refreshToken.getSubject());
        var tokens = this.generateTokens(user.userId(), user.role());

        this.loginMetrics.refreshTokenSuccess();
        this.loginMetrics.stopSuccessRefreshTokensTimer(timeSample);

        return tokens;
    }
}
