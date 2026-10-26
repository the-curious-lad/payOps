package com.payops.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "payment_resolution_summaries",
    indexes = {
        @Index(name = "idx_summary_payment_id", columnList = "payment_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResolutionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "payment_id", nullable = false, unique = true, updatable = false)
    private UUID paymentId;

    @Column(name = "structured_facts", columnDefinition = "TEXT", nullable = false)
    private String structuredFacts;

    @Column(name = "narrative_summary", columnDefinition = "TEXT", nullable = false)
    private String narrativeSummary;

    @Column(name = "last_updated_by", length = 50)
    private String lastUpdatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PaymentResolutionSummary(UUID paymentId, String structuredFacts, String narrativeSummary, String lastUpdatedBy) {
        this.paymentId = paymentId;
        this.structuredFacts = structuredFacts;
        this.narrativeSummary = narrativeSummary;
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
