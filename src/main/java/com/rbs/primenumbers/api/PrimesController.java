package com.rbs.primenumbers.api;

import com.rbs.primenumbers.domain.PrimesService;
import com.rbs.primenumbers.model.PrimeResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/v1/primes")
@Validated
@RequiredArgsConstructor // generates constructor for final fields (primesService)
public class PrimesController {

    private final PrimesService primesService;


    @Value("${primes.cache.max-age-seconds:3600}")
    private long cacheTtlSeconds;

    private Duration cacheTtl() {
        return Duration.ofSeconds(cacheTtlSeconds);
    }

    /**
     * GET /api/v1/primes/{max}
     * Optional: ?cache=true to enable HTTP caching (ETag + Cache-Control). ?algorithm=segmented to enable an advanced algorithm
     */
    @Operation(
            summary = "Get primes up to and including max",
            description = """
            Returns all prime numbers ≤ max.
            Optional query parameters:
            - `algorithm`: specify which algorithm to use (simple | segmented)
            - `cache=true`: enable HTTP caching via ETag/304
            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Prime list",
            content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.rbs.primenumbers.model.PrimeResponse.class)),
                    @Content(mediaType = "application/xml",
                            schema = @Schema(implementation = com.rbs.primenumbers.model.PrimeResponse.class))
            }
    )
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "422", description = "Upper bound exceeded")

    @GetMapping(value = "/{max}", produces = {"application/json", "application/xml"})
    public ResponseEntity<PrimeResponse> getPrimes(
            @Parameter(description = "Upper bound (≥ 0)", example = "100")
            @PathVariable @Min(0) int max,
            @Parameter(description = "Algorithm to use (optional)", example = "segmented")
            @RequestParam(name = "algorithm", required = false) String algorithm,
            @Parameter(description = "Enable HTTP caching via ETag/304")
            @RequestParam(name = "cache", defaultValue = "false") boolean useCache,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch
    ) {
        primesService.guardUpperBound(max);


        if (!useCache) {
            Instant start = Instant.now();
            var primes = primesService.compute(max,algorithm);
            long durationMs = Duration.between(start, Instant.now()).toMillis();

            log.info("Computed primes up to {} in {} ms (count={})", max, durationMs, primes.size());

            var body = new PrimeResponse(max, primes.size(), primes, Instant.now(), durationMs);
            return ResponseEntity.ok(body);
        }

        // Cached path

        String algoTag = (algorithm == null || algorithm.isBlank()) ? "default" : algorithm;
        String etag = "\"primes-%d-%s-v1\"".formatted(max, algoTag);

        if (etag.equals(ifNoneMatch)) {
            log.info("ETag match for max={} algorithm={}, returning 304", max, algoTag);
            return ResponseEntity.status(304)
                    .eTag(etag)
                    .cacheControl(CacheControl.maxAge(cacheTtl()).cachePublic())
                    .build();
        }

        Instant start = Instant.now();
        var primes = primesService.compute(max,algorithm);
        long durationMs = Duration.between(start, Instant.now()).toMillis();

        log.info("Computed primes (cached path) up to {} in {} ms (count={})", max, durationMs, primes.size());

        var body = new PrimeResponse(max, primes.size(), primes, Instant.now(), durationMs);
        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(cacheTtl()).cachePublic())
                .body(body);
    }
}
