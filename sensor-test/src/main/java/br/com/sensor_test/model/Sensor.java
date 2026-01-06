package br.com.sensor_test.model;

import br.com.sensor_test.enums.Status;
import br.com.sensor_test.enums.Type;
import br.com.sensor_test.enums.Unit;
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
    @Enumerated(EnumType.STRING)
    private Type type;
    private String description;
    private String deviceModel;
    private String manufacturer;
    private Unit unit;

    @Enumerated(EnumType.STRING)
    private Status status;
}
