package br.com.sensor_test.model;

import br.com.sensor_test.enums.Status;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_sensors")
@Data
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long sensorId;
    private String name;
    private String type;
    private String description;
    @Column(name = "device_model")
    private String deviceModel;
    private String manufacturer;
    private String unit;
    @Column(name = "min_limit")
    private Float minLimit;
    @Column(name = "max_limit")
    private Float maxLimit;

    @Enumerated(EnumType.STRING)
    private Status status;
}
