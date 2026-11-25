package com.itla.autobuses.demo;

import com.itla.autobuses.dao.RutaDAO;
import com.itla.autobuses.model.Ruta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RutaDAODemo extends RutaDAO {

    private final DemoAlmacen almacen;

    public RutaDAODemo(DemoAlmacen almacen) {
        this.almacen = almacen;
    }

    @Override
    public List<Ruta> listar() {
        List<Ruta> copia = new ArrayList<>();
        for (Ruta r : almacen.rutas) {
            copia.add(copia(r));
        }
        return copia;
    }

    @Override
    public List<Ruta> buscar(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        List<Ruta> resultado = new ArrayList<>();
        for (Ruta r : almacen.rutas) {
            if (contiene(r.getCodigo(), filtro)
                    || contiene(r.getOrigen(), filtro)
                    || contiene(r.getDestino(), filtro)) {
                resultado.add(copia(r));
            }
        }
        return resultado;
    }

    @Override
    public List<Ruta> listarActivas() {
        List<Ruta> resultado = new ArrayList<>();
        for (Ruta r : almacen.rutas) {
            if (r.isActivo()) {
                resultado.add(copia(r));
            }
        }
        return resultado;
    }

    @Override
    public int insertar(Ruta r) throws SQLException {
        for (Ruta x : almacen.rutas) {
            if (x.getCodigo().equalsIgnoreCase(r.getCodigo())) {
                throw DemoAlmacen.duplicado("Ruta duplicada");
            }
        }
        r.setId(almacen.siguienteRuta());
        almacen.rutas.add(copia(r));
        return r.getId();
    }

    @Override
    public void actualizar(Ruta r) {
        for (int i = 0; i < almacen.rutas.size(); i++) {
            if (almacen.rutas.get(i).getId() == r.getId()) {
                almacen.rutas.set(i, copia(r));
                return;
            }
        }
    }

    @Override
    public void desactivar(int id) {
        for (Ruta r : almacen.rutas) {
            if (r.getId() == id) {
                r.setActivo(false);
                return;
            }
        }
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase().contains(filtro);
    }

    private Ruta copia(Ruta original) {
        Ruta r = new Ruta();
        r.setId(original.getId());
        r.setCodigo(original.getCodigo());
        r.setOrigen(original.getOrigen());
        r.setDestino(original.getDestino());
        r.setDistanciaKm(original.getDistanciaKm());
        r.setDuracionMin(original.getDuracionMin());
        r.setActivo(original.isActivo());
        return r;
    }
}
