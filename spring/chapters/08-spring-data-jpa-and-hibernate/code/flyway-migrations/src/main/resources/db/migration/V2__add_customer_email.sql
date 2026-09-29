ALTER TABLE customer ADD COLUMN email VARCHAR(200);
CREATE UNIQUE INDEX customer_email_uk ON customer (email);
