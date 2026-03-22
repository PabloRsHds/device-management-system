package br.com.device_user.service.adm;

// Importa o enum de papéis (ex: ADMIN, USER)
import br.com.device_user.enums.Role;

// Importa a entidade User
import br.com.device_user.model.User;

// Importa o repositório para acessar o banco de dados
import br.com.device_user.repository.UserRepository;

// Lombok: cria automaticamente um logger (log.info, log.error, etc)
import lombok.extern.slf4j.Slf4j;

// Injeta dependências automaticamente
import org.springframework.beans.factory.annotation.Autowired;

// Interface que permite executar código ao iniciar a aplicação
import org.springframework.boot.CommandLineRunner;

// Usado para criptografar senha
import org.springframework.security.crypto.password.PasswordEncoder;

// Define como um componente gerenciado pelo Spring
import org.springframework.stereotype.Component;

// Classes de data e hora
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j // Cria um logger chamado "log"
@Component // Faz o Spring reconhecer essa classe automaticamente
public class AdmService implements CommandLineRunner {

    // Repositório para acessar usuários no banco
    private final UserRepository userRepository;

    // Encoder para criptografar senha
    private final PasswordEncoder passwordEncoder;

    // Injeção de dependências via construtor
    @Autowired
    public AdmService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Esse método roda automaticamente quando a aplicação inicia
    @Override
    public void run(String... args) throws Exception {

        log.info("Criando ADM"); // Log informando início do processo

        // Busca um usuário pelo email
        var entity = this.userRepository.findByEmail("pablo@gmail.com");

        // Se encontrar, executa o primeiro bloco
        // Se não encontrar, executa o segundo bloco (criação do ADM)
        entity.ifPresentOrElse(
                present -> System.out.println("ADM ON"), // Já existe

                () -> {

                    // Cria um novo usuário
                    var newEntity = new User();

                    // Define nome
                    newEntity.setName("Pablo Renato");

                    // Define email
                    newEntity.setEmail("pablo@gmail.com");

                    // Define senha (criptografada)
                    newEntity.setPassword(this.passwordEncoder.encode("123456789Rr@"));

                    // Define data de criação formatada (string)
                    newEntity.setCreatedAt(
                            LocalDateTime.now() // pega data atual
                                    .atZone(ZoneId.of("America/Sao_Paulo")) // ajusta timezone
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) // formata
                    );

                    // Define o papel como ADMIN
                    newEntity.setRole(Role.ADMIN);

                    // Salva no banco de dados
                    this.userRepository.save(newEntity);

                    // Mensagem de confirmação
                    System.out.println("Foi salvo");
                }
        );
    }
}