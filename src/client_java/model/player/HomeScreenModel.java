package client_java.model.player;

import Server.PlayerSide.*;
import Server.PlayerSide.PlayerInterfaceHelper;
import Server.Exceptions.*;
import client_java.util.PlayerServerConnection;
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
        this.username = retrieveUsernameFromServer();
    }

    private void initializeORBConnection() {
        try {
            playerServer = PlayerServerConnection.getPlayerServerConnection();
        } catch (Exception e) {
            PlayerServerConnection.handleConnectionError(e);
            JOptionPane.showMessageDialog(null, "Connection failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public void logout() {
        try {
            if (playerServer != null && userToken != null) {
                playerServer.logout(userToken);
            }
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
                if (parts.length >= 3) {
                    int lobbyId = Integer.parseInt(parts[0]);
                    String lobbyName = parts[1];
                    int playerCount = Integer.parseInt(parts[2]);
                    String hostUsername = parts.length >= 4 ? parts[3] : "Unknown";

                    lobbies.add(new LobbyData(lobbyId, lobbyName, playerCount, hostUsername));
                }
            }
            return lobbies;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to fetch lobby list: " + e.getMessage());
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
            this.hostUsername = hostUsername != null ? hostUsername : "Unknown";
        }

        @Override
        public String toString() {
            return String.format("Lobby[id=%d, name='%s', players=%d, host='%s']",
                    lobbyId, lobbyName, playerCount, hostUsername);
        }
    }

    public void joinLobby(int lobbyId) throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            boolean success = playerServer.joinLobby(userToken, lobbyId);
            if (!success) {
                throw new RuntimeException("Failed to join lobby - server returned false");
            }
        } catch (NotLoggedInException | LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to join lobby: " + e.getMessage());
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
            if (playerServer != null && userToken != null) {
                String retrievedUsername = playerServer.getUsernameByToken(userToken);
                return retrievedUsername != null ? retrievedUsername : "UnknownUser";
            }
            return "UnknownUser";
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
            ensureConnection();
            int result = playerServer.createLobby(userToken, lobbyName);
            return result;
        } catch (NotLoggedInException e) {
            System.err.println("User not logged in: " + e.getMessage());
        } catch (LostConnectionException e) {
            System.err.println("Lost connection to server: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error while creating lobby: " + e.getMessage());
        }
        return -1;
    }
}