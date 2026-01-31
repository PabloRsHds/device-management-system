package br.com.device_user.microservice;

import br.com.device_user.dtos.login.ResponseUserForLogin;
import br.com.device_user.service.user_service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/microservice")
public class ServiceForLogin {

    private final UserService userService;

    public ServiceForLogin(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/verify-if-email-already-cadastred")
    public ResponseUserForLogin getUserForLoginWithEmail(@RequestParam String email) {
        return this.userService.getResponseUserWithEmail(email);
    }
}
