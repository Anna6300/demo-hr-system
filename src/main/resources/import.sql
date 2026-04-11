-- =============================================================================
-- Testdaten für das Demo HR System
-- Wird beim Anwendungsstart automatisch von Hibernate ausgeführt
-- (gesteuert durch quarkus.hibernate-orm.sql-load-script=import.sql)
-- =============================================================================
-- Hinweis: Hibernate erzeugt das Schema (CREATE TABLE) vor diesem Skript.
-- Dieses Skript fügt nur Daten ein.
-- =============================================================================

INSERT INTO employees (id, first_name, last_name, email, department, position, salary, hire_date, active)
VALUES (1, 'Anna', 'Schmidt', 'a.schmidt@demo-hr.de', 'IT', 'Senior Java Entwicklerin', 75000.00, '2019-03-15', true);

INSERT INTO employees (id, first_name, last_name, email, department, position, salary, hire_date, active)
VALUES (2, 'Thomas', 'Müller', 't.mueller@demo-hr.de', 'HR', 'HR Business Partner', 62000.00, '2021-07-01', true);

INSERT INTO employees (id, first_name, last_name, email, department, position, salary, hire_date, active)
VALUES (3, 'Lisa', 'Weber', 'l.weber@demo-hr.de', 'IT', 'QA Engineer / Testautomatisierung', 68000.00, '2020-01-20', true);

INSERT INTO employees (id, first_name, last_name, email, department, position, salary, hire_date, active)
VALUES (4, 'Klaus', 'Bauer', 'k.bauer@demo-hr.de', 'FINANCE', 'Controller', 70000.00, '2018-11-05', true);

INSERT INTO employees (id, first_name, last_name, email, department, position, salary, hire_date, active)
VALUES (5, 'Maria', 'Fischer', 'm.fischer@demo-hr.de', 'MANAGEMENT', 'Teamleiterin Entwicklung', 90000.00, '2016-04-12', true);

INSERT INTO employees (id, first_name, last_name, email, department, position, salary, hire_date, active)
VALUES (6, 'Jonas', 'Koch', 'j.koch@demo-hr.de', 'IT', 'Junior Entwickler', 48000.00, '2023-09-01', false);

-- Sequence zurücksetzen: import.sql setzt IDs 1-6 explizit (Sequence wurde dabei nicht inkrementiert).
-- Ohne diesen Reset würde Hibernate beim nächsten persist() ID=1 anfordern → Primärschlüssel-Konflikt.
-- ALTER SEQUENCE ... RESTART WITH 7 sagt H2: "Nächster Wert der Sequence ist 7".
-- In Oracle lautet die äquivalente Syntax: ALTER SEQUENCE employee_sequence RESTART START WITH 7;
ALTER SEQUENCE employee_sequence RESTART WITH 7;
