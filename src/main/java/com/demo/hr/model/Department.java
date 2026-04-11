package com.demo.hr.model;

/**
 * Enum für Abteilungen im HR-System.
 *
 * In einem realen System wäre dies eine eigene Datenbanktabelle mit einer
 * @ManyToOne-Beziehung in Employee. Für dieses Demo genügt ein Enum.
 */
public enum Department {
    IT,
    HR,
    FINANCE,
    LEGAL,
    OPERATIONS,
    MANAGEMENT
}