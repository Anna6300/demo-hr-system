package com.demo.hr.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page Object for the Create / Edit employee modal form.
 *
 * All selectors live here — tests never reference raw CSS strings.
 * Methods return {@code this} for fluent chaining:
 *
 *   formPage.fillFirstName("Anna")
 *           .fillLastName("Schmidt")
 *           .fillEmail("a@demo.de")
 *           .submit();
 */
public class EmployeeFormPage {

    private final Page page;

    public EmployeeFormPage(Page page) {
        this.page = page;
    }

    // ── Locators (inline — Playwright locators are lazy, no need to cache) ─────
    private Locator overlay()          { return page.locator(".overlay"); }
    private Locator modal()            { return page.locator(".modal"); }
    private Locator firstNameInput()   { return page.locator("input[name='firstName']"); }
    private Locator lastNameInput()    { return page.locator("input[name='lastName']"); }
    private Locator emailInput()       { return page.locator("input[name='email']"); }
    private Locator departmentSelect() { return page.locator("select[name='department']"); }
    private Locator positionInput()    { return page.locator("input[name='position']"); }
    private Locator salaryInput()      { return page.locator("input[name='salary']"); }
    private Locator hireDateInput()    { return page.locator("input[name='hireDate']"); }
    private Locator saveButton()       { return page.locator(".modal-footer .btn-primary"); }
    private Locator cancelButton()     { return page.locator(".modal-footer .btn-secondary"); }
    private Locator formError()        { return page.locator("#formError"); }

    // ── Fill methods (fluent) ──────────────────────────────────────────────────

    public EmployeeFormPage fillFirstName(String value) {
        firstNameInput().fill(value);
        return this;
    }

    public EmployeeFormPage fillLastName(String value) {
        lastNameInput().fill(value);
        return this;
    }

    public EmployeeFormPage fillEmail(String value) {
        emailInput().fill(value);
        return this;
    }

    public EmployeeFormPage selectDepartment(String department) {
        departmentSelect().selectOption(department);
        return this;
    }

    public EmployeeFormPage fillPosition(String value) {
        positionInput().fill(value);
        return this;
    }

    public EmployeeFormPage fillSalary(double salary) {
        salaryInput().fill(String.valueOf((long) salary));
        return this;
    }

    public EmployeeFormPage fillHireDate(String isoDate) {
        hireDateInput().fill(isoDate);
        return this;
    }

    // ── Composite helper ───────────────────────────────────────────────────────

    /** Fills all required fields in one call. */
    public EmployeeFormPage fillAll(String firstName, String lastName, String email,
                                    String department, String position, double salary) {
        return fillFirstName(firstName)
                .fillLastName(lastName)
                .fillEmail(email)
                .selectDepartment(department)
                .fillPosition(position)
                .fillSalary(salary);
    }

    // ── Actions ────────────────────────────────────────────────────────────────

    /** Clicks Save. Returns {@link EmployeeListPage} for post-submit assertions. */
    public EmployeeListPage submit() {
        saveButton().click();
        return new EmployeeListPage(page);
    }

    public void cancel() {
        cancelButton().click();
    }

    // ── Assertions ─────────────────────────────────────────────────────────────

    public EmployeeFormPage assertIsVisible() {
        assertThat(modal()).isVisible();
        return this;
    }

    public EmployeeFormPage assertIsOpen() {
        assertThat(overlay()).hasClass("open");
        return this;
    }

    public EmployeeFormPage assertIsClosed() {
        assertThat(overlay()).not().hasClass("open");
        return this;
    }

    public EmployeeFormPage assertHasFormError() {
        assertThat(formError()).isVisible();
        assertThat(formError()).not().hasText("");
        return this;
    }

    public EmployeeFormPage assertFormError(String expectedText) {
        assertThat(formError()).isVisible();
        assertThat(formError()).containsText(expectedText);
        return this;
    }
}