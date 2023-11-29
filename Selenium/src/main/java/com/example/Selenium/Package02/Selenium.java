package com.example.Selenium.Package02;

import com.example.Selenium.Package03.CaptchaSolove_bot;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.Selenium.Package02.CheckFileName.flag_checkFileName;
import static com.example.Selenium.Package02.CheckText.flag_checkText;
import static com.example.Selenium.Package02.CheckText.notification;

@RestController
@RequestMapping("/api/web")
public class Selenium {

    //private static AtomicInteger count = new AtomicInteger();

    @GetMapping("/ttsfree_captcha_noForLoop_thread2")
    public ResponseEntity<?> ttsfree_captcha_noForLoop_Threads(@RequestParam Map<String, String> params) throws InterruptedException, IOException {
        WebDriverWait wait;
        List<WebElement> element_solve;
        String user_name = "nam02test"; // mô phỏng tên user
        String user_password = "IUtrangmaimai02"; // mô phỏng password user
        JavascriptExecutor js;
        WebElement Element_inputText;
        WebElement webElement;

        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("webdriver.chrome.driver", "E:\\CongViecHocTap\\ChromeDriver\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("useAutomationExtension", false); // disable chrome running as automation
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation")); // disable chrome running as automation
        options.addArguments("--blink-settings=imagesEnabled=false"); // block tất cả hình ảnh -> tăng tốc độ load website

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

        if (flag_checkFileName == false) {
            flag_checkFileName = true;
            driver.close();
            return ResponseEntity.ok(new String("Tên File bị trùng trong dữ liệu của bạn , hãy đổi tên khác hoặc xóa file cũ của bạn"));
        }
        if (flag_checkText == false) {
            flag_checkText = true;
            driver.close();
            return ResponseEntity.ok(new String(notification));
        }

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


        element_solve = driver.findElements(By.xpath("//*[@id=\"frm_login\"]/div[2]/div/font"));
        if (element_solve.size() > 0 && element_solve.get(0).isDisplayed()) {
            webElement = driver.findElement(By.xpath("//*[@id=\"frm_login\"]/div[2]/div/font"));
            String notification = webElement.getText();
            driver.close();
            return ResponseEntity.ok(new String(notification));
        } else {
            driver.get("https://ttsfree.com/vn"); //Chuyển vùng sang việt nam ( né được những bước không cần thiết như tùy chỉnh giọng nói theo nước )

        }

        js = (JavascriptExecutor) driver;

        Element_inputText = driver.findElement(By.xpath("//*[@id=\"input_text\"]"));
        js.executeScript("arguments[0].scrollIntoView();", Element_inputText);

        driver.findElement(By.xpath("//textarea[@id='input_text']")).sendKeys(params.get("Text"));

        if (params.get("Voice").equals("Female")) {
            driver.findElement(
                            By.xpath("//div[@id='voice_name_bin']//div[@class='form-check icheck-info text-left item_voice item_voice_selected']"))
                    .click();
        } else if (params.get("Voice").equals("Male")) {
            driver.findElement(
                            By.xpath("//div[@class='form-check icheck-info text-left item_voice']"))
                    .click();
        }

        driver.findElement(By.xpath("//a[contains(text(),'Tạo Voice')]")).click();

        /**
         * sau khi bấm nút tạo voice , sẽ có 2 quảng cáo làm che mất các element cần phải thao tác xuất hiện cùng 1 lúc -> giải quyết bằng cách
         * tạo 2 thread 1 lúc cùng bấm sẽ không chính xác vì cả 2 cùng bấm mà 1 trong 2 chưa tắt sẽ bấm đè lên nhau
         * nên giải quyết bằng cách bấm từng thằng 1
         */
        latch = new CountDownLatch(1);
        Thread threadCheckAdsTOP_ESC = new Thread(new CheckAdsTOP_ESC(driver, latch, null));
        threadCheckAdsTOP_ESC.start();
        latch.await();

        try {
            wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='alert alert-danger alert-dismissable']"))).isDisplayed();
        } catch (Exception e) {
            driver.findElement(By.xpath("//*[@id=\"progessResults\"]/div[2]/center[1]/div/a")).click(); // nút tải xuống
        }

        latch = new CountDownLatch(2);
        Thread threadCheckHostAD = new Thread(new CheckHostAD(driver, latch));
        Thread threadCheckAdSpecial = new Thread(new CheckAdSpecial(driver, latch));
        threadCheckHostAD.start();
        threadCheckAdSpecial.start();
        latch.await();

        driver.close();

        /**
         * đổi tên file theo yêu cầu user ( đơn luồng thì hoạt động oke , đa luồng thì lỗi -> đang nghiên cứu login 1 lúc có request cùng đổi để đảm bảo không có lỗi xảy ra
         * đang nghiên cứu để update
         */
//        File folder = new File("E:\\New folder");
//        File[] files = folder.listFiles();
//        if (files != null && files.length > 0) {
//            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
//            File latestFile = files[0];
//            System.out.println(latestFile.getName());
//            String newFileName = params.get("FileName") + ".mp3";
//            File newFile = new File(folder, newFileName);
//            latestFile.renameTo(newFile);
//        }

        return ResponseEntity.ok(new String("Downloaded successfully"));
    }


