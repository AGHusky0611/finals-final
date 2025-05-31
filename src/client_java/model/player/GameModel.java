package client_java.model.player;

import Server.PlayerSide.PlayerInterface;
import client_java.util.PlayerServerConnection;

import javax.swing.*;

public class GameModel {
    public int tryCount;
    private int lobbyID;
    private String username;
    private PlayerInterface playerServer;

    public GameModel() {
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

    public String guess(String shownWord, char c, String userId, int lobbyID) {
        String show = playerServer.guess(shownWord, lobbyID, c, userId);
        if (!show.contains("_"))
            signalWin();
        if (shownWord.equalsIgnoreCase(show))
            tryCount--;
        return show;
    }

    public void signalWin() {
        /*
        return player to lobby and signals to lobby mvc that they are the winner
         */
    }

    public void exitGame() {
        /*
        return player to lobby screen, wait for other players to finish
        can this part also be called by the lobby mvc? :0
        to pull players out of their games when someone else wins na?
         */
    }
}
