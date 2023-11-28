package tests.Login;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import javax.json.JsonObject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

import tests.UniversalMethods;
import tests.UniversalVariables;

public class LoginMethods {
  UniversalVariables universalVariables = UniversalVariables.getInstance();
  JsonObject usersData = universalVariables.getUsersDataList();
  JsonObject envVar = universalVariables.getEnvVariableList();
  UniversalMethods universalMethods = new UniversalMethods();
  JsonObject initialSelectorsList = universalMethods.ReadSelectorsJSON();
  JsonObject authPages = initialSelectorsList.getJsonObject("authPages");
  JsonObject authPagesSel = universalMethods.UpdateSelectorsList(authPages,
      initialSelectorsList.getString("attrName"));

  void CheckAuthPages(String email, String password, String name) {// Map<String, String> userData
    WebDriver driver = LoginMain.driver;
    WebElement emailInput = driver.findElement(By.cssSelector(authPagesSel.getString("emailInput")));
    WebElement passwordInput = driver.findElement(By.cssSelector(authPagesSel.getString("passwordInput")));

    assertTrue(emailInput.isDisplayed());
    assertTrue(passwordInput.isDisplayed());
    assertEquals(email, emailInput.getAttribute("value"));
    assertEquals(password, passwordInput.getAttribute("value"));
    if (name != null) {
      WebElement nameInput = driver.findElement(By.cssSelector(authPagesSel.getString("nameInput")));
      assertEquals(name, nameInput.getAttribute("value"));
    }
    universalMethods.CheckNavigationHeader(false, new Boolean[] { true, false }, "");
  }

  void Authorization(String email, String password, JsonObject additionalUserData) { // Boolean isPositive
    WebDriver driver = LoginMain.driver;
    WebElement emailInput = driver.findElement(By.cssSelector(authPagesSel.getString("emailInput")));
    WebElement passwordInput = driver.findElement(By.cssSelector(authPagesSel.getString("passwordInput")));
    WebElement submitButton = driver.findElement(By.cssSelector(authPagesSel.getString("submitButton")));

    if (emailInput.getAttribute("value") != "" | passwordInput.getAttribute("value") != "") {
      emailInput.clear();
      passwordInput.clear();
    }
    emailInput.sendKeys(email);
    passwordInput.sendKeys(password);
    // System.out.println("usersData" + usersData);
    Map<String, Object> loginReqData = universalMethods.WaitForRequest("login", () -> submitButton.click(),
        additionalUserData != null);
    HttpRequest requestData = (HttpRequest) loginReqData.get("request");
    HttpResponse responseData = (HttpResponse) loginReqData.get("response");
    String requestBody = UniversalMethods.getStringFromInputStream(requestData.getContent());
    String responseBody = UniversalMethods.getStringFromInputStream(responseData.getContent());
    if (additionalUserData != null) {
      assertTrue(requestBody.contains("\"variables\":{\"email\":\"" + email + "\",\"password\":\"" + password
          + "\"},\"query\":\"query login($email: String!, $password: String!)"));
      assertEquals(200, responseData.getStatus());
      assertTrue(responseBody.contains("\"email\":\"" + email + "\""));
      assertTrue(responseBody.contains("\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."));
      assertTrue(responseBody.contains("\"username\":\"" + additionalUserData.getString("username") + "\""));
      assertTrue(responseBody.contains("\"image\":\"" + additionalUserData.getString("image") + "\""));
      assertTrue(responseBody.contains("\"bio\":\"" + additionalUserData.getString("bio") + "\""));
      assertTrue(responseBody.contains("\"__typename\":\"LoginUserOutput\""));
      universalMethods.CheckNavigationHeader(true, new Boolean[] { true, false, false, false },
          usersData.getJsonObject(universalVariables.firstUserKey).getString("username"));
    } else {
      WebElement errorMessage = driver.findElement(By.cssSelector(authPagesSel.getString("errorMessageBlock")));
      assertTrue(errorMessage.isDisplayed());
      assertEquals(envVar.getString("loginErrorMes"), errorMessage.getText());
      CheckAuthPages(email, password, null);
    }
  }
}