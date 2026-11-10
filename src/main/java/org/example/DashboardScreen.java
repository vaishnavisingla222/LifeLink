
package org.example;

public class DashboardScreen {

    public void show() {

        UserSession user = UserSession.getCurrentUser();

        if (user == null) {
            System.out.println("No user is logged in.");
            return;
        }

        String role = user.getRole();

        if ("DONOR".equalsIgnoreCase(role)) {
            new DonorDashboard().show();
        } else if ("SEEKER".equalsIgnoreCase(role)) {
            new SeekerDashboard().show();
        } else {
            System.out.println("Unknown user role: " + role);
        }
    }
}
