param(
    [string]$ResourceGroup,

    [string]$SubscriptionParametersFile = "paas_deployment.subscription.parameters.json",

    [string]$CoreAppName,

    [string]$UiAppName,

    [switch]$SkipBuild
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

function Resolve-LatestArtifact {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Folder,
        [Parameter(Mandatory = $true)]
        [string]$Pattern,
        [string[]]$ExcludePatterns = @("*.original")
    )

    if (-not (Test-Path $Folder)) {
        throw "Artifact folder not found: $Folder"
    }

    $files = Get-ChildItem -Path $Folder -File -Filter $Pattern | Sort-Object LastWriteTime -Descending
    foreach ($exclude in $ExcludePatterns) {
        $files = $files | Where-Object { $_.Name -notlike $exclude }
    }

    $artifact = $files | Select-Object -First 1
    if (-not $artifact) {
        throw "No artifact found in $Folder matching $Pattern"
    }

    return $artifact.FullName
}

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = (Resolve-Path (Join-Path $scriptRoot "..\..")).Path
$paramPath = if ([System.IO.Path]::IsPathRooted($SubscriptionParametersFile)) { $SubscriptionParametersFile } else { Join-Path $scriptRoot $SubscriptionParametersFile }

if (-not (Test-Path $paramPath)) {
    throw "Parameters file not found: $paramPath"
}

$paramJson = Get-Content $paramPath -Raw | ConvertFrom-Json

if (-not $ResourceGroup) {
    $ResourceGroup = Get-ParamValue -Json $paramJson -Name "resourceGroupName"
}

if (-not $CoreAppName) {
    $CoreAppName = Get-ParamValue -Json $paramJson -Name "coreAppServiceName"
}

if (-not $UiAppName) {
    $UiAppName = Get-ParamValue -Json $paramJson -Name "uiAppServiceName"
}

$sqlServerName = Get-ParamValue -Json $paramJson -Name "sqlServerName"
$sqlDatabaseName = Get-ParamValue -Json $paramJson -Name "sqlDatabaseName"
$sqlAdminLogin = Get-ParamValue -Json $paramJson -Name "sqlAdminLogin"
$sqlAdminPassword = Get-ParamValue -Json $paramJson -Name "sqlAdminPassword"
$storageAccountName = Get-ParamValue -Json $paramJson -Name "storageAccountName"

if (-not $ResourceGroup -or -not $CoreAppName -or -not $UiAppName) {
    throw "ResourceGroup/CoreAppName/UiAppName are required. Provide parameters or set resourceGroupName/coreAppServiceName/uiAppServiceName in subscription parameters file."
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
}
catch {
    $account = $null
}

if (-not $account) {
    Write-Host "No active login found. Running az login..." -ForegroundColor Yellow
    Invoke-Az -AzPath $az -Arguments @("login", "--use-device-code") | Out-Null
}

Write-Host "Resource group: $ResourceGroup" -ForegroundColor DarkCyan
Write-Host "Core app:        $CoreAppName" -ForegroundColor DarkCyan
Write-Host "UI app:          $UiAppName" -ForegroundColor DarkCyan

Write-Host "Checking target App Services..." -ForegroundColor Cyan
Invoke-Az -AzPath $az -Arguments @("webapp", "show", "--resource-group", $ResourceGroup, "--name", $CoreAppName, "--output", "none")
Invoke-Az -AzPath $az -Arguments @("webapp", "show", "--resource-group", $ResourceGroup, "--name", $UiAppName, "--output", "none")

if ($sqlServerName -and $sqlDatabaseName -and $sqlAdminLogin -and $sqlAdminPassword) {
    $jdbcUrl = "jdbc:sqlserver://$sqlServerName.database.windows.net:1433;database=$sqlDatabaseName;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"

    Write-Host "Configuring core app SQL settings for Azure SQL private endpoint access..." -ForegroundColor Cyan
    Invoke-Az -AzPath $az -Arguments @(
        "webapp", "config", "appsettings", "set",
        "--resource-group", $ResourceGroup,
        "--name", $CoreAppName,
        "--settings",
        "SPRING_PROFILES_ACTIVE=prod",
        "SPRING_DATASOURCE_URL=$jdbcUrl",
        "SPRING_DATASOURCE_USERNAME=$sqlAdminLogin",
        "SPRING_DATASOURCE_PASSWORD=$sqlAdminPassword",
        "SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.SQLServer2012Dialect",
        "SPRING_JPA_HIBERNATE_DDL_AUTO=none",
        "H2_CONSOLE_ENABLED=false"
    )
}
else {
    Write-Host "SQL settings not fully present in parameters; skipping core SQL appsettings update." -ForegroundColor Yellow
}

