package com.demo.hr.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium Page Object for the main employee list screen.
 * Mirrors the Playwright EmployeeListPage API — same fluent methods, same return types.
 */
public class EmployeeListPage {

    static final String BASE_URL = "http://localhost:8081";

    private final WebDriver driver;
    private final WebDriverWait wait;

    public EmployeeListPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    // ── Locators ───────────────────────────────────────────────────────────────

    private WebElement newEmployeeBtn()   { return driver.findElement(By.cssSelector("button.btn-primary:not(.modal *)")); }
    private WebElement toast()            { return driver.findElement(By.id("toast")); }
    private List<WebElement> rows()       { return driver.findElements(By.cssSelector("tbody tr")); }
    private WebElement tableBody()        { return driver.findElement(By.cssSelector("tbody")); }

    // ── Navigation ─────────────────────────────────────────────────────────────

    public static EmployeeListPage navigate(WebDriver driver) {
        driver.get(BASE_URL);
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> !d.findElements(By.cssSelector("tbody tr")).isEmpty()
                        && d.findElements(By.cssSelector("td.loading")).isEmpty());
        return new EmployeeListPage(driver);
    }

    // ── Actions ────────────────────────────────────────────────────────────────

    public EmployeeFormPage openCreateForm() {
        newEmployeeBtn().click();
        return new EmployeeFormPage(driver);
    }

    public EmployeeFormPage clickEditOnFirstRow() {
        rows().get(0).findElement(By.cssSelector("button.btn-warning")).click();
        return new EmployeeFormPage(driver);
    }

    public EmployeeListPage deactivateActiveEmployee(String employeeName) {
        waitForToastHidden();
        WebElement row = findRowByName(employeeName);
        row.findElement(By.cssSelector("button.btn-secondary")).click();
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        return this;
    }

    public EmployeeListPage deleteDiactivatedEmployee(String employeeName) {
        waitForToastHidden();
        WebElement row = findRowByName(employeeName);
        row.findElement(By.cssSelector("button.btn-danger")).click();
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        return this;
    }

    // ── Assertions ─────────────────────────────────────────────────────────────

    public EmployeeListPage assertToast(String expectedText) {
        wait.until(ExpectedConditions.visibilityOf(toast()));
        assertThat(toast().getText()).contains(expectedText);
        return this;
    }

    public void assertIsClosed() {
        wait.until(d -> !d.findElement(By.cssSelector(".overlay")).getAttribute("class").contains("open"));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private WebElement findRowByName(String employeeName) {
        return wait.until(d -> rows().stream()
                .filter(r -> r.getText().contains(employeeName))
                .findFirst()
                .orElse(null));
    }

    private void waitForToastHidden() {
        try {
            wait.until(ExpectedConditions.invisibilityOf(toast()));
        } catch (TimeoutException ignored) {
            // toast already hidden or never shown
        }
    }
}
