package client_java.controller.login;

import client_java.controller.player.HomeScreenController;
import client_java.model.login.LoginModel;
import client_java.model.player.HomeScreenModel;
import client_java.view.login.Login;
import client_java.view.player.HomeScreenUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginController {
    private Login view;
    private LoginModel model;

    public LoginController(Login view, LoginModel model) {
        this.view = view;
        this.model = model;

        setupListeners();
    }

    private void setupListeners() {
        view.getLoginButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = view.getUsername();
                char[] password = view.getPassword();

                // Validate input
                if (username.isEmpty() || username.equals("Username..") ||
                        password.length == 0 || new String(password).equals("Password..")) {
                    JOptionPane.showMessageDialog(view, "Please enter both username and password",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Attempt login via model
                String userId = model.authenticate(username, password);
                if (userId != null) {
                    // Close the login window
                    view.dispose();

                    // Use SwingUtilities.invokeLater to ensure thread safety
                    SwingUtilities.invokeLater(() -> {
                        if (username.equalsIgnoreCase("admin")) {
                            // Show CreatePlayerUI for admin
                            new client_java.view.admin.CreatePlayerUI();
                        } else {
                            // Create the view
                            HomeScreenUI view = new HomeScreenUI();

                            HomeScreenModel model = new HomeScreenModel(userId);

                            // Create the controller and connect it to view and model
                            new HomeScreenController(view, model, userId, username);
                        }
                    });
                } else {
                    JOptionPane.showMessageDialog(view, "Invalid username or password",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}