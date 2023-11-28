package tests;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class UniversalVariables {
  public Boolean isLocalhostEnv = true;
  String localhostUrl = "http://localhost:4200/#/";
  public String firstUserKey = "firstUserData";
  String demoUrl = "";
  String appUrl = isLocalhostEnv ? localhostUrl : demoUrl;
  private JsonObject envVariables;
  private JsonObject usersData;

  private UniversalVariables() {
    // Build Universal Variables
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("appUrl", appUrl);
    builder.add("loginPageUrl", appUrl + "login");
    builder.add("registerPageUrl", appUrl + "register");
    builder.add("newArticlePageUrl", appUrl + "editor");
    builder.add("settingsPageUrl", appUrl + "settings");
    builder.add("profilePageUrl", appUrl + "profile/");
    builder.add("appMainFontStyles", "\"source sans pro\", sans-serif");
    builder.add("loginErrorMes", "Incorrect Username or Password");
    envVariables = builder.build();

    // Build users data
    JsonObjectBuilder usersDataBuilder = Json.createObjectBuilder();
    JsonObjectBuilder firstUserDataBuilder = Json.createObjectBuilder();
    firstUserDataBuilder.add("email", "first@created.user");
    firstUserDataBuilder.add("password", "Qwerty123");
    firstUserDataBuilder.add("username", "First User");
    firstUserDataBuilder.add("image", "https://api.realworld.io/images/smiley-cyrus.jpeg");
    firstUserDataBuilder.add("bio", "");
    usersDataBuilder.add(firstUserKey, firstUserDataBuilder.build());
    usersData = usersDataBuilder.build();
  }

  private static class SingletonHelper {
    private static final UniversalVariables INSTANCE = new UniversalVariables();
  }

  public static UniversalVariables getInstance() {
    return SingletonHelper.INSTANCE;
  }

  public JsonObject getEnvVariableList() {
    return envVariables;
  }

  public JsonObject getUsersDataList() {
    return usersData;
  }

  public JsonObject updUserData(String userDataKey, String key, String newValue) {
    JsonObjectBuilder usersDataBuilder = Json.createObjectBuilder();
    JsonObjectBuilder userBuilder = Json.createObjectBuilder();
    // Iterates over the original properties
    usersData.forEach((dataKey1, value1) -> {
      if (dataKey1 == userDataKey) {
        ((JsonObject) value1).forEach((dataKey2, value2) -> {
          if (dataKey2.equals(key)) {
            // If it's the property to update, uses the new value
            userBuilder.add(key, newValue);
          } else {
            // Otherwise, uses the existing value
            userBuilder.add(dataKey2, value2);
          }
        });
        usersDataBuilder.add(dataKey1, userBuilder.build());
      } else {
        usersDataBuilder.add(dataKey1, value1);
      }
    });
    JsonObject updUserData = usersDataBuilder.build();
    usersData = updUserData;
    return updUserData;
  }
}