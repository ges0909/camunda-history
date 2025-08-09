package com.valantic.sti;

import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class TestcontainerCamundaEngineTest {

    @Container
    static GenericContainer<?> camundaContainer = new GenericContainer<>(DockerImageName.parse("camunda/camunda-bpm-platform:7.10.0"))
            .withExposedPorts(8080);

    static RequestSpecification REQUEST_SPEC;

    @BeforeAll
    static void setUp() {
        String baseUri = "http://localhost:" + camundaContainer.getMappedPort(8080);
        REQUEST_SPEC = given()
                .baseUri(baseUri)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filters(
                        new RequestLoggingFilter(),
                        new ResponseLoggingFilter(),
                        new ErrorLoggingFilter());
    }

    @AfterAll
    static void tearDown() {
        camundaContainer.stop();
    }

    @Test
    @Order(1)
    void testCamundaConnection() {
        given()
                .spec(REQUEST_SPEC)
                .when()
                .get("/engine-rest/engine")
                .then()
                .statusCode(200)
                .body("[0].name", equalTo("default"));
    }

    @Test
    @Order(2)
    void testQueryCamundaHistory() {
        given()
                .spec(REQUEST_SPEC)
                .when()
                .get("/engine-rest/history/process-instance")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(3)
    void testDeployBpmnProcess() {
        given()
                .spec(REQUEST_SPEC)
                .contentType("multipart/form-data") // override content type
                .multiPart("deployment-name", "test-deployment")
                .multiPart("data", new File("src/test/resources/test-process.bpmn"))
                .post("/engine-rest/deployment/create")
                .then()
                .statusCode(200)
                .body("name", equalTo("test-deployment"));
    }

    @Test
    @Order(4)
    void testStartSingleBpmnProcessInstance() {
        Map<String, Object> variables = Map.of(
                "someVar", Map.of("someValue", "test", "type", "String")
        );
        given()
                .spec(REQUEST_SPEC)
                .body(Map.of("variables", variables))
                .post("/engine-rest/process-definition/key/testProcess/start")
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Disabled("too slow")
    @Test
    @Order(5)
    void testStartMultipleBpmnProcessInstance() {
        for (int i = 0; i < 1_000_000; i++) {
            given()
                    .spec(REQUEST_SPEC)
                    .body(Map.of("variables", Map.of()))
                    .post("/engine-rest/process-definition/key/testProcess/start")
                    .then()
                    .statusCode(200)
                    .body("id", notNullValue());
        }
    }
}
