ALTER TABLE transactions
    ADD COLUMN description VARCHAR(255) NULL AFTER status;
