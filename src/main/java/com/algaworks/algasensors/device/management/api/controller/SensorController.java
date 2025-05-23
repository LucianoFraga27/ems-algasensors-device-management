package com.algaworks.algasensors.device.management.api.controller;

import com.algaworks.algasensors.device.management.api.client.SensorMonitoringClient;
import com.algaworks.algasensors.device.management.api.model.SensorInput;
import com.algaworks.algasensors.device.management.api.model.SensorOutput;
import com.algaworks.algasensors.device.management.common.IdGenerator;
import com.algaworks.algasensors.device.management.domain.model.Sensor;
import com.algaworks.algasensors.device.management.domain.model.SensorId;
import com.algaworks.algasensors.device.management.domain.repository.SensorRepository;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorRepository sensorRepository;
    private final SensorMonitoringClient sensorMonitoringClient;


    @GetMapping
    public Page<SensorOutput> search(@PageableDefault(size = 5, page = 0) Pageable pageable) {
        var sensors = sensorRepository.findAll(pageable);
        return sensors.map(this::convertToModelOutput);
    }

    @GetMapping("{sensorId}")
    public SensorOutput get (@PathVariable TSID sensorId) {
        var sensor = sensorRepository.findById(new SensorId(sensorId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        return convertToModelOutput(sensor);
    }

    @PutMapping("{sensorId}")
    public SensorOutput edit(@PathVariable TSID sensorId, @RequestBody SensorInput input) {
        var sensorExistente = sensorRepository.findById(new SensorId(sensorId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        var sensorEditado = Sensor.builder()
                .id(sensorExistente.getId())
                .name(input.getName())
                .ip(input.getIp())
                .protocol(input.getProtocol())
                .location(input.getLocation())
                .model(input.getModel())
                .enabled(false)
                .build();
        sensorEditado = sensorRepository.saveAndFlush(sensorEditado);
        return convertToModelOutput(sensorEditado);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorOutput create(@RequestBody SensorInput input) {
        Sensor sensor = Sensor.builder()
                .id(new SensorId(IdGenerator.generateTSID()))
                .name(input.getName())
                .ip(input.getIp())
                .protocol(input.getProtocol())
                .location(input.getLocation())
                .model(input.getModel())
                .enabled(false)
                .build();

        sensor = sensorRepository.saveAndFlush(sensor);

        return convertToModelOutput(sensor);
    }

    @DeleteMapping("{sensorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete (@PathVariable TSID sensorId) {
        var sensorExistente = sensorRepository.findById(new SensorId(sensorId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        sensorRepository.delete(sensorExistente);
        sensorMonitoringClient.disableMonitoring(sensorId);
    }

    @PutMapping("{sensorId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable (@PathVariable TSID sensorId) {
        var sensorExistente = sensorRepository.findById(new SensorId(sensorId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        sensorExistente.enable();
        sensorRepository.saveAndFlush(sensorExistente);
        sensorMonitoringClient.enableMonitoring(sensorId);
    }


    @DeleteMapping("{sensorId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable (@PathVariable TSID sensorId) {
        var sensorExistente = sensorRepository.findById(new SensorId(sensorId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        sensorExistente.disable();
        sensorRepository.saveAndFlush(sensorExistente);
        sensorMonitoringClient.disableMonitoring(sensorId);
    }


    private SensorOutput convertToModelOutput(Sensor sensor) {
        return SensorOutput.builder()
                .id(sensor.getId().getValue())
                .name(sensor.getName())
                .ip(sensor.getIp())
                .protocol(sensor.getProtocol())
                .location(sensor.getLocation())
                .model(sensor.getModel())
                .enabled(sensor.getEnabled())
                .build();
    }

}
