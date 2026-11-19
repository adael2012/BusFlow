package com.itla.autobuses.model;

public class Resumen {

    private int conductoresActivos;
    private int autobusesOperativos;
    private int asignacionesHoy;
    private int licenciasPorVencer;

    public int getConductoresActivos() { return conductoresActivos; }
    public void setConductoresActivos(int conductoresActivos) { this.conductoresActivos = conductoresActivos; }
    public int getAutobusesOperativos() { return autobusesOperativos; }
    public void setAutobusesOperativos(int autobusesOperativos) { this.autobusesOperativos = autobusesOperativos; }
    public int getAsignacionesHoy() { return asignacionesHoy; }
    public void setAsignacionesHoy(int asignacionesHoy) { this.asignacionesHoy = asignacionesHoy; }
    public int getLicenciasPorVencer() { return licenciasPorVencer; }
    public void setLicenciasPorVencer(int licenciasPorVencer) { this.licenciasPorVencer = licenciasPorVencer; }
}
