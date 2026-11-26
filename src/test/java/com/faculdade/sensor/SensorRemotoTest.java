package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensorRemotoTest {

    @Test
    void deveLerComCalibracaoPadraoNeutra() {
        SensorRemoto s = new SensorRemoto("T-01", () -> 85.0);

        assertEquals(85.0, s.ler());
        assertEquals("T-01", s.id());
    }

    @Test
    void deveAplicarGanhoEOffsetDepoisDeCalibrar() {
        SensorRemoto s = new SensorRemoto("T-01", () -> 100.0);

        s.calibrar(1.02, -0.5);

        assertEquals(101.5, s.ler(), 1e-9);
    }

    @Test
    void deveConsultarOHardwareACadaLeituraEContar() {
        double[] valor = {10.0};
        SensorRemoto s = new SensorRemoto("T-01", () -> valor[0]);

        assertEquals(10.0, s.ler());
        valor[0] = 20.0;
        assertEquals(20.0, s.ler());

        assertEquals(2, s.leituras());
    }

    @Test
    void semSinalDeveLancarENaoContarLeitura() {
        SensorRemoto s = new SensorRemoto("T-01", () -> Double.NaN);

        assertThrows(IllegalStateException.class, s::ler);
        assertEquals(0, s.leituras());
    }

    @Test
    void calibracaoInvalidaDeveSerRecusadaSemAlterarAAnterior() {
        SensorRemoto s = new SensorRemoto("T-01", () -> 10.0);
        s.calibrar(2.0, 1.0);

        assertThrows(IllegalArgumentException.class, () -> s.calibrar(0, 0));
        assertThrows(IllegalArgumentException.class, () -> s.calibrar(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> s.calibrar(Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> s.calibrar(1, Double.POSITIVE_INFINITY));

        assertEquals(21.0, s.ler());
    }

    @Test
    void deveValidarConstrucao() {
        assertThrows(NullPointerException.class, () -> new SensorRemoto(null, () -> 1));
        assertThrows(IllegalArgumentException.class, () -> new SensorRemoto(" ", () -> 1));
        assertThrows(NullPointerException.class, () -> new SensorRemoto("T", null));
    }

    @Test
    void usuarioEPapelDevemSerValidados() {
        assertThrows(NullPointerException.class, () -> new Usuario(null, Papel.OPERADOR));
        assertThrows(IllegalArgumentException.class, () -> new Usuario(" ", Papel.OPERADOR));
        assertThrows(NullPointerException.class, () -> new Usuario("Ana", null));
    }
}
