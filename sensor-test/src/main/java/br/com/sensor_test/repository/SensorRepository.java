package br.com.sensor_test.repository;

import br.com.sensor_test.enums.Status;
import br.com.sensor_test.model.Sensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findByDeviceModel(String deviceModel);

    @Query("SELECT s FROM Sensor s WHERE s.status = :status")
    Page<Sensor> findAllByStatus(@Param("status") Status status, Pageable pageable);
}
