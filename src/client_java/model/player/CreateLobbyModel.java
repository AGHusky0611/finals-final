package client_java.model.player;

import Server.CommonObjects.*;
import Server.PlayerSide.*;
import Server.PlayerSide.PlayerInterfaceHelper;
import Server.Exceptions.*;
import client_java.util.PlayerServerConnection;
import org.omg.CORBA.ORB;

import javax.swing.*;

public class CreateLobbyModel {
    private PlayerInterface playerServer;
    private String userToken;
    private int lobbyId;
    private ORB orb;

    public CreateLobbyModel(String userToken) {
        this.userToken = userToken;
        initializeORBConnection();
    }

    private void initializeORBConnection() {
        try {
            playerServer = PlayerServerConnection.getPlayerServerConnection();
        } catch (Exception e) {
            PlayerServerConnection.handleConnectionError(e);
            JOptionPane.showMessageDialog(null,
                    "Connection failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public boolean isValidLobbyName(String lobbyName) {
        return lobbyName != null && !lobbyName.trim().isEmpty() && !lobbyName.equals("Enter lobby name..") && lobbyName.trim().length() >= 3 && lobbyName.trim().length() <= 30;
    }

    public int createLobby(String lobbyName) throws NotLoggedInException, LostConnectionException {
        try {
            return playerServer.createLobby(userToken, lobbyName);
        } catch (NotLoggedInException | LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to create lobby: " + e.getMessage());
        }
    }

    public boolean joinLobby(int lobbyId) throws NotLoggedInException, LostConnectionException {
        try {
            return playerServer.joinLobby(userToken, lobbyId);
        } catch (NotLoggedInException | LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to join lobby as host: " + e.getMessage());
        }
    }

    public void setLobbyId(int lobbyId) {
        this.lobbyId = lobbyId;
    }

    public String getHostUsername(String userId, int lobbyId) throws LostConnectionException, NotLoggedInException {
        return playerServer.getLobbyHost(userId, lobbyId);
    }

    public void cleanup() {
        if (orb != null) {
            try {
                orb.shutdown(false);
            } catch (Exception e) {
                System.err.println("Error during ORB cleanup: " + e.getMessage());
            }
        }
    }
}