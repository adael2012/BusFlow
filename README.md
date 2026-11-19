# Sistema de Control de Autobuses

Aplicacion de escritorio construida con **Java Swing** y **SQL Server** para gestionar conductores, autobuses, rutas, asignaciones, disponibilidad, usuarios y bitacora. Proyecto final de ITLA.

**Autor:** Adael Araujo Pineda - Matricula 2025-1127

## Entrega final

| Requisito | Estado |
|---|---|
| Proyecto compilable y ejecutable | `mvn clean package` genera `target/control-autobuses-1.0.0.jar` |
| Todos los modulos implementados | 11 modulos operativos segun la seccion siguiente |
| Conexion a base de datos estable y operativa | Configuracion externa, timeouts, 36 llamadas a procedimientos auditadas |
| Validaciones en funcionalidades criticas | 72 pruebas automaticas en verde mas reglas en SQL Server |
| Codigo limpio, organizado y versionado | Capas UI, service, dao, model y util; historial en GitHub |

No se agregaron tecnologias nuevas. La interfaz usa Swing, FlatLaf 3.5.4, el driver JDBC de SQL Server y JUnit 5 solo para pruebas. Los iconos son dibujos Java2D propios.

## Diseno de la interfaz

El rediseno toma como referencia `Mockups_Interfaces_Control_Autobuses.pdf`:

- Login de 1000x620 con panel verde oscuro, marca **RUTA CONTROL DE AUTOBUSES**, bus dibujado y formulario blanco con placeholder, mostrar contrasena y errores en linea.
- Ventana principal de 1180x760 con sidebar de 240 px agrupada en **GENERAL**, **OPERACION** y **SISTEMA**, marca, avatar con iniciales y ficha del usuario.
- Barra superior de 66 px con titulo de seccion, usuario, rol, estado **Sistema activo / Sin conexion**, cambio de contrasena y cierre de sesion.
- Navegacion con `CardLayout`, sin recargar ventanas.
- Tablas con filas alternas, orden por columnas, etiquetas de estado y buscador con placeholder.
- Formularios modales unificados: nuevo registro, editar registro, crear usuario y cambiar contrasena.
- Selector desplegable de fechas y horas con calendario y combos de hora/minuto en Asignaciones, Disponibilidad, Bitacora y vencimiento de licencia; no requiere escribir texto.
- Inicio con tarjetas de indicadores, **Actividad reciente** desde la bitacora y **Alertas** desde licencias por vencer.
- Paleta extraida de los mockups: fondo `#F4F6F4`, superficie `#FFFFFF`, verde oscuro `#113B2D`, verde principal `#0A6B4D`, acento `#16855D` y verde claro `#DBEFE4`.

## Modulos

| Modulo | Funciones |
|---|---|
| Acceso | Login, roles, bloqueo temporal de 5 minutos tras 3 fallos y cierre de sesion |
| Usuarios | Crear, editar rol, restablecer contrasena y desactivar (solo administrador) |
| Contrasena | Cambio propio con verificacion de contrasena actual y nueva diferencia |
| Conductores | Altas, busqueda, estados, licencias vigentes y alerta de vencimiento |
| Autobuses | Altas, busqueda, cambio de estado y control de capacidad |
| Rutas | Altas, busqueda, desactivacion y validacion de origen/destino |
| Asignaciones | Programacion de viajes con control de licencia, recursos activos y solapes |
| Disponibilidad | Consulta de conductores y autobuses libres por rango de fechas |
| Inicio | Indicadores, actividad reciente y alertas |
| Bitacora | Filtros por usuario, accion y rango de fechas (solo administrador) |
| Exportacion | Generacion de `asignaciones.csv` desde la tabla visible |

**Administrador:** ve todos los modulos y puede editar, desactivar y exportar.
**Usuario generico:** ve Inicio, Conductores, Autobuses, Rutas, Asignaciones y Disponibilidad solo para consultar.

## Arquitectura

