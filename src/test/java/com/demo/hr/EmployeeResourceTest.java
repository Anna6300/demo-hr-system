package com.demo.hr;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Integrationstests für die REST-API (EmployeeResource).
 *
 * Strategie: Diese Tests starten die gesamte Quarkus-Anwendung mit einer
 * echten H2-Datenbank (kein Mocking). REST Assured sendet echte HTTP-Requests
 * gegen die laufende Anwendung."
 *
 * Unterschied zu EmployeeServiceTest:
 *   Unit-Test   → mockt Abhängigkeiten, testet nur eine Klasse in Isolation
 *   Integrationstest → testet das Zusammenspiel aller Schichten (Resource → Service → Repository → DB)
 *
 * @QuarkusTest            → Startet die komplette Anwendung auf einem Testport (8081)
 * @TestMethodOrder        → Reihenfolge der Tests festlegen (wegen Testdaten-Abhängigkeiten)
 *
 * REST Assured DSL:
 *   given()   → Request-Konfiguration (Header, Body, Auth)
 *   when()    → HTTP-Aktion (get, post, put, delete)
 *   then()    → Assertions über die Antwort (statusCode, body)
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeResourceTest {

    // ─── GET Alle Mitarbeiter ─────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("GET /api/employees gibt Liste zurück")
    void getAllEmployees_returnsListWithTestData() {
        given()
            .when()
                .get("/api/employees")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(6))  // 6 Einträge aus import.sql
                .body("[0].firstName", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/employees/active gibt nur aktive Mitarbeiter zurück")
    void getActiveEmployees_returnsOnlyActive() {
        given()
            .when()
                .get("/api/employees/active")
            .then()
                .statusCode(200)
                .body("size()", is(5))           // Jonas Koch ist inaktiv
                .body("active", everyItem(is(true)));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/employees?search=schmidt findet Mitarbeiter nach Name")
    void searchEmployees_byName() {
        given()
            .queryParam("search", "schmidt")
            .when()
                .get("/api/employees")
            .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].lastName", equalTo("Schmidt"));
    }

    // ─── GET Einzelner Mitarbeiter ────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("GET /api/employees/{id} gibt korrekten Mitarbeiter zurück")
    void getEmployeeById_found() {
        given()
            .when()
                .get("/api/employees/1")
            .then()
                .statusCode(200)
                .body("id", is(1))
                .body("firstName", equalTo("Anna"))
                .body("lastName", equalTo("Schmidt"))
                .body("email", equalTo("a.schmidt@demo-hr.de"));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/employees/{id} gibt 404 für unbekannte ID zurück")
    void getEmployeeById_notFound() {
        given()
            .when()
                .get("/api/employees/999")
            .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/employees/{id}/service berechnet Betriebszugehörigkeit")
    void getYearsOfService_returnsCorrectValue() {
        given()
            .when()
                .get("/api/employees/5/service")  // Maria Fischer, eingestellt 2016
            .then()
                .statusCode(200)
                .body("yearsOfService", is(greaterThanOrEqualTo(8)));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/employees/department/IT gibt nur IT-Mitarbeiter zurück")
    void getByDepartment_it() {
        given()
            .when()
                .get("/api/employees/department/IT")
            .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("department", everyItem(equalTo("IT")));
    }

    // ─── POST: Mitarbeiter anlegen ────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("POST /api/employees legt neuen Mitarbeiter an und gibt 201 zurück")
    void createEmployee_success() {
        String requestBody = """
                {
                    "firstName": "Max",
                    "lastName": "Mustermann",
                    "email": "max.mustermann@test.de",
                    "department": "IT",
                    "position": "Testautomatisierer",
                    "salary": 65000.00,
                    "hireDate": "2024-01-01"
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
                .post("/api/employees")
            .then()
                .statusCode(201)
                .header("Location", containsString("/api/employees/"))
                .body("firstName", equalTo("Max"))
                .body("active", is(true));
    }

    @Test
    @Order(9)
    @DisplayName("POST /api/employees gibt 409 Conflict bei doppelter E-Mail zurück")
    void createEmployee_duplicateEmail_conflict() {
        String requestBody = """
                {
                    "firstName": "Duplicate",
                    "lastName": "User",
                    "email": "a.schmidt@demo-hr.de",
                    "department": "HR",
                    "position": "Test",
                    "salary": 50000.00,
                    "hireDate": "2024-01-01"
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
                .post("/api/employees")
            .then()
                .statusCode(409);
    }

    @Test
    @Order(10)
    @DisplayName("POST /api/employees gibt 400 Bad Request bei fehlenden Pflichtfeldern zurück")
    void createEmployee_missingFields_badRequest() {
        String invalidBody = """
                {
                    "firstName": "Kein"
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .body(invalidBody)
            .when()
                .post("/api/employees")
            .then()
                .statusCode(400);
    }

    // ─── PUT: Mitarbeiter aktualisieren ───────────────────────────────────────

    @Test
    @Order(11)
    @DisplayName("PUT /api/employees/{id} aktualisiert Gehalt")
    void updateEmployee_salary() {
        String updateBody = """
                {
                    "salary": 80000.00
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .body(updateBody)
            .when()
                .put("/api/employees/1")
            .then()
                .statusCode(200)
                .body("salary", is(80000.0f));
    }

    // ─── DELETE: Mitarbeiter deaktivieren ─────────────────────────────────────

    @Test
    @Order(12)
    @DisplayName("DELETE /api/employees/{id} deaktiviert Mitarbeiter (Soft-Delete)")
    void deactivateEmployee_success() {
        // Deaktivieren
        given()
            .when()
                .delete("/api/employees/2")
            .then()
                .statusCode(204);

        // Prüfen dass Mitarbeiter noch existiert, aber inaktiv ist
        given()
            .when()
                .get("/api/employees/2")
            .then()
                .statusCode(200)
                .body("active", is(false));
    }
}
