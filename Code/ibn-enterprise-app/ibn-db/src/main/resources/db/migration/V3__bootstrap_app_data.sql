-- Flyway migration: one-time bootstrap data
-- Source seed adapted from src/main/resources/user_table_inserts_only.sql
-- These are protected with IF NOT EXISTS for additional safety.

IF NOT EXISTS (SELECT 1 FROM [INB].[USER] WHERE username = 'banker001')
BEGIN
	INSERT INTO [INB].[USER]
	(
		user_id,
		customer_id,
		idp_subject,
		username,
		pwd,
		email,
		phone,
		user_type,
		status,
		locked_until,
		last_login_at,
		created_at,
		updated_at
	)
	VALUES
	(
		'210c3ee5-58fc-40ec-8d02-7c9dd094251c',
		NULL,
		'LOCAL-BANKER001',
		'banker001',
		'2434784583e9cdfe469bc0ea094769d7c63c3f48ceeac226690d10d418eadd8e',
		'banker001@ibn.local',
		'9000000000',
		'BANKER',
		'ACTIVE',
		NULL,
		'2026-02-22T00:27:13.047966',
		'2026-02-22T00:26:17.040847',
		'2026-02-22T00:27:13.047966'
	);
END;

IF NOT EXISTS (SELECT 1 FROM [INB].[USER] WHERE username = 'admin')
BEGIN
	INSERT INTO [INB].[USER]
	(
		user_id,
		customer_id,
		idp_subject,
		username,
		pwd,
		email,
		phone,
		user_type,
		status,
		locked_until,
		last_login_at,
		created_at,
		updated_at
	)
	VALUES
	(
		'0fce1ec5-757f-4dd3-9ed8-8c8a08bb8ec9',
		NULL,
		'LOCAL-ADMIN',
		'admin',
		'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7',
		'admin@ibn.local',
		'9000000000',
		'ADMIN',
		'ACTIVE',
		NULL,
		NULL,
		'2026-02-22T00:26:17.161269',
		'2026-02-22T00:26:17.161269'
	);
END;

IF NOT EXISTS
(
	SELECT 1
	FROM [INB].USER_ROLE ur
	JOIN [INB].[USER] u ON ur.user_id = u.user_id
	JOIN [INB].[ROLE] r ON ur.role_id = r.role_id
	WHERE u.username = 'admin' AND r.role_name = 'ADMIN'
)
BEGIN
	INSERT INTO [INB].USER_ROLE (user_id, role_id, assigned_at, assigned_by)
	SELECT u.user_id, r.role_id, SYSUTCDATETIME(), NULL
	FROM [INB].[USER] u
	CROSS JOIN [INB].[ROLE] r
	WHERE u.username = 'admin' AND r.role_name = 'ADMIN';
END;

IF NOT EXISTS
(
	SELECT 1
	FROM [INB].USER_ROLE ur
	JOIN [INB].[USER] u ON ur.user_id = u.user_id
	JOIN [INB].[ROLE] r ON ur.role_id = r.role_id
	WHERE u.username = 'banker001' AND r.role_name = 'BANKER'
)
BEGIN
	INSERT INTO [INB].USER_ROLE (user_id, role_id, assigned_at, assigned_by)
	SELECT u.user_id, r.role_id, SYSUTCDATETIME(), NULL
	FROM [INB].[USER] u
	CROSS JOIN [INB].[ROLE] r
	WHERE u.username = 'banker001' AND r.role_name = 'BANKER';
END;
