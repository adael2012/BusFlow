param([switch]$OmitirPruebas)

$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
$escritorio = [Environment]::GetFolderPath('Desktop')
$destino = Join-Path $escritorio 'BusFlow'
$runtime = Join-Path $destino 'runtime'
$jarOrigen = Join-Path $raiz 'target\busflow-1.0.0.jar'
$jarDestino = Join-Path $destino 'busflow-1.0.0.jar'
$bat = Join-Path $destino 'Iniciar BusFlow.bat'

if ($OmitirPruebas) {
    & mvn -q -f (Join-Path $raiz 'pom.xml') -DskipTests clean package
} else {
    & mvn -q -f (Join-Path $raiz 'pom.xml') clean package
}
if ($LASTEXITCODE -ne 0) {
    throw 'No se pudo compilar el proyecto.'
}

New-Item -ItemType Directory -Force -Path $destino | Out-Null
Copy-Item -Path $jarOrigen -Destination $jarDestino -Force
Copy-Item -Path (Join-Path $raiz 'src\main\resources\db.properties') -Destination (Join-Path $destino 'db.properties') -Force
Copy-Item -Path (Join-Path $raiz 'README.md') -Destination (Join-Path $destino 'README.md') -Force

$modulos = 'java.base,java.desktop,java.management,java.naming,java.security.jgss,java.security.sasl,java.sql,jdk.crypto.ec'
if (Test-Path $runtime) {
    Remove-Item -Recurse -Force $runtime
}
& jlink --add-modules $modulos --output $runtime --strip-debug --no-header-files --no-man-pages --compress=2
if ($LASTEXITCODE -ne 0) {
    throw 'No se pudo crear el entorno Java incluido.'
}

$contenidoBat = @'
@echo off
cd /d "%~dp0"
if not exist "%~dp0runtime\bin\javaw.exe" (
  echo Falta la carpeta runtime. Vuelve a ejecutar scripts\preparar-escritorio.ps1.
  pause
  exit /b 1
)
start "" "%~dp0runtime\bin\javaw.exe" -jar "%~dp0busflow-1.0.0.jar"
'@
Set-Content -Path $bat -Value $contenidoBat -Encoding ASCII

$contenidoLeeme = @'
BUSFLOW - EJECUTABLE DE ESCRITORIO

Doble clic en "Iniciar BusFlow.bat".

El programa incluye su propio Java y el logotipo de BusFlow, por lo que no hay que instalar nada en la computadora.

Si SQL Server no está disponible o db.properties no tiene la contraseña real, la aplicación abre automáticamente en modo demostración con datos de ejemplo.

Credenciales:
  Administrador: admin / Admin123!
  Usuario de consulta: usuario / Usuario123!

Para usar la base de datos real en esta computadora, reemplaza db.properties por tu archivo con la contraseña. No compartas ese archivo con otra persona.
'@
Set-Content -Path (Join-Path $destino 'LEEME.txt') -Value $contenidoLeeme -Encoding UTF8

$shell = New-Object -ComObject WScript.Shell
$acceso = $shell.CreateShortcut((Join-Path $escritorio 'BusFlow.lnk'))
$acceso.TargetPath = $bat
$acceso.WorkingDirectory = $destino
$acceso.IconLocation = (Join-Path $runtime 'bin\java.exe') + ',0'
$acceso.Description = 'BusFlow - Sistema de Control de Autobuses'
$acceso.Save()

Write-Host "Paquete creado en: $destino"
Write-Host "Acceso directo creado en: $(Join-Path $escritorio 'BusFlow.lnk')"
