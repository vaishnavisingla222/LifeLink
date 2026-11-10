
package org.example;

public class UserSession {

    private static UserSession currentUser;

    private final int userId;
    private final String fullName;
    private final String email;
    private final String role;

    public UserSession(int userId, String fullName,
                       String email, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public static void login(UserSession user) {
        currentUser = user;
    }

    public static UserSession getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
