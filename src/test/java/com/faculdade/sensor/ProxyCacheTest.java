package com.faculdade.sensor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ProxyCacheTest {

    private static final Duration VALIDADE = Duration.ofSeconds(5);

    private SensorEspiao alvo;
    private RelogioManual relogio;
    private ProxyCache cache;

    @BeforeEach
    void montar() {
        alvo = new SensorEspiao("T-01", 10.0);
        relogio = new RelogioManual(Instant.parse("2026-11-25T10:00:00Z"));
        cache = new ProxyCache(alvo, VALIDADE, relogio);
    }

    @Test
    void primeiraLeituraDeveIrAoAlvoESeguintesDentroDaValidadeNao() {
        assertEquals(10.0, cache.ler());
        alvo.valor = 99.0;                       // o alvo mudou, mas o cache ainda vale
        assertEquals(10.0, cache.ler());
        assertEquals(10.0, cache.ler());

        assertEquals(1, alvo.leituras);
        assertEquals(2, cache.acertos());
    }

    @Test
    void naoDeveExpirarUmMilissegundoAntesDaValidade() {
        cache.ler();
        relogio.avancar(VALIDADE.minusMillis(1));
        alvo.valor = 99.0;

        assertEquals(10.0, cache.ler());
        assertEquals(1, alvo.leituras);
    }

    @Test
    void deveExpirarNoInstanteExatoDaValidade() {
        cache.ler();
        relogio.avancar(VALIDADE);
        alvo.valor = 99.0;

        assertEquals(99.0, cache.ler());
        assertEquals(2, alvo.leituras);
    }

    @Test
    void novaLeituraDeveReiniciarAContagemDaValidade() {
        cache.ler();                             // t = 0
        relogio.avancar(VALIDADE);
        cache.ler();                             // t = 5 s, vai ao alvo e reinicia
        relogio.avancar(VALIDADE.minusMillis(1));
        alvo.valor = 99.0;

        assertEquals(10.0, cache.ler());         // t = 9,999 s: ainda vale a leitura de t = 5 s
        assertEquals(2, alvo.leituras);
    }

    @Test
    void calibrarDeveInvalidarOCache() {
        cache.ler();
        alvo.valor = 20.0;

        cache.calibrar(1.0, 0.0);

        assertEquals(20.0, cache.ler());
        assertEquals(2, alvo.leituras);
        assertEquals(1, alvo.calibracoes.size());
    }

    @Test
    void calibracaoRecusadaPeloAlvoNaoDeveInvalidarOCache() {
        cache.ler();
        alvo.recusarCalibracao = true;

        assertThrows(IllegalArgumentException.class, () -> cache.calibrar(0, 0));

        alvo.valor = 99.0;
        assertEquals(10.0, cache.ler());
        assertEquals(1, alvo.leituras);
    }

    @Test
    void falhaDoAlvoNaoDeveSerGuardadaENemApagarOQueJaEstavaValido() {
        alvo.falharNaLeitura = true;
        assertThrows(IllegalStateException.class, cache::ler);      // nada guardado

        alvo.falharNaLeitura = false;
        assertEquals(10.0, cache.ler());                              // vai ao alvo, não repete a falha
        assertEquals(1, alvo.leituras);
        assertEquals(0, cache.acertos());
    }

    @Test
    void falhaAposExpirarDevePropagarEmVezDeDevolverValorVelho() {
        cache.ler();
        relogio.avancar(VALIDADE);
        alvo.falharNaLeitura = true;

        assertThrows(IllegalStateException.class, cache::ler);
    }

    @Test
    void idDeveVirDoAlvoSemContarComoAcerto() {
        assertEquals("T-01", cache.id());
        assertEquals(0, alvo.leituras);
        assertEquals(0, cache.acertos());
    }

    @Test
    void deveValidarConstrucao() {
        assertThrows(NullPointerException.class, () -> new ProxyCache(null, VALIDADE, relogio));
        assertThrows(NullPointerException.class, () -> new ProxyCache(alvo, null, relogio));
        assertThrows(NullPointerException.class, () -> new ProxyCache(alvo, VALIDADE, null));
        assertThrows(IllegalArgumentException.class, () -> new ProxyCache(alvo, Duration.ZERO, relogio));
        assertThrows(IllegalArgumentException.class, () -> new ProxyCache(alvo, Duration.ofSeconds(-1), relogio));
    }

    @Test
    void relogioManualNaoDeveAndarParaTras() {
        assertThrows(IllegalArgumentException.class, () -> relogio.avancar(Duration.ofSeconds(-1)));
    }
}
