package client_java.model.player;

import Server.CommonObjects.*;
import Server.PlayerSide.*;
import Server.PlayerSide.PlayerInterfaceHelper;
import Server.Exceptions.*;
import client_java.util.PlayerServerConnection;
import com.sun.org.apache.bcel.internal.generic.ARETURN;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HomeScreenModel {
    private PlayerInterface playerServer;
    private String userToken;
    private String username;
    private ORB orb;

    public HomeScreenModel(String userToken) {
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

    public void logout() {
        try {
            // logs out user
            playerServer.logout(userToken);
        } catch (LostConnectionException e) {
            System.err.println("Logout failed (connection): " + e.message);
        } catch (NotLoggedInException e) {
            System.err.println("Logout failed (not logged in): " + e.message);
        } catch (Exception e) {
            System.err.println("Logout failed: " + e.getMessage());
        } finally {
            if (orb != null) {
                orb.shutdown(true);
            }
        }
    }

    public List<LobbyData> getLobbyList() throws LostConnectionException {
        try {
            ensureConnection();
            String[] lobbyData = playerServer.getLobbyList(userToken);
            List<LobbyData> lobbies = new ArrayList<>();

            if (lobbyData == null || lobbyData.length == 0) {
                return lobbies;
            }

            for (String entry : lobbyData) {
                String[] parts = entry.split(":");
                if (parts.length == 4) {  // Changed from 3 to 4
                    lobbies.add(new LobbyData(
                            Integer.parseInt(parts[0]),     // lobbyId
                            parts[1],                       // lobbyName
                            Integer.parseInt(parts[2]),     // playerCount
                            parts[3]                        // hostUsername
                    ));
                }
            }
            return lobbies;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to fetch lobby list");
        }
    }

    public static class LobbyData {
        public final int lobbyId;
        public final String lobbyName;
        public final int playerCount;
        public final String hostUsername;

        public LobbyData(int lobbyId, String lobbyName, int playerCount, String hostUsername) {
            this.lobbyId = lobbyId;
            this.lobbyName = lobbyName;
            this.playerCount = playerCount;
            this.hostUsername = hostUsername;
        }
    }

    public void joinLobby(int lobbyId) throws LostConnectionException, NotLoggedInException {
        try {
            playerServer.joinLobby(userToken, lobbyId);
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to join lobby");
        }
    }

    public void ensureConnection() throws LostConnectionException {
        if (playerServer == null || orb == null) {
            initializeORBConnection();
        }
    }

    public void setUserToken(String token) {
        this.userToken = token;
    }

    public String getUserToken() {
        return userToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String retrieveUsernameFromServer() {
        try {
            return playerServer.getUsernameByToken(userToken);
        } catch (NotLoggedInException e) {
            System.err.println("User not logged in: " + e.getMessage());
            return "NotLoggedIn";
        } catch (LostConnectionException e) {
            System.err.println("Server connection lost: " + e.getMessage());
            return "UnknownUser";
        } catch (Exception e) {
            System.err.println("Failed to retrieve username: " + e.getMessage());
            return "UnknownUser";
        }
    }

    public int createLobby(String lobbyName) {
        try {
            int result = playerServer.createLobby(userToken, lobbyName);
            return result;
        } catch (NotLoggedInException e) {
            System.err.println("User not logged in: " + e.getMessage());
            return -1; // Return an invalid lobby ID to indicate failure
        } catch (LostConnectionException e) {
            System.err.println("Lost connection to server: " + e.getMessage());
            return -1; // Return an invalid lobby ID to indicate failure
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error while creating lobby: " + e.getMessage());
            return -1; // Return an invalid lobby ID to indicate failure
        }
    }
}