package com.faculdade.sensor;

import java.util.Objects;
import java.util.function.Supplier;

public class ProxyVirtual implements Sensor {

    private final String id;
    private final Supplier<? extends Sensor> criador;
    private Sensor real;

    public ProxyVirtual(String id, Supplier<? extends Sensor> criador) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id não pode ser vazio");
        }
        this.id = id;
        this.criador = Objects.requireNonNull(criador, "criador não pode ser nulo");
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public double ler() {
        return real().ler();
    }

    @Override
    public void calibrar(double ganho, double offset) {
        real().calibrar(ganho, offset);
    }

    public boolean foiCriado() {
        return real != null;
    }

    private Sensor real() {
        if (real == null) {
            Sensor criado = criador.get();
            if (criado == null) {
                throw new IllegalStateException("o criador devolveu nulo para o sensor " + id);
            }
            if (!id.equals(criado.id())) {
                throw new IllegalStateException("o sensor criado tem id " + criado.id() + ", esperado " + id);
            }
            real = criado;
        }
        return real;
    }
}
