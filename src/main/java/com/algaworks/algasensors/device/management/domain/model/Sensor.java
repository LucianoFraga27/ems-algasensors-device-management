package com.algaworks.algasensors.device.management.domain.model;

import io.hypersistence.tsid.TSID;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Sensor {

    @Id
    @AttributeOverride(name="value", column = @Column(name = "id", columnDefinition = "BIGINT"))
    private SensorId id;
    private String name;
    private String ip;
    private String location;
    private String protocol;
    private String model;
    private Boolean enabled;

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

}
