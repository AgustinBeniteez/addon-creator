# Script to create Windows Installer for Addon Creator

# 1. Build the project
Write-Host "Building project with Maven..."
mvn package

if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build failed!" -ForegroundColor Red
    exit 1
}

# 2. Define variables
$APP_NAME = "AddonCreator"
$APP_VERSION = "1.0.1"
$INPUT_DIR = "target"
$MAIN_JAR = "addon-creator-1.0.1.jar"
$MAIN_CLASS = "com.agustinbenitez.addoncreator.Launcher"
$OUTPUT_DIR = "installer"

# 2.5 Copy templates to target directory (so they are included in the installer)
$TEMPLATES_SRC = "templates"
$TEMPLATES_DEST = Join-Path $INPUT_DIR "plantillas"
if (Test-Path $TEMPLATES_SRC) {
    Write-Host "Copying templates to build directory..."
    if (Test-Path $TEMPLATES_DEST) {
        Remove-Item -Path $TEMPLATES_DEST -Recurse -Force
    }
    Copy-Item -Path $TEMPLATES_SRC -Destination $TEMPLATES_DEST -Recurse -Force
}

# 3. Create installer using jpackage
Write-Host "Creating installer with jpackage..."

# Add WiX to PATH
$WIX_PATH = Join-Path $PWD "tools\wix"
$env:PATH = "$WIX_PATH;" + $env:PATH
Write-Host "Added WiX to PATH: $WIX_PATH"

# Create output directory if it doesn't exist
if (!(Test-Path -Path $OUTPUT_DIR)) {
    New-Item -ItemType Directory -Path $OUTPUT_DIR | Out-Null
}

# JPackage command
# Note: --icon requires an .ico file on Windows.
jpackage `
  --type exe `
  --input $INPUT_DIR `
  --name $APP_NAME `
  --app-version $APP_VERSION `
  --main-jar $MAIN_JAR `
  --main-class $MAIN_CLASS `
  --dest $OUTPUT_DIR `
  --icon "addoncreator.ico" `
  --resource-dir "installer_resources" `
  --win-upgrade-uuid "D4771504-2070-4376-8094-118831932305" `
  --win-dir-chooser `
  --win-menu `
  --win-shortcut `
  --verbose

if ($LASTEXITCODE -eq 0) {
    Write-Host "Installer created successfully in $OUTPUT_DIR" -ForegroundColor Green
} else {
    Write-Host "jpackage failed!" -ForegroundColor Red
    exit 1
}
