package client_java.model.login;

import Server.PlayerSide.PlayerInterface;

import client_java.util.PlayerServerConnection;

import javax.swing.*;
import java.util.Arrays;

public class LoginModel {
    private PlayerInterface playerServer;

    public LoginModel() {
        try{
            playerServer = PlayerServerConnection.getPlayerServerConnection();
        } catch (Exception e) {
            PlayerServerConnection.handleConnectionError(e);
            JOptionPane.showMessageDialog(null,
                    "Connection failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public String authenticate(String username, char[] password) {
        try {
            System.out.println("[Client] Attempting login for: " + username);
            String userId;
            if (username.equalsIgnoreCase("admin")) {
                userId = playerServer.adminLogin(username, new String(password));
            } else {
                userId = playerServer.login(username, new String(password));
            }
            Arrays.fill(password, '0');

            System.out.println("[Client] Login successful! Token: " + userId);
            return userId;
        } catch (Exception e) {
            System.err.println("[Client] Login failed:");
            return null;
        }
    }
}