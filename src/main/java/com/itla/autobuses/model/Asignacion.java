package com.itla.autobuses.model;

import java.time.LocalDateTime;

public class Asignacion {

    public static final String PROGRAMADA = "Programada";
    public static final String COMPLETADA = "Completada";
    public static final String CANCELADA = "Cancelada";

    private int id;
    private int idConductor;
    private int idAutobus;
    private int idRuta;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String estado = PROGRAMADA;
    private int idUsuarioRegistro;
    private String nombreConductor;
    private String placaAutobus;
    private String codigoRuta;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdConductor() { return idConductor; }
    public void setIdConductor(int idConductor) { this.idConductor = idConductor; }
    public int getIdAutobus() { return idAutobus; }
    public void setIdAutobus(int idAutobus) { this.idAutobus = idAutobus; }
    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(int idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }
    public String getNombreConductor() { return nombreConductor; }
    public void setNombreConductor(String nombreConductor) { this.nombreConductor = nombreConductor; }
    public String getPlacaAutobus() { return placaAutobus; }
    public void setPlacaAutobus(String placaAutobus) { this.placaAutobus = placaAutobus; }
    public String getCodigoRuta() { return codigoRuta; }
    public void setCodigoRuta(String codigoRuta) { this.codigoRuta = codigoRuta; }

    public boolean seSolapaCon(LocalDateTime inicio, LocalDateTime fin) {
        if (fechaInicio == null || fechaFin == null || inicio == null || fin == null) {
            return false;
        }
        return inicio.isBefore(fechaFin) && fin.isAfter(fechaInicio);
    }
}
