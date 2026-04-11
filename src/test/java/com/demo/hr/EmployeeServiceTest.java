package com.demo.hr;

import com.demo.hr.model.Department;
import com.demo.hr.model.Employee;
import com.demo.hr.repository.EmployeeRepository;
import com.demo.hr.service.EmployeeService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für EmployeeService.
 *
 * Strategie: Die Datenbankschicht (EmployeeRepository) wird mit Mockito gemockt,
 * damit Tests ohne echte Datenbank und ohne Transaktionen laufen.
 * Nur die Fachlogik im Service wird geprüft.
 *
 * @QuarkusTest        → Startet den Quarkus CDI-Container für Tests
 * @InjectMock         → Ersetzt die echte Bean durch einen Mockito-Mock im Container
 * @Inject             → Injiziert den Service (mit dem gemockten Repository)
 *
 * JUnit 5-Konzepte:
 *   @Test             → Testmethode
 *   @BeforeEach       → Wird vor jedem Test ausgeführt (Test-Setup)
 *   @DisplayName      → Lesbarer Testname für Berichte
 */
@QuarkusTest
class EmployeeServiceTest {

    @InjectMock
    EmployeeRepository employeeRepository;

    @Inject
    EmployeeService employeeService;

    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        // Test-Fixture: Ein Beispiel-Mitarbeiter für alle Tests
        sampleEmployee = new Employee();
        sampleEmployee.id = 1L;
        sampleEmployee.firstName = "Anna";
        sampleEmployee.lastName = "Schmidt";
        sampleEmployee.email = "a.schmidt@test.de";
        sampleEmployee.department = Department.IT;
        sampleEmployee.position = "Entwicklerin";
        sampleEmployee.salary = new BigDecimal("70000.00");
        sampleEmployee.hireDate = LocalDate.of(2020, 1, 15);
        sampleEmployee.active = true;
    }

    // ─── Tests: getEmployeeById ───────────────────────────────────────────────

    @Test
    @DisplayName("getEmployeeById gibt Mitarbeiter zurück wenn ID existiert")
    void getEmployeeById_found() {
        // Arrange: Mock gibt unseren Testmitarbeiter zurück
        when(employeeRepository.findByIdOptional(1L))
                .thenReturn(Optional.of(sampleEmployee));

        // Act
        Employee result = employeeService.getEmployeeById(1L);

        // Assert: AssertJ für lesbare Fehlermeldungen
        assertThat(result).isNotNull();
        assertThat(result.firstName).isEqualTo("Anna");
        assertThat(result.email).isEqualTo("a.schmidt@test.de");
    }

    @Test
    @DisplayName("getEmployeeById wirft NotFoundException wenn ID nicht existiert")
    void getEmployeeById_notFound() {
        // Arrange
        when(employeeRepository.findByIdOptional(99L))
                .thenReturn(Optional.empty());

        // Assert: Prüfen dass die richtige Exception mit passendem Typ geworfen wird
        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ─── Tests: getYearsOfService ─────────────────────────────────────────────

    @Test
    @DisplayName("getYearsOfService berechnet Betriebszugehörigkeit korrekt")
    void getYearsOfService_correct() {
        // Mitarbeiter eingestellt vor 5 Jahren
        sampleEmployee.hireDate = LocalDate.now().minusYears(5);
        when(employeeRepository.findByIdOptional(1L))
                .thenReturn(Optional.of(sampleEmployee));

        int years = employeeService.getYearsOfService(1L);

        assertThat(years).isEqualTo(5);
    }

    @Test
    @DisplayName("getYearsOfService gibt 0 zurück für Mitarbeiter im ersten Jahr")
    void getYearsOfService_newEmployee() {
        sampleEmployee.hireDate = LocalDate.now().minusMonths(3);
        when(employeeRepository.findByIdOptional(1L))
                .thenReturn(Optional.of(sampleEmployee));

        int years = employeeService.getYearsOfService(1L);

        assertThat(years).isZero();
    }

    // ─── Tests: createEmployee ────────────────────────────────────────────────

    @Test
    @DisplayName("createEmployee schlägt fehl wenn E-Mail bereits vergeben ist")
    void createEmployee_duplicateEmail_throwsConflict() {
        // Arrange: E-Mail ist bereits vergeben (anderer Mitarbeiter)
        Employee existingEmployee = new Employee();
        existingEmployee.id = 99L;
        existingEmployee.email = "a.schmidt@test.de";

        when(employeeRepository.findByEmail("a.schmidt@test.de"))
                .thenReturn(Optional.of(existingEmployee));

        Employee newEmployee = new Employee();
        newEmployee.email = "a.schmidt@test.de";
        newEmployee.firstName = "Anderer";
        newEmployee.lastName = "Nutzer";

        // Assert: Conflict-Exception wird erwartet
        assertThatThrownBy(() -> employeeService.createEmployee(newEmployee))
                .isInstanceOf(WebApplicationException.class)
                .hasMessageContaining("bereits vergeben");
    }

    @Test
    @DisplayName("createEmployee setzt hireDate auf heute wenn nicht angegeben")
    void createEmployee_setsHireDateIfNull() {
        Employee newEmployee = new Employee();
        newEmployee.email = "neu@test.de";
        newEmployee.firstName = "Neu";
        newEmployee.lastName = "Mitarbeiter";
        newEmployee.hireDate = null;  // bewusst nicht gesetzt

        when(employeeRepository.findByEmail("neu@test.de"))
                .thenReturn(Optional.empty());
        doNothing().when(employeeRepository).persist(any(Employee.class));

        employeeService.createEmployee(newEmployee);

        // Hires date sollte automatisch auf heute gesetzt worden sein
        assertThat(newEmployee.hireDate).isEqualTo(LocalDate.now());
    }

    // ─── Tests: deactivateEmployee ────────────────────────────────────────────

    @Test
    @DisplayName("deactivateEmployee setzt active auf false (Soft-Delete)")
    void deactivateEmployee_setsActiveToFalse() {
        when(employeeRepository.findByIdOptional(1L))
                .thenReturn(Optional.of(sampleEmployee));

        assertThat(sampleEmployee.active).isTrue();  // Vorbedingung

        employeeService.deactivateEmployee(1L);

        assertThat(sampleEmployee.active).isFalse();
    }
}
