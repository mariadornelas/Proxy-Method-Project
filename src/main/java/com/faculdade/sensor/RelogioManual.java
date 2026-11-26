package com.faculdade.sensor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;

public class RelogioManual extends Clock {

    private Instant agora;

    public RelogioManual(Instant inicio) {
        this.agora = Objects.requireNonNull(inicio, "início não pode ser nulo");
    }

    public void avancar(Duration tempo) {
        if (tempo.isNegative()) {
            throw new IllegalArgumentException("o tempo não anda para trás");
        }
        agora = agora.plus(tempo);
    }

    @Override
    public Instant instant() {
        return agora;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return this;
    }
}
