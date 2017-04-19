package org.dizitart.no2.sample.android;

/**
 * @author Anindya Chatterjee.
 */
public class User {
    private String id;
    private String username;
    private String email;

    // needed for deserialization
    public User(){}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.id = ""+System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
