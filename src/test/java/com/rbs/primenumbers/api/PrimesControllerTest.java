package com.rbs.primenumbers.api;

import com.rbs.primenumbers.App;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = App.class
)
@TestPropertySource(properties = {
        "primes.max-allowed=1000000",
        "primes.segment-size=32",
        "primes.algorithm.default=simple",
        "spring.mvc.contentnegotiation.favor-parameter=false",
        "spring.mvc.contentnegotiation.default-content-type=application/json"
})
class PrimesControllerTest {

    @LocalServerPort
    int port;

    private RequestSpecification jsonReq;
    private ResponseSpecification okJson;

    @BeforeEach
    void setup() {
        RestAssured.port = port;

        jsonReq = new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .log(LogDetail.URI)
                .build();

        okJson = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .expectBody("input", notNullValue())
                .expectBody("count", greaterThanOrEqualTo(0))
                .expectBody("primes", notNullValue())
                .build();
    }

    @Test
    void returnsPrimes_JSON_defaultAlgorithm() {
        given().spec(jsonReq)
                .when().get("/api/v1/primes/10")
                .then().spec(okJson)
                .body("input", equalTo(10))
                .body("count", equalTo(4))
                .body("primes", contains(2, 3, 5, 7));
    }

    @Test
    void returnsPrimes_JSON_withExplicitAlgorithm_simple() {
        given().spec(jsonReq)
                .queryParam("algorithm", "simple")
                .when().get("/api/v1/primes/30")
                .then().spec(okJson)
                .body("input", equalTo(30))
                .body("primes", hasItems(2,3,5,7,11,13,17,19,23,29));
    }

    @Test
    void returnsPrimes_JSON_withExplicitAlgorithm_segmented() {
        given().spec(jsonReq)
                .queryParam("algorithm", "segmented")
                .when().get("/api/v1/primes/30")
                .then().spec(okJson)
                .body("input", equalTo(30))
                .body("primes", hasItems(2,3,5,7,11,13,17,19,23,29));
    }

    @Test
    void contentNegotiation_XML_whenRequested() {
        given()
                .accept("application/xml")
                .when()
                .get("/api/v1/primes/10")
                .then()
                .statusCode(200)
                .contentType(containsString("application/xml"))
                .body("PrimeResponse.input", equalTo("10"))
                .body("PrimeResponse.count", equalTo("4"));
    }

    @Test
    void validationError_whenNegativeInput_returns400() {
        given().spec(jsonReq)
                .when().get("/api/v1/primes/-1")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON);
    }

    @Test
    void domainError_whenAboveMax_returns422() {
        given().spec(jsonReq)
                .when().get("/api/v1/primes/1000001")
                .then()
                .statusCode(422)
                .contentType(ContentType.JSON)
                .body("message", containsString("must be ≤"));
    }

    @Test
    void caching_ETag_and_304_whenCacheEnabled() {
        // First call gets an ETag
        String etag =
                given().spec(jsonReq)
                        .queryParam("cache", true)
                        .queryParam("algorithm", "simple")
                        .when()
                        .get("/api/v1/primes/50")
                        .then()
                        .statusCode(200)
                        .header("ETag", not(isEmptyOrNullString()))
                        .header("Cache-Control", containsString("max-age"))
                        .extract().header("ETag");

        // Second call with If-None-Match returns 304
        given().spec(jsonReq)
                .queryParam("cache", true)
                .queryParam("algorithm", "simple")
                .header("If-None-Match", etag)
                .when()
                .get("/api/v1/primes/50")
                .then()
                .statusCode(304)
                .body(emptyString());
    }

    @Test
    void invalidAlgorithm_returns400() {
        given().spec(jsonReq)
                .queryParam("algorithm", "does-not-exist")
                .when()
                .get("/api/v1/primes/10")
                .then()
                .statusCode(400);
    }

    @Test
    void primesArray_isSorted_andCountMatches() {
        var primes =
                given().spec(jsonReq)
                        .when().get("/api/v1/primes/100")
                        .then().statusCode(200)
                        .extract().jsonPath().getList("primes", Integer.class);

        // sorted & count = known 25
        org.assertj.core.api.Assertions.assertThat(primes)
                .isSorted()
                .contains(97)
                .hasSize(25);
    }
}
