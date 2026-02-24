CREATE TABLE sellers (
    seller_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR2(150) NOT NULL,
    contact_person VARCHAR2(100) NOT NULL,
    phone VARCHAR2(20),
    email VARCHAR2(150),
    address CLOB,
    gst_number VARCHAR2(30),
    is_active NUMBER(1,0) DEFAULT 1 NOT NULL
        CONSTRAINT chk_seller_active CHECK (is_active IN (0,1)),
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP
);
