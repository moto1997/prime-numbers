package com.rbs.primenumbers.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO representing the API response for /api/v1/primes/{max}.
 *
 * This class is a simple data holder (no logic) — part of the "Data Transfer Object" layer.
 * It defines exactly what the API returns to the client.
 */
@Getter
@Builder
@AllArgsConstructor
public class PrimeResponse {
    @Schema(description = "Original input", example = "100")
    private final int input; // The input number provided by user
    @Schema(description = "Number of primes returned", example = "25")
    private final int count; // How many primes found
    @Schema(description = "Ascending list of primes")
    private final List<Integer> primes;   // List of primes up to 'input'
    @Schema(description = "UTC timestamp the result was computed at", example = "2025-10-24T13:59:12Z")
    private final Instant computedAt;     // Timestamp of computation
    @Schema(description = "Computation duration in milliseconds", example = "2")
    private final long durationMs;        // How long it took (in ms)
}