    @GetMapping("/test")
    public ResponseEntity<?> test(@RequestParam Map<String, String> params) throws InterruptedException, IOException {
        WebDriverWait wait;
        List<WebElement> element_solve;
        String user_name = "nam02test"; // mô phỏng tên user
        String user_password = "IUtrangmaimai02"; // mô phỏng password user
        JavascriptExecutor js;
        WebElement Element_inputText;
        WebElement webElement;

        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("webdriver.chrome.driver", "E:\\CongViecHocTap\\ChromeDriver\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("useAutomationExtension", false); // disable chrome running as automation
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation")); // disable chrome running as automation
        options.addArguments("--blink-settings=imagesEnabled=false"); // block tất cả hình ảnh -> tăng tốc độ load website

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

        if (flag_checkFileName == false) {
            flag_checkFileName = true;
            driver.close();
            return ResponseEntity.ok(new String("Tên File bị trùng trong dữ liệu của bạn , hãy đổi tên khác hoặc xóa file cũ của bạn"));
        }
        if (flag_checkText == false) {
            flag_checkText = true;
            driver.close();
            return ResponseEntity.ok(new String(notification));
        }

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


        element_solve = driver.findElements(By.xpath("//*[@id=\"frm_login\"]/div[2]/div/font"));
        if (element_solve.size() > 0 && element_solve.get(0).isDisplayed()) {
            webElement = driver.findElement(By.xpath("//*[@id=\"frm_login\"]/div[2]/div/font"));
            String notification = webElement.getText();
            driver.close();
            return ResponseEntity.ok(new String(notification));
        } else {
            driver.get("https://ttsfree.com/vn"); //Chuyển vùng sang việt nam ( né được những bước không cần thiết như tùy chỉnh giọng nói theo nước )

        }

        js = (JavascriptExecutor) driver;

        Element_inputText = driver.findElement(By.xpath("//*[@id=\"input_text\"]"));
        js.executeScript("arguments[0].scrollIntoView();", Element_inputText);

        driver.findElement(By.xpath("//textarea[@id='input_text']")).sendKeys(params.get("Text"));

        if (params.get("Voice").equals("Female")) {
            driver.findElement(
                            By.xpath("//div[@id='voice_name_bin']//div[@class='form-check icheck-info text-left item_voice item_voice_selected']"))
                    .click();
        } else if (params.get("Voice").equals("Male")) {
            driver.findElement(
                            By.xpath("//div[@class='form-check icheck-info text-left item_voice']"))
                    .click();
        }

        driver.findElement(By.xpath("//a[contains(text(),'Tạo Voice')]")).click();

        /**
         * sau khi bấm nút tạo voice , sẽ có 2 quảng cáo làm che mất các element cần phải thao tác xuất hiện cùng 1 lúc -> giải quyết bằng cách
         * tạo 2 thread 1 lúc cùng bấm sẽ không chính xác vì cả 2 cùng bấm mà 1 trong 2 chưa tắt sẽ bấm đè lên nhau
         * nên giải quyết bằng cách bấm từng thằng 1
         */
        latch = new CountDownLatch(1);
        Thread threadCheckAdsTOP_ESC = new Thread(new CheckAdsTOP_ESC(driver, latch, null));
        threadCheckAdsTOP_ESC.start();
        latch.await();

        try {
            wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='alert alert-danger alert-dismissable']"))).isDisplayed();
        } catch (Exception e) {
            driver.findElement(By.xpath("//*[@id=\"progessResults\"]/div[2]/center[1]/div/a")).click(); // nút tải xuống
        }

        latch = new CountDownLatch(2);
        Thread threadCheckHostAD = new Thread(new CheckHostAD(driver, latch));
        Thread threadCheckAdSpecial = new Thread(new CheckAdSpecial(driver, latch));
        threadCheckHostAD.start();
        threadCheckAdSpecial.start();
        latch.await();

        driver.close();

        /**
         * đổi tên file theo yêu cầu user ( đơn luồng thì hoạt động oke , đa luồng thì lỗi -> đang nghiên cứu login 1 lúc có request cùng đổi để đảm bảo không có lỗi xảy ra
         * đang nghiên cứu để update
         */
//        File folder = new File("E:\\New folder");
//        File[] files = folder.listFiles();
//        if (files != null && files.length > 0) {
//            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
//            File latestFile = files[0];
//            System.out.println(latestFile.getName());
//            String newFileName = params.get("FileName") + ".mp3";
//            File newFile = new File(folder, newFileName);
//            latestFile.renameTo(newFile);
//        }

        return ResponseEntity.ok(new String("Downloaded successfully"));
    }


    @GetMapping("/test2")
    public ResponseEntity<?> test2(@RequestParam Map<String, String> params) throws InterruptedException, IOException {
        WebDriverWait wait;
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

        driver.get("https://ttsfree.com/vn");

//        List<WebElement> images = driver.findElements(By.tagName("img"));
//        for (WebElement image : images) {
//            ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('src', '')", image);
//        }

        String blockImagesScript = "var images = document.getElementsByTagName('img'); " +
                "for (var i = 0; i < images.length; i++) { " +
                "   images[i].setAttribute('src', ''); " +
                "}";
        ((JavascriptExecutor) driver).executeScript(blockImagesScript);


        CaptchaSolove_bot captchaSoloveBot = new CaptchaSolove_bot();


        return ResponseEntity.ok(new String("Downloaded successfully"));
    }
}



