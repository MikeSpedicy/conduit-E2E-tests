package tests.Login;

import javax.json.JsonObject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import tests.UniversalMethods;
import tests.UniversalVariables;

public class LoginMain {
    UniversalMethods universalMethods = new UniversalMethods();
    LoginMethods loginMethods = new LoginMethods();
    UniversalVariables universalVariables = UniversalVariables.getInstance();
    JsonObject envVar = universalVariables.getEnvVariableList();
    JsonObject usersData = universalVariables.getUsersDataList();
    JsonObject firstUserData = usersData.getJsonObject(universalVariables.firstUserKey);
    public static WebDriver driver;

    @BeforeAll
    static void setUp() {
        // Launch Chrome browser
        driver = new ChromeDriver();
    }

    @BeforeEach
    void each() {
        driver.get(envVar.getString("loginPageUrl"));
    }

    @Test
    @DisplayName("Check Login Page")
    void CheckLoginPage() {
        loginMethods.CheckAuthPages("", "", null);
    }

    @Test
    @DisplayName("Authorization - Positive")
    void AuthPos() {
        JsonObject updUsersData = universalVariables.updUserData(universalVariables.firstUserKey, "bio", "123");
        JsonObject updFirstUserData = updUsersData.getJsonObject(universalVariables.firstUserKey);
        System.out.println("updFirstUserData - " + updFirstUserData);
        loginMethods.Authorization(firstUserData.getString("email"), firstUserData.getString("password"),
                updFirstUserData);
    }

    @Test
    @DisplayName("Authorization - Negative - Incorrect password & correct email")
    void AuthNeg1() {
        loginMethods.Authorization(firstUserData.getString("email"),
                firstUserData.getString("password") + "a", null);
    }

    @Test
    @DisplayName("Authorization - Negative - Incorrect email & correct password")
    void AuthNeg2() {
        loginMethods.Authorization(firstUserData.getString("email") + "a",
                firstUserData.getString("password"), null);
    }

    @Test
    @DisplayName("Authorization - Negative - Unexisting but valid email and password")
    void AuthNeg3() {
        loginMethods.Authorization(firstUserData.getString("email") + "a",
                firstUserData.getString("password") + "a", null);
    }

    @Test
    @DisplayName("Authorization - Negative - Unexisting and invalid email and password")
    void AuthNeg4() {
        loginMethods.Authorization("a", "a", null);
    }

    @Test
    @DisplayName("Authorization - Negative - Empty email and password")
    void AuthNeg5() {
        loginMethods.Authorization("", "", null);
    }

    @Test
    @DisplayName("Authorization - Negative - Empty email and filled password")
    void AuthNeg6() {
        loginMethods.Authorization("", "a", null);
    }

    @Test
    @DisplayName("Authorization - Negative - Filled email and empty password")
    void AuthNeg7() {
        loginMethods.Authorization("a", "", null);
    }

    @AfterEach
    void postTestConditions() {
        // makes basic clearing of the page's state
        driver.navigate().refresh();
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
