CREATE TABLE products (
    product_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_name VARCHAR2(150) NOT NULL,
    type_id NUMBER NOT NULL REFERENCES product_types(type_id),
    description VARCHAR2(4000),
    is_active NUMBER(1,0) DEFAULT 1 NOT NULL
        CONSTRAINT chk_prod_active CHECK (is_active IN (0,1)),
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP
);
