package br.com.device_login.controller;

import br.com.device_login.dtos.loginDto.RequestLoginDto;
import br.com.device_login.dtos.tokenDto.RequestTokensDto;
import br.com.device_login.dtos.tokenDto.ResponseTokens;
import br.com.device_login.service.login.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    private ResponseEntity<Map<String, String>> login(@RequestBody RequestLoginDto request) {
       return this.loginService.login(request);
    }

    @PostMapping("/refresh-tokens")
    private ResponseEntity<ResponseTokens> refreshTokens(@RequestBody RequestTokensDto request) {
        return this.loginService.refreshTokens(request);
    }
}
