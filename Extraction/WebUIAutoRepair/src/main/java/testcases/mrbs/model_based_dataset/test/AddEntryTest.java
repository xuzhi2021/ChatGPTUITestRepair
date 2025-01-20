package testcases.mrbs.model_based_dataset.test;
import config.DriverConfig;
import org.testng.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.Test;
import testcases.mrbs.model_based_dataset.sql.Area;
import testcases.mrbs.model_based_dataset.sql.Entry;
import testcases.mrbs.model_based_dataset.sql.MRBSConstants;
import testcases.mrbs.model_based_dataset.sql.Room;

public class AddEntryTest {
	private static WebDriver driver;

	@BeforeMethod
	public void setUp() throws Exception {
		System.setProperty("webdriver.chrome.driver",
				DriverConfig.DRIVER_PATH);
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.get(MRBSConstants.BASE_URL);
		Area.addArea("Area New","888");
		Room.addRoom("Room New","10","888");		// Login User Administrator
		Entry.clear();
		Thread.sleep(2000);
		driver.findElement(By.xpath("//input[@value=' Log in ']")).click();
		Thread.sleep(2000);
		driver.findElement(By.name("NewUserName")).clear();
		driver.findElement(By.name("NewUserName")).sendKeys(MRBSConstants.ADMIN_USER_NAME);
		driver.findElement(By.name("NewUserPassword")).clear();
		driver.findElement(By.name("NewUserPassword")).sendKeys(MRBSConstants.ADMIN_PASSWORD);
		driver.findElement(By.xpath("/html/body/form/input[3]")).click();
		Thread.sleep(2000);
	}

	@Test(priority = 0)
	public static void addEntryTest() throws Exception {
		driver.findElement(By.xpath("//a[text()='Area New']")).click();
		Thread.sleep(1000);
		driver.findElement(By.xpath("//a[text()='Go To Today']")).click();
		Thread.sleep(1000);
		List<WebElement> list = driver.findElements(By.xpath("//img[@src='new.gif']"));
		list.get(0).click();
		Thread.sleep(1000);
		driver.findElement(By.name("name")).clear();
		driver.findElement(By.name("name")).sendKeys("Demo Entry");
		driver.findElement(By.name("description")).clear();
		driver.findElement(By.name("description")).sendKeys("Demo Entry");
		driver.findElement(By.name("all_day")).click();
		driver.findElement(By.name("save_button")).click();
		Thread.sleep(2000);
		//try {
			Assert.assertEquals(driver.findElement(By.xpath("//a[@title='Demo Entry']")).getText(), "Demo Entry");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
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
