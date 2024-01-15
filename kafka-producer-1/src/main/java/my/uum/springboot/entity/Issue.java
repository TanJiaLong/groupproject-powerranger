package my.uum.springboot.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Issue {

    private Long id;
    private String username;
    private String comment;

    public Issue() {

    }
    public Issue(Long id, String username, String comment) {
        this.id = id;
        this.username = username;
        this.comment = comment;
    }

    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    @JsonProperty("body")
    public String getComment() {
        return comment;
    }
    @JsonProperty("user")
    private void unpackUser(Map<String, Object> user) {
        this.username = (String) user.get("login");
    }

    public String getUsername() {
        return username;
    }
}
