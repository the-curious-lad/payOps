package com.payops.entity;

import com.payops.enums.RetryStatus;
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
    name = "retry_records",
    indexes = {
        @Index(name = "idx_retry_payment_id", columnList = "payment_id"),
        @Index(name = "idx_retry_status_scheduled", columnList = "status, scheduled_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "retry_ordinal", nullable = false)
    private int retryOrdinal;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Column(name = "backoff_interval_seconds", nullable = false)
    private int backoffIntervalSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RetryStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public RetryRecord(UUID paymentId, int retryOrdinal, Instant scheduledAt, int backoffIntervalSeconds) {
        this.paymentId = paymentId;
        this.retryOrdinal = retryOrdinal;
        this.scheduledAt = scheduledAt;
        this.backoffIntervalSeconds = backoffIntervalSeconds;
        this.status = RetryStatus.SCHEDULED;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status == null) {
            this.status = RetryStatus.SCHEDULED;
        }
    }
}
