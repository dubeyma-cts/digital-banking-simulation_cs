# Azure deployment plan (UI + core-svc)

## Scope

This plan deploys:

- `ibn-core-svc` as a Java 11 Linux App Service.
- `ibn-ui` as a Java 11 Linux App Service (Spring Boot packaged artifact in this repository).
- Shared monitoring (Log Analytics + Application Insights).
- Storage account and private blob container.

## Corrections applied to match deployability

1. Replaced single-app template model with separate App Services for core and UI.
2. Removed incomplete Application Gateway block that fails ARM deployment when network child objects are empty.
3. Rewrote diagnostic settings as extension resources (`.../providers/diagnosticSettings`) bound to each target resource.
4. Added Linux Java runtime configuration (`JAVA|11-java11`) for both web apps.
5. Updated `ibn-core-svc` config to read runtime settings from environment variables.
6. Fixed `ibn-ui` Node/NPM versions to Angular 12-compatible versions to prevent OpenSSL build failures.
7. Updated Angular production bundle budget from kilobytes to realistic megabyte limits for CI builds.

## Deployment steps

### One-command deployment (recommended)

Run from repository root:

```powershell
.\deploy-azure.ps1 -ResourceGroup <your-rg-name>
```

The script runs a preflight ARM validation before build/deploy so quota and policy issues fail fast with clear messages.

Optional flags:

```powershell
.\deploy-azure.ps1 -ResourceGroup <your-rg-name> -SkipBuild
.\deploy-azure.ps1 -ResourceGroup <your-rg-name> -SkipValidate
```

Override app names at runtime:

```powershell
.\deploy-azure.ps1 -ResourceGroup <your-rg-name> -CoreAppName <core-app-name> -UiAppName <ui-app-name>
```

### 1) Build both deployable artifacts

From repository root:

```powershell
mvn -pl ibn-core-svc clean package
mvn -pl ibn-ui clean package
```

Expected outputs:

- `ibn-core-svc/target/ibn-core-svc-1.0-SNAPSHOT.jar`
- `ibn-ui/target/ibn-ui-1.0-SNAPSHOT.war`

### 2) Deploy infrastructure

Update `paas_deployment.parameters.json` first (unique app/storage names).

```powershell
az deployment group create `
  --resource-group <your-rg-name> `
  --template-file paas_deployment.json `
  --parameters @paas_deployment.parameters.json
```

### 3) Deploy core artifact

```powershell
Copy-Item ibn-core-svc/target/ibn-core-svc-1.0-SNAPSHOT.jar ibn-core-svc/target/app.jar -Force
Compress-Archive -Path ibn-core-svc/target/app.jar -DestinationPath ibn-core-svc/target/core.zip -Force
az webapp deploy `
  --resource-group <your-rg-name> `
  --name <coreAppServiceName> `
  --src-path ibn-core-svc/target/core.zip `
  --type zip
```

### 4) Deploy UI artifact

```powershell
Copy-Item ibn-ui/target/ibn-ui-1.0-SNAPSHOT.war ibn-ui/target/app.war -Force
Compress-Archive -Path ibn-ui/target/app.war -DestinationPath ibn-ui/target/ui.zip -Force
az webapp deploy `
  --resource-group <your-rg-name> `
  --name <uiAppServiceName> `
  --src-path ibn-ui/target/ui.zip `
  --type zip
```

### 5) Configure core runtime settings

Set these in Core App Service (App Settings) if using external DB:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `SPRING_PROFILES_ACTIVE`

Optional:

- `LOG_LEVEL_SPRING`
- `LOG_LEVEL_APP`

## Validation checklist

- Core responds at `https://<coreAppServiceName>.azurewebsites.net`.
- UI responds at `https://<uiAppServiceName>.azurewebsites.net`.
- App Service logs flow into the configured Log Analytics workspace.

## Suggested follow-up corrections (optional but recommended)

- Add health endpoint dependency (`spring-boot-starter-actuator`) in `ibn-core-svc` for readiness/liveness checks.
- Add CORS configuration in `ibn-core-svc` if UI and core are hosted on different domains.
- Replace H2 with managed Azure SQL/Oracle in production and set DB app settings accordingly.
