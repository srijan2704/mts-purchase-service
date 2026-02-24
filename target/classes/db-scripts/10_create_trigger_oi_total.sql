CREATE OR REPLACE TRIGGER trg_oi_total
AFTER INSERT OR UPDATE OR DELETE ON order_items
FOR EACH ROW
DECLARE
    v_order_id NUMBER;
BEGIN
    v_order_id := CASE WHEN DELETING THEN :OLD.order_id ELSE :NEW.order_id END;

    UPDATE purchase_orders
       SET total_amount = (
           SELECT NVL(SUM(quantity * rate_per_unit), 0)
             FROM order_items
            WHERE order_id = v_order_id
       )
     WHERE order_id = v_order_id;
END;
/
