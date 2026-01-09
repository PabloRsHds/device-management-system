package br.com.device_user.service;

import br.com.device_user.enums.Role;
import br.com.device_user.model.User;
import br.com.device_user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class AdmService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdmService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        var entity = this.userRepository.findByEmail("pablo@gmail.com");

        entity.ifPresentOrElse(
                present -> System.out.println("ADM ON"),
                () -> {

                    var newEntity = new User();

                    newEntity.setName("Pablo Renato");
                    newEntity.setEmail("pablo@gmail.com");
                    newEntity.setPassword(this.passwordEncoder.encode("123456789Rr@"));
                    newEntity.setCreatedAt(LocalDateTime.now()
                            .atZone(ZoneId.of("America/Sao_Paulo"))
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    newEntity.setRole(Role.ADMIN);

                    this.userRepository.save(newEntity);
                    System.out.println("Foi salvo");
                }
        );
    }
}
