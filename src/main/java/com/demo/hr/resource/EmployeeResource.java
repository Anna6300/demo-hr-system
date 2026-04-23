package com.demo.hr.resource;

import com.demo.hr.model.Department;
import com.demo.hr.model.Employee;
import com.demo.hr.service.EmployeeService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * JAX-RS REST-Resource für Mitarbeiter-Endpunkte.
 *
 * JAX-RS ist Teil des JakartaEE-Standards und definiert REST-APIs deklarativ
 * über Annotationen:
 *   @Path          → URL-Pfad der Ressource
 *   @GET/POST/...  → HTTP-Methode
 *   @Produces      → Antwort-Format (hier JSON)
 *   @Consumes      → Erwartetes Request-Format
 *   @PathParam     → Pfadvariable aus der URL
 *   @QueryParam    → URL-Parameter (?name=...)
 *   @Valid         → Löst Bean-Validation vor der Methode aus
 *
 * Die Resource delegiert alle Fachlogik an den EmployeeService.
 * Hier wird nur HTTP-spezifische Logik (Status-Codes, Location-Header etc.) behandelt.
 *
 * API-Übersicht:
 *   GET    /api/employees              → Alle Mitarbeiter (optional ?search=...)
 *   GET    /api/employees/active       → Nur aktive Mitarbeiter
 *   GET    /api/employees/{id}         → Einzelner Mitarbeiter
 *   GET    /api/employees/{id}/service → Betriebszugehörigkeit in Jahren
 *   GET    /api/employees/department/{dept} → Nach Abteilung
 *   POST   /api/employees              → Neuen Mitarbeiter anlegen
 *   PUT    /api/employees/{id}         → Mitarbeiter aktualisieren
 *   DELETE /api/employees/{id}         → Soft-Delete (deaktivieren)
 *   DELETE /api/employees/{id}/hard    → Hard-Delete
 */
@ApplicationScoped
@Path("/api/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Mitarbeiter", description = "CRUD-Operationen für Mitarbeiterverwaltung")
public class EmployeeResource {

    @Inject
    EmployeeService employeeService;

    // ─── GET Endpunkte ─────────────────────────────────────────────────────────

    @GET
    @Operation(summary = "Alle Mitarbeiter abrufen",
               description = "Gibt alle Mitarbeiter zurück. Optional mit Namenssuche per first name")
    public List<Employee> getAll(@QueryParam("search") String search) {
        return employeeService.searchEmployees(search);
    }

    @GET
    @Path("/active")
    @Operation(summary = "Nur aktive Mitarbeiter")
    public List<Employee> getActive() {
        return employeeService.getActiveEmployees();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Mitarbeiter nach ID")
    public Employee getById(@PathParam("id") Long id) {
        return employeeService.getEmployeeById(id);
    }

    @GET
    @Path("/{id}/service")
    @Operation(summary = "Betriebszugehörigkeit in Jahren berechnen")
    public Response getYearsOfService(@PathParam("id") Long id) {
        int years = employeeService.getYearsOfService(id);
        return Response.ok(Map.of("employeeId", id, "yearsOfService", years)).build();
    }

    @GET
    @Path("/department/{department}")
    @Operation(summary = "Mitarbeiter nach Abteilung")
    public List<Employee> getByDepartment(@PathParam("department") Department department) {
        return employeeService.getEmployeesByDepartment(department);
    }

    // ─── POST ──────────────────────────────────────────────────────────────────

    @POST
    @Operation(summary = "Neuen Mitarbeiter anlegen")
    public Response create(@Valid Employee employee) {
        Employee created = employeeService.createEmployee(employee);
        // HTTP 201 Created mit Location-Header auf die neue Ressource
        URI location = URI.create("/api/employees/" + created.id);
        return Response.created(location).entity(created).build();
    }

    // ─── PUT ───────────────────────────────────────────────────────────────────

    @PUT
    @Path("/{id}")
    @Operation(summary = "Mitarbeiter aktualisieren (PATCH-Semantik: nur übermittelte Felder)")
    public Employee update(@PathParam("id") Long id, Employee update) {
        return employeeService.updateEmployee(id, update);
    }

    // ─── DELETE ────────────────────────────────────────────────────────────────

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Mitarbeiter deaktivieren (Soft-Delete – Daten bleiben erhalten)")
    public Response deactivate(@PathParam("id") Long id) {
        employeeService.deactivateEmployee(id);
        return Response.noContent().build();  // HTTP 204 No Content
    }

    @DELETE
    @Path("/{id}/hard")
    @Operation(summary = "Mitarbeiter endgültig löschen (Hard-Delete)")
    public Response delete(@PathParam("id") Long id) {
        employeeService.deleteEmployee(id);
        return Response.noContent().build();
    }
}
