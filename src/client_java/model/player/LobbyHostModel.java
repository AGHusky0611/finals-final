package client_java.model.player;

import Server.Exceptions.LostConnectionException;
import Server.Exceptions.NotLoggedInException;
import Server.PlayerSide.PlayerInterface;
import client_java.util.PlayerServerConnection;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class LobbyHostModel {
    private PlayerInterface playerServer;

    public LobbyHostModel() {
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

    public List<String> getPlayersInLobby(String userid, int lobbyId) throws NotLoggedInException, LostConnectionException {
        try {
            String[] players = playerServer.getPlayersInLobby(userid, lobbyId);
            return Arrays.asList(players);
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to get players in lobby: " + e.getMessage());
        }
    }

    public boolean leaveLobby(String userId, int lobbyId) throws NotLoggedInException, LostConnectionException {
        try {
            if (isLobbyHost(userId, lobbyId)) {
                playerServer.deleteLobby(userId,lobbyId);
                playerServer.removeAllPlayersFromLobby(lobbyId);
            }
            return true;
        } catch (LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to leave lobby: " + e.getMessage());
        }
    }

    /**
     * this would get the username of the host
     * @return
     */
    public boolean isLobbyHost(String userId, int lobbyId) {
        try {
            String host = playerServer.getLobbyHost(userId,lobbyId);
            return host != null && host.equals(playerServer.getUsernameByToken(userId));
        } catch (Exception e) {
            return false;
        }
    }
    public boolean leaveLobbyAsPlayer(String userId, int lobbyId) throws NotLoggedInException, LostConnectionException {
        try {
            return playerServer.removePlayerFromLobby(userId, lobbyId);
        } catch (LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to leave lobby: " + e.getMessage());
        }
    }

    public boolean hasMinimumPlayers(String userId, int lobbyId) throws NotLoggedInException, LostConnectionException {
        return getPlayerCount(userId, lobbyId) >= 2;
    }

    public int getPlayerCount(String userId, int lobbyId) throws NotLoggedInException, LostConnectionException {
        try {
            List<String> players = getPlayersInLobby(userId, lobbyId);
            return players.size();
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to get player count: " + e.getMessage());
        }
    }

    public boolean startGame(String userId, int lobbyId) throws NotLoggedInException, LostConnectionException {
        try {
            return playerServer.startGame(userId, lobbyId);
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to start game: " + e.getMessage());
        }
    }

    public int getWaitingDuration(String userId, int lobbyId) throws LostConnectionException, NotLoggedInException {
        try {
            return playerServer.getWaitingDuration(userId , lobbyId);
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to get waiting duration: " + e.getMessage());
        }
    }

    public boolean isGameStarted(String userId, int lobbyId) throws NotLoggedInException, LostConnectionException {
        try {
            return playerServer.isGameStarted(userId, lobbyId);
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to check game status: " + e.getMessage());
        }
    }
    public boolean closeLobby(String userId, int lobbyId) throws NotLoggedInException, LostConnectionException {
        try {
            return playerServer.closeLobby(userId, lobbyId);
        } catch (LostConnectionException | NotLoggedInException e) {
            throw e;
        } catch (Exception e) {
            throw new LostConnectionException("Failed to close lobby: " + e.getMessage());
        }
    }


}
