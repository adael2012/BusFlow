IF DB_ID('ControlAutobuses') IS NULL
    CREATE DATABASE ControlAutobuses;
GO
USE ControlAutobuses;
GO

IF OBJECT_ID('dbo.Rol') IS NULL
CREATE TABLE dbo.Rol (
    IdRol  INT IDENTITY(1,1) PRIMARY KEY,
    Nombre NVARCHAR(40) NOT NULL UNIQUE
);
GO
IF OBJECT_ID('dbo.Usuario') IS NULL
CREATE TABLE dbo.Usuario (
    IdUsuario     INT IDENTITY(1,1) PRIMARY KEY,
    Username      NVARCHAR(30)  NOT NULL UNIQUE,
    PasswordHash  NVARCHAR(100) NOT NULL,
    Salt          NVARCHAR(50)  NOT NULL,
    IdRol         INT NOT NULL REFERENCES dbo.Rol(IdRol),
    Activo            BIT NOT NULL DEFAULT 1,
    FechaCreacion     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    EstadoCuenta      NVARCHAR(20) NOT NULL DEFAULT (N'Activo'),
    FechaModificacion DATETIME2 NULL,
    UltimoAcceso      DATETIME2 NULL,
    Eliminado         BIT NOT NULL DEFAULT 0,
    FechaEliminacion  DATETIME2 NULL
);
GO
IF OBJECT_ID('dbo.Conductor') IS NULL
CREATE TABLE dbo.Conductor (
    IdConductor         INT IDENTITY(1,1) PRIMARY KEY,
    Cedula              NVARCHAR(11) NOT NULL UNIQUE,
    Nombre              NVARCHAR(60) NOT NULL,
    Apellido            NVARCHAR(60) NOT NULL,
    Telefono            NVARCHAR(15) NULL,
    NumeroLicencia      NVARCHAR(30) NOT NULL UNIQUE,
    VencimientoLicencia DATE NOT NULL,
    Activo              BIT NOT NULL DEFAULT 1
);
GO
IF OBJECT_ID('dbo.Autobus') IS NULL
CREATE TABLE dbo.Autobus (
    IdAutobus INT IDENTITY(1,1) PRIMARY KEY,
    Placa     NVARCHAR(10) NOT NULL UNIQUE,
    Marca     NVARCHAR(40) NOT NULL,
    Modelo    NVARCHAR(40) NOT NULL,
    Anio      INT NOT NULL,
    Capacidad INT NOT NULL CHECK (Capacidad > 0),
    Estado    NVARCHAR(15) NOT NULL DEFAULT 'Operativo'
              CHECK (Estado IN ('Operativo','Mantenimiento','Baja'))
);
GO
IF OBJECT_ID('dbo.Ruta') IS NULL
CREATE TABLE dbo.Ruta (
    IdRuta      INT IDENTITY(1,1) PRIMARY KEY,
    Codigo      NVARCHAR(10) NOT NULL UNIQUE,
    Origen      NVARCHAR(60) NOT NULL,
    Destino     NVARCHAR(60) NOT NULL,
    DistanciaKm DECIMAL(8,2) NOT NULL,
    DuracionMin INT NOT NULL,
    Activo      BIT NOT NULL DEFAULT 1
);
GO
IF OBJECT_ID('dbo.Asignacion') IS NULL
CREATE TABLE dbo.Asignacion (
    IdAsignacion      INT IDENTITY(1,1) PRIMARY KEY,
    IdConductor       INT NOT NULL REFERENCES dbo.Conductor(IdConductor),
    IdAutobus         INT NOT NULL REFERENCES dbo.Autobus(IdAutobus),
    IdRuta            INT NOT NULL REFERENCES dbo.Ruta(IdRuta),
    FechaInicio       DATETIME2 NOT NULL,
    FechaFin          DATETIME2 NOT NULL,
    Estado            NVARCHAR(15) NOT NULL DEFAULT 'Programada'
                      CHECK (Estado IN ('Programada','Completada','Cancelada')),
    IdUsuarioRegistro INT NOT NULL REFERENCES dbo.Usuario(IdUsuario),
    CONSTRAINT CK_Asignacion_Fechas CHECK (FechaFin > FechaInicio)
);
GO
IF OBJECT_ID('dbo.Bitacora') IS NULL
CREATE TABLE dbo.Bitacora (
    IdBitacora INT IDENTITY(1,1) PRIMARY KEY,
    IdUsuario  INT NOT NULL REFERENCES dbo.Usuario(IdUsuario),
    Accion     NVARCHAR(30) NOT NULL,
    Tabla      NVARCHAR(30) NOT NULL,
    IdRegistro INT NULL,
    Fecha      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    Detalle    NVARCHAR(300) NULL,
    Resultado  NVARCHAR(20) NOT NULL DEFAULT (N'ÉXITO')
);
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Rol WHERE Nombre = N'Administrador')
    INSERT INTO dbo.Rol (Nombre) VALUES (N'Administrador');
