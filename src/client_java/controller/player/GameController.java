package client_java.controller.player;

import client_java.model.player.*;
import client_java.view.player.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Scanner;

public class GameController {
    static GameModel model;
    static GameUI view;

    private String wordToGuess = "freeloaders", player;
    private char[] wordToShow;
    private int tryCount;

    //for testing
    public static void main(String[] args) {
        GameModel gameModel = new GameModel();
        GameUI gameUI = new GameUI();
        new GameController(gameModel, gameUI, "justinne");
    }

    public GameController(GameModel model, GameUI view, String player) {
        this.model = model;
        this.view = view;
        wordToShow = new char[wordToGuess.length()];
        tryCount = 5;

        for(int i = 0; i < wordToShow.length; i++)
            wordToShow[i] = '_';

        view.getWord().setText(new String(wordToShow));
        view.getTriesLeft().setText("Tries Left: "+ tryCount);
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
            // Scanner kbd = new Scanner(System.in);
            // make answerfield not interactable until user hits enter (low priority)
            // view.setAnswerFieldInteract(false);
            // kbd.nextLine();
            // view.setAnswerFieldInteract(true);
            String result = model.guess(wordToGuess, wordToShow, keyChar);
            if (result.equals("*")) {
                tryCount--;
                view.changeImage(5-tryCount);

                view.getTriesLeft().setText("Tries Left: "+ tryCount);
                //if trycount reaches zero, setinteract false. lose round.
            } else {
                view.getWord().setText(result);
            }
            view.getAnswerField().setText("");
        }
    }
}
