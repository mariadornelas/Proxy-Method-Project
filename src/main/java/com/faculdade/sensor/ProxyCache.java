package com.faculdade.sensor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class ProxyCache implements Sensor {

    private final Sensor alvo;
    private final Duration validade;
    private final Clock relogio;
    private Double valor;
    private Instant instante;
    private int acertos;

    public ProxyCache(Sensor alvo, Duration validade, Clock relogio) {
        this.alvo = Objects.requireNonNull(alvo, "alvo não pode ser nulo");
        Objects.requireNonNull(validade, "validade não pode ser nula");
        if (validade.isZero() || validade.isNegative()) {
            throw new IllegalArgumentException("a validade deve ser positiva");
        }
        this.validade = validade;
        this.relogio = Objects.requireNonNull(relogio, "relógio não pode ser nulo");
    }

    @Override
    public String id() {
        return alvo.id();
    }

    @Override
    public double ler() {
        Instant agora = relogio.instant();
        if (valor != null && agora.isBefore(instante.plus(validade))) {
            acertos++;
            return valor;
        }
        double novo = alvo.ler();        // se lançar, nada é guardado
        valor = novo;
        instante = agora;
        return novo;
    }

    @Override
    public void calibrar(double ganho, double offset) {
        alvo.calibrar(ganho, offset);    // se lançar, o cache continua válido
        valor = null;
    }

    /** Quantas leituras foram respondidas sem consultar o alvo. */
    public int acertos() {
        return acertos;
    }
}
