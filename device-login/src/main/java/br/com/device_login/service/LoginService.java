package br.com.device_login.service;

import br.com.device_login.dtos.loginDto.RequestLoginDto;
import br.com.device_login.dtos.tokenDto.RequestTokensDto;
import br.com.device_login.dtos.tokenDto.ResponseTokens;
import br.com.device_login.metrics.LoginMetrics;
import br.com.device_login.microservice.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

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

    // Função onde o usuário consegue fazer o seu login.
    public ResponseEntity<Map<String, String>> login(RequestLoginDto request) {

        log.info("Iniciando o timer para o serviço de LOGIN");
        var timeSample = this.loginMetrics.startTimer();

        var user = this.userClient.getUserForLoginWithEmail(request.email());

        if (user == null) {

            this.loginMetrics.userNotFound();
            this.loginMetrics.stopFailedLoginTimer(timeSample);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("Conflict","Email or Password is incorrect"));
        }

        if (!this.passwordEncoder.matches(request.password(), user.password())) {

            this.loginMetrics.invalidCredentials();
            this.loginMetrics.stopFailedLoginTimer(timeSample);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("Conflict","Email or Password is incorrect"));
        }

        var expireToken = LocalDateTime.now().plusHours(1).toInstant(ZoneOffset.of("-03:00"));
        var now = Instant.now();

        var claims = JwtClaimsSet.builder()
                .issuer("DEVICE-LOGIN")
                .issuedAt(now)
                .subject(user.userId())
                .expiresAt(expireToken)
                .claim("SCOPE", user.role())
                .build();

        var expireRefreshToken = LocalDateTime.now().plusDays(30).toInstant(ZoneOffset.of("-03:00"));

        var claimsRefresh = JwtClaimsSet.builder()
                .issuer("DEVICE-LOGIN")
                .issuedAt(now)
                .subject(user.userId())
                .expiresAt(expireRefreshToken)
                .claim("SCOPE", user.role())
                .build();

        var accessToken = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        var accessRefreshToken = this.jwtEncoder.encode(JwtEncoderParameters.from(claimsRefresh)).getTokenValue();

        this.loginMetrics.loginSuccess();
        this.loginMetrics.stopSuccessLoginTimer(timeSample);
        return ResponseEntity.ok().body(Map.of("accessToken", accessToken, "refreshToken", accessRefreshToken));
    }


    // Função onde o usuário consegue fazer o refresh do seu token.
    public ResponseEntity<ResponseTokens> refreshTokens(RequestTokensDto request){

        log.info("Iniciando o timer para o serviço de REFRESH-TOKENS");
        var timeSample = this.loginMetrics.startTimer();

        var accessToken = this.jwtDecoder.decode(request.accessToken());
        var refreshToken = this.jwtDecoder.decode(request.refreshToken());

        log.debug("Refresh token expiry: {}, Current time: {}", refreshToken.getExpiresAt(), Instant.now());

        // Se o refresh token a expiração não fora nula e ela não foi vencida, criará novos tokens.
        if (refreshToken.getExpiresAt() != null && Instant.now().isBefore(refreshToken.getExpiresAt())) {

            var user = this.userClient.getUserForLoginWithEmail(accessToken.getSubject());

            log.debug("Processing refresh for user: {}", user.userId());

            var expireToken = LocalDateTime.now().plusHours(1).toInstant(ZoneOffset.of("-03:00"));
            var now = Instant.now();

            //Criando o token, com apenas 1 hora de validação.
            var claims = JwtClaimsSet.builder()
                    .issuer("MYBANK") //nome da aplicação
                    .issuedAt(now)//horario que foi criado o token
                    .subject(user.userId())//o token vai estar ligado ao id do usuario
                    .expiresAt(expireToken)//o token vai expirar em 1 hora
                    .claim("scope", user.role())//o token vai ter o scope do usuario
                    .build();//criando o token

            var expireRefreshToken = LocalDateTime.now().plusDays(30).toInstant(ZoneOffset.of("-03:00"));
            //Criando o refresh-token, com 30 dias de validação
            var claimsRefresh = JwtClaimsSet.builder()
                    .issuer("MYBANK")
                    .issuedAt(now)
                    .subject(user.userId())
                    .expiresAt(expireRefreshToken)
                    .claim("scope", user.role())
                    .build();

            //gero o token
            var newAccessToken = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
            //gero o refresh-token
            var newRefreshToken = this.jwtEncoder.encode(JwtEncoderParameters.from(claimsRefresh)).getTokenValue();

            // Métrica de contagem de sucesso na renavação de tokens.
            this.loginMetrics.refreshTokenSuccess();
            // Métrica de tempo para ver quanto que demora para a renovar os tokens.
            this.loginMetrics.stopSuccessRefreshTokensTimer(timeSample);

            // Retorno os novos tokens
            return ResponseEntity.ok(new ResponseTokens(newAccessToken, newRefreshToken));
        }

        // Métrica de contagem de falha na renavação de tokens.
        this.loginMetrics.failedRefreshTokens();
        // Métrica de tempo para ver quanto que demora para a falha ao não conseguir renovar os tokens.
        this.loginMetrics.stopFailedRefreshTokensTimer(timeSample);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
