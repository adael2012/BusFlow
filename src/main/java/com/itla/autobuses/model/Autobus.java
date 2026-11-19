package com.itla.autobuses.model;

public class Autobus {

    public static final String OPERATIVO = "Operativo";
    public static final String MANTENIMIENTO = "Mantenimiento";
    public static final String BAJA = "Baja";

    private int id;
    private String placa;
    private String marca;
    private String modelo;
    private int anio;
    private int capacidad;
    private String estado = OPERATIVO;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean estaOperativo() { return OPERATIVO.equals(estado); }

    @Override
    public String toString() {
        return placa + " - " + marca + " " + modelo;
    }
}
