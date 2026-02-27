-- Flyway migration: add blob URL column for cheque attachment storage

IF COL_LENGTH('INB.CHEQUE_DEPOSIT', 'attachment_blob_url') IS NULL
BEGIN
    ALTER TABLE [INB].CHEQUE_DEPOSIT
        ADD attachment_blob_url VARCHAR(1024);
END;
