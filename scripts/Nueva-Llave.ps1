param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) { $JavaHome = 'C:\Program Files\Android\Android Studio\jbr' }
$keytool = Join-Path $JavaHome 'bin/keytool.exe'
if (-not (Test-Path -LiteralPath $keytool)) { throw 'No se encontro keytool. Indica -JavaHome con un JDK completo.' }
$raiz = Split-Path -Parent $PSScriptRoot
$privado = Join-Path $raiz '.private'
$jks = Join-Path $privado 'alarma-zaror-release.jks'
$propiedades = Join-Path $privado 'signing.properties'
if ((Test-Path -LiteralPath $jks) -or (Test-Path -LiteralPath $propiedades)) {
    throw 'Ya existe firma privada. No se reemplaza una llave ni sus credenciales.'
}
[void](New-Item -ItemType Directory -Path $privado -Force)
# Restringir el directorio privado al usuario de Windows antes de guardar secretos.
$cuenta = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
& icacls.exe $privado /inheritance:r /grant:r "${cuenta}:(OI)(CI)F" | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'No se pudieron restringir los permisos del directorio privado.' }
function Nueva-Clave {
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes); return [Convert]::ToBase64String($bytes) }
    finally { $rng.Dispose() }
}
$anteriorStore = $env:ZAROR_STORE_PASSWORD
$anteriorKey = $env:ZAROR_KEY_PASSWORD
try {
    $env:ZAROR_STORE_PASSWORD = Nueva-Clave
    $env:ZAROR_KEY_PASSWORD = Nueva-Clave
    & $keytool -genkeypair -storetype JKS -keystore $jks -alias zaror-release -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=Alarma Comunitaria Zaror, OU=Proyecto Academico, O=Zaror, C=CL' -storepass:env ZAROR_STORE_PASSWORD -keypass:env ZAROR_KEY_PASSWORD
    if ($LASTEXITCODE -ne 0) { throw 'Fallo keytool. Revisa el archivo parcial antes de repetir.' }
    $lineas = @(
        'storeFile=.private/alarma-zaror-release.jks',
        "storePassword=$env:ZAROR_STORE_PASSWORD",
        'keyAlias=zaror-release',
        "keyPassword=$env:ZAROR_KEY_PASSWORD"
    )
    [IO.File]::WriteAllLines($propiedades, $lineas, [Text.Encoding]::ASCII)
    Write-Output 'Llave creada en .private/alarma-zaror-release.jks. Credenciales en .private/signing.properties.'
    Write-Output 'Conserva un respaldo privado de ambos archivos. No los subas a Git ni al chat.'
} finally {
    $env:ZAROR_STORE_PASSWORD = $anteriorStore
    $env:ZAROR_KEY_PASSWORD = $anteriorKey
}
