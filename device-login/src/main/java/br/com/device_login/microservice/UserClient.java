package br.com.device_login.microservice;

import br.com.device_login.dtos.ResponseUserForLogin;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "DeviceManagement", url = "http://localhost:8080/microservice")
public interface UserClient {

    @GetMapping("/verify-if-email-already-cadastred")
    ResponseUserForLogin getUserForLoginWithEmail(@RequestParam String email);
}
