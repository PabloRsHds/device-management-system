package br.com.device_management.repository;

import br.com.device_management.model.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, String> {

    Optional<Device> findByDeviceModel(String deviceModel);

    @Query("SELECT d FROM Device d")
    Page<Device> findAllDevices(Pageable pageable);
}
