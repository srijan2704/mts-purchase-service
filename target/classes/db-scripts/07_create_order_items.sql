CREATE TABLE order_items (
    item_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id NUMBER NOT NULL REFERENCES purchase_orders(order_id) ON DELETE CASCADE,
    variant_id NUMBER NOT NULL REFERENCES product_variants(variant_id),
    quantity NUMBER(10,3) NOT NULL CONSTRAINT chk_oi_qty CHECK (quantity > 0),
    rate_per_unit NUMBER(12,2) NOT NULL CONSTRAINT chk_oi_rate CHECK (rate_per_unit >= 0),
    line_total NUMBER(14,2) GENERATED ALWAYS AS (quantity * rate_per_unit) VIRTUAL,
    notes VARCHAR2(255)
);
