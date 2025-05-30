package client_java.model.player;

import Server.Exceptions.LostConnectionException;
import Server.Exceptions.NotLoggedInException;
import Server.PlayerSide.PlayerInterface;
import client_java.util.PlayerServerConnection;

import javax.swing.*;

public class GameModel {
    public int tryCount;
    private int lobbyID;
    private String username;
    private String userId;
    private PlayerInterface playerServer;
    private int currentRound = 1;
    private int playerWins = 0;
    private int opponentWins = 0;

    public GameModel(int lobbyID, String username, String userId) {
        this.username = username;
        this.userId = userId;
        this.lobbyID = lobbyID;
        this.username = username;
        tryCount = 5;

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

    public String guess(String shownWord, char c, String userId) {
        String show = null;
        try {
            show = playerServer.guess(shownWord, lobbyID, c, userId);
        } catch (LostConnectionException e) {
            throw new RuntimeException(e);
        } catch (NotLoggedInException e) {
            throw new RuntimeException(e);
        }

        if (!show.contains("_")) {
            playerWins++;
            if (playerWins >= 5) {
                signalWin();
            } else {
                prepareNextRound();
            }
        }
        if (shownWord.equalsIgnoreCase(show)) {
            tryCount--;
            if (tryCount == 0) {
                opponentWins++;
                if (opponentWins >= 5) {
                    signalLoss();
                } else {
                    prepareNextRound();
                }
            }
        }
        return show;
    }

    private void prepareNextRound() {
        tryCount = 5;
        currentRound++;
        try {
            playerServer.returnToLobby(lobbyID, username);
            playerServer.prepareNextRound(lobbyID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void signalWin() {
        try {
            playerServer.declareWinner(lobbyID, username);
            exitGame();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void signalLoss() {
        exitGame();
    }

    public void exitGame() {
        try {
            playerServer.returnToLobby(lobbyID, username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getPlayerWins() {
        return playerWins;
    }

    public int getOpponentWins() {
        return opponentWins;
    }
}