IF NOT EXISTS (SELECT 1 FROM dbo.Rol WHERE Nombre = N'Usuario Genérico')
    INSERT INTO dbo.Rol (Nombre) VALUES (N'Usuario Genérico');
IF EXISTS (SELECT 1 FROM dbo.Rol WHERE Nombre = N'Usuario Generico')
   AND NOT EXISTS (SELECT 1 FROM dbo.Rol WHERE Nombre = N'Usuario Genérico')
    UPDATE dbo.Rol SET Nombre = N'Usuario Genérico' WHERE Nombre = N'Usuario Generico';
GO

/* -------------------------------------------------------------------------
   Migración segura y repetible para la jerarquía administrativa de BusFlow.
   Solo agrega objetos/columnas; no borra ni recrea tablas ni datos.
   ------------------------------------------------------------------------- */
IF COL_LENGTH(N'dbo.Usuario', N'EstadoCuenta') IS NULL
    ALTER TABLE dbo.Usuario ADD EstadoCuenta NVARCHAR(20) NOT NULL
        CONSTRAINT DF_Usuario_EstadoCuenta DEFAULT (N'Activo');
GO
IF COL_LENGTH(N'dbo.Usuario', N'FechaModificacion') IS NULL
    ALTER TABLE dbo.Usuario ADD FechaModificacion DATETIME2 NULL;
GO
IF COL_LENGTH(N'dbo.Usuario', N'UltimoAcceso') IS NULL
    ALTER TABLE dbo.Usuario ADD UltimoAcceso DATETIME2 NULL;
GO
IF COL_LENGTH(N'dbo.Usuario', N'Eliminado') IS NULL
    ALTER TABLE dbo.Usuario ADD Eliminado BIT NOT NULL
        CONSTRAINT DF_Usuario_Eliminado DEFAULT (0);
GO
IF COL_LENGTH(N'dbo.Usuario', N'FechaEliminacion') IS NULL
    ALTER TABLE dbo.Usuario ADD FechaEliminacion DATETIME2 NULL;
GO
IF COL_LENGTH(N'dbo.Bitacora', N'Resultado') IS NULL
    ALTER TABLE dbo.Bitacora ADD Resultado NVARCHAR(20) NOT NULL
        CONSTRAINT DF_Bitacora_Resultado DEFAULT (N'ÉXITO');
GO
IF NOT EXISTS (SELECT 1 FROM dbo.Rol WHERE Nombre = N'SUPERADMINISTRADOR')
    INSERT INTO dbo.Rol (Nombre) VALUES (N'SUPERADMINISTRADOR');
GO
UPDATE dbo.Usuario SET FechaModificacion = FechaCreacion
WHERE FechaModificacion IS NULL;
GO
UPDATE dbo.Usuario SET EstadoCuenta = N'Inactivo'
WHERE Activo = 0 AND EstadoCuenta = N'Activo';
GO
UPDATE dbo.Usuario SET EstadoCuenta = N'Activo'
WHERE Activo = 1 AND EstadoCuenta = N'Inactivo';
GO
UPDATE dbo.Usuario SET EstadoCuenta = N'Eliminado'
WHERE Eliminado = 1 AND EstadoCuenta <> N'Eliminado';
GO
UPDATE u SET UltimoAcceso = (
    SELECT MAX(b.Fecha) FROM dbo.Bitacora b
    WHERE b.IdUsuario = u.IdUsuario AND b.Accion = N'LOGIN')
FROM dbo.Usuario u
WHERE u.UltimoAcceso IS NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name = N'CK_Usuario_EstadoCuenta'
                 AND parent_object_id = OBJECT_ID(N'dbo.Usuario'))
    ALTER TABLE dbo.Usuario ADD CONSTRAINT CK_Usuario_EstadoCuenta
        CHECK (EstadoCuenta IN (N'Activo', N'Inactivo', N'Bloqueado', N'Eliminado'));
GO

