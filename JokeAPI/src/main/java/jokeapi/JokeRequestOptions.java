package jokeapi;

import jokeapi.util.JokeEnums.BlacklistFlag;
import jokeapi.util.JokeEnums.JokeLanguage;
import jokeapi.util.JokeEnums.JokeType;
import jokeapi.util.JokeEnums.ResponseFormat;

import java.util.ArrayList;
import java.util.List;

public class JokeRequestOptions {

    private final List<String> categories = new ArrayList<>();
    private final List<String> blacklistFlags = new ArrayList<>();
    private JokeLanguage language = JokeLanguage.DEFAULT;
    private ResponseFormat responseFormat = ResponseFormat.DEFAULT;
    private JokeType jokeType = JokeType.DEFAULT;
    private String contains = "";
    private int amount = 1;
    private boolean safeMode = false;

    public JokeRequestOptions addCategory(String category) {
        this.categories.add(category);
        return this;
    }

    public JokeRequestOptions addBlacklistFlag(BlacklistFlag flag) {
        blacklistFlags.add(flag.toString());
        return this;
    }

    public JokeRequestOptions withJokeLanguage(JokeLanguage language) {
        this.language = language;
        return this;
    }

    public JokeRequestOptions withResponseFormat(ResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
        return this;
    }

    public JokeRequestOptions withJokeType(JokeType jokeType) {
        this.jokeType = jokeType;
        return this;
    }

    public JokeRequestOptions withContains(String contains) {
        this.contains = contains;
        return this;
    }

    public JokeRequestOptions withAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public JokeRequestOptions safeMode(boolean safeMode) {
        this.safeMode = safeMode;
        return this;
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<String> getBlacklistFlags() {
        return blacklistFlags;
    }

    public JokeLanguage getLanguage() {
        return language;
    }

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    public JokeType getJokeType() {
        return jokeType;
    }

    public String getContains() {
        return contains;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isSafeMode() {
        return safeMode;
    }
}
