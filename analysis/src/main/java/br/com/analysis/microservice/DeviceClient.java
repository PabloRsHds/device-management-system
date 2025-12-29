package br.com.analysis.microservice;

import br.com.analysis.enums.Unit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "Device-Management", url = "http://localhost:8080/microservice")
public interface DeviceClient {

    @GetMapping("/verification-for-device-analysis")
    Boolean verificationForDeviceAnalysis(@RequestParam String deviceId,
                                          @RequestParam Unit unit,
                                          @RequestParam Float minLimit,
                                          @RequestParam Float maxLimit);
}
