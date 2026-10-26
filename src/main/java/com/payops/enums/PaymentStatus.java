package com.payops.enums;

public enum PaymentStatus {
    INITIATED,      // Persisted in PayOps, queued for execution
    PENDING,        // Processing in-flight or PayPal pending review (eCheck/risk)
    AUTHORIZED,     // Approved by buyer / authorized, pending capture
    CAPTURED,       // Confirmed capture success (PayPal COMPLETED)
    AMBIGUOUS,      // Network timeout/unknown provider outcome; requires reconciliation
    FAILED_FINAL,   // Confirmed terminal failure (hard decline, invalid payload)
    CANCELLED       // Explicitly cancelled or voided
}
