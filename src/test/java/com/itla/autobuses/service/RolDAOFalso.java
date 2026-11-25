package com.itla.autobuses.service;

import com.itla.autobuses.dao.RolDAO;
import com.itla.autobuses.model.Rol;

import java.util.List;

class RolDAOFalso extends RolDAO {

    @Override
    public List<Rol> listar() {
        return List.of(
                new Rol(1, Rol.ADMINISTRADOR),
                new Rol(2, Rol.USUARIO_GENERICO),
                new Rol(3, Rol.SUPERADMINISTRADOR));
    }
}