CREATE OR ALTER PROCEDURE dbo.sp_Rol_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT IdRol, Nombre FROM dbo.Rol ORDER BY IdRol;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Usuario_Contar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT COUNT(*) AS Total FROM dbo.Usuario;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Usuario_ObtenerPorUsername
    @Username NVARCHAR(30)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT u.IdUsuario, u.Username, u.PasswordHash, u.Salt, u.IdRol,
           r.Nombre AS RolNombre, u.Activo,
           u.EstadoCuenta, u.FechaCreacion, u.FechaModificacion,
           u.UltimoAcceso, u.Eliminado, u.FechaEliminacion
    FROM dbo.Usuario u INNER JOIN dbo.Rol r ON r.IdRol = u.IdRol
    WHERE u.Username = @Username;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Usuario_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT u.IdUsuario, u.Username, u.PasswordHash, u.Salt, u.IdRol,
           r.Nombre AS RolNombre, u.Activo,
           u.EstadoCuenta, u.FechaCreacion, u.FechaModificacion,
           u.UltimoAcceso, u.Eliminado, u.FechaEliminacion
    FROM dbo.Usuario u INNER JOIN dbo.Rol r ON r.IdRol = u.IdRol
    ORDER BY u.Username;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Usuario_Insertar
    @Username     NVARCHAR(30),
    @PasswordHash NVARCHAR(100),
    @Salt         NVARCHAR(50),
    @IdRol        INT
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO dbo.Usuario (Username, PasswordHash, Salt, IdRol)
    VALUES (@Username, @PasswordHash, @Salt, @IdRol);
    SELECT CAST(SCOPE_IDENTITY() AS INT) AS IdUsuario;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Usuario_Actualizar
    @IdUsuario INT,
    @IdRol     INT,
    @Activo    BIT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Usuario
       SET IdRol = @IdRol,
           Activo = @Activo,
           EstadoCuenta = CASE WHEN @Activo = 1 THEN N'Activo' ELSE N'Inactivo' END,
           FechaModificacion = SYSDATETIME()
     WHERE IdUsuario = @IdUsuario AND Eliminado = 0;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Usuario_CambiarPassword
    @IdUsuario    INT,
    @PasswordHash NVARCHAR(100),
    @Salt         NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Usuario
       SET PasswordHash = @PasswordHash,
           Salt = @Salt,
           FechaModificacion = SYSDATETIME()
     WHERE IdUsuario = @IdUsuario AND Eliminado = 0;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Usuario_Desactivar
    @IdUsuario INT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Usuario
       SET Activo = 0,
           EstadoCuenta = N'Inactivo',
           FechaModificacion = SYSDATETIME()
     WHERE IdUsuario = @IdUsuario;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Usuario_Eliminar
    @IdUsuario INT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Usuario
       SET Activo = 0,
           Eliminado = 1,
           EstadoCuenta = N'Eliminado',
           FechaEliminacion = SYSDATETIME(),
           FechaModificacion = SYSDATETIME()
     WHERE IdUsuario = @IdUsuario;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Usuario_Reactivar
    @IdUsuario INT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Usuario
       SET Activo = 1,
           Eliminado = 0,
           EstadoCuenta = N'Activo',
           FechaEliminacion = NULL,
           FechaModificacion = SYSDATETIME()
     WHERE IdUsuario = @IdUsuario;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Usuario_RegistrarAcceso
    @IdUsuario INT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Usuario SET UltimoAcceso = SYSDATETIME()
     WHERE IdUsuario = @IdUsuario AND Eliminado = 0;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Conductor_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT IdConductor, Cedula, Nombre, Apellido, Telefono,
           NumeroLicencia, VencimientoLicencia, Activo
    FROM dbo.Conductor ORDER BY Apellido, Nombre;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Conductor_Buscar
    @Texto NVARCHAR(60)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT IdConductor, Cedula, Nombre, Apellido, Telefono,
           NumeroLicencia, VencimientoLicencia, Activo
    FROM dbo.Conductor
    WHERE Cedula LIKE '%' + @Texto + '%'
       OR Nombre LIKE '%' + @Texto + '%'
       OR Apellido LIKE '%' + @Texto + '%'
       OR NumeroLicencia LIKE '%' + @Texto + '%'
    ORDER BY Apellido, Nombre;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Conductor_Insertar
    @Cedula NVARCHAR(11), @Nombre NVARCHAR(60), @Apellido NVARCHAR(60),
    @Telefono NVARCHAR(15), @NumeroLicencia NVARCHAR(30),
    @VencimientoLicencia DATE, @Activo BIT
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO dbo.Conductor
        (Cedula, Nombre, Apellido, Telefono, NumeroLicencia, VencimientoLicencia, Activo)
    VALUES
        (@Cedula, @Nombre, @Apellido, @Telefono, @NumeroLicencia, @VencimientoLicencia, @Activo);
    SELECT CAST(SCOPE_IDENTITY() AS INT) AS IdConductor;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Conductor_Actualizar
    @IdConductor INT, @Cedula NVARCHAR(11), @Nombre NVARCHAR(60),
    @Apellido NVARCHAR(60), @Telefono NVARCHAR(15), @NumeroLicencia NVARCHAR(30),
    @VencimientoLicencia DATE, @Activo BIT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Conductor
    SET Cedula = @Cedula, Nombre = @Nombre, Apellido = @Apellido,
        Telefono = @Telefono, NumeroLicencia = @NumeroLicencia,
        VencimientoLicencia = @VencimientoLicencia, Activo = @Activo
    WHERE IdConductor = @IdConductor;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Conductor_Eliminar
    @IdConductor INT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Conductor SET Activo = 0 WHERE IdConductor = @IdConductor;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Bitacora_Insertar
    @IdUsuario INT, @Accion NVARCHAR(30), @Tabla NVARCHAR(30),
    @IdRegistro INT, @Detalle NVARCHAR(300)
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO dbo.Bitacora (IdUsuario, Accion, Tabla, IdRegistro, Detalle)
    VALUES (@IdUsuario, @Accion, @Tabla, @IdRegistro, @Detalle);
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Bitacora_InsertarSeguro
    @IdUsuario INT, @Accion NVARCHAR(30), @Tabla NVARCHAR(30),
    @IdRegistro INT, @Detalle NVARCHAR(300), @Resultado NVARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO dbo.Bitacora (IdUsuario, Accion, Tabla, IdRegistro, Detalle, Resultado)
    VALUES (@IdUsuario, @Accion, @Tabla, @IdRegistro, @Detalle, @Resultado);
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Bitacora_Listar
    @Username NVARCHAR(30), @Accion NVARCHAR(30),
    @Desde DATE, @Hasta DATE
