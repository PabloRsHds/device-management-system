package br.com.device_login.microservice;

import br.com.device_login.dtos.loginDto.ResponseUserForLogin;
import br.com.device_login.infra.user_fallback.DeviceUserFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "DEVICE-USER", fallbackFactory = DeviceUserFallBack.class)
public interface UserClient {

     /*
      * Eu faço uma verificação passando o e-mail do usuário, caso ele esteja registrado ele me retorna os dados
      * do usuário, como userId, password(para fazer a verificação do password), e a role do usuário.
      */
    @GetMapping("/microservice/verify-if-email-already-cadastred")
    ResponseUserForLogin getResponseUserWithEmailOrUserId(@RequestParam String email, @RequestParam String userId);
}
