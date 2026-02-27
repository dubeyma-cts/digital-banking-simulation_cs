# Flyway migrations for `ibn-db`

## Files
- `V1__init_schema.sql`: Initial schema migration converted for Azure SQL.
- `V2__seed_reference_data.sql`: Idempotent reference/master data migration.
- `V3__bootstrap_app_data.sql`: One-time bootstrap data migration, including initial seeded users.
- `RUNBOOK_FIRST_PIPELINE_EXECUTION.md`: One-page operational runbook for first pipeline execution and rollback checks.

## Important before production
1. Review whether `DROP TABLE IF EXISTS ...` statements in `V1` match your rollout policy for first deployment.
2. Keep `V2` idempotent (`MERGE`, `IF NOT EXISTS`).
3. Keep `V3` limited to bootstrap data only.
4. Keep all subsequent changes in new versioned migrations (`V4`, `V5`, ...), never by editing applied files.

## Module ownership
- Migrations are owned by Maven submodule `ibn-db`.
- Execute Flyway goals against `ibn-db` (not `ibn-core-svc`).

## Suggested prod pipeline order
1. `flyway validate`
2. manual approval
3. `flyway migrate`
4. smoke tests

## One-command Azure DevOps setup
Automate pipeline + variables + environment creation:

`powershell -ExecutionPolicy Bypass -File .\ibn-db\scripts\setup-ibn-db-pipeline.ps1 -OrganizationUrl https://dev.azure.com/<org> -Project <project> -Repository <repo> -ProdSqlServer <prod-sql-server> -ProdSqlDatabase <prod-db> -ProdSqlUser <prod-user> -ProdSqlPassword <prod-password> -QueueRun`

Repository notes:
- Azure Repos (default): `-Repository <azure-repos-name>`
- GitHub: `-Repository <github-owner>/<github-repo> -RepositoryType GitHub`

Optional post-run DB checks:

`... -RunSqlChecks`

## Azure DevOps pipeline
- Pipeline file: `azure-pipelines-ibn-db-prod.yml`
- Flow: `ValidateProd` -> `MigrateProd`.
- `MigrateProd` is deployment-gated by Azure DevOps Environment approval (`PROD_APPROVAL_ENVIRONMENT`).

## GitHub Actions pipeline (Azure DevOps alternative)
- Workflow file: `.github/workflows/ibn-db-prod.yml`
- Flow: `validate-prod` -> `migrate-prod` -> `verify-prod`
- `migrate-prod` uses GitHub Environment `ibn-db-prod` for manual approval gating.
- `verify-prod` runs SQL checks after migration and fails the workflow if rollback criteria are violated.

### GitHub setup (one-time)
1. In GitHub repo settings, create secrets:
	- `PROD_SQL_SERVER`
	- `PROD_SQL_DATABASE`
	- `PROD_AZURE_SQL_USER`
	- `PROD_AZURE_SQL_PASSWORD`
2. Create environment `ibn-db-prod`.
3. Add required reviewers to environment `ibn-db-prod` to enforce manual approval.
4. Push to `main` (or run workflow manually) to execute validate -> approval -> migrate.

### Bare minimum end-to-end setup
Use only these pipeline variables/secrets:
- `PROD_SQL_SERVER`, `PROD_SQL_DATABASE`
- `PROD_AZURE_SQL_USER` (secret), `PROD_AZURE_SQL_PASSWORD` (secret)
- `PROD_APPROVAL_ENVIRONMENT` (default `ibn-db-prod`)

Execution behavior:
- Main branch: validate prod, then approval-gated prod migrate.

For prod pipeline variables:
- `PROD_SQL_SERVER`, `PROD_SQL_DATABASE`
- `PROD_AZURE_SQL_USER` (secret), `PROD_AZURE_SQL_PASSWORD` (secret)
- `PROD_APPROVAL_ENVIRONMENT` (default `ibn-db-prod`)

For manual approvals, configure checks/approvals on the Azure DevOps Environment referenced by `PROD_APPROVAL_ENVIRONMENT`.