AS
BEGIN
    SET NOCOUNT ON;
    SELECT b.IdBitacora, u.Username, b.Accion, b.Tabla, b.IdRegistro, b.Fecha, b.Detalle,
           b.Resultado
    FROM dbo.Bitacora b
    INNER JOIN dbo.Usuario u ON u.IdUsuario = b.IdUsuario
    WHERE (@Username IS NULL OR u.Username LIKE '%' + @Username + '%')
      AND (@Accion IS NULL OR b.Accion = @Accion)
      AND (@Desde IS NULL OR CAST(b.Fecha AS DATE) >= @Desde)
      AND (@Hasta IS NULL OR CAST(b.Fecha AS DATE) <= @Hasta)
    ORDER BY b.Fecha DESC, b.IdBitacora DESC;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Autobus_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT IdAutobus, Placa, Marca, Modelo, Anio, Capacidad, Estado
    FROM dbo.Autobus ORDER BY Placa;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Autobus_Buscar
    @Texto NVARCHAR(30)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT IdAutobus, Placa, Marca, Modelo, Anio, Capacidad, Estado
    FROM dbo.Autobus
    WHERE Placa LIKE '%' + @Texto + '%'
       OR Marca LIKE '%' + @Texto + '%'
       OR Modelo LIKE '%' + @Texto + '%'
    ORDER BY Placa;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Autobus_ListarOperativos
AS
BEGIN
    SET NOCOUNT ON;
    SELECT IdAutobus, Placa, Marca, Modelo, Anio, Capacidad, Estado
    FROM dbo.Autobus WHERE Estado = 'Operativo' ORDER BY Placa;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Autobus_Disponibles
    @Inicio DATETIME2, @Fin DATETIME2
