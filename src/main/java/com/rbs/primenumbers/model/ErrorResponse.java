package com.rbs.primenumbers.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;


@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private final String error;
    private final String message;

    @Builder.Default
    private final Instant timestamp = Instant.now();



}
