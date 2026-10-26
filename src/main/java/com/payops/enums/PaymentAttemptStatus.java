package com.payops.enums;

public enum PaymentAttemptStatus {
    PENDING,    // Request dispatched to PayPal, waiting for HTTP response
    SUCCESS,    // 2xx response confirming capture/authorization
    FAILURE,    // 4xx/5xx confirmed decline or bad request
    UNKNOWN     // Network timeout (e.g., SocketTimeoutException, HTTP 504) where outcome is uncertain
}