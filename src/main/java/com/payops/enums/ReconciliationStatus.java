package com.payops.enums;

public enum ReconciliationStatus {
    PENDING,                // Waiting for Agent 2 inspection
    RESOLVED_CAPTURED,      // Verified with PayPal: money was successfully captured
    RESOLVED_NOT_CHARGED,   // Verified with PayPal: no charge occurred; safe for policy action
    REQUIRES_HUMAN_REVIEW   // Inconclusive after max inspection attempts; alerts human operations
}