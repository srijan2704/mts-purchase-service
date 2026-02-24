CREATE OR REPLACE TRIGGER trg_po_updated_at
BEFORE UPDATE ON purchase_orders
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/
