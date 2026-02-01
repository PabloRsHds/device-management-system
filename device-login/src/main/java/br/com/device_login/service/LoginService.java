package br.com.device_login.service;

import br.com.device_login.dtos.loginDto.RequestLoginDto;
import br.com.device_login.dtos.loginDto.ResponseUserForLogin;
import br.com.device_login.dtos.tokenDto.RequestTokensDto;
import br.com.device_login.dtos.tokenDto.ResponseTokens;
import br.com.device_login.infra.exceptions.InvalidCredentialsException;
import br.com.device_login.metrics.login.LoginMetrics;
import br.com.device_login.microservice.UserClient;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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

        log.info("Iniciando o timer para o serviço de LOGIN");
        var timeSample = this.loginMetrics.startTimer();

        var user = this.verifyUser(request.email(), request.password(), timeSample);

        var tokens = this.generateTokens(user.userId(), user.role());

        this.loginMetrics.loginSuccess();
        this.loginMetrics.stopSuccessLoginTimer(timeSample);
        return tokens;
    }

    public ResponseUserForLogin verifyUser(String email, String password, Timer.Sample timeSample) {
        log.debug("Verifying credentials for email: {}", email);
        var user = this.userClient.getResponseUserWithEmailOrUserId(email, null);

        if (user == null) {

            this.loginMetrics.userNotFound();
            this.loginMetrics.stopFailedLoginTimer(timeSample);
            throw new InvalidCredentialsException("Email or Password is incorrect");
        }

        if (!this.passwordEncoder.matches(password, user.password())) {

            this.loginMetrics.invalidCredentials();
            this.loginMetrics.stopFailedLoginTimer(timeSample);
            throw new InvalidCredentialsException("Email or Password is incorrect");
        }

        log.debug("Credentials verified for user: {}", user.userId());
        return user;
    }

    public ResponseTokens generateTokens(String userId, String role) {
        log.debug("Generating tokens for user: {}", userId);
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

        log.debug("Tokens generated for user: {}", userId);
        return new ResponseTokens(accessToken, accessRefreshToken);
    }
    // ================================================================================================================



    // ======================================== REFRESH TOKENS ========================================================

    public ResponseTokens refreshTokens(RequestTokensDto request){

        log.info("Iniciando o timer para o serviço de REFRESH-TOKENS");
        var timeSample = this.loginMetrics.startTimer();

        var accessToken = this.jwtDecoder.decode(request.accessToken());
        var refreshToken = this.jwtDecoder.decode(request.refreshToken());

        log.debug("Refresh token expiry: {}, Current time: {}", refreshToken.getExpiresAt(), Instant.now());
        if (refreshToken.getExpiresAt() != null && Instant.now().isBefore(refreshToken.getExpiresAt())) {

            var user = this.userClient.getResponseUserWithEmailOrUserId(null,accessToken.getSubject());
            var tokens = this.generateTokens(user.userId(), user.role());

            // Métrica de contagem de sucesso na renavação de tokens.
            this.loginMetrics.refreshTokenSuccess();
            // Métrica de tempo para ver quanto que demora para a renovar os tokens.
            this.loginMetrics.stopSuccessRefreshTokensTimer(timeSample);
            // Retorno os novos tokens
            return tokens;
        }

        // Métrica de contagem de falha na renavação de tokens.
        this.loginMetrics.failedRefreshTokens();
        // Métrica de tempo para ver quanto que demora para a falha ao não conseguir renovar os tokens.
        this.loginMetrics.stopFailedRefreshTokensTimer(timeSample);
        log.warn("Invalid or expired refresh token");
        throw new InvalidCredentialsException("Invalid or expired refresh token");
    }
}
