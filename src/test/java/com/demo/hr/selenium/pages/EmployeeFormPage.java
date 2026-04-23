package com.demo.hr.selenium.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium Page Object for the Create / Edit employee modal form.
 * Mirrors the Playwright EmployeeFormPage API — same fluent methods, same return types.
 */
public class EmployeeFormPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public EmployeeFormPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    // ── Locators ───────────────────────────────────────────────────────────────

    private WebElement modal()            { return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal"))); }
    private WebElement overlay()          { return driver.findElement(By.cssSelector(".overlay")); }
    private WebElement firstNameInput()   { return driver.findElement(By.cssSelector("input[name='firstName']")); }
    private WebElement lastNameInput()    { return driver.findElement(By.cssSelector("input[name='lastName']")); }
    private WebElement emailInput()       { return driver.findElement(By.cssSelector("input[name='email']")); }
    private WebElement positionInput()    { return driver.findElement(By.cssSelector("input[name='position']")); }
    private WebElement salaryInput()      { return driver.findElement(By.cssSelector("input[name='salary']")); }
    private WebElement hireDateInput()    { return driver.findElement(By.cssSelector("input[name='hireDate']")); }
    private WebElement saveButton()       { return driver.findElement(By.cssSelector(".modal-footer .btn-primary")); }
    private WebElement cancelButton()     { return driver.findElement(By.cssSelector(".modal-footer .btn-secondary")); }
    private WebElement formError()        { return driver.findElement(By.id("formError")); }

    // ── Fill methods (fluent) ──────────────────────────────────────────────────

    public EmployeeFormPage fillFirstName(String value) {
        firstNameInput().clear();
        firstNameInput().sendKeys(value);
        return this;
    }

    public EmployeeFormPage fillLastName(String value) {
        lastNameInput().clear();
        lastNameInput().sendKeys(value);
        return this;
    }

    public EmployeeFormPage fillEmail(String value) {
        emailInput().clear();
        emailInput().sendKeys(value);
        return this;
    }

    public EmployeeFormPage selectDepartment(String department) {
       clickButton("department");
       selectOption(department);
        return this;
    }

    public EmployeeFormPage selectOption(String option) {
        WebElement dropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("department"))
        );

        Select select = new Select(dropdown);
        select.selectByVisibleText(option);

        return this;
    }

    public EmployeeFormPage clickButton(String buttonName) {
        WebElement button = wait.until(
                ExpectedConditions.elementToBeClickable(By.name(buttonName))
        );
        button.click();
        return this;
    }

    public EmployeeFormPage fillPosition(String value) {
        positionInput().clear();
        positionInput().sendKeys(value);
        return this;
    }

    public EmployeeFormPage fillSalary(double salary) {
        WebElement el = salaryInput();
        el.clear();
        el.sendKeys(String.valueOf((long) salary));
        return this;
    }

    public EmployeeFormPage fillHireDate(String isoDate) {
        // JS set needed — Selenium sendKeys on date inputs is browser-dependent
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1]", hireDateInput(), isoDate);
        return this;
    }

    // ── Composite helper ───────────────────────────────────────────────────────

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

    public EmployeeListPage submit() {
        saveButton().click();
        return new EmployeeListPage(driver);
    }

    public void cancel() {
        cancelButton().click();
    }

    // ── Assertions ─────────────────────────────────────────────────────────────

    public EmployeeFormPage assertIsVisible() {
        modal(); // wait.until(visibilityOf) already asserts it is visible
        return this;
    }

    public EmployeeFormPage assertIsOpen() {
        assertThat(overlay().getAttribute("class")).contains("open");
        return this;
    }

    public EmployeeFormPage assertIsClosed() {
        wait.until(d -> !overlay().getAttribute("class").contains("open"));
        return this;
    }

    public EmployeeFormPage assertHasFormError() {
        wait.until(ExpectedConditions.visibilityOf(formError()));
        assertThat(formError().getText()).isNotBlank();
        return this;
    }

    public EmployeeFormPage assertFormError(String expectedText) {
        wait.until(ExpectedConditions.visibilityOf(formError()));
        assertThat(formError().getText()).contains(expectedText);
        return this;
    }
}