if ($storageAccountName) {
    Write-Host "Configuring core app blob storage settings..." -ForegroundColor Cyan

    $storageKey = (Invoke-Az -AzPath $az -Arguments @(
        "storage", "account", "keys", "list",
        "--resource-group", $ResourceGroup,
        "--account-name", $storageAccountName,
        "--query", "[0].value",
        "-o", "tsv"
    )).Trim()

    if (-not $storageKey) {
        throw "Unable to retrieve storage account key for '$storageAccountName'."
    }

    $storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=$storageAccountName;AccountKey=$storageKey;EndpointSuffix=core.windows.net"

    Invoke-Az -AzPath $az -Arguments @(
        "webapp", "config", "appsettings", "set",
        "--resource-group", $ResourceGroup,
        "--name", $CoreAppName,
        "--settings",
        "APP_STORAGE_AZURE_CONNECTION_STRING=$storageConnectionString",
        "APP_STORAGE_AZURE_CONTAINER_NAME=cheque-attachments"
    )
}
else {
    Write-Host "Storage account not present in parameters; skipping blob appsettings update." -ForegroundColor Yellow
}

if (-not $SkipBuild) {
    Write-Host "Building core artifact..." -ForegroundColor Cyan
    Push-Location $repoRoot
    try {
        $originalNodeOptions = $env:NODE_OPTIONS
        if ($env:NODE_OPTIONS) {
            Write-Host "Temporarily clearing NODE_OPTIONS for UI Maven/npm compatibility..." -ForegroundColor DarkYellow
            Remove-Item Env:NODE_OPTIONS -ErrorAction SilentlyContinue
        }

        mvn --% -pl ibn-core-svc -DskipTests clean package

        Write-Host "Building UI artifact..." -ForegroundColor Cyan
        mvn --% -f ibn-ui/pom.xml -DskipTests clean package

        if ($null -ne $originalNodeOptions) {
            $env:NODE_OPTIONS = $originalNodeOptions
        }
    }
    finally {
        if ($null -ne $originalNodeOptions) {
            $env:NODE_OPTIONS = $originalNodeOptions
        }
        Pop-Location
    }
}

$coreTarget = Join-Path $scriptRoot "ibn-core-svc\target"
$uiTarget = Join-Path $scriptRoot "ibn-ui\target"
$uiDist = Join-Path $scriptRoot "ibn-ui\dist\ibn-ui"

$coreTarget = Join-Path $repoRoot "ibn-core-svc\target"
$uiTarget = Join-Path $repoRoot "ibn-ui\target"
$uiDist = Join-Path $repoRoot "ibn-ui\dist\ibn-ui"

$coreArtifact = Resolve-LatestArtifact -Folder $coreTarget -Pattern "*.jar"

$coreZip = Join-Path $coreTarget "core-deploy.zip"
$uiZip = Join-Path $uiTarget "ui-static-deploy.zip"
$coreDeployName = Join-Path $coreTarget "app.jar"

Copy-Item $coreArtifact $coreDeployName -Force

if (-not (Test-Path $uiDist)) {
        throw "UI dist folder not found: $uiDist"
}

$uiWebConfigPath = Join-Path $uiDist "web.config"
$uiWebConfig = @'
<?xml version="1.0" encoding="utf-8"?>
<configuration>
    <system.webServer>
        <rewrite>
            <rules>
                <rule name="AngularRoutes" stopProcessing="true">
                    <match url=".*" />
                    <conditions logicalGrouping="MatchAll">
                        <add input="{REQUEST_FILENAME}" matchType="IsFile" negate="true" />
                        <add input="{REQUEST_FILENAME}" matchType="IsDirectory" negate="true" />
                    </conditions>
                    <action type="Rewrite" url="/" />
                </rule>
            </rules>
        </rewrite>
    </system.webServer>
</configuration>
'@
Set-Content -Path $uiWebConfigPath -Value $uiWebConfig -Encoding UTF8

if (Test-Path $coreZip) { Remove-Item $coreZip -Force }
if (Test-Path $uiZip) { Remove-Item $uiZip -Force }

Compress-Archive -Path $coreDeployName -DestinationPath $coreZip -Force
Compress-Archive -Path (Join-Path $uiDist "*") -DestinationPath $uiZip -Force

Write-Host "Deploying core artifact to $CoreAppName..." -ForegroundColor Cyan
Invoke-Az -AzPath $az -Arguments @(
    "webapp", "deploy",
    "--resource-group", $ResourceGroup,
    "--name", $CoreAppName,
    "--src-path", $coreZip,
    "--type", "zip"
)

Write-Host "Deploying UI artifact to $UiAppName..." -ForegroundColor Cyan
Invoke-Az -AzPath $az -Arguments @(
    "webapp", "deploy",
    "--resource-group", $ResourceGroup,
    "--name", $UiAppName,
    "--src-path", $uiZip,
    "--type", "zip",
    "--target-path", "webapps/ROOT"
)

Write-Host "Application deployment completed." -ForegroundColor Green
Write-Host "Core URL: https://$CoreAppName.azurewebsites.net" -ForegroundColor Green
Write-Host "UI URL:   https://$UiAppName.azurewebsites.net" -ForegroundColor Green
