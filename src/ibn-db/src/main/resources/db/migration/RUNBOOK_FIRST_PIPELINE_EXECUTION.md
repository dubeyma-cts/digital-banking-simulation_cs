# Runbook: First `ibn-db` Pipeline Execution + Rollback Checks

## Scope
This runbook covers the first execution of `azure-pipelines-ibn-db-prod.yml` and the minimum rollback checks for prod.

## 1) Preconditions (must be true)
- Pipeline variables are configured:
  - `PROD_SQL_SERVER`, `PROD_SQL_DATABASE`
  - `PROD_AZURE_SQL_USER` (secret), `PROD_AZURE_SQL_PASSWORD` (secret)
  - `PROD_APPROVAL_ENVIRONMENT` (default `ibn-db-prod`)
- Azure DevOps Environment `ibn-db-prod` exists with manual approval check enabled.
- Database principals have DDL/DML rights needed by Flyway migrations.
- Target DBs are reachable from Azure DevOps hosted agents.

## 2) First run sequence
1. Commit is merged to `main` with migration files under `ibn-db/src/main/resources/db/migration`.
2. Queue `azure-pipelines-ibn-db-prod.yml`.
3. Verify stage order:
   - `ValidateProd` passes
   - `MigrateProd` pauses for approval
4. Approver verifies pre-approval checks (section 3) before approving prod.
5. Approve `MigrateProd` and confirm completion.

## 3) Post-migration validation checks
Run on target prod DB:

```sql
SELECT installed_rank, version, description, type, success, installed_on
FROM INB.flyway_schema_history
ORDER BY installed_rank;
```

Expected:
- `success = 1` for all rows.
- Versions `1`, `2`, `3` present for first deployment.

```sql
SELECT COUNT(*) AS role_count FROM INB.[ROLE];
SELECT COUNT(*) AS user_count FROM INB.[USER] WHERE username IN ('admin','banker001');
SELECT COUNT(*) AS user_role_count
FROM INB.USER_ROLE ur
JOIN INB.[USER] u ON ur.user_id = u.user_id
WHERE u.username IN ('admin','banker001');
```

Expected:
- `role_count >= 3` (`ADMIN`, `BANKER`, `CUSTOMER`)
- `user_count = 2`
- `user_role_count = 2`

## 4) Rollback checks (minimum)
> Flyway versioned migrations are forward-only by default. Use restore/roll-forward strategy, not ad-hoc manual deletes.

If `ValidateProd` fails:
- No DB changes applied.
- Fix migration script issue and re-run pipeline.

If `MigrateProd` fails:
- Freeze further releases.
- Capture failing migration/version from logs and `flyway_schema_history`.
- Decide one of:
  - **Preferred:** Restore prod from latest backup to known good point.
  - **Alternative:** Roll-forward with corrective migration `V_next__hotfix.sql` after incident review.

## 5) Go / No-Go criteria
Go only if all are true:
- All pipeline stages succeeded.
- `flyway_schema_history` has no failed rows.
- Required seed data exists and application smoke checks pass.

No-Go if any are true:
- Any migration row with `success = 0`.
- Missing required seed rows/role links.
- App critical health checks fail post-deploy.

## 6) Evidence to capture (first run)
- Pipeline run URL and stage results screenshots.
- Output of SQL checks from section 3.
- Approval record for `MigrateProd`.
- Incident notes if any retry/hotfix was needed.
