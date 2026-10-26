package com.payops.entity;

import com.payops.enums.DecisionRecommendation;
import com.payops.enums.FailureCategory;
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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "failure_analyses",
    indexes = {
        @Index(name = "idx_failure_analysis_payment_id", columnList = "payment_id")
    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FailureAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_category", nullable = false, length = 50)
    private FailureCategory failureCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", nullable = false, length = 50)
    private DecisionRecommendation recommendation;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(name = "confidence_score", nullable = false, precision = 4, scale = 3)
    private BigDecimal confidenceScore;

    @Column(name = "rationale", columnDefinition = "TEXT")
    private String rationale;

    @Column(name = "suggested_backoff_seconds")
    private Integer suggestedBackoffSeconds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public FailureAnalysis(UUID paymentId, int attemptNumber, FailureCategory failureCategory,
                           DecisionRecommendation recommendation, BigDecimal confidenceScore,
                           String rationale, Integer suggestedBackoffSeconds) {
        this.paymentId = paymentId;
        this.attemptNumber = attemptNumber;
        this.failureCategory = failureCategory;
        this.recommendation = recommendation;
        this.confidenceScore = confidenceScore;
        this.rationale = rationale;
        this.suggestedBackoffSeconds = suggestedBackoffSeconds;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
