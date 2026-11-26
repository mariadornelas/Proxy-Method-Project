package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ProxyDesignTest {

    private static final Instant INICIO = Instant.parse("2026-11-25T10:00:00Z");

    private static double mediaDeTresLeituras(Sensor s) {
        return (s.ler() + s.ler() + s.ler()) / 3;
    }

    @Test
    void realEProxiesDevemSerIntercambiaveisParaOCliente() {
        SensorEspiao alvo = new SensorEspiao("T", 30.0);
        List<Sensor> todos = List.of(
                new SensorRemoto("T", () -> 30.0),
                new ProxyVirtual("T", () -> alvo),
                new ProxyProtecao(alvo, new Usuario("a", Papel.OPERADOR)),
                new ProxyCache(alvo, Duration.ofSeconds(1), new RelogioManual(INICIO)));

        for (Sensor s : todos) {
            assertEquals(30.0, mediaDeTresLeituras(s), s.getClass().getSimpleName());
        }
    }

    @Test
    void proxiesDevemDependerDaInterfaceENaoDoSensorConcreto() {
        for (Class<?> c : List.of(ProxyProtecao.class, ProxyCache.class)) {
            boolean temCampoDoTipoSensor = Arrays.stream(c.getDeclaredFields()).anyMatch(f -> f.getType() == Sensor.class);
            boolean conheceRemoto = Arrays.stream(c.getDeclaredFields()).anyMatch(f -> f.getType() == SensorRemoto.class);
            assertTrue(temCampoDoTipoSensor, c.getSimpleName());
            assertFalse(conheceRemoto, c.getSimpleName());
        }
    }

    @Test
    void sensorRealNaoDeveConhecerOsProxies() {
        for (Field f : SensorRemoto.class.getDeclaredFields()) {
            String tipo = f.getGenericType().getTypeName();
            assertFalse(tipo.contains("Proxy"), f.getName() + ": " + tipo);
        }
        assertTrue(Sensor.class.isAssignableFrom(SensorRemoto.class));
    }

    @Test
    void camadasDevemSerEmpilhaveisEPoderSerCombinadasLivremente() {
        AtomicInteger conexoes = new AtomicInteger();
        SensorRemoto[] real = new SensorRemoto[1];
        RelogioManual relogio = new RelogioManual(INICIO);
        ProxyVirtual virtual = new ProxyVirtual("T-01", () -> {
            conexoes.incrementAndGet();
            real[0] = new SensorRemoto("T-01", () -> 85.0);
            return real[0];
        });
        Sensor completo = new ProxyProtecao(new ProxyCache(virtual, Duration.ofSeconds(5), relogio),
                new Usuario("Ana", Papel.OPERADOR));

        completo.ler();
        completo.ler();
        completo.ler();

        assertEquals(1, conexoes.get());          // virtual: uma conexão só
        assertEquals(1, real[0].leituras());      // cache: uma ida ao hardware só
    }

    @Test
    void protecaoPorForaDoCacheNaoDeveDeixarVazarLeituraGuardadaParaQuemNaoPode() {
        SensorEspiao alvo = new SensorEspiao("T-01", 85.0);
        ProxyCache cacheCompartilhado = new ProxyCache(alvo, Duration.ofSeconds(60), new RelogioManual(INICIO));
        Sensor operador = new ProxyProtecao(cacheCompartilhado, new Usuario("Ana", Papel.OPERADOR));
        Sensor visitante = new ProxyProtecao(cacheCompartilhado, new Usuario("Visita", Papel.VISITANTE));

        operador.ler();                            // aquece o cache

        assertThrows(AcessoNegadoException.class, visitante::ler);
        assertEquals(0, cacheCompartilhado.acertos());   // o visitante nem chegou ao cache
    }

    @Test
    void calibracaoPeloProxyDeProtecaoDeveChegarAoSensorRealEMudarAsLeituras() {
        SensorRemoto real = new SensorRemoto("T-01", () -> 100.0);
        RelogioManual relogio = new RelogioManual(INICIO);
        ProxyCache cache = new ProxyCache(real, Duration.ofSeconds(60), relogio);
        Sensor manutencao = new ProxyProtecao(cache, new Usuario("Beto", Papel.MANUTENCAO));

        assertEquals(100.0, manutencao.ler(), 1e-9);
        manutencao.calibrar(1.02, 0.0);

        assertEquals(102.0, manutencao.ler(), 1e-9);   // cache invalidado: leitura nova e corrigida
    }

    @Test
    void proxiesNaoDevemEstenderOSensorRemoto() {
        for (Class<?> c : List.of(ProxyVirtual.class, ProxyProtecao.class, ProxyCache.class)) {
            assertEquals(Object.class, c.getSuperclass(), c.getSimpleName());
            assertTrue(Sensor.class.isAssignableFrom(c), c.getSimpleName());
        }
    }
}
