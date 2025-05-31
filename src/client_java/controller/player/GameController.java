package client_java.controller.player;

import Server.Exceptions.LostConnectionException;
import Server.Exceptions.NotLoggedInException;
import client_java.model.player.*;
import client_java.view.player.*;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Scanner;

public class GameController {
    private GameModel model;
    private GameUI view;

    private int lobbyId;


    private String userId;
    private String wordToShow;

    /*
    for testing
    public static void main(String[] args) {
        GameModel gameModel = new GameModel();
        GameUI gameUI = new GameUI();
        new GameController(gameModel, gameUI, "justinne", 11);
    }
     */

    public GameController(GameModel model, GameUI view, String userId, int wordLength, int lobbyID) {
        this.model = model;
        this.view = view;

        this.userId = userId;
        this.lobbyId = lobbyID;

        char[] temp = new char[wordLength];

        for(int i = 0; i < temp.length; i++)
            temp[i] = '_';

        wordToShow = new String(temp);
        view.getWord().setText(wordToShow);
        view.getTriesLeft().setText("Tries Left: "+ model.tryCount);
        view.setAnswerField(new AnswerFieldListener());
    }

    private class AnswerFieldListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {}
        @Override
        public void keyPressed(KeyEvent e) {}
        @Override
        public void keyReleased(KeyEvent e) {
            char keyChar = e.getKeyChar();
            String result = null;
            try {
                result = model.guess(wordToShow, keyChar, userId, lobbyId);
            } catch (LostConnectionException ex) {
                throw new RuntimeException(ex);
            } catch (NotLoggedInException ex) {
                throw new RuntimeException(ex);
            }
            view.getWord().setText(result);
            view.changeImage(5-model.tryCount);
            view.getTriesLeft().setText("Tries Left: "+ model.tryCount);
            view.getAnswerField().setText("");
            if (model.tryCount == 0){
                view.setAnswerFieldInteract(false);
                JOptionPane.showMessageDialog(view,
                        "You've run out of tries.\nBetter luck in the next round!",
                        "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
                model.exitGame();
            }
        }
    }
}
