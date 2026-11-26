package com.faculdade.sensor;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class App {

    public static void main(String[] args) {
        AtomicInteger conexoesAbertas = new AtomicInteger();
        SensorRemoto[] real = new SensorRemoto[1];
        RelogioManual relogio = new RelogioManual(Instant.parse("2026-11-25T10:00:00Z"));

        // Camadas, de dentro para fora: criação sob demanda -> cache -> proteção.
        ProxyVirtual virtual = new ProxyVirtual("T-01", () -> {
            conexoesAbertas.incrementAndGet();
            real[0] = new SensorRemoto("T-01", () -> 85.0);
            return real[0];
        });
        ProxyCache cache = new ProxyCache(virtual, Duration.ofSeconds(5), relogio);
        Sensor operador = new ProxyProtecao(cache, new Usuario("Ana", Papel.OPERADOR));
        Sensor visitante = new ProxyProtecao(cache, new Usuario("Visitante", Papel.VISITANTE));
        Sensor manutencao = new ProxyProtecao(cache, new Usuario("Beto", Papel.MANUTENCAO));

        System.out.println("=== Proxy virtual: nada foi criado ainda ===");
        System.out.println("  id = " + operador.id() + " | sensor real criado? " + virtual.foiCriado()
                + " | conexões abertas: " + conexoesAbertas.get());

        System.out.println();
        System.out.println("=== Proxy de cache: 4 leituras, 1 ida ao hardware ===");
        for (int i = 0; i < 4; i++) {
            System.out.printf(Locale.ROOT, "  leitura %.1f%n", operador.ler());
        }
        System.out.println("  idas ao hardware: " + real[0].leituras() + " | acertos de cache: " + cache.acertos()
                + " | conexões abertas: " + conexoesAbertas.get());

        System.out.println();
        System.out.println("=== Cache expira com o tempo ===");
        relogio.avancar(Duration.ofSeconds(5));
        operador.ler();
        System.out.println("  depois de 5 s: idas ao hardware = " + real[0].leituras());

        System.out.println();
        System.out.println("=== Proxy de proteção: o cache aquecido NÃO vaza para quem não tem permissão ===");
        tentar("visitante lê", () -> visitante.ler());
        tentar("operador calibra", () -> operador.calibrar(1.02, 0.0));
        tentar("manutenção calibra", () -> manutencao.calibrar(1.02, 0.0));

        System.out.println();
        System.out.println("=== Calibrar invalida o cache: a próxima leitura já vem corrigida ===");
        System.out.printf(Locale.ROOT, "  leitura depois de calibrar: %.2f%n", operador.ler());
    }

    private static void tentar(String descricao, Runnable r) {
        try {
            r.run();
            System.out.println("  " + descricao + ": permitido");
        } catch (AcessoNegadoException e) {
            System.out.println("  " + descricao + ": NEGADO (" + e.getMessage() + ")");
        }
    }
}