```
src/main/java/com/itla/autobuses
|-- Main.java          Punto de entrada
|-- ui/                Swing: Tema, Iconos, frames, paneles y dialogos
|-- service/           Reglas de negocio, permisos y validaciones
|-- dao/               CallableStatement contra procedimientos almacenados
|-- model/             Entidades del dominio
`-- util/              ConexionDB, Sesion, Seguridad y NegocioException
database/script.sql    Tablas, datos iniciales, procedimientos, restricciones y trigger
src/test/java          Pruebas JUnit 5 con DAO en memoria
test-reports           Resumen de ejecucion de pruebas
```

Patrones: **Singleton**, **DAO** y capa de **servicios**. Seguridad: PBKDF2-HMAC-SHA256 con salt aleatorio. La interfaz no contiene SQL.

## Requisitos

- JDK 17 o superior
- Maven 3.8 o superior
- SQL Server Express o Developer con TCP/IP habilitado en el puerto 1433

## Instalacion y ejecucion

1. Ejecuta `database/script.sql` completo en SSMS o Azure Data Studio. Crea la base `ControlAutobuses`, las tablas, los datos iniciales, los procedimientos, las restricciones y el trigger anti solapes.
2. Configura la conexion. El programa busca `db.properties` primero en el directorio de trabajo y despues junto al JAR. El archivo incluido es:

   ```properties
   db.url=jdbc:sqlserver://localhost:1433;databaseName=ControlAutobuses;encrypt=true;trustServerCertificate=true
   db.user=sa
   db.password=TuPasswordAqui
   ```

   Reemplaza `TuPasswordAqui` por tu contrasena real en una copia externa para no versionar credenciales. Si el placeholder sigue presente, la aplicacion muestra un aviso y no abre el login.

3. Compila y empaqueta:

   ```
   mvn clean package
   ```

4. Ejecuta:

   ```
   java -jar target/control-autobuses-1.0.0.jar
   ```

En el primer arranque se crea el administrador inicial:

| Usuario | Contrasena |
|---|---|
| `admin` | `Admin123!` |

Cambiala desde **Cambiar contrasena** en la barra superior.

## Validaciones criticas

- Usuarios: complejidad de contrasena, rol existente, no reutilizar la contrasena actual y no desactivar ni degradar al propio administrador.
- Conductores: cedula de 11 digitos, telefono, formato de licencia y vigencia para asignar viajes.
- Autobuses: placa unica, anio valido y capacidad entre 1 y 200.
- Rutas: origen y destino obligatorios y diferentes, distancia y duracion dentro de rango.
- Asignaciones: no editar ni cancelar estados terminales, licencia vigente, recursos activos y sin solapes.
- Base de datos: restricciones de capacidad, rango de ruta y destino diferente, ademas del trigger `TR_Asignacion_Solape`.

## Pruebas

```
mvn test
```

| Grupo | Clases | Pruebas |
|---|---|---|
| Modelo y utilidades | `ModeloTest`, `SeguridadTest`, `SesionTest` | 13 |
| Acceso y usuarios | `BloqueoLoginTest`, `UsuarioServiceTest`, `CambiarPasswordPropioTest` | 11 |
| Operaciones | `ConductorServiceTest`, `AutobusServiceTest`, `RutaServiceTest`, `AsignacionServiceTest`, `PanelServiceTest` | 31 |
| Integracion y reglas finales | `FlujosIntegracionTest`, `ValidacionesCriticasTest` | 17 |

**Total: 72 pruebas, 0 fallos, 0 errores.** No requieren SQL Server porque los servicios reciben los DAO por constructor. El detalle queda en `test-reports/log-ejecucion.txt` y en `target/surefire-reports`.

## Verificacion manual pendiente en tu maquina

Como el desarrollo no tuvo acceso a tu instancia de SQL Server, confirma lo siguiente:

1. Ejecutar `database/script.sql` completo.
2. Copiar `db.properties` con tu contrasena junto al JAR o en la carpeta de trabajo.
3. Ejecutar `mvn clean package` y luego el JAR.
4. Entrar con `admin / Admin123!`, cambiar la contrasena y volver a entrar.
5. Probar el bloqueo con 3 intentos fallidos y esperar 5 minutos.
6. Crear conductor, autobus, ruta y asignacion; repetir la asignacion en el mismo horario para confirmar el aviso de solape.
7. Revisar actividad y alertas en Inicio, filtrar Bitacora y exportar el CSV de asignaciones.
8. Verificar que el chip muestre **Sistema activo** con la base disponible.

## Versionado

Rama principal: `main`.
Remoto: `https://github.com/adael2012/control-autobuses.git`.

Para publicar cambios futuros:

```
git add .
git commit -m "feat: descripcion del cambio"
git push origin main
```

El archivo `.gitignore` excluye `target/` y artefactos temporales. `db.properties` se versiona solo con el placeholder.
