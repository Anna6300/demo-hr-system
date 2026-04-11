# Demo HR System

Ein Demo-Projekt zur Bewerbung für die Position **Java Entwickler | Schwerpunkt Testautomatisierung** bei der GIP GmbH.

Das System implementiert eine REST-API zur Mitarbeiterverwaltung und demonstriert den geforderten Tech Stack: **Quarkus**, **JakartaEE**, **JPA/Hibernate**, **JUnit 5**, **REST Assured** und **Docker**.

---

## Tech Stack im Überblick

| Technologie | Verwendung im Projekt | Warum |
|---|---|---|
| **Java 17** | Gesamter Quellcode | LTS-Version, aktuelle JakartaEE-Basis |
| **Quarkus 3.8** | Anwendungsframework | Cloud-native JakartaEE-Implementierung, schneller Start |
| **JAX-RS (JakartaEE)** | REST-Endpunkte (`EmployeeResource`) | Standard-API für RESTful Services in JakartaEE |
| **CDI (JakartaEE)** | Dependency Injection überall | `@ApplicationScoped`, `@Inject` statt `new` |
| **JPA / Hibernate Panache** | Datenbankzugriff (`Employee`, `EmployeeRepository`) | ORM-Standard in JakartaEE, Panache reduziert Boilerplate |
| **Bean Validation (JakartaEE)** | `@NotBlank`, `@Email` an Entitäten | Validierung nach Standard, automatisch durch Quarkus |
| **H2 In-Memory** | Entwicklung & Tests | Kein Setup nötig, Oracle-kompatible SQL-Syntax |
| **Oracle (Konfiguration vorhanden)** | Produktion | In `application.properties` auskommentiert vorbereitet |
| **JUnit 5** | Unit-Tests | Standard-Testframework für Java |
| **REST Assured** | API-Integrationstests | Lesbares DSL für HTTP-Tests |
| **Mockito** | Mocking in Unit-Tests | CDI-Beans im Test ersetzen |
| **Docker** | Containerisierung | Reproduzierbare Umgebung, Multi-Stage Build |

---

## Voraussetzungen

Für die einfachste Ausführung (Option 1 & 2) wird benötigt:
- **Java 17** oder neuer (`java -version`)
- **Maven 3.9+** (`mvn -version`)

Für Docker (Option 3):
- **Docker Desktop** oder Docker Engine

> **Kein Oracle nötig.** Das Projekt nutzt H2 (In-Memory-Datenbank), die Oracle-kompatible SQL-Syntax versteht. Die Oracle-Konfiguration ist in `application.properties` auskommentiert vorbereitet.

---

## Anwendung starten

### Option 1: Quarkus Dev-Modus (empfohlen für Entwicklung)

```bash
mvn quarkus:dev
```

Der Dev-Modus bietet:
- **Hot Reload**: Code-Änderungen werden sofort wirksam ohne Neustart
- **Dev UI**: http://localhost:8080/q/dev (Quarkus-Entwicklungswerkzeuge)
- **Swagger UI**: http://localhost:8080/q/swagger-ui (API-Dokumentation & interaktives Testing)

### Option 2: Normaler Start

```bash
mvn package
java -jar target/quarkus-app/quarkus-run.jar
```

### Option 3: Docker

```bash
docker compose up --build
```

Die Anwendung ist dann erreichbar unter:
- **API**: http://localhost:8080/api/employees
- **Swagger UI**: http://localhost:8080/q/swagger-ui

---

## Tests ausführen

### Alle Tests

```bash
mvn test
```

### Nur Unit-Tests (schnell, kein Datenbankstart)

```bash
mvn test -Dtest=EmployeeServiceTest
```

### Nur Integrationstests (startet vollständige Anwendung)

```bash
mvn test -Dtest=EmployeeResourceTest
```

### Ergebnisse

Maven gibt eine Zusammenfassung aus:

```
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## API-Endpunkte

Alle Endpunkte sind auch über Swagger UI (http://localhost:8080/q/swagger-ui) abrufbar und testbar.

### Mitarbeiter abrufen

```bash
# Alle Mitarbeiter
curl http://localhost:8080/api/employees

# Nur aktive Mitarbeiter
curl http://localhost:8080/api/employees/active

# Einzelner Mitarbeiter nach ID
curl http://localhost:8080/api/employees/1

# Suche nach Name
curl "http://localhost:8080/api/employees?search=schmidt"

# Nach Abteilung filtern
curl http://localhost:8080/api/employees/department/IT

# Betriebszugehörigkeit berechnen
curl http://localhost:8080/api/employees/1/service
```

### Mitarbeiter anlegen

```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Max",
    "lastName": "Mustermann",
    "email": "max@beispiel.de",
    "department": "IT",
    "position": "Testautomatisierer",
    "salary": 65000.00,
    "hireDate": "2024-01-15"
  }'
