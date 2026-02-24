CREATE TABLE purchase_orders (
    order_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    seller_id NUMBER NOT NULL REFERENCES sellers(seller_id),
    order_date DATE NOT NULL,
    invoice_number VARCHAR2(100),
    remarks VARCHAR2(4000),
    total_amount NUMBER(14,2) DEFAULT 0 NOT NULL,
    status VARCHAR2(20) DEFAULT 'DRAFT' NOT NULL
        CONSTRAINT chk_po_status CHECK (status IN ('DRAFT', 'CONFIRMED')),
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    updated_at TIMESTAMP DEFAULT SYSTIMESTAMP
);
