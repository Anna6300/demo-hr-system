package com.demo.hr;

import com.demo.hr.selenium.pages.EmployeeFormPage;
import com.demo.hr.selenium.pages.EmployeeListPage;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Selenium E2E-Tests — точная копия EmployeeE2eTestPW, но на Selenium WebDriver.
 *
 * Selenium Manager (встроен в Selenium 4.6+) автоматически скачивает ChromeDriver.
 * @QuarkusTest запускает сервер на порту 8081 — оба теста используют одно приложение.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeE2eTestSelenium {

    static WebDriver driver;
    EmployeeListPage listPage;

    @BeforeAll
    static void launchBrowser() {
        ChromeOptions options = new ChromeOptions();
        // Убрать "--headless" чтобы видеть браузер во время теста
        // options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
    }

    @AfterAll
    static void closeBrowser() {
        if (driver != null) driver.quit();
    }

    @BeforeEach
    void openPage() {
        listPage = EmployeeListPage.navigate(driver);
    }

    @Test
    @Order(1)
    @DisplayName("Create employee via form shows success toast")
    void createEmployee_success() {
        listPage.openCreateForm()
                .assertIsVisible()
                .fillFirstName("Selenium")
                .fillLastName("Test")
                .fillEmail("selenium.pw@demo.de")
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
        listPage.deactivateActiveEmployee("Selenium Test")
                .assertToast("deactivated");
        listPage.deleteDiactivatedEmployee("Selenium Test")
                .assertToast("deleted");
    }
}
