package br.com.iot_gateway.microservice;

import br.com.iot_gateway.enums.Type;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "Device-Management", url = "http://localhost:8080/microservice")
public interface DeviceClient {


    @GetMapping("/verification-for-iot_gateway")
    Boolean verificationForIotGateway(  @RequestParam String deviceId,
                                        @RequestParam String name,
                                        @RequestParam Type type,
                                        @RequestParam String description,
                                        @RequestParam String deviceModel,
                                        @RequestParam String manufacturer);
}
