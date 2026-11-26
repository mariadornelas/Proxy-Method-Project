package com.faculdade.sensor;

public interface Sensor {

    String id();

    double ler();

    void calibrar(double ganho, double offset);
}
