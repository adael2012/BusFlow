package com.itla.autobuses.service;

import com.itla.autobuses.dao.RutaDAO;
import com.itla.autobuses.model.Ruta;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class RutaService {

    private static final BigDecimal DISTANCIA_MAXIMA = new BigDecimal("999999.99");
    private static final int DURACION_MAXIMA = 1440;

    private final RutaDAO dao;

    public RutaService() {
        this(new RutaDAO());
    }

    public RutaService(RutaDAO dao) {
        this.dao = dao;
    }

    public List<Ruta> listar() throws NegocioException {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public List<Ruta> buscar(String texto) throws NegocioException {
        if (texto == null || texto.isBlank()) {
            return listar();
        }
        try {
            return dao.buscar(texto.trim());
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public List<Ruta> listarActivas() throws NegocioException {
        try {
            return dao.listarActivas();
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public void guardar(Ruta r) throws NegocioException {
        exigirAdmin();
        normalizar(r);
        validar(r);
        try {
            if (r.getId() == 0) {
                int id = dao.insertar(r);
                r.setId(id);
                BitacoraHelper.registrar("CREAR", "Ruta", id, r.getCodigo());
            } else {
                dao.actualizar(r);
                BitacoraHelper.registrar("ACTUALIZAR", "Ruta", r.getId(), r.getCodigo());
            }
        } catch (SQLException e) {
            if (UsuarioService.esDuplicado(e)) {
                throw new NegocioException("Ya existe una ruta con ese código.");
            }
            throw UsuarioService.errorBD(e);
        }
    }

    public void desactivar(Ruta r) throws NegocioException {
        exigirAdmin();
        try {
            dao.desactivar(r.getId());
            r.setActivo(false);
            BitacoraHelper.registrar("DESACTIVAR", "Ruta", r.getId(), r.getCodigo());
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    private void validar(Ruta r) throws NegocioException {
        if (r.getCodigo() == null || !r.getCodigo().trim().matches("[A-Za-z0-9-]{2,10}")) {
            throw new NegocioException("El código debe tener entre 2 y 10 caracteres.");
        }
        if (vacio(r.getOrigen()) || vacio(r.getDestino())) {
            throw new NegocioException("El origen y el destino son obligatorios.");
        }
        if (r.getOrigen().equalsIgnoreCase(r.getDestino())) {
            throw new NegocioException("El origen y el destino deben ser diferentes.");
        }
        if (r.getOrigen().length() > 60 || r.getDestino().length() > 60) {
            throw new NegocioException("El origen y el destino no pueden superar 60 caracteres.");
        }
        if (r.getDistanciaKm() == null || r.getDistanciaKm().compareTo(BigDecimal.ZERO) <= 0
                || r.getDistanciaKm().compareTo(DISTANCIA_MAXIMA) > 0) {
            throw new NegocioException("La distancia debe estar entre 0.01 y 999999.99 km.");
        }
        if (r.getDuracionMin() <= 0 || r.getDuracionMin() > DURACION_MAXIMA) {
            throw new NegocioException("La duración debe estar entre 1 y 1440 minutos.");
        }
    }

    private void normalizar(Ruta r) {
        if (r.getCodigo() != null) {
            r.setCodigo(r.getCodigo().trim().toUpperCase());
        }
        if (r.getOrigen() != null) {
            r.setOrigen(r.getOrigen().trim());
        }
        if (r.getDestino() != null) {
            r.setDestino(r.getDestino().trim());
        }
    }

    private boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private void exigirAdmin() throws NegocioException {
        Permisos.exigir(Permisos.Permiso.GESTIONAR_OPERACION);
    }
}
