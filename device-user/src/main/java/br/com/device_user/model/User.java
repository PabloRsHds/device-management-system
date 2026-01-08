package br.com.device_user.model;

import br.com.device_user.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;
    private String name;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String createdAt;
}
