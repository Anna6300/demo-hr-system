package com.demo.hr.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA-Entität für einen Mitarbeiter.
 *
 * Erbt von PanacheEntity (Quarkus-Aufsatz auf Hibernate), was automatisch
 * eine generierte ID sowie statische Methoden wie find(), listAll(), persist()
 * bereitstellt – ähnlich dem Active-Record-Muster.
 *
 * Die Annotationen folgen dem JakartaEE-Standard:
 *   @Entity, @Table, @Column, @Enumerated  → JPA (Jakarta Persistence)
 *   @NotBlank, @Email, @Size, @DecimalMin  → Bean Validation (Jakarta Validation)
 *
 * Datenbankschema (Oracle-kompatibel):
 *   - SEQUENCE statt AUTO_INCREMENT, da Oracle kein AUTO_INCREMENT kennt
 *   - Alle Spaltenlängen und Präzisionen explizit definiert
 */
@Entity
@Table(name = "employees")
@SequenceGenerator(name = "employee_gen", sequenceName = "employee_sequence", allocationSize = 1)
public class Employee extends PanacheEntityBase {

    // Überschreibt PanacheEntity's @GeneratedValue mit einem expliziten Sequence-Namen.
    // allocationSize = 1: Sequence erhöht sich um genau 1 pro Insert → IDs sind vorhersagbar.
    // In Produktion (Oracle) gilt dieselbe Konfiguration – Sequence ist Oracle-nativ.
    //
    // Ohne diese Deklaration: Hibernate wählt einen internen Sequence-Namen und nutzt
    // allocationSize=50, wodurch import.sql-Inserts mit manuell gesetzten IDs zu
    // Primärschlüssel-Konflikten führen (Sequence startet bei 1, ID 1 bereits vergeben).
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_gen")
    public Long id;

    @NotBlank(message = "Vorname darf nicht leer sein")
    @Size(max = 100, message = "Vorname darf max. 100 Zeichen haben")
    @Column(name = "first_name", nullable = false, length = 100)
    public String firstName;

    @NotBlank(message = "Nachname darf nicht leer sein")
    @Size(max = 100, message = "Nachname darf max. 100 Zeichen haben")
    @Column(name = "last_name", nullable = false, length = 100)
    public String lastName;

    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "Ungültige E-Mail-Adresse")
    @Column(unique = true, nullable = false, length = 255)
    public String email;

    @NotNull(message = "Abteilung muss angegeben werden")
    @Enumerated(EnumType.STRING)   // Speichert "IT", "HR" etc. statt 0, 1 ...
    @Column(nullable = false, length = 50)
    public Department department;

    @NotBlank(message = "Position darf nicht leer sein")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    public String position;

    @NotNull(message = "Gehalt muss angegeben werden")
    @DecimalMin(value = "0.01", message = "Gehalt muss positiv sein")
    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal salary;

    @NotNull(message = "Einstellungsdatum muss angegeben werden")
    @PastOrPresent(message = "Einstellungsdatum darf nicht in der Zukunft liegen")
    @Column(name = "hire_date", nullable = false)
    public LocalDate hireDate;

    @Column(nullable = false)
    public boolean active = true;

    // Kein expliziter Konstruktor nötig – Panache und JPA brauchen nur den Default-Konstruktor,
    // der vom Compiler automatisch erzeugt wird, solange kein anderer definiert ist.

    /**
     * Gibt den vollständigen Namen zurück.
     * Utility-Methode direkt an der Entität – erlaubt im Domain-Modell Logik zu kapseln.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
