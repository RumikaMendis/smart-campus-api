package com.smartcampus.model;

/**
 * Standard error response body returned by all exception mappers.
 *
 * Example JSON:
 * {
 *   "status": 404,
 *   "error": "Room with ID 5 not found"
 * }
 */
public class ErrorResponse {

    private int status;
    private String error;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public ErrorResponse() {}

    public ErrorResponse(int status, String error) {
        this.status = status;
        this.error = error;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
