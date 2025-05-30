package client_java.model.player;

import Server.CommonObjects.*;
import Server.PlayerSide.*;
import Server.PlayerSide.PlayerInterfaceHelper;
import Server.Exceptions.*;
import client_java.util.PlayerServerConnection;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import javax.swing.*;
import java.util.Properties;

public class CreateLobbyModel {
    private PlayerInterface playerServer;
    private String userToken;
    private int lobbyId;
    private ORB orb;

    public CreateLobbyModel(String userToken) {
        this.userToken = userToken;
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
            ensureConnection();
            return playerServer.createLobby(userToken, lobbyName);
        } catch (NotLoggedInException | LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to create lobby: " + e.getMessage());
        }
    }

    public boolean joinLobby(int lobbyId) throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            return playerServer.joinLobby(userToken, lobbyId);
        } catch (NotLoggedInException | LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to join lobby as host: " + e.getMessage());
        }
    }

    public String retrieveUsernameFromServer() throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            return playerServer.getUsernameByToken(userToken);
        } catch (NotLoggedInException | LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to retrieve username: " + e.getMessage());
        }
    }

    public void ensureConnection() throws LostConnectionException {
        if (playerServer == null || orb == null) {
            initializeORBConnection();
            if (playerServer == null) {
                throw new LostConnectionException("Unable to establish server connection");
            }
        }
    }

    public int getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(int lobbyId) {
        this.lobbyId = lobbyId;
    }

    public String getUserToken() {
        return userToken;
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