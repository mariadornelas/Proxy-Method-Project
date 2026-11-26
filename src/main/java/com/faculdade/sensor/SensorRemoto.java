package com.faculdade.sensor;

import java.util.Objects;

public class SensorRemoto implements Sensor {

    public interface Fonte {
        double lerBruto();
    }

    private final String id;
    private final Fonte fonte;
    private double ganho = 1.0;
    private double offset = 0.0;
    private int leituras;

    public SensorRemoto(String id, Fonte fonte) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id não pode ser vazio");
        }
        this.id = id;
        this.fonte = Objects.requireNonNull(fonte, "fonte não pode ser nula");
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public double ler() {
        double bruto = fonte.lerBruto();
        if (!Double.isFinite(bruto)) {
            throw new IllegalStateException("sensor " + id + " sem sinal");
        }
        leituras++;
        return bruto * ganho + offset;
    }

    @Override
    public void calibrar(double novoGanho, double novoOffset) {
        if (!Double.isFinite(novoGanho) || !Double.isFinite(novoOffset)) {
            throw new IllegalArgumentException("ganho e offset devem ser finitos");
        }
        if (novoGanho <= 0) {
            throw new IllegalArgumentException("o ganho deve ser positivo");
        }
        this.ganho = novoGanho;
        this.offset = novoOffset;
    }

    public int leituras() {
        return leituras;
    }
}
