<#
  Inicia los dos microservicios y genera la APK apuntando a la IP de ESTE computador.
  No necesita llave: si no hay llave de entrega, la release se firma con la de depuracion.

  Uso (desde la raiz del repositorio):
    .\scripts\Iniciar-Todo.ps1                 detecta la IP, levanta servicios y compila la APK
    .\scripts\Iniciar-Todo.ps1 -Instalar       ademas instala la APK en el telefono conectado por USB
    .\scripts\Iniciar-Todo.ps1 -SoloServicios  solo levanta los microservicios
    .\scripts\Iniciar-Todo.ps1 -SinNavegador   no abre Edge con los endpoints
    .\scripts\Iniciar-Todo.ps1 -Firewall       abre los puertos 8081 y 8082 (requiere PowerShell como administrador)
    .\scripts\Iniciar-Todo.ps1 -BackendHost 192.168.1.20   fuerza otra IP
#>
param(
    [string]$BackendHost,
    [switch]$Instalar,
    [switch]$SoloServicios,
    [switch]$SinNavegador,
    [switch]$Firewall,
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$AndroidSdk = $env:ANDROID_HOME
)
$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot

# --- Entorno ---
if (-not $JavaHome) { $JavaHome = 'C:\Program Files\Android\Android Studio\jbr' }
if (-not (Test-Path (Join-Path $JavaHome 'bin\java.exe'))) { throw "No se encontro Java en $JavaHome. Indica -JavaHome con un JDK 21." }
if (-not $AndroidSdk) { $AndroidSdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$env:JAVA_HOME = $JavaHome

# --- IP de este computador (la interfaz con salida a la red) ---
if (-not $BackendHost) {
    $cfg = Get-NetIPConfiguration |
        Where-Object { $_.IPv4DefaultGateway -ne $null -and $_.NetAdapter.Status -eq 'Up' } |
        Select-Object -First 1
    if (-not $cfg) { throw 'No se detecto una red activa. Conectate a WiFi o indica -BackendHost.' }
    $BackendHost = $cfg.IPv4Address.IPAddress
}
Write-Output "IP de este computador: $BackendHost"

# --- Firewall (opcional) ---
if ($Firewall) {
    $admin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
    if (-not $admin) { Write-Warning 'Para abrir los puertos hay que ejecutar PowerShell como administrador.' }
    else {
        foreach ($puerto in 8081, 8082) {
            $nombre = "Alarma Zaror $puerto"
            if (-not (Get-NetFirewallRule -DisplayName $nombre -ErrorAction SilentlyContinue)) {
                [void](New-NetFirewallRule -DisplayName $nombre -Direction Inbound -Protocol TCP -LocalPort $puerto -Action Allow -Profile Private)
            }
        }
        Write-Output 'Puertos 8081 y 8082 permitidos en la red privada.'
    }
}

# --- Microservicios: una ventana por servicio ---
function Iniciar-Servicio($modulo, $puerto) {
    $enUso = Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue
    if ($enUso) { Write-Output "$modulo ya esta escuchando en el puerto $puerto."; return }
    $cmd = "`$env:JAVA_HOME='$JavaHome'; Set-Location '$raiz\backend'; .\gradlew.bat :${modulo}:bootRun"
    Start-Process powershell -ArgumentList '-NoExit', '-Command', $cmd | Out-Null
    Write-Output "Iniciando $modulo (puerto $puerto)..."
}
Iniciar-Servicio 'ms-usuarios' 8081
Iniciar-Servicio 'ms-alertas' 8082

function Esperar-Servicio($url, $nombre) {
    for ($i = 0; $i -lt 90; $i++) {
        try { [void](Invoke-RestMethod -Uri $url -TimeoutSec 2); Write-Output "$nombre listo: $url"; return }
        catch { Start-Sleep -Seconds 2 }
    }
    throw "$nombre no respondio en 3 minutos. Revisa su ventana."
}
Esperar-Servicio 'http://localhost:8081/api/usuarios' 'ms-usuarios'
Esperar-Servicio 'http://localhost:8082/api/alertas' 'ms-alertas'

# --- Abrir los microservicios en Edge (o en el navegador predeterminado si no esta) ---
if (-not $SinNavegador) {
    $urls = @('http://localhost:8081/api/usuarios', 'http://localhost:8082/api/alertas')
    try { Start-Process 'msedge' -ArgumentList $urls -ErrorAction Stop }
    catch {
        Write-Warning 'No se pudo abrir Edge; se usa el navegador predeterminado.'
        foreach ($u in $urls) { Start-Process $u }
    }
    Write-Output 'Endpoints abiertos en el navegador.'
}

if ($SoloServicios) {
    Write-Output "Servicios activos. Desde el telefono: http://${BackendHost}:8081 y http://${BackendHost}:8082"
    return
}

# --- APK apuntando a este computador ---
Push-Location $raiz
try {
    & .\gradlew.bat :app:assembleRelease "-PbackendHost=$BackendHost" --no-configuration-cache --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Fallo la compilacion de la APK.' }
} finally { Pop-Location }

$apk = Join-Path $raiz 'app\build\outputs\apk\release\app-release.apk'
$salida = Join-Path $raiz 'dist\entrega'
[void](New-Item -ItemType Directory -Path $salida -Force)
$destino = Join-Path $salida 'AlarmaComunitariaZaror-celular.apk'
Copy-Item -LiteralPath $apk -Destination $destino -Force
$hash = (Get-FileHash -LiteralPath $destino -Algorithm SHA256).Hash.ToLowerInvariant()
"$hash  AlarmaComunitariaZaror-celular.apk  (host $BackendHost)" | Set-Content -Encoding ascii (Join-Path $salida 'SHA256SUMS.txt')

# --- Instalacion opcional por USB ---
$adb = Join-Path $AndroidSdk 'platform-tools\adb.exe'
if ($Instalar) {
    if (-not (Test-Path $adb)) { throw "No se encontro adb en $adb." }
    $dispositivos = & $adb devices | Select-String -Pattern "`tdevice$"
    if (-not $dispositivos) { Write-Warning 'No hay un telefono o emulador conectado; la APK no se instalo.' }
    else {
        & $adb install -r $destino
        if ($LASTEXITCODE -ne 0) { Write-Warning 'adb no pudo instalar la APK. Si la firma es distinta, desinstala la anterior (se borran sus datos locales).' }
    }
}

Write-Output ''
Write-Output 'Listo.'
Write-Output "  APK:          $destino"
Write-Output "  SHA-256:      $hash"
Write-Output "  Backend host: $BackendHost (puertos 8081 y 8082)"
Write-Output '  El telefono debe estar en la misma red WiFi que este computador.'
