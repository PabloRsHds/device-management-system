package br.com.analysis.model;

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

    @Column(name = "last_reading_min_limit")
    private Float lastReadingMinLimit;
    @Column(name = "last_reading_max_limit")
    private Float lastReadingMaxLimit;
    @Column(name = "last_reading_update_at")
    private String lastReadingUpdateAt;

    @Column(name = "update_at")
    private String updatedAt;
    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "history_min_limit")
    @ElementCollection
    private List<Float> historyMinLimit = new ArrayList<>();

    @Column(name = "history_max_limit")
    @ElementCollection
    private List<Float> historyMaxLimit = new ArrayList<>();

    @Column(name = "history_update")
    @ElementCollection
    private List<String> historyUpdate = new ArrayList<>();

    @Column(name = "analysis_worked")
    private int analysisWorked;
    @Column(name = "analysis_failed")
    private int analysisFailed;
}
