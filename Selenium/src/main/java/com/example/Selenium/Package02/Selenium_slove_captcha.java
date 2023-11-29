package com.example.Selenium.Package02;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class Selenium_slove_captcha {

    public ResponseEntity test(@RequestParam Map<String, String> params, String user_name, String user_password) throws InterruptedException, IOException {
        List<WebElement> element_solve;
        JavascriptExecutor js;
        WebElement Element_inputText;
        WebElement webElement;

        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("webdriver.chrome.driver", "E:\\CongViecHocTap\\ChromeDriver\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("useAutomationExtension", false); // disable chrome running as automation
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation")); // disable chrome running as automation
        WebDriver driver = new ChromeDriver(options);

        CountDownLatch latch = new CountDownLatch(2);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2)); // số giây mà 1 driver chờ để load 1 phần tử nếu không có thiết lập của wait
        driver.manage().window().maximize();

        System.out.println("-----------------------------\n" + params.get("Text") + " " + params.get("Voice") + " " + params.get("FileName"));

        Thread checkFileName = new Thread(new CheckFileName(params.get("FileName"), latch));
        Thread checkText = new Thread(new CheckText(params.get("Text"), latch));

        checkFileName.start();
        checkText.start();

        latch.await();

        driver.get("https://ttsfree.com/login");

        driver.findElement(By.xpath("//input[@placeholder='Username']")).sendKeys(user_name);
        driver.findElement(By.xpath("//input[@placeholder='Enter password']")).sendKeys(user_password);

        latch = new CountDownLatch(2); // thiết lập 2 Thread ( trường hợp sau khi send key password sẽ có 1 trong 2 hiển thị nên thiết lập 2 thread kiểm tra cùng 1 lúc )

        Thread threadCheckESC = new Thread(new CheckESC(driver, latch, null));
        Thread threadCheckHandAD = new Thread(new CheckHandAD(driver, latch, null));

        threadCheckESC.start();
        threadCheckHandAD.start();

        latch.await();

        driver.findElement(By.xpath("//ins[@class='iCheck-helper']")).click();
        driver.findElement(By.xpath("//input[@id='btnLogin']")).click();

        Thread.sleep(2000);
        driver.get("https://ttsfree.com/vn"); //Chuyển vùng sang việt nam ( né được những bước không cần thiết như tùy chỉnh giọng nói theo nước )
        String blockImagesScript = "var images = document.getElementsByTagName('img'); " +
                "for (var i = 0; i < images.length; i++) { " +
                "   images[i].setAttribute('src', ''); " +
                "}";
        ((JavascriptExecutor) driver).executeScript(blockImagesScript);

        js = (JavascriptExecutor) driver;

        Element_inputText = driver.findElement(By.xpath("//*[@id=\"input_text\"]"));
        js.executeScript("arguments[0].scrollIntoView();", Element_inputText);

        driver.findElement(By.xpath("//textarea[@id='input_text']")).sendKeys(params.get("Text"));

        if (params.get("Voice").equals("Female")) {
            driver.findElement(
                            By.xpath("//div[@id='voice_name_bin']//div[@class='form-check icheck-info text-left item_voice item_voice_selected']"))
                    .click();
        } else if (params.get("Voice").equals("Male")) {
//            driver.findElement(
//                            By.xpath("//div[@class='form-check icheck-info text-left item_voice']"))
//                    .click();
            driver.findElement(
                            By.xpath("(//label[@for='radioPrimaryvi-VN2'])[1]"))
                    .click();
        }

        driver.findElement(By.xpath("//a[contains(text(),'Tạo Voice')]")).click();

        return ResponseEntity.ok(new String("Downloaded successfully"));
    }
}
