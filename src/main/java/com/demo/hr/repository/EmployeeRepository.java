package com.demo.hr.repository;

import com.demo.hr.model.Department;
import com.demo.hr.model.Employee;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repository für Mitarbeiter-Datenbankzugriffe.
 *
 * PanacheRepository<Employee> stellt bereits folgende Methoden ohne eigenen Code bereit:
 *   - findById(id)       → Optional<Employee>
 *   - listAll()          → List<Employee>
 *   - persist(entity)    → speichert oder aktualisiert
 *   - delete(entity)     → löscht
 *   - count()            → Anzahl aller Einträge
 *   - find("field", val) → Suche per JPQL-Fragment
 *
 * @ApplicationScoped ist eine CDI-Annotation und bedeutet:
 *   Genau eine Instanz dieser Klasse existiert pro Anwendung (Singleton).
 *   Quarkus injiziert diese Instanz per @Inject überall wo sie benötigt wird.
 */
@ApplicationScoped
public class EmployeeRepository implements PanacheRepository<Employee> {

    /**
     * Sucht einen Mitarbeiter anhand seiner E-Mail-Adresse.
     * find() akzeptiert ein JPQL-Fragment (kein vollständiges Query).
     */
    public Optional<Employee> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    /**
     * Gibt alle aktiven Mitarbeiter zurück.
     * Das JPQL-Fragment wird von Panache zu einem vollständigen Query erweitert:
     *   SELECT e FROM Employee e WHERE e.active = ?1
     */
    public List<Employee> findAllActive() {
        return list("active", true);
    }

    /**
     * Gibt alle Mitarbeiter einer bestimmten Abteilung zurück.
     */
    public List<Employee> findByDepartment(Department department) {
        return list("department", department);
    }

    /**
     * Sucht Mitarbeiter anhand von Vor- oder Nachname (case-insensitive).
     * Hier wird ein vollständiges JPQL-Prädikat verwendet, da LOWER() benötigt wird.
     */
    public List<Employee> searchByName(String nameFragment) {
        String pattern = "%" + nameFragment.toLowerCase() + "%";
        return list("LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?1", pattern);
    }
}
