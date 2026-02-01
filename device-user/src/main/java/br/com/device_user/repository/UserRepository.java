package br.com.device_user.repository;

import br.com.device_user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String mail);

    Optional<User> findByUserId(String userId);
}
