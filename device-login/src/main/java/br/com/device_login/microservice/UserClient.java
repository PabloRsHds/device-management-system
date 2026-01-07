package br.com.device_login.microservice;

import br.com.device_login.dtos.ResponseUserForLogin;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "device_management", url = "http://localhost:8080/api")
public interface UserClient {

    @GetMapping("/verify-if-email-already-cadastred")
    ResponseUserForLogin getUserForLoginWithEmail(String email);
}
