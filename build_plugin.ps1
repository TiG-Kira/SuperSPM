param(
    [string]$pluginDir = "."
)

Write-Host "====================================="
Write-Host "  SuperSPM Native Plugin Builder"
Write-Host "====================================="
Write-Host ""

$pluginDir = Resolve-Path $pluginDir
Write-Host "Plugin source directory: $pluginDir"

$pluginJson = Get-ChildItem -Path $pluginDir -Filter "plugin.json" -Recurse | Select-Object -First 1
if (-not $pluginJson) {
    Write-Error "ERROR: plugin.json not found in $pluginDir"
    exit 1
}

$jsonContent = Get-Content $pluginJson.FullName -Raw -Encoding UTF8 | ConvertFrom-Json
Write-Host "Plugin Name: $($jsonContent.name)"
Write-Host "Plugin Version: $($jsonContent.version)"
Write-Host "Plugin Type: $($jsonContent.type)"
Write-Host ""

if ($jsonContent.type -ne "NATIVE") {
    Write-Error "ERROR: Only NATIVE type plugins are supported by this script"
    exit 1
}

$projectRoot = Split-Path $pluginDir -Parent
while (-not (Test-Path "$projectRoot\settings.gradle.kts")) {
    $projectRoot = Split-Path $projectRoot -Parent
    if ($projectRoot -eq "") {
        Write-Error "ERROR: Cannot find project root directory"
        exit 1
    }
}

Write-Host "Project root: $projectRoot"

$gradleDir = "$projectRoot\gradle-8.11.1"
$gradlePath = "$gradleDir\bin\gradle.bat"

if (-not (Test-Path $gradlePath)) {
    Write-Error "ERROR: Gradle not found at $gradlePath"
    exit 1
}

$outputDir = "$pluginDir\build"

if (Test-Path $outputDir) {
    Write-Host "Cleaning previous build..."
    Remove-Item -Recurse -Force $outputDir
}

Write-Host "Copying plugin source to plugin-sdk module..."

$sdkSrcDir = "$projectRoot\plugin-sdk\src\main\kotlin\com\kira\superspm\plugin"
$pluginKtFile = Get-ChildItem -Path $pluginDir -Filter "*.kt" -Recurse | Select-Object -First 1

if (-not $pluginKtFile) {
    Write-Error "ERROR: No Kotlin source file found in plugin directory"
    exit 1
}

Copy-Item -Path $pluginKtFile.FullName -Destination "$sdkSrcDir\" -Force
Write-Host "Copied plugin source: $($pluginKtFile.Name)"

Write-Host ""
Write-Host "Building plugin-sdk with Gradle..."

$gradleCmd = "`"$gradlePath`" :plugin-sdk:build"

Write-Host "Running: $gradleCmd"
cmd.exe /c "cd /d `"$projectRoot`" && $gradleCmd"

if ($LASTEXITCODE -ne 0) {
    Write-Error "ERROR: Gradle build failed"
    exit 1
}

Write-Host "Gradle build successful!"
Write-Host ""

$sdkBuildDir = "$projectRoot\plugin-sdk\build\outputs\aar"
$aarFile = Get-ChildItem -Path $sdkBuildDir -Filter "*.aar" | Select-Object -First 1

if (-not $aarFile) {
    Write-Error "ERROR: AAR file not found in $sdkBuildDir"
    exit 1
}

Write-Host "Extracting classes.jar from AAR..."

$tempDir = "$outputDir\temp_extract"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

$zipTempFile = "$tempDir\temp.zip"
Copy-Item -Path $aarFile.FullName -Destination $zipTempFile -Force
Expand-Archive -Path $zipTempFile -DestinationPath $tempDir -Force

$jarFile = "$tempDir\classes.jar"
if (-not (Test-Path $jarFile)) {
    Write-Error "ERROR: classes.jar not found in AAR"
    exit 1
}

Write-Host "Found classes.jar"
Write-Host ""

Write-Host "Converting to dex format..."

$androidHome = $env:ANDROID_HOME
if (-not $androidHome) {
    $androidHome = "C:\Users\Kira\AppData\Local\Android\Sdk"
    Write-Host "Using default ANDROID_HOME: $androidHome"
}

$buildToolsVersion = "35.0.0"
$d8Path = "$androidHome\build-tools\$buildToolsVersion\d8.bat"

if (-not (Test-Path $d8Path)) {
    Write-Error "ERROR: d8.bat not found at $d8Path"
    Write-Host "Please install Android Build Tools $buildToolsVersion"
    exit 1
}

$dexFile = "$outputDir\classes.dex"

$d8Cmd = "`"$d8Path`" `"$jarFile`" --release --min-api 24 --output `"$outputDir`""

Write-Host "Running: $d8Cmd"
cmd.exe /c $d8Cmd

if ($LASTEXITCODE -ne 0) {
    Write-Error "ERROR: D8 conversion failed"
    exit 1
}

if (-not (Test-Path $dexFile)) {
    Write-Error "ERROR: classes.dex not found after D8 conversion"
    exit 1
}

Write-Host "D8 conversion successful!"
Write-Host ""

Write-Host "Packaging plugin..."

$zipFileName = "$($jsonContent.name)_$($jsonContent.version).zip"
$zipFile = "$outputDir\$zipFileName"

$tempZipDir = "$outputDir\temp_zip"
New-Item -ItemType Directory -Path $tempZipDir | Out-Null

Copy-Item -Path $dexFile -Destination "$tempZipDir\classes.dex"
Copy-Item -Path $pluginJson.FullName -Destination "$tempZipDir\plugin.json"

Compress-Archive -Path "$tempZipDir\*" -DestinationPath $zipFile -Force

Remove-Item -Recurse -Force $tempZipDir
Remove-Item -Recurse -Force $tempDir

Write-Host ""
Write-Host "====================================="
Write-Host "  Plugin packaged successfully!"
Write-Host "====================================="
Write-Host "Output file: $zipFile"
$fileSize = (Get-Item $zipFile).Length / 1024
Write-Host "File size: $($fileSize.ToString("F2")) KB"
Write-Host ""
Write-Host "To install this plugin:"
Write-Host "1. Copy this ZIP file to your device"
Write-Host "2. Open SuperSPM app"
Write-Host "3. Go to Settings > Plugins"
Write-Host "4. Tap 'Import Plugin' and select this ZIP file"
Write-Host ""