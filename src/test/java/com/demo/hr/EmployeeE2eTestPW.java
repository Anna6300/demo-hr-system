package com.demo.hr;

import com.demo.hr.playwright.pages.EmployeeListPage;
import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

/**
 * E2E-Tests using the Page Object Model pattern.
 *
 * Tests never reference CSS selectors directly — all UI interactions
 * go through EmployeeListPage and EmployeeFormPage.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeE2eTestPW {

    static Playwright playwright;
    static Browser browser;

    EmployeeListPage listPage;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        PlaywrightAssertions.setDefaultAssertionTimeout(5_000);
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void openPage() {
        Page page = browser.newPage();
        listPage = EmployeeListPage.navigate(page);
    }

    @Test
    @Order(1)
    @DisplayName("Create employee via form shows success toast")
    void createEmployee_success() {
        listPage.openCreateForm()
                .assertIsVisible()
                .fillFirstName("Playwright")
                .fillLastName("Test")
                .fillEmail("playwright.pw@demo.de")
                .selectDepartment("IT")
                .fillPosition("QA Engineer")
                .fillSalary(62000)
                .fillHireDate("2024-03-01")
                .submit()
                .assertToast("Employee created")
                .assertIsClosed();
        listPage.clickEditOnFirstRow()
                .assertIsVisible()
                .fillSalary(99000)
                .submit()
                .assertToast("Employee updated");
        listPage.deactivateActiveEmployee("Playwright Test")
                .assertToast("deactivated");
        listPage.deleteDiactivatedEmployee("Playwright Test")
                .assertToast("deleted");
    }
}
