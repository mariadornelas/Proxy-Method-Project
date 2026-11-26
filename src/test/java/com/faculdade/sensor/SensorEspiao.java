package com.faculdade.sensor;

import java.util.ArrayList;
import java.util.List;

class SensorEspiao implements Sensor {

    private final String id;
    double valor;
    int leituras;
    boolean falharNaLeitura;
    boolean recusarCalibracao;
    final List<String> calibracoes = new ArrayList<>();

    SensorEspiao(String id, double valor) {
        this.id = id;
        this.valor = valor;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public double ler() {
        if (falharNaLeitura) {
            throw new IllegalStateException("sem sinal");
        }
        leituras++;
        return valor;
    }

    @Override
    public void calibrar(double ganho, double offset) {
        if (recusarCalibracao) {
            throw new IllegalArgumentException("calibração recusada");
        }
        calibracoes.add(ganho + "/" + offset);
    }
}
