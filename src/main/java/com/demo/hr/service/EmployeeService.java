package com.demo.hr.service;

import com.demo.hr.model.Department;
import com.demo.hr.model.Employee;
import com.demo.hr.repository.EmployeeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Service-Schicht für Mitarbeiter-Geschäftslogik.
 *
 * Trennung von Verantwortlichkeiten (Separation of Concerns):
 *   - Resource  → HTTP-Handling (Routen, Status-Codes, Request/Response)
 *   - Service   → Fachlogik (Validierung, Berechnungen, Orchestrierung)
 *   - Repository → Datenbankzugriff (Queries, Persistenz)
 *
 * CDI-Annotationen:
 *   @ApplicationScoped  → eine Instanz pro Anwendung (Thread-safe)
 *   @Inject             → Quarkus injiziert das Repository automatisch
 *   @Transactional      → öffnet eine DB-Transaktion für die Methode;
 *                         bei Exception wird automatisch ein Rollback gemacht
 */
@ApplicationScoped
public class EmployeeService {

    @Inject
    EmployeeRepository employeeRepository;

    // ─── Lesen ────────────────────────────────────────────────────────────────

    public List<Employee> getAllEmployees() {
        return employeeRepository.listAll();
    }

    public List<Employee> getActiveEmployees() {
        return employeeRepository.findAllActive();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Mitarbeiter mit ID " + id + " nicht gefunden"));
    }

    public List<Employee> getEmployeesByDepartment(Department department) {
        return employeeRepository.findByDepartment(department);
    }

    public List<Employee> searchEmployees(String nameFragment) {
        if (nameFragment == null || nameFragment.isBlank()) {
            return getAllEmployees();
        }
        return employeeRepository.searchByName(nameFragment);
    }

    // ─── Schreiben ────────────────────────────────────────────────────────────

    /**
     * Erstellt einen neuen Mitarbeiter.
     *
     * @Transactional stellt sicher, dass die Datenbankoperation atomar ist.
     * Wenn eine Exception auftritt, wird die Transaktion zurückgerollt.
     */
    @Transactional
    public Employee createEmployee(Employee employee) {
        validateEmailUnique(employee.email, null);
        employee.hireDate = employee.hireDate != null ? employee.hireDate : LocalDate.now();
        employeeRepository.persist(employee);
        return employee;
    }

    /**
     * Aktualisiert einen bestehenden Mitarbeiter.
     * Merge-Strategie: Nur nicht-null-Felder werden übernommen.
     */
    @Transactional
    public Employee updateEmployee(Long id, Employee update) {
        Employee existing = getEmployeeById(id);

        if (update.email != null && !update.email.equals(existing.email)) {
            validateEmailUnique(update.email, id);
            existing.email = update.email;
        }
        if (update.firstName != null)  existing.firstName  = update.firstName;
        if (update.lastName != null)   existing.lastName   = update.lastName;
        if (update.department != null) existing.department = update.department;
        if (update.position != null)   existing.position   = update.position;
        if (update.salary != null)     existing.salary     = update.salary;
        if (update.hireDate != null)   existing.hireDate   = update.hireDate;

        // persist() ist hier nicht nötig – Hibernate erkennt die geänderte
        // managed entity innerhalb der Transaktion und schreibt automatisch (dirty checking).
        return existing;
    }

    /**
     * Soft-Delete: Mitarbeiter wird nicht gelöscht, sondern als inaktiv markiert.
     * In HR-Systemen ist das der Standard, um Historien zu bewahren.
     */
    @Transactional
    public void deactivateEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employee.active = false;
    }

    /**
     * Hard-Delete – nur für administrative Zwecke.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }

    // ─── Fachlogik / Berechnungen ─────────────────────────────────────────────

    /**
     * Berechnet die Betriebszugehörigkeit in Jahren.
     * Period.between() ist die korrekte Methode für Datumsberechnungen
     * (berücksichtigt Schaltjahre, unterschiedliche Monatslängen etc.)
     */
    public int getYearsOfService(Long id) {
        Employee employee = getEmployeeById(id);
        return Period.between(employee.hireDate, LocalDate.now()).getYears();
    }

    // ─── Private Hilfsmethoden ────────────────────────────────────────────────

    private void validateEmailUnique(String email, Long excludeId) {
        employeeRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.id.equals(excludeId)) {
                throw new WebApplicationException(
                        "E-Mail '" + email + "' ist bereits vergeben",
                        Response.Status.CONFLICT
                );
            }
        });
    }
}
