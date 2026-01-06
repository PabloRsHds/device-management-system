package br.com.analysis.repository;

import br.com.analysis.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, String> {

    Optional<Analysis> findByDeviceModel(String deviceModel);
}
