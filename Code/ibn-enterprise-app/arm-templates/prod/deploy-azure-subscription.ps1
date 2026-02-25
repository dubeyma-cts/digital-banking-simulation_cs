param(
    [string]$Location = "centralus",

    [string]$TemplateFile = "paas_deployment.subscription.json",

    [string]$ParametersFile = "paas_deployment.subscription.parameters.json",

    [string]$AppGatewayDnsLabel = "inb",

    [switch]$SkipValidate,

    [switch]$WhatIf
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
                throw "Azure quota check failed (SubscriptionIsOverQuotaForSku). Quota is insufficient for one or more selected SKUs in this subscription/location."
            }

            if ($outputText -match "RequestDisallowedByPolicy") {
                throw "Azure Policy blocked deployment (RequestDisallowedByPolicy). Update parameters/SKUs/naming to satisfy assigned policies."
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

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$templatePath = if ([System.IO.Path]::IsPathRooted($TemplateFile)) { $TemplateFile } else { Join-Path $scriptRoot $TemplateFile }
$parametersPath = if ([System.IO.Path]::IsPathRooted($ParametersFile)) { $ParametersFile } else { Join-Path $scriptRoot $ParametersFile }

if (-not (Test-Path $templatePath)) {
    throw "Template file not found: $templatePath"
}

if (-not (Test-Path $parametersPath)) {
    throw "Parameters file not found: $parametersPath"
}

$parametersJson = Get-Content $parametersPath -Raw | ConvertFrom-Json
$resourceGroupName = Get-ParamValue -Json $parametersJson -Name "resourceGroupName"
$appGatewayPublicIpName = Get-ParamValue -Json $parametersJson -Name "appGatewayPublicIpName"

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

Write-Host "Using template:   $templatePath" -ForegroundColor DarkCyan
Write-Host "Using parameters: $parametersPath" -ForegroundColor DarkCyan
Write-Host "Deployment location: $Location" -ForegroundColor DarkCyan

if (-not $SkipValidate) {
    Write-Host "Running subscription-scope ARM validation..." -ForegroundColor Cyan
    Invoke-Az -AzPath $az -Arguments @(
        "deployment", "sub", "validate",
        "--location", $Location,
        "--template-file", $templatePath,
        "--parameters", "@$parametersPath",
        "--output", "table"
    )
}

if ($WhatIf) {
    Write-Host "Running what-if preview..." -ForegroundColor Cyan
    Invoke-Az -AzPath $az -Arguments @(
        "deployment", "sub", "what-if",
        "--location", $Location,
        "--template-file", $templatePath,
        "--parameters", "@$parametersPath"
    )
    return
}

Write-Host "Deploying subscription-scope ARM template..." -ForegroundColor Cyan
Invoke-Az -AzPath $az -Arguments @(
    "deployment", "sub", "create",
    "--location", $Location,
    "--template-file", $templatePath,
    "--parameters", "@$parametersPath",
    "--output", "table"
)

Write-Host "Deployment completed." -ForegroundColor Green
Write-Host "Resource group is created/updated by template using parameter resourceGroupName (default: inb)." -ForegroundColor Green

if (-not [string]::IsNullOrWhiteSpace($AppGatewayDnsLabel) -and
    -not [string]::IsNullOrWhiteSpace($resourceGroupName) -and
    -not [string]::IsNullOrWhiteSpace($appGatewayPublicIpName)) {

    Write-Host "Ensuring Application Gateway fixed DNS label '$AppGatewayDnsLabel'..." -ForegroundColor Cyan
    Invoke-Az -AzPath $az -Arguments @(
        "network", "public-ip", "update",
        "--resource-group", $resourceGroupName,
        "--name", $appGatewayPublicIpName,
        "--dns-name", $AppGatewayDnsLabel,
        "--only-show-errors",
        "--output", "none"
    ) | Out-Null

    $appGatewayFqdn = (Invoke-Az -AzPath $az -Arguments @(
        "network", "public-ip", "show",
        "--resource-group", $resourceGroupName,
        "--name", $appGatewayPublicIpName,
        "--query", "dnsSettings.fqdn",
        "-o", "tsv"
    )).Trim()

    if (-not [string]::IsNullOrWhiteSpace($appGatewayFqdn)) {
        Write-Host "Application Gateway URL: http://$appGatewayFqdn`:8080" -ForegroundColor Green
    }
}
