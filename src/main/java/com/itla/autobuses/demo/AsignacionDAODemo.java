package com.itla.autobuses.demo;

import com.itla.autobuses.dao.AsignacionDAO;
import com.itla.autobuses.model.Asignacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AsignacionDAODemo extends AsignacionDAO {

    private final DemoAlmacen almacen;

    public AsignacionDAODemo(DemoAlmacen almacen) {
        this.almacen = almacen;
    }

    @Override
    public List<Asignacion> listar() {
        List<Asignacion> copia = new ArrayList<>();
        for (Asignacion a : almacen.asignaciones) {
            copia.add(copia(a));
        }
        return copia;
    }

    @Override
    public List<Asignacion> buscar(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        List<Asignacion> resultado = new ArrayList<>();
        for (Asignacion a : almacen.asignaciones) {
            if (contiene(a.getNombreConductor(), filtro)
                    || contiene(a.getPlacaAutobus(), filtro)
                    || contiene(a.getCodigoRuta(), filtro)) {
                resultado.add(copia(a));
            }
        }
        return resultado;
    }

    @Override
    public int insertar(Asignacion a) {
        a.setId(almacen.siguienteAsignacion());
        almacen.enriquecer(a);
        almacen.asignaciones.add(copia(a));
        return a.getId();
    }

    @Override
    public void actualizar(Asignacion a) {
        for (int i = 0; i < almacen.asignaciones.size(); i++) {
            if (almacen.asignaciones.get(i).getId() == a.getId()) {
                almacen.enriquecer(a);
                almacen.asignaciones.set(i, copia(a));
                return;
            }
        }
    }

    @Override
    public void cancelar(int id) {
        for (Asignacion a : almacen.asignaciones) {
            if (a.getId() == id) {
                a.setEstado(Asignacion.CANCELADA);
                return;
            }
        }
    }

    @Override
    public boolean conductorOcupado(int idConductor, LocalDateTime inicio,
                                    LocalDateTime fin, int idExcluido) {
        for (Asignacion a : almacen.asignaciones) {
            if (a.getIdConductor() == idConductor
                    && a.getId() != idExcluido
                    && Asignacion.PROGRAMADA.equals(a.getEstado())
                    && a.seSolapaCon(inicio, fin)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean autobusOcupado(int idAutobus, LocalDateTime inicio,
                                  LocalDateTime fin, int idExcluido) {
        for (Asignacion a : almacen.asignaciones) {
            if (a.getIdAutobus() == idAutobus
                    && a.getId() != idExcluido
                    && Asignacion.PROGRAMADA.equals(a.getEstado())
                    && a.seSolapaCon(inicio, fin)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int contarDelDia(LocalDate dia) {
        int total = 0;
        for (Asignacion a : almacen.asignaciones) {
            if (a.getFechaInicio() != null
                    && a.getFechaInicio().toLocalDate().equals(dia)
                    && !Asignacion.CANCELADA.equals(a.getEstado())) {
                total++;
            }
        }
        return total;
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase().contains(filtro);
    }

    private Asignacion copia(Asignacion original) {
        Asignacion a = new Asignacion();
        a.setId(original.getId());
        a.setIdConductor(original.getIdConductor());
        a.setIdAutobus(original.getIdAutobus());
        a.setIdRuta(original.getIdRuta());
        a.setFechaInicio(original.getFechaInicio());
        a.setFechaFin(original.getFechaFin());
        a.setEstado(original.getEstado());
        a.setIdUsuarioRegistro(original.getIdUsuarioRegistro());
        a.setNombreConductor(original.getNombreConductor());
        a.setPlacaAutobus(original.getPlacaAutobus());
        a.setCodigoRuta(original.getCodigoRuta());
        return a;
    }
}
