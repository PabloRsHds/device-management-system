package br.com.analysis.model;

import br.com.analysis.enums.Status;
import br.com.analysis.enums.Type;
import br.com.analysis.enums.Unit;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_analysis")
@Data
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "device_analysis_id")
    private String deviceAnalysisId;

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

    private Float lastReadingMinLimit;
    private Float lastReadingMaxLimit;
    private String lastReadingUpdateAt;

    private String updatedAt;
    private String createdAt;

    @ElementCollection
    private List<Float> historyMinLimit = new ArrayList<>();
    @ElementCollection
    private List<Float> historyMaxLimit = new ArrayList<>();
    @ElementCollection
    private List<String> historyUpdate = new ArrayList<>();


}