AS
BEGIN
    SET NOCOUNT ON;
    SELECT a.IdAutobus, a.Placa, a.Marca, a.Modelo, a.Anio, a.Capacidad, a.Estado
    FROM dbo.Autobus a
    WHERE a.Estado = 'Operativo'
      AND NOT EXISTS (
          SELECT 1 FROM dbo.Asignacion x
          WHERE x.IdAutobus = a.IdAutobus
            AND x.Estado = 'Programada'
            AND @Inicio < x.FechaFin
            AND @Fin > x.FechaInicio
      )
    ORDER BY a.Placa;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Autobus_Insertar
    @Placa NVARCHAR(10), @Marca NVARCHAR(40), @Modelo NVARCHAR(40),
    @Anio INT, @Capacidad INT, @Estado NVARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO dbo.Autobus (Placa, Marca, Modelo, Anio, Capacidad, Estado)
    VALUES (@Placa, @Marca, @Modelo, @Anio, @Capacidad, @Estado);
    SELECT CAST(SCOPE_IDENTITY() AS INT) AS IdAutobus;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Autobus_Actualizar
    @IdAutobus INT, @Placa NVARCHAR(10), @Marca NVARCHAR(40), @Modelo NVARCHAR(40),
    @Anio INT, @Capacidad INT, @Estado NVARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Autobus
    SET Placa = @Placa, Marca = @Marca, Modelo = @Modelo,
        Anio = @Anio, Capacidad = @Capacidad, Estado = @Estado
    WHERE IdAutobus = @IdAutobus;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Autobus_CambiarEstado
    @IdAutobus INT, @Estado NVARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Autobus SET Estado = @Estado WHERE IdAutobus = @IdAutobus;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Ruta_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT IdRuta, Codigo, Origen, Destino, DistanciaKm, DuracionMin, Activo
    FROM dbo.Ruta ORDER BY Codigo;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Ruta_Buscar
    @Texto NVARCHAR(30)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT IdRuta, Codigo, Origen, Destino, DistanciaKm, DuracionMin, Activo
    FROM dbo.Ruta
    WHERE Codigo LIKE '%' + @Texto + '%'
       OR Origen LIKE '%' + @Texto + '%'
       OR Destino LIKE '%' + @Texto + '%'
    ORDER BY Codigo;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Ruta_ListarActivas
AS
BEGIN
    SET NOCOUNT ON;
    SELECT IdRuta, Codigo, Origen, Destino, DistanciaKm, DuracionMin, Activo
    FROM dbo.Ruta WHERE Activo = 1 ORDER BY Codigo;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Ruta_Insertar
    @Codigo NVARCHAR(10), @Origen NVARCHAR(60), @Destino NVARCHAR(60),
    @DistanciaKm DECIMAL(8,2), @DuracionMin INT
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO dbo.Ruta (Codigo, Origen, Destino, DistanciaKm, DuracionMin)
    VALUES (@Codigo, @Origen, @Destino, @DistanciaKm, @DuracionMin);
    SELECT CAST(SCOPE_IDENTITY() AS INT) AS IdRuta;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Ruta_Actualizar
    @IdRuta INT, @Codigo NVARCHAR(10), @Origen NVARCHAR(60), @Destino NVARCHAR(60),
    @DistanciaKm DECIMAL(8,2), @DuracionMin INT, @Activo BIT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Ruta
    SET Codigo = @Codigo, Origen = @Origen, Destino = @Destino,
        DistanciaKm = @DistanciaKm, DuracionMin = @DuracionMin, Activo = @Activo
    WHERE IdRuta = @IdRuta;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Ruta_Desactivar
    @IdRuta INT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Ruta SET Activo = 0 WHERE IdRuta = @IdRuta;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Asignacion_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT a.IdAsignacion, a.IdConductor, a.IdAutobus, a.IdRuta,
           a.FechaInicio, a.FechaFin, a.Estado,
           k.Nombre + ' ' + k.Apellido AS NombreConductor,
           b.Placa AS PlacaAutobus,
           r.Codigo AS CodigoRuta
    FROM dbo.Asignacion a
    INNER JOIN dbo.Conductor k ON k.IdConductor = a.IdConductor
    INNER JOIN dbo.Autobus b ON b.IdAutobus = a.IdAutobus
    INNER JOIN dbo.Ruta r ON r.IdRuta = a.IdRuta
    ORDER BY a.FechaInicio DESC;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Asignacion_Buscar
    @Texto NVARCHAR(60)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT a.IdAsignacion, a.IdConductor, a.IdAutobus, a.IdRuta,
           a.FechaInicio, a.FechaFin, a.Estado,
           k.Nombre + ' ' + k.Apellido AS NombreConductor,
           b.Placa AS PlacaAutobus,
           r.Codigo AS CodigoRuta
    FROM dbo.Asignacion a
    INNER JOIN dbo.Conductor k ON k.IdConductor = a.IdConductor
    INNER JOIN dbo.Autobus b ON b.IdAutobus = a.IdAutobus
    INNER JOIN dbo.Ruta r ON r.IdRuta = a.IdRuta
    WHERE k.Cedula LIKE '%' + @Texto + '%'
       OR k.Nombre LIKE '%' + @Texto + '%'
       OR k.Apellido LIKE '%' + @Texto + '%'
       OR b.Placa LIKE '%' + @Texto + '%'
       OR r.Codigo LIKE '%' + @Texto + '%'
    ORDER BY a.FechaInicio DESC;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Asignacion_Insertar
    @IdConductor INT, @IdAutobus INT, @IdRuta INT,
    @FechaInicio DATETIME2, @FechaFin DATETIME2,
    @Estado NVARCHAR(15), @IdUsuarioRegistro INT
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO dbo.Asignacion
        (IdConductor, IdAutobus, IdRuta, FechaInicio, FechaFin, Estado, IdUsuarioRegistro)
    VALUES
        (@IdConductor, @IdAutobus, @IdRuta, @FechaInicio, @FechaFin, @Estado, @IdUsuarioRegistro);
    SELECT CAST(SCOPE_IDENTITY() AS INT) AS IdAsignacion;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Asignacion_Actualizar
    @IdAsignacion INT, @IdConductor INT, @IdAutobus INT, @IdRuta INT,
    @FechaInicio DATETIME2, @FechaFin DATETIME2, @Estado NVARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Asignacion
    SET IdConductor = @IdConductor, IdAutobus = @IdAutobus, IdRuta = @IdRuta,
        FechaInicio = @FechaInicio, FechaFin = @FechaFin, Estado = @Estado
    WHERE IdAsignacion = @IdAsignacion;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Asignacion_Cancelar
    @IdAsignacion INT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.Asignacion SET Estado = 'Cancelada' WHERE IdAsignacion = @IdAsignacion;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Asignacion_ConductorOcupado
    @IdConductor INT, @Inicio DATETIME2, @Fin DATETIME2, @IdExcluido INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT COUNT(*) AS Total
    FROM dbo.Asignacion
    WHERE IdConductor = @IdConductor
      AND IdAsignacion <> @IdExcluido
      AND Estado = 'Programada'
      AND @Inicio < FechaFin
      AND @Fin > FechaInicio;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Asignacion_AutobusOcupado
    @IdAutobus INT, @Inicio DATETIME2, @Fin DATETIME2, @IdExcluido INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT COUNT(*) AS Total
    FROM dbo.Asignacion
    WHERE IdAutobus = @IdAutobus
      AND IdAsignacion <> @IdExcluido
      AND Estado = 'Programada'
      AND @Inicio < FechaFin
      AND @Fin > FechaInicio;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Asignacion_ContarDelDia
    @Dia DATE
