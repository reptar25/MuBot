package mubot.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.Date;

public class AccessToken {

    private static final String CLIENT_ID = System.getenv("API_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("API_CLIENT_SECRET");
    private static final String AUDIENCE = "https://mu--botapi.herokuapp.com/";
    private final String ACCESS_TOKEN;
    private final String TOKEN_TYPE;
    private final Date EXPIRY_DATE;

    public AccessToken(String token, String type, int expiresInSeconds) {
        ACCESS_TOKEN = token;
        TOKEN_TYPE = type;
        EXPIRY_DATE = new Date(System.currentTimeMillis() + (expiresInSeconds * 1000L));
    }

    public static AccessToken createAccessToken() {
        try {
            HttpResponse<String> response = Unirest.post("https://bitter-sun-4965.us.auth0.com/oauth/token")
                    .header("content-type", "application/json")
                    .body("{" +
                            "\"client_id\":\"" + CLIENT_ID + "\"," +
                            "\"client_secret\":\"" + CLIENT_SECRET + "\"," +
                            "\"audience\":\"" + AUDIENCE + "\",\"grant_type\":\"client_credentials\"}")
                    .asString();
            JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
            return new AccessToken(jsonNode.get("access_token").asText(), jsonNode.get("token_type").asText(), jsonNode.get("expires_in").asInt());
        } catch (UnirestException | JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAccessToken() {
        return ACCESS_TOKEN;
    }

    public String getTokenType() {
        return TOKEN_TYPE;
    }

    public String toString() {
        return "token: " + ACCESS_TOKEN + "\ntype: " + TOKEN_TYPE + "\nexpires: " + EXPIRY_DATE.getTime();
    }
}
