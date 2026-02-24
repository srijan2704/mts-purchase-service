package com.mts.mts_purchase_service.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity mapped to the SELLERS table.
 *
 * Table structure (from v2 design):
 *   seller_id      NUMBER  PK IDENTITY
 *   name           VARCHAR2(150) NOT NULL
 *   contact_person VARCHAR2(100) NOT NULL
 *   phone          VARCHAR2(20)
 *   email          VARCHAR2(150)
 *   address        CLOB
 *   gst_number     VARCHAR2(30)
 *   is_active      NUMBER(1,0)  DEFAULT 1
 *   created_at     TIMESTAMP    DEFAULT SYSTIMESTAMP
 */
@Entity
@Table(name = "SELLERS")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Maps to Oracle GENERATED ALWAYS AS IDENTITY
    @Column(name = "SELLER_ID")
    private Long sellerId;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "CONTACT_PERSON", nullable = false, length = 100)
    private String contactPerson;

    @Column(name = "PHONE", length = 20)
    private String phone;

    @Column(name = "EMAIL", length = 150)
    private String email;

    @Lob // Maps to Oracle CLOB
    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "GST_NUMBER", length = 30)
    private String gstNumber;

    // Oracle has no BOOLEAN — stored as NUMBER(1,0): 1=active, 0=inactive
    @Column(name = "IS_ACTIVE", nullable = false)
    private int isActive = 1; // Default: active

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle Callbacks ───────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isActive  = 1; // Always create as active
    }

    // ── Constructors ──────────────────────────────────────────────

    public Seller() {}

    public Seller(String name, String contactPerson, String phone,
                  String email, String address, String gstNumber) {
        this.name          = name;
        this.contactPerson = contactPerson;
        this.phone         = phone;
        this.email         = email;
        this.address       = address;
        this.gstNumber     = gstNumber;
    }

    // ── Convenience helpers ───────────────────────────────────────

    /**
     * Returns true if seller is active (is_active = 1).
     * Abstracts the int → boolean conversion from service/controller layer.
     */
    public boolean isActive() {
        return this.isActive == 1;
    }

    /**
     * Sets active status.
     * Converts boolean to NUMBER(1,0) for Oracle compatibility.
     */
    public void setActive(boolean active) {
        this.isActive = active ? 1 : 0;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public Long getSellerId()                         { return sellerId; }
    public void setSellerId(Long sellerId)            { this.sellerId = sellerId; }

    public String getName()                           { return name; }
    public void setName(String name)                  { this.name = name; }

    public String getContactPerson()                  { return contactPerson; }
    public void setContactPerson(String contactPerson){ this.contactPerson = contactPerson; }

    public String getPhone()                          { return phone; }
    public void setPhone(String phone)                { this.phone = phone; }

    public String getEmail()                          { return email; }
    public void setEmail(String email)                { this.email = email; }

    public String getAddress()                        { return address; }
    public void setAddress(String address)            { this.address = address; }

    public String getGstNumber()                      { return gstNumber; }
    public void setGstNumber(String gstNumber)        { this.gstNumber = gstNumber; }

    public int getIsActive()                          { return isActive; }
    public void setIsActive(int isActive)             { this.isActive = isActive; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Seller{" +
                "sellerId=" + sellerId +
                ", name='" + name + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
