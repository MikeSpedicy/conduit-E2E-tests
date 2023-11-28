package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.support.ui.WebDriverWait;

import tests.Login.CodeToExecute;
import tests.Login.LoginMain;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.http.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@FunctionalInterface
interface CreateSelectorInterface {
  String createSelector(JsonObject jsonObject, String keyName, String attrName);
}

interface CheckHeaderStylesInterface {
  void checkHeaderStyles(WebElement element, String text, String href, String color);
}

public class UniversalMethods {
  // UniversalVariables universalVariables = new UniversalVariables();

  public JsonObject ReadSelectorsJSON() {
    JsonObject jsonObject = null;
    try (// Read JSON from a file
        JsonReader jsonReader = Json
            .createReader(new FileReader(
                "../../TestSelectorsList.json"))) {
      jsonObject = jsonReader.readObject();
      jsonReader.close();
      // System.out.println("jsonObject " + jsonObject);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return jsonObject;
  }

  CreateSelectorInterface CreateSelector = (jsonObj, keyName, attrName) -> {
    // Access each property's value using the key
    JsonValue jsonValue = jsonObj.get(keyName);
    // Convert JsonValue to String
    String value = jsonValue.toString();
    // Create element selector
    String selector = "[" + attrName + value + "]";
    return selector;
  };

  public JsonObject UpdateSelectorsList(JsonObject selectorsList, String attrName) {
    JsonObjectBuilder mainJsonObjectBuilder = Json.createObjectBuilder();
    // Get the set of keys (property names)
    Set<String> keys = selectorsList.keySet();

    // Iterate through the properties using the keys
    for (String key : keys) {
      if (selectorsList.get(key) instanceof JsonObject) {
        JsonObject returnedObject = UpdateSelectorsList((JsonObject) selectorsList.get(key), attrName);
        mainJsonObjectBuilder.add(key, returnedObject);
      } else {
        mainJsonObjectBuilder.add(key, CreateSelector.createSelector(selectorsList, key, attrName));
      }
    }
    // Build the updated JsonObject
    JsonObject updatedJsonObject = mainJsonObjectBuilder.build();
    // System.out.println("updatedJsonObject: ");
    // System.out.println(updatedJsonObject);
    return updatedJsonObject;
  }

  public static String encodeURLString(String string, Boolean isURLpath) {
    String encodedUrl;
    try {
      encodedUrl = URLEncoder.encode(string, StandardCharsets.UTF_8);
    } catch (Exception e) {
      e.printStackTrace();
      return "null";
    }
    if (isURLpath) {
      encodedUrl = encodedUrl.replace("+", "%20");
    }
    return encodedUrl;
  }

  public void CheckNavigationHeader(Boolean isAuthorized, Boolean[] openedPage, String userName) {
    WebDriver driver = LoginMain.driver;
    String notOpenedPageColor = "rgba(0, 0, 0, 0.3)";
    String openedPageColor = "rgba(0, 0, 0, 0.8)";
    UniversalVariables universalVariables = UniversalVariables.getInstance();
    JsonObject envVar = universalVariables.getEnvVariableList();

    CheckHeaderStylesInterface CheckHeaderStyles = (element, text, href, color) -> {
      // checks: element's text
      assertEquals(text, element.getText());
      // href attribute and his value
      assertEquals(href, element.getAttribute("href"));
      // text color
      assertEquals(color, element.getCssValue("color"));
      // text font
      assertEquals(text == "conduit" ? "\"titillium web\", sans-serif" : envVar.getString("appMainFontStyles"),
          element.getCssValue("font-family"));
    };

    JsonObject initialSelectorsList = this.ReadSelectorsJSON();
    JsonObject navBarBlock = initialSelectorsList.getJsonObject("multiPagesElements").getJsonObject("navBarBlock");
    JsonObject navBarSel = this.UpdateSelectorsList(navBarBlock, initialSelectorsList.getString("attrName"));
    JsonObject navButtonsJsonObject = navBarSel.getJsonObject("navButtons");

    WebElement logoElement = driver.findElement(By.cssSelector(navBarSel.getString("logoElement")));
    WebElement homeLink = null;
    WebElement newArticleLink = null;
    WebElement settingsLink = null;
    WebElement signInLink = null;
    WebElement signUpLink = null;
    WebElement avatarBlock = null;
    try {// catches the exception and prevents tests from failing
      homeLink = driver.findElement(By.cssSelector(navButtonsJsonObject.getString("home")));
      newArticleLink = driver.findElement(By.cssSelector(navButtonsJsonObject.getString("newArticle")));
      settingsLink = driver.findElement(By.cssSelector(navButtonsJsonObject.getString("settings")));
      avatarBlock = driver.findElement(By.cssSelector(navBarSel.getString("avatarImage")));
    } catch (NoSuchElementException e) {
    }
    try {// devided by 2 parts due to specifics of the try {} catch
      signInLink = driver.findElement(By.cssSelector(navButtonsJsonObject.getString("signIn")));
      signUpLink = driver.findElement(By.cssSelector(navButtonsJsonObject.getString("signUp")));
    } catch (NoSuchElementException e) {
    }

    // Checks that element is displayed and have correspoding styles
    assertTrue(logoElement.isDisplayed());
    CheckHeaderStyles.checkHeaderStyles(logoElement, "conduit", envVar.getString("appUrl"), "rgba(92, 184, 92, 1)");
    if (isAuthorized == true) {
      assertNull(signInLink);
      assertNull(signUpLink);
      // Checks that element is not exist in the DOM
      assertTrue(homeLink.isDisplayed());
      assertTrue(newArticleLink.isDisplayed());
      assertTrue(settingsLink.isDisplayed());
      CheckHeaderStyles.checkHeaderStyles(homeLink, "Home", envVar.getString("appUrl"),
          openedPage[0] == true ? openedPageColor : notOpenedPageColor);
      CheckHeaderStyles.checkHeaderStyles(newArticleLink, " New Article", envVar.getString("newArticlePageUrl"),
          openedPage[1] == true ? openedPageColor : notOpenedPageColor);
      CheckHeaderStyles.checkHeaderStyles(settingsLink, " Settings", envVar.getString("settingsPageUrl"),
          openedPage[2] == true ? openedPageColor : notOpenedPageColor);
      CheckHeaderStyles.checkHeaderStyles(avatarBlock,
          userName, envVar.getString("profilePageUrl") + encodeURLString(userName, true),
          openedPage[3] == true ? openedPageColor : notOpenedPageColor);

    } else {
      assertTrue(signInLink.isDisplayed());
      assertTrue(signUpLink.isDisplayed());
      assertNull(homeLink);
      assertNull(newArticleLink);
      assertNull(settingsLink);
      assertNull(avatarBlock);
      CheckHeaderStyles.checkHeaderStyles(signInLink, "Sign in", envVar.getString("loginPageUrl"),
          openedPage[0] == true ? openedPageColor : notOpenedPageColor);
      CheckHeaderStyles.checkHeaderStyles(signUpLink, "Sign up", envVar.getString("registerPageUrl"),
          openedPage[1] == true ? openedPageColor : notOpenedPageColor);
    }
  }

  public static String getStringFromInputStream(Supplier<InputStream> is) {
    return getStringFromInputStream(is.get()); // <-- Call the method with the InputStream object directly
  }

  private static String getStringFromInputStream(InputStream is) {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    try {
      while ((length = is.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result.toString(StandardCharsets.UTF_8);
  }

  public Map<String, Object> WaitForRequest(String operationName, CodeToExecute appAction, Boolean isPositive) {
    WebDriver driver = LoginMain.driver;
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    CopyOnWriteArrayList<HttpResponse> responses = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<HttpRequest> requests = new CopyOnWriteArrayList<>();
    Map<String, Object> reqRespData = new HashMap<>();
    try (NetworkInterceptor ignored = new NetworkInterceptor(
        driver,
        (Filter) next -> req -> {
          String requestHeader = req.getHeaderNames().toString();
          String requestBody = getStringFromInputStream(req.getContent());
          HttpResponse res = next.execute(req);
          if (req.getUri().contains("/graphql") && requestBody.contains(operationName) && res.getStatus() != 204) {
            System.out.println("requestHeader - " + requestHeader);
            System.out.println("requestBody - " + requestBody);
            String responseHeader = res.getHeaderNames().toString();
            String responseBody = getStringFromInputStream(res.getContent());
            System.out.println("responseHeader - " + responseHeader);
            System.out.println("responseBody - " + responseBody);
            requests.add(req);
            responses.add(res);
            return res;
          } else {
            return next.execute(req);
          }
        })) {
      appAction.execute();
      wait.until(_d -> responses.size() == 1);
    }
    reqRespData.put("request", requests.get(0));
    reqRespData.put("response", responses.get(0));

    return reqRespData;
  }

}