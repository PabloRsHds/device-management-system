package br.com.device_management.model;

import br.com.device_management.enums.Type;
import br.com.device_management.enums.Unit;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_devices")
@Data
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "device_id")
    private String deviceId;
    private String name;
    @Enumerated(EnumType.STRING)
    private Type type;
    private String description;
    @Column(name = "device_model", unique = true)
    private String deviceModel;
    private String manufacturer;
    private String location;

    @Enumerated(EnumType.STRING)
    private Unit unit;
    @Column(name = "min_limit")
    private Float minLimit;
    @Column(name = "max_limit")
    private Float maxLimit;
    @Column(name = "created_at")
    private String createdAt;
}
