param(
    [Parameter(Mandatory = $true)]
    [string]$ResourceGroup,

    [string]$TemplateFile = "paas_deployment.json",

    [string]$ParametersFile = "paas_deployment.parameters.json",

    [string]$CoreAppName,

    [string]$UiAppName,

    [switch]$SkipBuild,

    [switch]$SkipValidate
)

$ErrorActionPreference = "Stop"
$PSNativeCommandUseErrorActionPreference = $false

function Invoke-Az {
    param(
        [Parameter(Mandatory = $true)]
        [string]$AzPath,
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $stdoutFile = [System.IO.Path]::GetTempFileName()
    $stderrFile = [System.IO.Path]::GetTempFileName()

    try {
        $proc = Start-Process -FilePath $AzPath -ArgumentList $Arguments -NoNewWindow -Wait -PassThru -RedirectStandardOutput $stdoutFile -RedirectStandardError $stderrFile
        $stdout = if (Test-Path $stdoutFile) { Get-Content $stdoutFile -Raw } else { "" }
        $stderr = if (Test-Path $stderrFile) { Get-Content $stderrFile -Raw } else { "" }
        $outputText = ($stdout + [Environment]::NewLine + $stderr).Trim()

        if ($proc.ExitCode -ne 0) {
            if ($outputText -match "SubscriptionIsOverQuotaForSku") {
                throw "Azure quota check failed (SubscriptionIsOverQuotaForSku). App Service plan quota is insufficient for the selected SKU in this subscription/location. Request quota increase or use a subscription with available quota."
            }

            if ($outputText -match "RequestDisallowedByPolicy") {
                throw "Azure Policy blocked deployment (RequestDisallowedByPolicy). Update template parameters/SKUs to comply with assigned policy definitions."
            }

            throw "Azure CLI command failed: az $($Arguments -join ' ')`n$outputText"
        }

        if (-not [string]::IsNullOrWhiteSpace($outputText)) {
            Write-Output $outputText
        }
    }
    finally {
        if (Test-Path $stdoutFile) { Remove-Item $stdoutFile -Force -ErrorAction SilentlyContinue }
        if (Test-Path $stderrFile) { Remove-Item $stderrFile -Force -ErrorAction SilentlyContinue }
    }
}

function Get-AzCliCommand {
    $azFromPath = Get-Command az -ErrorAction SilentlyContinue
    if ($azFromPath) {
        return $azFromPath.Source
    }

    $defaultAzCmd = "C:\Program Files\Microsoft SDKs\Azure\CLI2\wbin\az.cmd"
    if (Test-Path $defaultAzCmd) {
        return $defaultAzCmd
    }

    throw "Azure CLI not found. Install Azure CLI or restart terminal/VS Code so PATH is refreshed."
}

function Get-ParamValue {
    param(
        [Parameter(Mandatory = $true)]
        [psobject]$Json,
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    $node = $Json.parameters.$Name
    if (-not $node) {
        return $null
    }

    return $node.value
}

if (-not (Test-Path $TemplateFile)) {
    throw "Template file not found: $TemplateFile"
}

if (-not (Test-Path $ParametersFile)) {
    throw "Parameters file not found: $ParametersFile"
}

$paramJson = Get-Content $ParametersFile -Raw | ConvertFrom-Json

if (-not $CoreAppName) {
    $CoreAppName = Get-ParamValue -Json $paramJson -Name "coreAppServiceName"
}

if (-not $UiAppName) {
    $UiAppName = Get-ParamValue -Json $paramJson -Name "uiAppServiceName"
}

if (-not $CoreAppName -or -not $UiAppName) {
    throw "core/ui app names are required. Provide -CoreAppName and -UiAppName or set coreAppServiceName/uiAppServiceName in parameters file."
}

$az = Get-AzCliCommand

Write-Host "Checking Azure CLI and login..." -ForegroundColor Cyan
Invoke-Az -AzPath $az -Arguments @("version") | Out-Null
$account = $null
try {
    $account = & $az account show --output json 2>$null
    if ($LASTEXITCODE -ne 0) {
        $account = $null
    }
} catch {
    $account = $null
}

if (-not $account) {
    Write-Host "No active login found. Running az login..." -ForegroundColor Yellow
    Invoke-Az -AzPath $az -Arguments @("login", "--use-device-code") | Out-Null
}

if (-not $SkipValidate) {
    Write-Host "Running preflight ARM validation (quota/policy checks)..." -ForegroundColor Cyan
    Invoke-Az -AzPath $az -Arguments @(
        "deployment", "group", "validate",
        "--resource-group", $ResourceGroup,
        "--template-file", $TemplateFile,
        "--parameters", "@$ParametersFile",
        "--output", "table"
    )
}

if (-not $SkipBuild) {
    Write-Host "Building core artifact..." -ForegroundColor Cyan
    mvn --% -pl ibn-core-svc -DskipTests clean package

    Write-Host "Building UI artifact..." -ForegroundColor Cyan
    mvn --% -f ibn-ui/pom.xml -DskipTests clean package
}

Write-Host "Deploying infrastructure..." -ForegroundColor Cyan
Invoke-Az -AzPath $az -Arguments @(
    "deployment", "group", "create",
    "--resource-group", $ResourceGroup,
    "--template-file", $TemplateFile,
    "--parameters", "@$ParametersFile",
    "--output", "table"
)

$coreJar = "ibn-core-svc/target/ibn-core-svc-1.0-SNAPSHOT.jar"
$coreJarDeployName = "ibn-core-svc/target/app.jar"
$coreZip = "ibn-core-svc/target/core.zip"

if (-not (Test-Path $coreJar)) {
    throw "Core artifact not found: $coreJar"
}

Copy-Item $coreJar $coreJarDeployName -Force
Compress-Archive -Path $coreJarDeployName -DestinationPath $coreZip -Force

Write-Host "Deploying core artifact to $CoreAppName..." -ForegroundColor Cyan
Invoke-Az -AzPath $az -Arguments @(
        "webapp", "deploy",
        "--resource-group", $ResourceGroup,
        "--name", $CoreAppName,
        "--src-path", $coreZip,
        "--type", "zip"
)

$uiWar = "ibn-ui/target/ibn-ui-1.0-SNAPSHOT.war"
$uiWarDeployName = "ibn-ui/target/app.war"
$uiZip = "ibn-ui/target/ui.zip"

if (-not (Test-Path $uiWar)) {
    throw "UI artifact not found: $uiWar"
}

Copy-Item $uiWar $uiWarDeployName -Force
Compress-Archive -Path $uiWarDeployName -DestinationPath $uiZip -Force

Write-Host "Deploying UI artifact to $UiAppName..." -ForegroundColor Cyan
Invoke-Az -AzPath $az -Arguments @(
        "webapp", "deploy",
        "--resource-group", $ResourceGroup,
        "--name", $UiAppName,
        "--src-path", $uiZip,
        "--type", "zip"
)

Write-Host "Deployment completed." -ForegroundColor Green
Write-Host "Core URL: https://$CoreAppName.azurewebsites.net" -ForegroundColor Green
Write-Host "UI URL:   https://$UiAppName.azurewebsites.net" -ForegroundColor Green
