package testcases.mantisbt.model_based_dataset.test;

import java.util.concurrent.TimeUnit;

import config.DriverConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.AssertJUnit;
import org.testng.annotations.*;
import testcases.Constants;

public class AddNegativeProfileTest {
	private static WebDriver driver;

	@BeforeMethod

	public void setUp() throws Exception {
		System.setProperty("webdriver.chrome.driver",
				DriverConfig.DRIVER_PATH);
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.get(testcases.Constants.getMantisUrl());
		// Login User Administrator
		driver.findElement(By.name("username")).clear();
		driver.findElement(By.name("username")).sendKeys(testcases.Constants.Mantisbt_ADMIN_USER_NAME);
		driver.findElement(By.name("password")).clear();
		driver.findElement(By.name("password")).sendKeys(Constants.Mantisbt_ADMIN_PASSWORD);
		driver.findElement(By.xpath("//input[@value='Login']")).click();
		Thread.sleep(2000);
	}

	@Test(priority = 0)
	public static void addNegativeProfileTest() throws Exception {

		driver.findElement(By.xpath("//a[@href='account_page.php']")).click();
		Thread.sleep(1000);
		driver.findElement(By.xpath("//a[@href='account_prof_menu_page.php']")).click();
		Thread.sleep(2000);

		driver.findElement(By.name("platform")).clear();

		driver.findElement(By.name("os")).clear();
		driver.findElement(By.name("os_build")).clear();
		driver.findElement(By.name("os_build")).sendKeys("V-10");

		driver.findElement(By.xpath("//input[@value='Add Profile']")).click();
		Thread.sleep(5000);
		try {
			AssertJUnit.assertEquals(driver.findElement(By.xpath("//p[@style='color:red']")).getText(),
					"A necessary field \"Platform\" was empty. Please recheck your inputs.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterMethod
	public void close() {
		driver.quit();
	}

	public static void jsClick(WebElement element) {
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
	}

	public boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (org.openqa.selenium.NoSuchElementException e) {
			return false;
		}
	}

}
