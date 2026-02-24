package com.mts.mts_purchase_service.models;



import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * DTO for Seller entity.
 * Used for both request (create/update) and response payloads.
 * Validation annotations enforce mandatory fields on incoming requests.
 */
public class SellerDTO {

    // Null on create request; populated in response
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "System-generated seller id", example = "12")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long sellerId;

    @NotBlank(message = "Seller name is required")
    @Size(max = 150, message = "Seller name must not exceed 150 characters")
    @Schema(description = "Seller business name", example = "Sharma Traders", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Contact person name is required")
    @Size(max = 100, message = "Contact person name must not exceed 100 characters")
    @Schema(description = "Primary contact person", example = "Amit Sharma", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactPerson;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Schema(description = "Contact phone number", example = "9876543210")
    private String phone;

    @Email(message = "Please provide a valid email address")
    @Size(max = 150)
    @Schema(description = "Contact email address", example = "amit.sharma@example.com")
    private String email;

    @Schema(description = "Business address", example = "Main Road, Ranchi")
    private String address;

    @Size(max = 30, message = "GST number must not exceed 30 characters")
    @Schema(description = "GST registration number", example = "20ABCDE1234F1Z5")
    private String gstNumber;

    // Returned in response; ignored on create (defaults to true)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Active flag managed by system")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean active;

    // Returned in response only
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Record creation timestamp")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────

    public SellerDTO() {}

    public SellerDTO(Long sellerId, String name, String contactPerson,
                     String phone, String email, String address,
                     String gstNumber, Boolean active, LocalDateTime createdAt) {
        this.sellerId      = sellerId;
        this.name          = name;
        this.contactPerson = contactPerson;
        this.phone         = phone;
        this.email         = email;
        this.address       = address;
        this.gstNumber     = gstNumber;
        this.active        = active;
        this.createdAt     = createdAt;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public Long getSellerId()                      { return sellerId; }
    public void setSellerId(Long sellerId)         { this.sellerId = sellerId; }

    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }

    public String getContactPerson()               { return contactPerson; }
    public void setContactPerson(String cp)        { this.contactPerson = cp; }

    public String getPhone()                       { return phone; }
    public void setPhone(String phone)             { this.phone = phone; }

    public String getEmail()                       { return email; }
    public void setEmail(String email)             { this.email = email; }

    public String getAddress()                     { return address; }
    public void setAddress(String address)         { this.address = address; }

    public String getGstNumber()                   { return gstNumber; }
    public void setGstNumber(String gstNumber)     { this.gstNumber = gstNumber; }

    public Boolean isActive()                      { return active; }
    public void setActive(Boolean active)          { this.active = active; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
