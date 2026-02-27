param(
    [string]$ResourceGroup = "inb",
    [string]$SqlServerName = "inb-sqlsrv-001",
    [string]$SqlDatabase = "inbdb",
    [string]$SqlUser = "sqladmininb",
    [string]$FlywaySchema = "INB",
    [switch]$SkipValidate,
    [switch]$SkipMigrate,
    [string]$ProjectRoot = "."
)

$ErrorActionPreference = "Stop"

function Require-Command {
    param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command '$Name' not found."
    }
}

Require-Command -Name "az"
Require-Command -Name "mvn"

$securePassword = Read-Host "Enter SQL password" -AsSecureString
$ptr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
$plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($ptr)

$ruleName = "temp-local-flyway-$((Get-Date).ToString('yyyyMMddHHmmss'))"
$publicIp = (Invoke-RestMethod -Uri "https://api.ipify.org?format=text").Trim()
$locationPushed = $false
$firewallCreated = $false
$publicAccessChanged = $false

Write-Host "Detected public IP: $publicIp"
Write-Host "Using SQL Server: $SqlServerName"
Write-Host "Using Database: $SqlDatabase"

$originalPna = az sql server show `
    --resource-group $ResourceGroup `
    --name $SqlServerName `
    --query "publicNetworkAccess" -o tsv

if ([string]::IsNullOrWhiteSpace($originalPna)) {
    throw "Unable to read current publicNetworkAccess for SQL server '$SqlServerName'."
}

Write-Host "Original publicNetworkAccess: $originalPna"

$flywayUrl = "jdbc:sqlserver://$SqlServerName.database.windows.net:1433;databaseName=$SqlDatabase;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"

try {
    Write-Host "Enabling public network access temporarily..."
    az sql server update `
        --resource-group $ResourceGroup `
        --name $SqlServerName `
        --set publicNetworkAccess=Enabled | Out-Null
    $publicAccessChanged = $true

    Write-Host "Adding temporary firewall rule '$ruleName' for IP $publicIp..."
    az sql server firewall-rule create `
        --resource-group $ResourceGroup `
        --server $SqlServerName `
        --name $ruleName `
        --start-ip-address $publicIp `
        --end-ip-address $publicIp | Out-Null
    $firewallCreated = $true

    Push-Location $ProjectRoot
    $locationPushed = $true

    if (-not $SkipValidate) {
        Write-Host "Running Flyway validate..."
        mvn -pl ibn-db `
            "-Dflyway.url=$flywayUrl" `
            "-Dflyway.user=$SqlUser" `
            "-Dflyway.password=$plainPassword" `
            "-Dflyway.schemas=$FlywaySchema" `
            "-Dflyway.defaultSchema=$FlywaySchema" `
            flyway:validate

        if ($LASTEXITCODE -ne 0) {
            throw "Flyway validate failed with exit code $LASTEXITCODE."
        }
    }

    if (-not $SkipMigrate) {
        Write-Host "Running Flyway migrate..."
        mvn -pl ibn-db `
            "-Dflyway.url=$flywayUrl" `
            "-Dflyway.user=$SqlUser" `
            "-Dflyway.password=$plainPassword" `
            "-Dflyway.schemas=$FlywaySchema" `
            "-Dflyway.defaultSchema=$FlywaySchema" `
            flyway:migrate

        if ($LASTEXITCODE -ne 0) {
            throw "Flyway migrate failed with exit code $LASTEXITCODE."
        }
    }

    Write-Host "Flyway execution completed successfully."
}
finally {
    Write-Host "Cleaning up temporary firewall rule and restoring SQL network setting..."

    if ($firewallCreated) {
        az sql server firewall-rule delete `
            --resource-group $ResourceGroup `
            --server $SqlServerName `
            --name $ruleName | Out-Null
    }

    if ($publicAccessChanged) {
        az sql server update `
            --resource-group $ResourceGroup `
            --name $SqlServerName `
            --set publicNetworkAccess=$originalPna | Out-Null
    }

    [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    $plainPassword = $null

    if ($locationPushed) {
        Pop-Location
    }

    Write-Host "Cleanup complete. Restored publicNetworkAccess to '$originalPna'."
}
