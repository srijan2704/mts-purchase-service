CREATE TABLE product_variants (
    variant_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id NUMBER NOT NULL REFERENCES products(product_id),
    unit_id NUMBER NOT NULL REFERENCES units(unit_id),
    variant_label VARCHAR2(100) NOT NULL,
    pack_size NUMBER(10,3) NOT NULL,
    pieces_per_pack NUMBER(5) DEFAULT 1 NOT NULL,
    barcode VARCHAR2(50),
    is_active NUMBER(1,0) DEFAULT 1 NOT NULL
        CONSTRAINT chk_var_active CHECK (is_active IN (0,1)),
    CONSTRAINT uq_variant UNIQUE (product_id, variant_label)
);
