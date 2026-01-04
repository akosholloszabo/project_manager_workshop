param(
    [string]$KeystorePath = "server/src/main/resources/ssl/server-keystore.p12",
    [string]$Alias = "server",
    [string]$StorePassword = "changeit",
    [string]$ExportPath = "server/src/main/resources/ssl/server-cert.cer"
)

$keytoolName = "keytool"
$keytoolPath = (Get-Command $keytoolName -ErrorAction SilentlyContinue)?.Source
if (-not $keytoolPath -and $env:JAVA_HOME) {
    $candidate = Join-Path $env:JAVA_HOME "bin" "$keytoolName.exe"
    if (Test-Path $candidate) {
        $keytoolPath = $candidate
    }
}
if (-not $keytoolPath) {
    $userJdks = Join-Path $env:USERPROFILE ".jdks"
    if (Test-Path $userJdks) {
        $keytoolPath = Get-ChildItem $userJdks -Directory | ForEach-Object {
            Join-Path $_.FullName "bin" "$keytoolName.exe"
        } | Where-Object { Test-Path $_ } | Select-Object -First 1
    }
}
if (-not $keytoolPath) {
    Write-Error "$keytoolName not found on PATH, ensure a JDK/bin directory is accessible."
    return
}

$keytool = $keytoolPath

if (-not (Test-Path $KeystorePath)) {
    Write-Error "Keystore '$KeystorePath' not found. Run from repository root."
    return
}

$keytoolArgs = @(
    "-exportcert",
    "-alias", $Alias,
    "-keystore", $KeystorePath,
    "-storetype", "PKCS12",
    "-storepass", $StorePassword,
    "-file", $ExportPath
)

Write-Host "Exporting server certificate to $ExportPath"
$keytool @keytoolArgs | Write-Host
Write-Host "Done. Run 'certutil -addstore -f Root $ExportPath' from an elevated prompt"
