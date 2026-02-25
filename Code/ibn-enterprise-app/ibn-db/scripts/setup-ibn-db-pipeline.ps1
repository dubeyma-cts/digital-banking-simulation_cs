param(
    [Parameter(Mandatory = $true)]
    [string]$OrganizationUrl,

    [Parameter(Mandatory = $true)]
    [string]$Project,

    [Parameter(Mandatory = $true)]
    [string]$Repository,

    [ValidateSet("AzureRepos", "GitHub")]
    [string]$RepositoryType = "AzureRepos",

    [string]$PipelineName = "ibn-db-prod",
    [string]$YamlPath = "ibn-db/azure-pipelines-ibn-db-prod.yml",
    [string]$Branch = "main",

    [Parameter(Mandatory = $true)]
    [string]$ProdSqlServer,

    [Parameter(Mandatory = $true)]
    [string]$ProdSqlDatabase,

    [Parameter(Mandatory = $true)]
    [string]$ProdSqlUser,

    [Parameter(Mandatory = $true)]
    [string]$ProdSqlPassword,

    [string]$ProdApprovalEnvironment = "ibn-db-prod",
    [switch]$QueueRun,
    [switch]$RunSqlChecks
)

$ErrorActionPreference = "Stop"

function Require-Command {
    param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command '$Name' not found. Install it and retry."
    }
}

function Set-PipelineVariable {
    param(
        [string]$PipelineId,
        [string]$Name,
        [string]$Value,
        [bool]$Secret
    )

    $existing = az pipelines variable list --pipeline-id $PipelineId | ConvertFrom-Json
    $match = $existing.PSObject.Properties.Name -contains $Name

    if ($match) {
        if ($Secret) {
            az pipelines variable update --pipeline-id $PipelineId --name $Name --secret true --value $Value | Out-Null
        } else {
            az pipelines variable update --pipeline-id $PipelineId --name $Name --value $Value | Out-Null
        }
        Write-Host "Updated variable: $Name"
    } else {
        if ($Secret) {
            az pipelines variable create --pipeline-id $PipelineId --name $Name --secret true --value $Value | Out-Null
        } else {
            az pipelines variable create --pipeline-id $PipelineId --name $Name --value $Value | Out-Null
        }
        Write-Host "Created variable: $Name"
    }
}

function Invoke-FlywayCheckQueries {
    param(
        [string]$SqlServer,
        [string]$Database,
        [string]$User,
        [string]$Password,
        [string]$Label
    )

    $base = "tcp:$SqlServer.database.windows.net,1433"

    Write-Host "Running validation queries for $Label ($SqlServer/$Database)..."

    sqlcmd -S $base -d $Database -U $User -P $Password -Q "SELECT installed_rank, version, description, type, success, installed_on FROM INB.flyway_schema_history ORDER BY installed_rank;"

    sqlcmd -S $base -d $Database -U $User -P $Password -Q "SELECT COUNT(*) AS role_count FROM INB.[ROLE]; SELECT COUNT(*) AS user_count FROM INB.[USER] WHERE username IN ('admin','banker001'); SELECT COUNT(*) AS user_role_count FROM INB.USER_ROLE ur JOIN INB.[USER] u ON ur.user_id=u.user_id WHERE u.username IN ('admin','banker001');"
}

Require-Command -Name "az"

az extension add --name azure-devops --upgrade | Out-Null
az devops configure --defaults organization=$OrganizationUrl project=$Project | Out-Null

$pipelineId = az pipelines list --query "[?name=='$PipelineName'].id | [0]" -o tsv

if ([string]::IsNullOrWhiteSpace($pipelineId)) {
    Write-Host "Creating pipeline '$PipelineName' from '$YamlPath'..."
    $repoTypeArg = if ($RepositoryType -eq "GitHub") { "github" } else { "tfsgit" }
    $created = az pipelines create --name $PipelineName --repository $Repository --repository-type $repoTypeArg --branch $Branch --yml-path $YamlPath --skip-first-run true | ConvertFrom-Json
    $pipelineId = [string]$created.id
} else {
    Write-Host "Pipeline '$PipelineName' already exists (id=$pipelineId)."
}

Set-PipelineVariable -PipelineId $pipelineId -Name "PROD_SQL_SERVER" -Value $ProdSqlServer -Secret $false
Set-PipelineVariable -PipelineId $pipelineId -Name "PROD_SQL_DATABASE" -Value $ProdSqlDatabase -Secret $false
Set-PipelineVariable -PipelineId $pipelineId -Name "PROD_AZURE_SQL_USER" -Value $ProdSqlUser -Secret $true
Set-PipelineVariable -PipelineId $pipelineId -Name "PROD_AZURE_SQL_PASSWORD" -Value $ProdSqlPassword -Secret $true
Set-PipelineVariable -PipelineId $pipelineId -Name "PROD_APPROVAL_ENVIRONMENT" -Value $ProdApprovalEnvironment -Secret $false

$envId = az pipelines environment list --query "[?name=='$ProdApprovalEnvironment'].id | [0]" -o tsv
if ([string]::IsNullOrWhiteSpace($envId)) {
    az pipelines environment create --name $ProdApprovalEnvironment | Out-Null
    Write-Host "Created environment: $ProdApprovalEnvironment"
} else {
    Write-Host "Environment '$ProdApprovalEnvironment' already exists."
}

Write-Host "IMPORTANT: Configure manual approval check on environment '$ProdApprovalEnvironment' in Azure DevOps UI if not already set."

if ($QueueRun) {
    $run = az pipelines run --name $PipelineName --branch $Branch | ConvertFrom-Json
    Write-Host "Queued run id: $($run.id)"

    $runUrl = "$OrganizationUrl/$Project/_build/results?buildId=$($run.id)&view=results"
    Write-Host "Run URL: $runUrl"
    Write-Host "Approve prod stage manually when the deployment gate appears."
}

if ($RunSqlChecks) {
    Require-Command -Name "sqlcmd"

    Invoke-FlywayCheckQueries -SqlServer $ProdSqlServer -Database $ProdSqlDatabase -User $ProdSqlUser -Password $ProdSqlPassword -Label "PROD"
}

Write-Host "Setup complete."
