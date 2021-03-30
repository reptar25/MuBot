package mubot.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import mubot.api.AccessToken;

public abstract class Endpoint {
    protected static ObjectMapper mapper = new ObjectMapper();
    protected final String BASE_URL = System.getenv("API_BASE_URL");//"http://localhost:3000";
    protected final String[] authHeader;
    protected final String route;

    public Endpoint(AccessToken token, String route) {
        authHeader = new String[]{"Authorization", token.getTokenType() + " " + token.getAccessToken()};
        this.route = route;
    }

    public RequestBodyEntity add(Object body) {
        return Unirest.post(BASE_URL + route).header(authHeader[0], authHeader[1]).body(body.toString());
    }

    public RequestBodyEntity remove(Object body) {
        return Unirest.delete(BASE_URL + route).header(authHeader[0], authHeader[1]).body(body.toString());
    }

    public JsonNode getAll() {
        try {
            HttpResponse<JsonNode> response = Unirest.get(BASE_URL + route).header(authHeader[0], authHeader[1]).asJson();
            return response.getBody();
        } catch (UnirestException e) {
            return null;
        }
    }

    public String getById(long id, String key) {
        try {
            HttpResponse<JsonNode> response = Unirest.get(BASE_URL + route + "/" + id).header(authHeader[0], authHeader[1]).asJson();
            return response.getBody().getArray().getJSONObject(0).get(key).toString();
        } catch (UnirestException e) {
            return null;
        }
    }

    public String getById(long id) {
        try {
            HttpResponse<JsonNode> response = Unirest.get(BASE_URL + route + "/" + id).header(authHeader[0], authHeader[1]).asJson();
            return response.getBody().getArray().getJSONObject(0).toString();
        } catch (UnirestException e) {
            return null;
        }
    }
}
