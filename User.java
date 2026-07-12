package gcashapp;

/*
 * User.java
 * -------------------------------------------------------
 * Represents ONE ROW of the "users" table. This class just HOLDS
 * data - it doesn't talk to the database itself. UserAuthentication.java
 * is the one that reads/writes rows and builds these objects.
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String number;
    private String pin;

    public User(int id, String name, String email, String number, String pin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.number = number;
        this.pin = pin;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getNumber() { return number; }
    public String getPin() { return pin; }

    public void setPin(String pin) { this.pin = pin; }

    @Override
    public String toString() {
        // We never print the PIN here, for security.
        return "User #" + id + " [Name: " + name + ", Email: " + email + ", Number: " + number + "]";
    }
}
