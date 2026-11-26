package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ProxyVirtualTest {

    private final AtomicInteger criacoes = new AtomicInteger();
    private final SensorEspiao espiao = new SensorEspiao("T-01", 42.0);

    private ProxyVirtual proxy() {
        return new ProxyVirtual("T-01", () -> {
            criacoes.incrementAndGet();
            return espiao;
        });
    }

    @Test
    void naoDeveCriarOSensorRealNaConstrucaoNemAoPerguntarOId() {
        ProxyVirtual p = proxy();

        assertEquals("T-01", p.id());
        assertEquals(0, criacoes.get());
        assertFalse(p.foiCriado());
    }

    @Test
    void deveCriarNaPrimeiraLeituraEReusarNasSeguintes() {
        ProxyVirtual p = proxy();

        assertEquals(42.0, p.ler());
        assertEquals(42.0, p.ler());
        p.calibrar(1.0, 0.0);

        assertEquals(1, criacoes.get());
        assertTrue(p.foiCriado());
        assertEquals(2, espiao.leituras);
        assertEquals(1, espiao.calibracoes.size());
    }

    @Test
    void calibrarTambemDeveCriarSobDemanda() {
        ProxyVirtual p = proxy();

        p.calibrar(1.5, 0.0);

        assertEquals(1, criacoes.get());
        assertEquals("1.5/0.0", espiao.calibracoes.get(0));
    }

    @Test
    void falhaNaCriacaoNaoDeveSerGuardadaENovaTentativaDeveFuncionar() {
        AtomicInteger tentativas = new AtomicInteger();
        ProxyVirtual p = new ProxyVirtual("T-01", () -> {
            if (tentativas.incrementAndGet() == 1) {
                throw new IllegalStateException("conexão recusada");
            }
            return espiao;
        });

        assertThrows(IllegalStateException.class, p::ler);
        assertFalse(p.foiCriado());

        assertEquals(42.0, p.ler());
        assertEquals(2, tentativas.get());
    }

    @Test
    void criadorQueDevolveNuloDeveSerRecusado() {
        ProxyVirtual p = new ProxyVirtual("T-01", () -> null);

        assertThrows(IllegalStateException.class, p::ler);
        assertFalse(p.foiCriado());
    }

    @Test
    void sensorCriadoComIdDiferenteDeveSerRecusado() {
        ProxyVirtual p = new ProxyVirtual("T-01", () -> new SensorEspiao("OUTRO", 1.0));

        assertThrows(IllegalStateException.class, p::ler);
        assertFalse(p.foiCriado());
    }

    @Test
    void deveValidarConstrucao() {
        assertThrows(NullPointerException.class, () -> new ProxyVirtual(null, () -> espiao));
        assertThrows(IllegalArgumentException.class, () -> new ProxyVirtual(" ", () -> espiao));
        assertThrows(NullPointerException.class, () -> new ProxyVirtual("T", null));
    }
}
