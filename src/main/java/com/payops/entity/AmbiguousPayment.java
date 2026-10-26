package com.payops.entity;

import com.payops.enums.ReconciliationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "ambiguous_payments",
    indexes = {
        @Index(name = "idx_ambiguous_payment_id", columnList = "payment_id", unique = true),
        @Index(name = "idx_ambiguous_status", columnList = "reconciliation_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AmbiguousPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "payment_id", nullable = false, unique = true, updatable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reconciliation_status", nullable = false, length = 50)
    private ReconciliationStatus reconciliationStatus;

    @Column(name = "reconciliation_attempts", nullable = false)
    private int reconciliationAttempts = 0;

    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    @Column(name = "findings", columnDefinition = "TEXT")
    private String findings;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public AmbiguousPayment(UUID paymentId) {
        this.paymentId = paymentId;
        this.reconciliationStatus = ReconciliationStatus.PENDING;
        this.reconciliationAttempts = 0;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.reconciliationStatus == null) {
            this.reconciliationStatus = ReconciliationStatus.PENDING;
        }
    }
}
