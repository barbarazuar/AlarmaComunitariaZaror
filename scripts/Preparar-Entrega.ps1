param(
    [string]$BackendHost = '10.0.2.2',
    [string]$AndroidSdk = $env:ANDROID_HOME,
    [string]$JavaHome = $env:JAVA_HOME
)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) { $JavaHome = 'C:\Program Files\Android\Android Studio\jbr' }
if (-not $AndroidSdk) { throw 'Define ANDROID_HOME o indica -AndroidSdk.' }
$env:JAVA_HOME = $JavaHome
$raiz = Split-Path -Parent $PSScriptRoot
$salida = Join-Path $raiz 'dist/entrega'
[void](New-Item -ItemType Directory -Path $salida -Force)
$herramientas = Get-ChildItem (Join-Path $AndroidSdk 'build-tools') -Directory |
    Where-Object { $_.Name -match '^\d+\.\d+\.\d+$' } |
    Sort-Object { [version]$_.Name } -Descending | Select-Object -First 1
if (-not $herramientas) { throw 'Instala Android SDK Build Tools.' }
$apksigner = Join-Path $herramientas.FullName 'apksigner.bat'
Push-Location $raiz
try {
    & .\gradlew.bat :app:verificarCoberturaDebug :app:lintDebug :app:assembleRelease "-PbackendHost=$BackendHost" --no-configuration-cache --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Fallo la verificacion o compilacion Android.' }
    Push-Location backend
    try {
        & .\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Fallo la verificacion del backend.' }
    } finally { Pop-Location }
    $apk = Join-Path $raiz 'app/build/outputs/apk/release/app-release.apk'
    $verificacion = & $apksigner verify --verbose --print-certs $apk 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'Firma APK no valida.' }
    $verificacion | Set-Content -Encoding UTF8 (Join-Path $salida 'firma-apk.txt')
    Copy-Item -LiteralPath $apk -Destination (Join-Path $salida 'AlarmaComunitariaZaror-release.apk') -Force
    $hash = (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant()
    "$hash  AlarmaComunitariaZaror-release.apk" | Set-Content -Encoding ascii (Join-Path $salida 'SHA256SUMS.txt')
    $commit = (& git rev-parse HEAD).Trim()
    @("Commit: $commit", "Backend host: $BackendHost", "Generado: $([DateTimeOffset]::Now.ToString('o'))", 'Instalacion y prueba manual en dispositivo: pendiente de registrar.') |
        Set-Content -Encoding UTF8 (Join-Path $salida 'compilacion.txt')
    foreach ($modulo in @('app', 'backend/ms-alertas', 'backend/ms-usuarios')) {
        $destino = Join-Path $salida ($modulo.Replace('/', '-'))
        [void](New-Item -ItemType Directory -Path $destino -Force)
        Copy-Item -LiteralPath (Join-Path $raiz "$modulo/build/reports") -Destination $destino -Recurse -Force
    }
    Write-Output "Entrega verificada en $salida"
} finally { Pop-Location }
