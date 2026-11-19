package com.itla.autobuses.service;

import com.itla.autobuses.dao.AsignacionDAO;
import com.itla.autobuses.dao.AutobusDAO;
import com.itla.autobuses.dao.ConductorDAO;
import com.itla.autobuses.dao.RutaDAO;
import com.itla.autobuses.model.Asignacion;
import com.itla.autobuses.model.Autobus;
import com.itla.autobuses.model.Conductor;
import com.itla.autobuses.model.Ruta;
import com.itla.autobuses.util.NegocioException;
import com.itla.autobuses.util.Sesion;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AsignacionService {

    private final AsignacionDAO dao;
    private final ConductorDAO conductores;
    private final AutobusDAO autobuses;
    private final RutaDAO rutas;

    public AsignacionService() {
        this(new AsignacionDAO(), new ConductorDAO(), new AutobusDAO(), new RutaDAO());
    }

    public AsignacionService(AsignacionDAO dao, ConductorDAO conductores,
                             AutobusDAO autobuses, RutaDAO rutas) {
        this.dao = dao;
        this.conductores = conductores;
        this.autobuses = autobuses;
        this.rutas = rutas;
    }

    public List<Asignacion> listar() throws NegocioException {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public List<Asignacion> buscar(String texto) throws NegocioException {
        if (texto == null || texto.isBlank()) {
            return listar();
        }
        try {
            return dao.buscar(texto.trim());
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public void guardar(Asignacion a) throws NegocioException {
        exigirAdmin();
        validar(a);
        try {
            if (a.getId() > 0) {
                Asignacion anterior = buscarAsignacion(a.getId());
                if (anterior != null && Asignacion.CANCELADA.equals(anterior.getEstado())) {
                    throw new NegocioException("No se puede editar una asignación cancelada.");
                }
            }
            validarDisponibilidad(a);
            if (a.getId() == 0) {
                a.setIdUsuarioRegistro(Sesion.getInstancia().getUsuario().getId());
                int id = dao.insertar(a);
                a.setId(id);
                BitacoraHelper.registrar("CREAR", "Asignación", id, detalle(a));
            } else {
                dao.actualizar(a);
                BitacoraHelper.registrar("ACTUALIZAR", "Asignación", a.getId(), detalle(a));
            }
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public void cancelar(Asignacion a) throws NegocioException {
        exigirAdmin();
        if (a.getId() == 0) {
            throw new NegocioException("Selecciona una asignación de la tabla.");
        }
        try {
            Asignacion anterior = buscarAsignacion(a.getId());
            if (anterior == null) {
                throw new NegocioException("La asignación ya no existe.");
            }
            if (!Asignacion.PROGRAMADA.equals(anterior.getEstado())) {
                throw new NegocioException("Solo se puede cancelar una asignación programada.");
            }
            dao.cancelar(a.getId());
            a.setEstado(Asignacion.CANCELADA);
            BitacoraHelper.registrar("CANCELAR", "Asignación", a.getId(), detalle(a));
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public List<Conductor> conductoresDisponibles(LocalDateTime inicio, LocalDateTime fin)
            throws NegocioException {
        validarRango(inicio, fin);
        try {
            List<Conductor> resultado = new ArrayList<>();
            for (Conductor k : conductores.listar()) {
                if (!k.isActivo() || !k.licenciaVigente()) {
                    continue;
                }
                LocalDate finLicencia = k.getVencimientoLicencia();
                if (finLicencia != null && finLicencia.isBefore(fin.toLocalDate())) {
                    continue;
                }
                if (!dao.conductorOcupado(k.getId(), inicio, fin, 0)) {
                    resultado.add(k);
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    public List<Autobus> autobusesDisponibles(LocalDateTime inicio, LocalDateTime fin)
            throws NegocioException {
        validarRango(inicio, fin);
        try {
            List<Autobus> resultado = new ArrayList<>();
            for (Autobus b : autobuses.listarOperativos()) {
                if (!dao.autobusOcupado(b.getId(), inicio, fin, 0)) {
                    resultado.add(b);
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw UsuarioService.errorBD(e);
        }
    }

    private void validar(Asignacion a) throws NegocioException {
        if (a.getIdConductor() <= 0 || a.getIdAutobus() <= 0 || a.getIdRuta() <= 0) {
            throw new NegocioException("Selecciona conductor, autobús y ruta.");
        }
        validarRango(a.getFechaInicio(), a.getFechaFin());
        if (a.getId() == 0 && a.getFechaInicio().isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new NegocioException("La fecha de inicio no puede estar en el pasado.");
        }
        if (a.getEstado() == null || (!Asignacion.PROGRAMADA.equals(a.getEstado())
                && !Asignacion.COMPLETADA.equals(a.getEstado()))) {
            throw new NegocioException("Estado no válido.");
        }
    }

    private void validarDisponibilidad(Asignacion a) throws SQLException, NegocioException {
        Conductor k = buscarConductor(a.getIdConductor());
        if (k == null || !k.isActivo()) {
            throw new NegocioException("El conductor seleccionado no existe o esta inactivo.");
        }
        if (!k.licenciaVigente() || k.getVencimientoLicencia().isBefore(a.getFechaFin().toLocalDate())) {
            throw new NegocioException("La licencia del conductor no esta vigente durante la asignación.");
        }
        Autobus b = buscarAutobus(a.getIdAutobus());
        if (b == null || !b.estaOperativo()) {
            throw new NegocioException("El autobús seleccionado no esta operativo.");
        }
        Ruta r = buscarRuta(a.getIdRuta());
        if (r == null || !r.isActivo()) {
            throw new NegocioException("La ruta seleccionada no esta activa.");
        }
        if (Asignacion.PROGRAMADA.equals(a.getEstado())) {
            int idExcluido = a.getId();
            if (dao.conductorOcupado(a.getIdConductor(), a.getFechaInicio(), a.getFechaFin(), idExcluido)) {
                throw new NegocioException("El conductor ya tiene una asignación en ese horario.");
            }
            if (dao.autobusOcupado(a.getIdAutobus(), a.getFechaInicio(), a.getFechaFin(), idExcluido)) {
                throw new NegocioException("El autobús ya esta asignado en ese horario.");
            }
        }
    }

    private Asignacion buscarAsignacion(int id) throws SQLException {
        for (Asignacion a : dao.listar()) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }

    private Conductor buscarConductor(int id) throws SQLException {
        for (Conductor k : conductores.listar()) {
            if (k.getId() == id) {
                return k;
            }
        }
        return null;
    }

    private Autobus buscarAutobus(int id) throws SQLException {
        for (Autobus b : autobuses.listar()) {
            if (b.getId() == id) {
                return b;
            }
        }
        return null;
    }

    private Ruta buscarRuta(int id) throws SQLException {
        for (Ruta r : rutas.listar()) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    private void validarRango(LocalDateTime inicio, LocalDateTime fin) throws NegocioException {
        if (inicio == null || fin == null) {
            throw new NegocioException("Indica la fecha de inicio y la fecha de fin.");
        }
        if (!fin.isAfter(inicio)) {
            throw new NegocioException("La fecha de fin debe ser posterior a la de inicio.");
        }
    }

    private String detalle(Asignacion a) {
        return (a.getNombreConductor() == null ? "" : a.getNombreConductor())
                + " " + (a.getPlacaAutobus() == null ? "" : a.getPlacaAutobus());
    }

    private void exigirAdmin() throws NegocioException {
        if (!Sesion.getInstancia().esAdmin()) {
            throw new NegocioException("Solo el Administrador puede realizar esta acción.");
        }
    }
}
