package client_java.controller.player;


import client_java.model.player.LobbyHostModel;
import client_java.view.player.HomeScreenUI;
import client_java.view.player.LobbyHostDialog;

import javax.swing.*;

/**
 * this should be the lobby storage or where the players go when they join the lobby
 * -> this will connect to the server to get the following
 * - Countdown timer - TIMER
 * - playerUpdateTimer ( when a new player joins ) - TIMER
 * - GameStateTimer ( timer for each match ) - TIMER
 * -  countdown ( int )
 * - serverCountdown ( int )
 */
public class LobbyHostController {
    // view and model
    private LobbyHostDialog view;
    private LobbyHostModel model;

    // parent classes
    private HomeScreenUI parentView;
    private HomeScreenController parentController;

    // row of the lobby
    private JPanel createdLobbyRow;

    // variables needed::
    // timer
    private Timer countdownTimer;
    private Timer playerUpdateTimer;
    private Timer gameStateTimer;

    //variables
    private int countdown;
    private int serverCountdownSeconds;

    // keep threads on point
    private volatile boolean isDisposing = false;
    private volatile boolean gameStartedSuccessfully = false;

    private boolean isHost;
    private String userid;
    private int lastPlayerCount = 0;

    public LobbyHostController(LobbyHostDialog view, LobbyHostModel model,
                               HomeScreenUI parentView, HomeScreenController parentController,
                               JPanel createdLobbyRow, Boolean isHost, String userid) {
        this.userid = userid;

        this.view = view;
        this.model = model;
        this.parentView = parentView;
        this.parentController = parentController;
        this.createdLobbyRow = createdLobbyRow;
        this.isHost = isHost;
        initialize();
    }

    private void initialize() {
//        setupStartButtonAction();
//        setupLeaveButtonAction();
//        initializeCountdownFromServer();
//        startPlayerUpdateTimer();
//
//        startGameStateMonitoring();
//
//        updatePlayerList();

        if (!isHost) {
            view.getStartButton().setVisible(false);
        }
    }

}