```

### Mitarbeiter aktualisieren

```bash
# Nur die geänderten Felder übermitteln (PATCH-Semantik)
curl -X PUT http://localhost:8080/api/employees/1 \
  -H "Content-Type: application/json" \
  -d '{"salary": 80000.00}'
```

### Mitarbeiter deaktivieren (Soft-Delete)

```bash
# Daten bleiben in der Datenbank – Mitarbeiter wird nur als inaktiv markiert
curl -X DELETE http://localhost:8080/api/employees/2
```

---

## Projektstruktur

```
src/
├── main/java/com/demo/hr/
│   ├── model/
│   │   ├── Employee.java          # JPA-Entität (@Entity, Bean Validation)
│   │   └── Department.java        # Enum für Abteilungen
│   ├── repository/
│   │   └── EmployeeRepository.java # Datenbankzugriff (PanacheRepository)
│   ├── service/
│   │   └── EmployeeService.java   # Fachlogik (@ApplicationScoped, @Transactional)
│   └── resource/
│       └── EmployeeResource.java  # REST-Endpunkte (JAX-RS, @Path, @GET, @POST...)
├── main/resources/
│   ├── application.properties     # Konfiguration (DB, Logging, OpenAPI)
│   └── import.sql                 # Testdaten (6 Mitarbeiter)
└── test/java/com/demo/hr/
    ├── EmployeeServiceTest.java   # Unit-Tests mit Mockito
    └── EmployeeResourceTest.java  # Integrationstests mit REST Assured
```

---

## Architektur-Entscheidungen

### Warum Quarkus statt WildFly/JBoss?

Die Ausschreibung nennt beide. Quarkus ist die modernere Wahl für Cloud-native Entwicklung:
- Startet in unter 1 Sekunde (WildFly: 5-30 Sekunden)
- Minimaler Speicherverbrauch (wichtig für Kubernetes/Cloud)
- Implementiert denselben JakartaEE-Standard (CDI, JPA, JAX-RS) – Code ist weitgehend portierbar

In einer WildFly/JBoss-Umgebung würde derselbe Code mit minimalen Anpassungen laufen.

### Warum H2 statt Oracle für das Demo?

Oracle erfordert eine laufende Datenbankinstanz und Lizenzbedingungen. H2:
- Läuft im Speicher, kein Setup nötig
- Unterstützt Oracle-kompatible SQL-Syntax (`MODE=Oracle`)
- Sequences statt AUTO_INCREMENT (Oracle-Standard) bereits implementiert

Die Produktionskonfiguration für Oracle ist in `application.properties` auskommentiert.

### Warum Soft-Delete?

`DELETE /api/employees/{id}` setzt `active = false` statt den Datensatz zu löschen.
In HR-Systemen ist das der Standard: Gehaltsabrechnungen, Arbeitszeugnisse und
Compliance-Dokumente referenzieren Mitarbeiter-IDs. Ein Hard-Delete würde referenzielle
Integrität verletzen. Hard-Delete ist als separater Admin-Endpunkt `/hard` verfügbar.

### Teststrategie

```
                        ┌─────────────────────────────────┐
Integrationstests       │   EmployeeResourceTest          │  ← REST Assured
(testen Schichten       │   HTTP → Resource → Service     │
zusammen)               │         → Repository → H2 DB   │
                        └─────────────────────────────────┘
                        ┌─────────────────────────────────┐
Unit-Tests              │   EmployeeServiceTest           │  ← JUnit 5 + Mockito
(testen eine Klasse     │   Service (isoliert)            │
in Isolation)           │   Repository = Mock             │
                        └─────────────────────────────────┘
```

Ergänzend (nicht im Demo, aber beschrieben):
- **Selenium / Playwright**: End-to-End-Tests gegen ein Frontend (Browser-Automatisierung)
- **Arquillian**: Container-Tests gegen WildFly/JBoss (Deployment-Tests)
- **JMeter**: Lasttests (parallele HTTP-Anfragen, Durchsatz messen)

---

## Bezug zur Stellenausschreibung

| Anforderung | Umsetzung im Projekt |
|---|---|
| Java, JakartaEE | CDI (`@ApplicationScoped`, `@Inject`), JPA (`@Entity`, `@Table`), JAX-RS (`@Path`, `@GET`) |
| Quarkus | Anwendungsframework, Dev-Modus, Panache ORM |
| SQL / Oracle | H2 mit Oracle-kompatiblem Schema (Sequences), Oracle-Config vorbereitet |
| JUnit | `EmployeeServiceTest` – Unit-Tests mit JUnit 5 |
| Testautomatisierung | `EmployeeResourceTest` – API-Integrationstests mit REST Assured |
| JPA / Hibernate | `Employee.java` (Entity), `EmployeeRepository` (Panache) |
| CDI | `@ApplicationScoped`, `@Inject` in Service und Resource |
| RESTful Services | Vollständige CRUD-API in `EmployeeResource` |
| Docker | `Dockerfile` (Multi-Stage), `docker-compose.yml` |
