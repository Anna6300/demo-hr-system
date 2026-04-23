package com.demo.hr.playwright.pages;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page Object for the main employee list screen.
 *
 * Entry point for all E2E tests — navigate here first, then use
 * {@link #openCreateForm()} to get an {@link EmployeeFormPage}.
 */
public class EmployeeListPage {

    static final String BASE_URL = "http://localhost:8081";

    private final Page page;

    public EmployeeListPage(Page page) {
        this.page = page;
    }

    // ── Locators (inline — Playwright locators are lazy, no need to cache) ─────

    private Locator rows()             { return page.locator("tbody tr"); }
    private Locator newEmployeeBtn()   { return page.locator("button:has-text('+ New Employee')"); }
    private Locator toast()            { return page.locator("#toast"); }

    // ── Navigation ─────────────────────────────────────────────────────────────

    /** Opens the app and waits for the table to finish loading. */
    public static EmployeeListPage navigate(Page page) {
        page.navigate(BASE_URL);
        page.waitForSelector("tbody tr:not(:has(td.loading))");
        return new EmployeeListPage(page);
    }

    // ── Actions ────────────────────────────────────────────────────────────────

    public EmployeeFormPage openCreateForm() {
        newEmployeeBtn().click();
        return new EmployeeFormPage(page);
    }

    public EmployeeFormPage clickEditOnRow(int rowIndex) {
        rows().nth(rowIndex).locator("button:has-text('Edit')").click();
        return new EmployeeFormPage(page);
    }

    public EmployeeFormPage clickEditOnFirstRow() {
        return clickEditOnRow(0);
    }

    public EmployeeListPage deactivateActiveEmployee(String employeeName) {
        page.onDialog(Dialog::accept);

        Locator toast = page.locator("#toast");
        if (toast.isVisible()) {
            toast.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        }

        Locator employeeRow = page.locator("tbody tr")
                .filter(new Locator.FilterOptions()
                        .setHas(page.locator("td").filter(new Locator.FilterOptions().setHasText(employeeName))))
                .first();

        employeeRow.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Deactivate")
        ).click();

        return this;
    }

    public EmployeeListPage deleteDiactivatedEmployee(String employeeName) {


        Locator toast = page.locator("#toast");
        if (toast.isVisible()) {
            toast.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        }

        Locator employeeRow = page.locator("tbody tr")
                .filter(new Locator.FilterOptions()
                        .setHas(page.locator("td").filter(new Locator.FilterOptions().setHasText(employeeName))))
                .first();

        employeeRow.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete")
        ).click();

        return this;
    }


    // ── Assertions ─────────────────────────────────────────────────────────────

    public EmployeeListPage assertToast(String expectedText) {
        assertThat(toast()).isVisible();
        assertThat(toast()).containsText(expectedText);
        return this;
    }

    /**
     * Asserts the create/edit modal is closed (overlay lost the "open" class).
     */
    public void assertIsClosed() {
        assertThat(page.locator(".overlay")).not().hasClass("open");
    }
}
