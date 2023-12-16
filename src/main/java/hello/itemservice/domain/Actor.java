package hello.itemservice.domain;

import lombok.Data;

@Data
public class Actor {
    private long id;
    private String firstName;
    private String lastName;

    public Actor() {}

    public Actor(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Actor(long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
