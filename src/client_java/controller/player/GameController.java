package client_java.controller.player;

import client_java.model.player.*;
import client_java.view.player.*;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameController {
    static GameModel model;
    static GameUI view;
    private String userId;
    private String wordToShow;

    public GameController(GameModel model, GameUI view, String userId, int wordLength) {
        this.model = model;
        this.view = view;
        this.userId = userId;

        initializeRound(wordLength);
        view.setAnswerField(new AnswerFieldListener());
        updateRoundInfo();
    }

    private void initializeRound(int wordLength) {
        char[] temp = new char[wordLength];
        for(int i = 0; i < temp.length; i++)
            temp[i] = '_';
        wordToShow = new String(temp);
        view.getWord().setText(wordToShow);
        view.getTriesLeft().setText("Tries Left: "+ model.tryCount);
    }

    private void updateRoundInfo() {
        view.getRoundInfo().setText(String.format("Round %d | You: %d | Opponent: %d",
                model.getCurrentRound(), model.getPlayerWins(), model.getOpponentWins()));
    }

    private class AnswerFieldListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {}
        @Override
        public void keyPressed(KeyEvent e) {}

        @Override
        public void keyReleased(KeyEvent e) {
            char keyChar = e.getKeyChar();
            String result = model.guess(wordToShow, keyChar, userId);
            view.getWord().setText(result);
            view.changeImage(5-model.tryCount);
            view.getTriesLeft().setText("Tries Left: "+ model.tryCount);
            view.getAnswerField().setText("");
            updateRoundInfo();

            if (model.tryCount == 0) {
                view.setAnswerFieldInteract(false);
                JOptionPane.showMessageDialog(view,
                        "Round over! Waiting for other players...",
                        "Round Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}