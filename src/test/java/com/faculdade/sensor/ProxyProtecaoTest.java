package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProxyProtecaoTest {

    private final SensorEspiao alvo = new SensorEspiao("T-01", 42.0);

    private ProxyProtecao como(Papel papel) {
        return new ProxyProtecao(alvo, new Usuario("fulano", papel));
    }

    @Test
    void visitanteNaoDeveLerNemCalibrar() {
        ProxyProtecao p = como(Papel.VISITANTE);

        assertThrows(AcessoNegadoException.class, p::ler);
        assertThrows(AcessoNegadoException.class, () -> p.calibrar(1.0, 0.0));
    }

    @Test
    void operadorDeveLerMasNaoCalibrar() {
        ProxyProtecao p = como(Papel.OPERADOR);

        assertEquals(42.0, p.ler());
        assertThrows(AcessoNegadoException.class, () -> p.calibrar(1.0, 0.0));
    }

    @Test
    void manutencaoDeveLerECalibrar() {
        ProxyProtecao p = como(Papel.MANUTENCAO);

        assertEquals(42.0, p.ler());
        p.calibrar(1.1, 0.2);

        assertEquals("1.1/0.2", alvo.calibracoes.get(0));
    }

    @Test
    void chamadaNegadaNaoDeveChegarAoAlvo() {
        ProxyProtecao visitante = como(Papel.VISITANTE);
        ProxyProtecao operador = como(Papel.OPERADOR);

        assertThrows(AcessoNegadoException.class, visitante::ler);
        assertThrows(AcessoNegadoException.class, () -> visitante.calibrar(1, 0));
        assertThrows(AcessoNegadoException.class, () -> operador.calibrar(1, 0));

        assertEquals(0, alvo.leituras);
        assertTrue(alvo.calibracoes.isEmpty());
    }

    @Test
    void idDeveSerPublicoParaTodos() {
        assertEquals("T-01", como(Papel.VISITANTE).id());
    }

    @Test
    void mensagemDeNegacaoDeveIdentificarQuemOperacaoESensor() {
        AcessoNegadoException e = assertThrows(AcessoNegadoException.class, () ->
                new ProxyProtecao(alvo, new Usuario("Ana", Papel.OPERADOR)).calibrar(1, 0));

        assertTrue(e.getMessage().contains("Ana"), e.getMessage());
        assertTrue(e.getMessage().contains("OPERADOR"), e.getMessage());
        assertTrue(e.getMessage().contains("calibrar"), e.getMessage());
        assertTrue(e.getMessage().contains("T-01"), e.getMessage());
    }

    @Test
    void errosDoAlvoDevemPassarSemTraducao() {
        alvo.falharNaLeitura = true;

        assertThrows(IllegalStateException.class, como(Papel.OPERADOR)::ler);
    }

    @Test
    void deveValidarConstrucao() {
        assertThrows(NullPointerException.class, () -> new ProxyProtecao(null, new Usuario("a", Papel.OPERADOR)));
        assertThrows(NullPointerException.class, () -> new ProxyProtecao(alvo, null));
    }
}
