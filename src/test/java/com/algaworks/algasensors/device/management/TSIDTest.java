package com.algaworks.algasensors.device.management;

import io.hypersistence.tsid.TSID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TSIDTest {

    @Test
    public void shouldGenerateTSID1() {
        System.out.println("01");
        TSID tsid = TSID.fast();
        // Não devemos utilizar o método fast em produção
        System.out.println(tsid);
        System.out.println(tsid.toLong());
        System.out.println(tsid.getInstant());
    }

    @Test
    public void shouldGenerateTSID2() {
        System.out.println("02");
        TSID tsid = TSID.Factory.getTsid();
        // lendo configurações do ambiente
        System.out.println(tsid);
        System.out.println(tsid.toLong());
        System.out.println(tsid.getInstant());
    }

    @Test
    public void shouldGenerateTSID3() {
        System.out.println("03");
        System.setProperty("tsid.node", "2");
        System.setProperty("tsid.node.count", "32");
        TSID.Factory factory = TSID.Factory.builder().build();
        TSID tsid = factory.getTsid();
        // lendo configurações do ambiente
        System.out.println(tsid);
        System.out.println(tsid.toLong());
        System.out.println(tsid.getInstant());
    }

    @Test
    public void shouldGenerateTSID4() {
        System.out.println("04");
        // Para esse teste é necessário a geração das variaveis de ambiente na configuração
        TSID tsid = IdGenerator.generateTSID();
        System.out.println(tsid);
        System.out.println(tsid.toLong());
        System.out.println(tsid.getInstant());
    }

    @Test
    public void shouldGenerateTSID5() {
        System.out.println("05");
        // Para esse teste é necessário a geração das variaveis de ambiente na configuração
        TSID tsid = IdGenerator.generateTSID();
        Assertions.assertThat(tsid.getInstant()).isCloseTo(Instant.now(),
                Assertions.within(1,
                        ChronoUnit.MINUTES));
    }

}
