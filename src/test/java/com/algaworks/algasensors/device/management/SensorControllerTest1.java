package com.algaworks.algasensors.device.management;

import com.algaworks.algasensors.device.management.api.model.SensorInput;
import com.algaworks.algasensors.device.management.common.IdGenerator;
import com.algaworks.algasensors.device.management.domain.model.Sensor;
import com.algaworks.algasensors.device.management.domain.model.SensorId;
import com.algaworks.algasensors.device.management.domain.repository.SensorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.tsid.TSID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SensorControllerTest1 {

    private static final Logger log = LoggerFactory.getLogger(SensorControllerTest1.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Sensor sensor;

    @BeforeEach
    void setUp() {
        sensorRepository.deleteAll(); // Limpa a base para testes isolados
        sensor = Sensor.builder()
                .id(new SensorId(TSID.fast()))
                .name("Sensor 1")
                .ip("192.168.1.100")
                .protocol("MQTT")
                .location("Sala A")
                .model("S-100")
                .enabled(false)
                .build();
        sensorRepository.save(sensor);
    }

    @Test
    void deveCriarSensor() throws Exception {
        log.info("Realizando teste de criação de um novo sensor");
        SensorInput novoSensor = SensorInput.builder()
                .name("Sensor Novo")
                .ip("10.2.3.1")
                .protocol("HTTP")
                .location("Sala B")
                .model("X-200")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoSensor)))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        Assertions.assertThat(sensorRepository.findAll()).anyMatch(s -> s.getName().equals("Sensor Novo"));
    }

    @Test
    void deveAtualizarSensorExistente() throws Exception {
        log.info("Realizando teste de edição de sensor existente");
        SensorInput input = SensorInput.builder()
                .name(sensor.getName())
                .ip(sensor.getIp())
                .protocol(sensor.getProtocol())
                .location("Avenida da Américas, 600")
                .model(sensor.getModel())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/sensors/{sensorId}", sensor.getId().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

        var atualizado = sensorRepository.findById(sensor.getId()).orElseThrow();
        Assertions.assertThat(atualizado.getLocation()).isEqualTo("Avenida da Américas, 600");
    }

    @Test
    void deveRetornar404AoAtualizarSensorInexistente() throws Exception {
        log.info("Realizando teste de edição de sensor inexistente");
        Sensor sensorFake = Sensor.builder().id(new SensorId(IdGenerator.generateTSID())).build();
        mockMvc.perform(MockMvcRequestBuilders.put("/api/sensors/{sensorId}", sensorFake.getId().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sensorFake)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void deveDeletarSensorExistente() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/sensors/{sensorId}", sensor.getId().getValue()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        Assertions.assertThat(sensorRepository.existsById(sensor.getId())).isFalse();
    }

    @Test
    void deveRetornar404AoDeletarSensorInexistente() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/sensors/{sensorId}", new SensorId(IdGenerator.generateTSID())))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void deveAtivarSensorExistente() throws Exception {
        log.info("Realizando teste de ativação de sensor existente");
        mockMvc.perform(MockMvcRequestBuilders.put("/api/sensors/{sensorId}/enable", sensor.getId().getValue())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        var sensorAtualizado = sensorRepository.findById(sensor.getId()).orElseThrow();
        Assertions.assertThat(sensorAtualizado.getEnabled()).isTrue();
    }

    @Test
    void deveDesativarSensorExistente() throws Exception {
        // ativa antes, pra garantir que ele estará ativado antes de iniciar o teste
        sensor.enable();
        sensorRepository.save(sensor);
        log.info("Realizando teste de desativação de sensor existente");
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/sensors/{sensorId}/enable", sensor.getId().getValue())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        var sensorAtualizado = sensorRepository.findById(sensor.getId()).orElseThrow();
        Assertions.assertThat(sensorAtualizado.getEnabled()).isFalse();
    }

    @Test
    void deveRetornar404AoAtivarSensorInexistente() throws Exception {
        log.info("Realizando teste de ativação de sensor inexistente");
        mockMvc.perform(MockMvcRequestBuilders.put("/api/sensors/{sensorId}/enable", TSID.fast()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void deveRetornar404AoDesativarSensorInexistente() throws Exception {
        log.info("Realizando teste de desativação de sensor inexistente");
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/sensors/{sensorId}/enable", TSID.fast()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }



}