AS
BEGIN
    SET NOCOUNT ON;
    SELECT COUNT(*) AS Total
    FROM dbo.Asignacion
    WHERE CAST(FechaInicio AS DATE) = @Dia
      AND Estado <> 'Cancelada';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_Autobus_CapacidadMaxima')
    ALTER TABLE dbo.Autobus ADD CONSTRAINT CK_Autobus_CapacidadMaxima CHECK (Capacidad BETWEEN 1 AND 200);
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_Ruta_DestinoDiferente')
    ALTER TABLE dbo.Ruta ADD CONSTRAINT CK_Ruta_DestinoDiferente CHECK (LTRIM(RTRIM(Destino)) <> LTRIM(RTRIM(Origen)));
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_Ruta_DistanciaMaxima')
    ALTER TABLE dbo.Ruta ADD CONSTRAINT CK_Ruta_DistanciaMaxima CHECK (DistanciaKm BETWEEN 0.01 AND 999999.99);
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_Ruta_DuracionMaxima')
    ALTER TABLE dbo.Ruta ADD CONSTRAINT CK_Ruta_DuracionMaxima CHECK (DuracionMin BETWEEN 1 AND 1440);
GO

CREATE OR ALTER TRIGGER dbo.TR_Asignacion_Solape
ON dbo.Asignacion
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM inserted i
        INNER JOIN dbo.Asignacion a ON a.IdAsignacion <> i.IdAsignacion
        WHERE i.Estado = N'Programada'
          AND a.Estado = N'Programada'
          AND (i.IdConductor = a.IdConductor OR i.IdAutobus = a.IdAutobus)
          AND i.FechaInicio < a.FechaFin
          AND i.FechaFin > a.FechaInicio
    )
    BEGIN
        ;THROW 50001, 'El conductor o el autobus ya tiene una asignacion programada en ese horario.', 1;
    END
END
GO
