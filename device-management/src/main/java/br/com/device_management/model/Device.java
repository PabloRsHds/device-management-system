package br.com.device_management.model;

import br.com.device_management.enums.Status;
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
    private String deviceModel;
    private String manufacturer;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String location;

    @Enumerated(EnumType.STRING)
    private Unit unit;
    private Float minLimit;
    private Float maxLimit;

    private String createdAt;
}
