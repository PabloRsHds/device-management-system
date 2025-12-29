package br.com.device_management.repository;

import br.com.device_management.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, String> {

    Optional<Device> findByDeviceModel(String deviceModel);
}
