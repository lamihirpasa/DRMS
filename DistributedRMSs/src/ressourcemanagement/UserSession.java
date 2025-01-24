package ressourcemanagement;

public class UserSession {
    private static UserSession instance;
    private String loggedInUsername;

    private UserSession() {}  // Private constructor to prevent instantiation

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }
}
