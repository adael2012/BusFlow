package com.itla.autobuses.service;

import com.itla.autobuses.dao.AutobusDAO;
import com.itla.autobuses.model.Autobus;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AutobusService {

    private final AutobusDAO dao;

    public AutobusService() {
        this(new AutobusDAO());
    }

    public AutobusService(AutobusDAO dao) {
        this.dao = dao;
    }

    public List<Autobus> listar() throws NegocioException {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public List<Autobus> buscar(String texto) throws NegocioException {
        if (texto == null || texto.isBlank()) {
            return listar();
        }
        try {
            return dao.buscar(texto.trim());
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public List<Autobus> listarOperativos() throws NegocioException {
        try {
            return dao.listarOperativos();
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public List<Autobus> listarDisponibles(LocalDateTime inicio, LocalDateTime fin) throws NegocioException {
        validarRango(inicio, fin);
        try {
            return dao.listarDisponibles(inicio, fin);
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public void guardar(Autobus a) throws NegocioException {
        exigirAdmin();
        normalizar(a);
        validar(a);
        try {
            if (a.getId() == 0) {
                int id = dao.insertar(a);
                a.setId(id);
                BitacoraHelper.registrar("CREAR", "Autobús", id, a.getPlaca());
            } else {
                dao.actualizar(a);
                BitacoraHelper.registrar("ACTUALIZAR", "Autobús", a.getId(), a.getPlaca());
            }
        } catch (SQLException e) {
            if (UsuarioService.esDuplicado(e)) {
                throw new NegocioException("Ya existe un autobús con esa placa.");
            }
            throw UsuarioService.errorBD(e);
        }
    }

    public void cambiarEstado(Autobus a, String estado) throws NegocioException {
        exigirAdmin();
        if (!estadosPermitidos().contains(estado)) {
            throw new NegocioException("Estado no válido.");
        }
        try {
            dao.cambiarEstado(a.getId(), estado);
            a.setEstado(estado);
            BitacoraHelper.registrar("ESTADO", "Autobús", a.getId(), a.getPlaca() + " -> " + estado);
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    private void validar(Autobus a) throws NegocioException {
        if (a.getPlaca() == null || !a.getPlaca().trim().matches("[A-Za-z0-9-]{3,10}")) {
            throw new NegocioException("La placa debe tener entre 3 y 10 caracteres.");
        }
        if (vacio(a.getMarca()) || vacio(a.getModelo())) {
            throw new NegocioException("La marca y el modelo son obligatorios.");
        }
        if (a.getMarca().length() > 40 || a.getModelo().length() > 40) {
            throw new NegocioException("La marca y el modelo no pueden superar 40 caracteres.");
        }
        int anioActual = java.time.LocalDate.now().getYear();
        if (a.getAnio() < 1990 || a.getAnio() > anioActual + 1) {
            throw new NegocioException("El año debe estar entre 1990 y " + (anioActual + 1) + ".");
        }
        if (a.getCapacidad() <= 0 || a.getCapacidad() > 200) {
            throw new NegocioException("La capacidad debe estar entre 1 y 200 pasajeros.");
        }
        if (!estadosPermitidos().contains(a.getEstado())) {
            throw new NegocioException("Estado no válido.");
        }
    }

    private void validarRango(LocalDateTime inicio, LocalDateTime fin) throws NegocioException {
        if (inicio == null || fin == null) {
            throw new NegocioException("Indica la fecha de inicio y la fecha de fin.");
        }
        if (!fin.isAfter(inicio)) {
            throw new NegocioException("La fecha de fin debe ser posterior a la de inicio.");
        }
    }

    private List<String> estadosPermitidos() {
        return List.of(Autobus.OPERATIVO, Autobus.MANTENIMIENTO, Autobus.BAJA);
    }

    private void normalizar(Autobus a) {
        if (a.getPlaca() != null) {
            a.setPlaca(a.getPlaca().trim().toUpperCase());
        }
        if (a.getMarca() != null) {
            a.setMarca(a.getMarca().trim());
        }
        if (a.getModelo() != null) {
            a.setModelo(a.getModelo().trim());
        }
    }

    private boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private void exigirAdmin() throws NegocioException {
        if (!Sesion.getInstancia().esAdmin()) {
            throw new NegocioException("Solo el Administrador puede realizar esta acción.");
        }
    }
}
