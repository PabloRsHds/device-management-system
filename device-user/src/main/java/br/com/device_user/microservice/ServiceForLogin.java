package br.com.device_user.microservice;

import br.com.device_user.dtos.ResponseUserForLogin;
import br.com.device_user.model.User;
import br.com.device_user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/microservice")
public class ServiceForLogin {

    private final UserRepository userRepository;

    @Autowired
    public ServiceForLogin(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/verify-if-email-already-cadastred")
    public ResponseUserForLogin getUserForLoginWithEmail(@RequestParam String email) {

        Optional<User> entity = this.userRepository.findByEmail(email);

        return entity.map(user -> new ResponseUserForLogin(
                user.getUserId(),
                user.getPassword(),
                user.getRole()
        )).orElse(null);
    }
}
