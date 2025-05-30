package client_java.model.player;

import Server.Exceptions.*;
import Server.PlayerSide.PlayerInterface;
import client_java.util.PlayerServerConnection;
import org.omg.CORBA.ORB;
import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class LobbyHostModel {
    private PlayerInterface playerServer;
    private String userToken;
    private String lobbyName;
    private int lobbyId;
    private String hostUsername;
    private ORB orb;

    public LobbyHostModel(String userToken, String lobbyName, String hostUsername, int lobbyId) {
        this.userToken = userToken;
        this.lobbyName = lobbyName;
        this.hostUsername = hostUsername;
        this.lobbyId = lobbyId;
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

    // ========== Core Lobby Methods ========== //

    public List<String> getPlayersInLobby() throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            String[] players = playerServer.getPlayersInLobby(userToken, lobbyId);
            return Arrays.asList(players);
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to get players in lobby: " + e.getMessage());
        }
    }

    public int getWaitingDuration(String userToken, int lobbyId) throws LostConnectionException, NotLoggedInException {
        try {
            ensureConnection();
            return playerServer.getWaitingDuration(userToken, lobbyId);
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to get waiting duration: " + e.getMessage());
        }
    }

    public boolean startGame() throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            return playerServer.startGame(userToken, lobbyId);
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to start game: " + e.getMessage());
        }
    }

    public boolean startNextRound() throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            return playerServer.startNextRound(userToken, lobbyId);
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to start next round: " + e.getMessage());
        }
    }

    public boolean isGameStarted() throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            return playerServer.isGameStarted(userToken, lobbyId);
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to check game status: " + e.getMessage());
        }
    }

    // ========== Player Management ========== //

    public boolean leaveLobby() throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            if (isLobbyHost()) {
                playerServer.deleteLobby(lobbyId);
                playerServer.removeAllPlayersFromLobby(lobbyId);
            }
            return true;
        } catch (LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to leave lobby: " + e.getMessage());
        }
    }

    public boolean leaveLobbyAsPlayer(String playerUsername) throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            return playerServer.removePlayerFromLobby(lobbyId, playerUsername);
        } catch (LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to leave lobby: " + e.getMessage());
        }
    }

    // ========== Round Management ========== //

    public String[] getReturnedPlayers() throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            return playerServer.getReturnedPlayers(lobbyId);
        } catch (LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to get returned players: " + e.getMessage());
        }
    }

    public boolean prepareNextRound() throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            playerServer.prepareNextRound(lobbyId);
            return true;
        } catch (LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to prepare next round: " + e.getMessage());
        }
    }

    // ========== Game State Management ========== //

    public boolean closeLobby() throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            return playerServer.closeLobby(userToken, lobbyId);
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to close lobby: " + e.getMessage());
        }
    }

    public boolean declareWinner(String winnerUsername) throws NotLoggedInException, LostConnectionException {
        try {
            ensureConnection();
            playerServer.declareWinner(lobbyId, winnerUsername);
            return true;
        } catch (LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to declare winner: " + e.getMessage());
        }
    }

    // ========== Helper Methods ========== //

    public int getPlayerCount() throws NotLoggedInException, LostConnectionException {
        try {
            List<String> players = getPlayersInLobby();
            return players.size();
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to get player count: " + e.getMessage());
        }
    }

    public boolean hasMinimumPlayers() throws NotLoggedInException, LostConnectionException {
        return getPlayerCount() >= 2;
    }

    public boolean isLobbyHost() {
        try {
            ensureConnection();
            String host = playerServer.getLobbyHost(lobbyId);
            return host != null && host.equals(hostUsername);
        } catch (Exception e) {
            return false;
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

    // ========== Getters ========== //

    public String getUserToken() {
        return userToken;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public int getLobbyId() {
        return lobbyId;
    }

    public String getHostUsername() {
        return hostUsername;
    }

    // ========== Cleanup ========== //

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