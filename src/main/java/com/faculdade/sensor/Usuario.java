package com.faculdade.sensor;

import java.util.Objects;

public record Usuario(String nome, Papel papel) {

    public Usuario {
        Objects.requireNonNull(nome, "nome não pode ser nulo");
        Objects.requireNonNull(papel, "papel não pode ser nulo");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("nome não pode ser vazio");
        }
    }
}
