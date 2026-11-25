package com.itla.autobuses.demo;

import com.itla.autobuses.dao.AutobusDAO;
import com.itla.autobuses.model.Asignacion;
import com.itla.autobuses.model.Autobus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AutobusDAODemo extends AutobusDAO {

    private final DemoAlmacen almacen;

    public AutobusDAODemo(DemoAlmacen almacen) {
        this.almacen = almacen;
    }

    @Override
    public List<Autobus> listar() {
        List<Autobus> copia = new ArrayList<>();
        for (Autobus b : almacen.autobuses) {
            copia.add(copia(b));
        }
        return copia;
    }

    @Override
    public List<Autobus> buscar(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        List<Autobus> resultado = new ArrayList<>();
        for (Autobus b : almacen.autobuses) {
            if (contiene(b.getPlaca(), filtro)
                    || contiene(b.getMarca(), filtro)
                    || contiene(b.getModelo(), filtro)) {
                resultado.add(copia(b));
            }
        }
        return resultado;
    }

    @Override
    public List<Autobus> listarOperativos() {
        List<Autobus> resultado = new ArrayList<>();
        for (Autobus b : almacen.autobuses) {
            if (b.estaOperativo()) {
                resultado.add(copia(b));
            }
        }
        return resultado;
    }

    @Override
    public List<Autobus> listarDisponibles(LocalDateTime inicio, LocalDateTime fin) {
        List<Autobus> resultado = new ArrayList<>();
        for (Autobus b : almacen.autobuses) {
            if (!b.estaOperativo() || ocupado(b.getId(), inicio, fin)) {
                continue;
            }
            resultado.add(copia(b));
        }
        return resultado;
    }

    @Override
    public int insertar(Autobus a) throws SQLException {
        for (Autobus b : almacen.autobuses) {
            if (b.getPlaca().equalsIgnoreCase(a.getPlaca())) {
                throw DemoAlmacen.duplicado("Autobús duplicado");
            }
        }
        a.setId(almacen.siguienteAutobus());
        almacen.autobuses.add(copia(a));
        return a.getId();
    }

    @Override
    public void actualizar(Autobus a) {
        for (int i = 0; i < almacen.autobuses.size(); i++) {
            if (almacen.autobuses.get(i).getId() == a.getId()) {
                almacen.autobuses.set(i, copia(a));
                return;
            }
        }
    }

    @Override
    public void cambiarEstado(int id, String estado) {
        for (Autobus b : almacen.autobuses) {
            if (b.getId() == id) {
                b.setEstado(estado);
                return;
            }
        }
    }

    private boolean ocupado(int idAutobus, LocalDateTime inicio, LocalDateTime fin) {
        for (Asignacion a : almacen.asignaciones) {
            if (a.getIdAutobus() == idAutobus
                    && Asignacion.PROGRAMADA.equals(a.getEstado())
                    && a.seSolapaCon(inicio, fin)) {
                return true;
            }
        }
        return false;
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase().contains(filtro);
    }

    private Autobus copia(Autobus original) {
        Autobus b = new Autobus();
        b.setId(original.getId());
        b.setPlaca(original.getPlaca());
        b.setMarca(original.getMarca());
        b.setModelo(original.getModelo());
        b.setAnio(original.getAnio());
        b.setCapacidad(original.getCapacidad());
        b.setEstado(original.getEstado());
        return b;
    }
}
