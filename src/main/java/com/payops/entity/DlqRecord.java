package com.payops.entity;

import com.payops.enums.DlqStatus;
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
    name = "dlq_records",
    indexes = {
        @Index(name = "idx_dlq_payment_id", columnList = "payment_id"),
        @Index(name = "idx_dlq_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DlqRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "original_queue", nullable = false, length = 100)
    private String originalQueue;

    @Column(name = "message_payload", columnDefinition = "TEXT", nullable = false)
    private String messagePayload;

    @Column(name = "exception_class", length = 255)
    private String exceptionClass;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DlqStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public DlqRecord(UUID paymentId, String originalQueue, String messagePayload, String exceptionClass, String failureReason, int retryCount) {
        this.paymentId = paymentId;
        this.originalQueue = originalQueue;
        this.messagePayload = messagePayload;
        this.exceptionClass = exceptionClass;
        this.failureReason = failureReason;
        this.retryCount = retryCount;
        this.status = DlqStatus.PENDING_ANALYSIS;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status == null) {
            this.status = DlqStatus.PENDING_ANALYSIS;
        }
    }
}
