package br.com.device_user.microservice;

// DTO retornado para o microserviço de login
import br.com.device_user.dtos.login.ResponseUserForLogin;

// Serviço que contém a regra de negócio
import br.com.device_user.service.user_service.UserService;

// Anotações do Spring para criar endpoints REST
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Endpoint completo: GET /microservice/verify-if-email-already-cadastred

// Define que essa classe é um controller REST (retorna JSON)
@RestController

// Define o prefixo base da URL
@RequestMapping("/microservice")
public class ServiceForLogin {

    // Serviço responsável por buscar os dados do usuário
    private final UserService userService;

    // Injeção de dependência via construtor
    public ServiceForLogin(UserService userService) {
        this.userService = userService;
    }

    /*
        Endpoint GET responsável por fornecer os dados do usuário
        para o microserviço de login.

        URL completa:
        GET /microservice/verify-if-email-already-cadastred

        Parâmetros esperados:
        - email  (query param)
        - userId (query param)

        Exemplo de chamada:
        /microservice/verify-if-email-already-cadastred?email=teste@gmail.com&userId=123

        Fluxo:
        1. Recebe email e userId via request
        2. Chama o UserService
        3. Retorna os dados do usuário (DTO)
    */
    @GetMapping("/verify-if-email-already-cadastred")
    public ResponseUserForLogin getUserForLoginWithEmailOrUserId(
            @RequestParam String email,   // Email recebido na requisição
            @RequestParam String userId   // UserId recebido na requisição
    ) {

        // Delega a lógica para o service
        return this.userService.getResponseUserWithEmailOrUserId(email, userId);
    }
}
