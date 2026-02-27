param(
    [Parameter(Mandatory = $true)]
    [string]$ResourceGroup,

    [string]$TemplateFile = "paas_deployment.json",

    [string]$ParametersFile = "paas_deployment.parameters.json"
)

$ErrorActionPreference = "Stop"

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

$az = Get-AzCliCommand

Write-Host "Checking Azure CLI..." -ForegroundColor Cyan
& $az version | Out-Null

Write-Host "Checking Azure login..." -ForegroundColor Cyan
$account = & $az account show --output json 2>$null
if (-not $account) {
    Write-Host "You are not logged in. Launching az login..." -ForegroundColor Yellow
    & $az login | Out-Null
}

Write-Host "Validating template against resource group '$ResourceGroup'..." -ForegroundColor Cyan
& $az deployment group validate `
  --resource-group $ResourceGroup `
  --template-file $TemplateFile `
  --parameters "@$ParametersFile" `
  --output table

if ($LASTEXITCODE -eq 0) {
    Write-Host "Validation succeeded." -ForegroundColor Green
} else {
    Write-Host "Validation failed." -ForegroundColor Red
    exit $LASTEXITCODE
}
