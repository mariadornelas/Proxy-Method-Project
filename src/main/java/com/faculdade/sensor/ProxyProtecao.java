package com.faculdade.sensor;

import java.util.Objects;

public class ProxyProtecao implements Sensor {

    private final Sensor alvo;
    private final Usuario usuario;

    public ProxyProtecao(Sensor alvo, Usuario usuario) {
        this.alvo = Objects.requireNonNull(alvo, "alvo não pode ser nulo");
        this.usuario = Objects.requireNonNull(usuario, "usuário não pode ser nulo");
    }

    @Override
    public String id() {
        return alvo.id();
    }

    @Override
    public double ler() {
        if (usuario.papel() != Papel.OPERADOR && usuario.papel() != Papel.MANUTENCAO) {
            throw negado("ler");
        }
        return alvo.ler();
    }

    @Override
    public void calibrar(double ganho, double offset) {
        if (usuario.papel() != Papel.MANUTENCAO) {
            throw negado("calibrar");
        }
        alvo.calibrar(ganho, offset);
    }

    private AcessoNegadoException negado(String operacao) {
        return new AcessoNegadoException(usuario.nome() + " (" + usuario.papel() + ") não pode " + operacao
                + " o sensor " + alvo.id());
    }
}
