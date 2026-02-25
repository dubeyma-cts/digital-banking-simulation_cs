-- Flyway migration: reference/master data
-- Keep this script idempotent. Use MERGE or IF NOT EXISTS patterns.
-- This file includes core role master data expected by authentication flows.

IF NOT EXISTS (SELECT 1 FROM [INB].[ROLE] WHERE role_name = 'ADMIN')
BEGIN
	INSERT INTO [INB].[ROLE] (role_id, role_name, description)
	VALUES ('8D4E8B69-2551-492A-9AD5-EE610A45D514', 'ADMIN', 'System administrator');
END;

IF NOT EXISTS (SELECT 1 FROM [INB].[ROLE] WHERE role_name = 'BANKER')
BEGIN
	INSERT INTO [INB].[ROLE] (role_id, role_name, description)
	VALUES ('9CC137D5-8487-4571-B819-B5485DCC2F3D', 'BANKER', 'Branch banker user');
END;

IF NOT EXISTS (SELECT 1 FROM [INB].[ROLE] WHERE role_name = 'CUSTOMER')
BEGIN
	INSERT INTO [INB].[ROLE] (role_id, role_name, description)
	VALUES ('871882D1-7688-4C97-962B-E2830CF119D0', 'CUSTOMER', 'Retail banking customer');
END;
