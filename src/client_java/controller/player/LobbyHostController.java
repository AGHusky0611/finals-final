package client_java.controller.player;


import Server.Exceptions.LostConnectionException;
import Server.Exceptions.NotLoggedInException;
import client_java.model.player.GameModel;
import client_java.model.player.LobbyHostModel;
import client_java.view.player.GameUI;
import client_java.view.player.HomeScreenUI;
import client_java.view.player.LobbyHostDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

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

    private static final int DEFAULT_COUNTDOWN_SECONDS = 10;
    private static final int PLAYER_UPDATE_INTERVAL = 1000;
    private static final int GAME_STATE_CHECK_INTERVAL = 500;

    // keep threads on point
    private volatile boolean isDisposing = false;
    private volatile boolean gameStartedSuccessfully = false;

    //variables
    private int countdown;
    private int serverCountdownSeconds;
    private boolean isHost;
    private String userid;
    private int lobbyId;
    private int lastPlayerCount = 0;

    public LobbyHostController(LobbyHostDialog view, LobbyHostModel model,
                               HomeScreenUI parentView, HomeScreenController parentController,
                               JPanel createdLobbyRow, Boolean isHost, String userid, int lobbyId) {
        this.userid = userid;
        this.lobbyId = lobbyId;

        this.view = view;
        this.model = model;

        this.parentView = parentView;
        this.parentController = parentController;
        this.createdLobbyRow = createdLobbyRow;

        this.isHost = isHost;
        initialize();
    }

    private void initialize() {
        setupStartButtonAction();
        setupLeaveButtonAction();

        initializeCountdownFromServer();

        startPlayerUpdateTimer();
        startGameStateMonitoring();

        updatePlayerList();

        if (!isHost) {
            view.getStartButton().setVisible(false);
        }
    }

    /***
     * this should handle if the start button is pressed
     * -> only the host can start the game
     * -> can only start if there are more than 2 players
     */
    private void setupStartButtonAction() {
        view.getStartButton().addActionListener(e -> {
            if (isHost) {
                try {
                    handleStartGame();
                } catch (LostConnectionException | NotLoggedInException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                view.showErrorMessage("Only the host can start the game.");
            }
        });

        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (isHost) {
                        handleLeaveLobby();
                    } else {
                        handleLeaveLobbyAsPlayer();
                    }
                } catch (Exception ex) {
                    System.err.println("Error during window closing: " + ex.getMessage());
                    if (isHost) {
                        handleLeaveLobby();
                    } else {
                        handleLeaveLobbyAsPlayer();
                    }
                }
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (!isDisposing && !gameStartedSuccessfully) {
                    if (isHost) {
                        handleLeaveLobby();
                    } else {
                        handleLeaveLobbyAsPlayer();
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error during shutdown: " + ex.getMessage());
            }
        }));
    }

    /**
     * if the leave button is pressed this should go back to the homescreen
     * -> if host the whole will collapse
     * -> if not the player will only be deleted
     */
    private void setupLeaveButtonAction() {
        view.getLeaveButton().addActionListener(e -> {
            if (isHost) {
                handleLeaveLobby();
            } else {
                handleLeaveLobbyAsPlayer();
            }
        });
    }

    private void handleLeaveLobby() {
        isDisposing = true;

        try {
            stopTimers();

            boolean leftLobby = model.leaveLobby(userid, lobbyId);

            if (leftLobby) {
                SwingUtilities.invokeLater(() -> {
                    parentView.removeLobbyRow(createdLobbyRow);
                    parentController.forceLobbyListRefresh(); // Add this line
                });
                view.dispose();
            } else {
                view.showErrorMessage("Failed to leave lobby. Please try again.");
            }

        } catch (NotLoggedInException e) {
            view.showErrorMessage("Session expired. Please login again.");
            handleLogout();
        } catch (LostConnectionException e) {
            view.showErrorMessage("Connection lost: " + e.getMessage());
            view.dispose();
            SwingUtilities.invokeLater(() -> {
                parentView.removeLobbyRow(createdLobbyRow);
                parentController.forceLobbyListRefresh(); // Add this line
            });
        } catch (Exception e) {
            view.showErrorMessage("Unexpected error: " + e.getMessage());
        }

        cleanup();
    }

    private void handleLeaveLobbyAsPlayer() {
        isDisposing = true;

        try {
            boolean leftLobby = model.leaveLobbyAsPlayer(parentController.getUserId(), lobbyId);

            if (leftLobby) {
                view.dispose();
            } else {
                view.showErrorMessage("Failed to leave lobby. Please try again.");
            }

        } catch (NotLoggedInException e) {
            view.showErrorMessage("Session expired. Please login again.");
            handleLogout();
        } catch (LostConnectionException e) {
            view.showErrorMessage("Connection lost: " + e.getMessage());
            view.dispose();
            SwingUtilities.invokeLater(() -> {
                parentView.removeLobbyRow(createdLobbyRow);
            });
        } catch (Exception e) {
            view.showErrorMessage("Unexpected error: " + e.getMessage());
        }

        cleanup();
    }

    private void handleStartGame() throws LostConnectionException, NotLoggedInException {
        if (!isHost) {
            view.showErrorMessage("Only the host can start the game.");
            return;
        }
        try {
            if (!model.hasMinimumPlayers(userid,lobbyId)) {
                view.showErrorMessage("Need at least 2 players to start the game.");
                return;
            }

            stopTimers();
            boolean gameStarted = model.startGame(userid, lobbyId);

            if (gameStarted) {
                handleGameStartedForAll();
            } else {
                view.showErrorMessage("Failed to start game. Please try again.");
                initializeCountdownFromServer();
            }

        } catch (NotLoggedInException e) {
            view.showErrorMessage("Session expired. Please login again.");
            handleLogout();
        } catch (Exception e) {
            view.showErrorMessage("Unexpected error: " + e.getMessage());
        }

        cleanup();
    }

    private void handleLogout() {
        stopTimers();
        view.dispose();
        parentController.handleLogout();
    }


    // ==================== HELPER METHODS ========================== //
    private void stopTimers() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        if (playerUpdateTimer != null && playerUpdateTimer.isRunning()) {
            playerUpdateTimer.stop();
        }
        if (gameStateTimer != null && gameStateTimer.isRunning()) {
            gameStateTimer.stop();
        }
    }

    public void cleanup() {
        stopTimers();
    }


    private void resetCountdownFromServer() {
        try {
            serverCountdownSeconds = model.getWaitingDuration(userid, lobbyId);

            if (countdownTimer != null && countdownTimer.isRunning()) {
                serverCountdownSeconds = DEFAULT_COUNTDOWN_SECONDS;
                countdownTimer.stop();
            }

            startCountdownTimer();
        } catch (Exception e) {
            System.err.println("Error resetting countdown from server: " + e.getMessage());
        }
    }


    private void handleGameStartedForAll() {
        try {
            isDisposing = true;
            gameStartedSuccessfully = true;

            stopTimers();

            SwingUtilities.invokeLater(() -> {
                parentView.removeLobbyRow(createdLobbyRow);
            });

            view.dispose();
            parentView.dispose();

//            GameUI gameUI = new GameUI();
//            GameModel gameModel = new GameModel(lobbyId, parentController.username, parentController.getUserId());
//            GameController gameController = new GameController(gameModel, gameUI, parentController.username, 5);

            cleanup();
        } catch (Exception e) {
            System.err.println("Error starting game: " + e.getMessage());
            view.showErrorMessage("Failed to start game: " + e.getMessage());
        }
    }

    private void initializeCountdownFromServer() {
        try {
            serverCountdownSeconds = model.getWaitingDuration(userid, lobbyId);
            startCountdownTimer();
        } catch (NotLoggedInException e) {
            view.showErrorMessage("Session expired. Please login again.");
            handleLogout();
        } catch (LostConnectionException e) {
            view.showErrorMessage("Connection lost: " + e.getMessage());
            startCountdownTimer();
        } catch (Exception e) {
            System.err.println("Error getting countdown from server: " + e.getMessage());
            startCountdownTimer();
        }
    }

    private void startCountdownTimer() {
        countdown = serverCountdownSeconds;
        view.updateCountdown(countdown);

        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown--;
                view.updateCountdown(countdown);

                if (countdown <= 0) {
                    countdownTimer.stop();
                    handleCountdownEnd();
                }
            }
        });
        countdownTimer.start();
    }

    private void handleCountdownEnd() {
        try {
            List<String> currentPlayers = model.getPlayersInLobby(userid,  lobbyId);

            if (isHost) {
                if (currentPlayers == null || currentPlayers.size() < 2) {
                    view.showErrorMessage("Need at least 2 players to start the game.");
                    if (currentPlayers != null && currentPlayers.size() == 1) {
                        handleOnlyHostRemaining();
                    } else {
                        initializeCountdownFromServer();
                    }
                    return;
                }

                handleStartGame();
            } else {
                return;
            }

        } catch (NotLoggedInException e) {
            view.showErrorMessage("Session expired. Please login again.");
            handleLogout();
        } catch (LostConnectionException e) {
            view.showErrorMessage("Connection lost: " + e.getMessage());
            if (isHost) {
                handleReturnToHomeScreen();
            } else {
                handleCloseLobby();
            }
        } catch (Exception e) {
            System.err.println("Error handling countdown end: " + e.getMessage());
            view.showErrorMessage("An error occurred. Returning to home screen.");
            if (isHost) {
                handleReturnToHomeScreen();
            } else {
                handleCloseLobby();
            }
        }
    }
    private void handleOnlyHostRemaining() {
        try {
            stopTimers();
            view.showInfoMessage("No other players joined the lobby. Returning to home screen.");
            boolean lobbyDeleted = model.leaveLobby(userid, lobbyId);

            if (!lobbyDeleted) {
                System.err.println("Warning: Failed to delete lobby on server");
            }

            SwingUtilities.invokeLater(() -> {
                parentView.removeLobbyRow(createdLobbyRow);
            });
            view.dispose();

            parentController.refreshLobbyList();

        } catch (NotLoggedInException e) {
            view.showErrorMessage("Session expired. Please login again.");
            handleLogout();
        } catch (LostConnectionException e) {
            System.err.println("Connection lost while closing lobby: " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                parentView.removeLobbyRow(createdLobbyRow);
            });
            view.dispose();
        } catch (Exception e) {
            System.err.println("Error handling only host scenario: " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                parentView.removeLobbyRow(createdLobbyRow);
            });
            view.dispose();
        }

        cleanup();
    }

    private void handleReturnToHomeScreen() {
        try {
            stopTimers();
            model.leaveLobby(userid, lobbyId);
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            parentView.removeLobbyRow(createdLobbyRow);
        });
        view.dispose();
        cleanup();
    }
    private void startPlayerUpdateTimer() {
        playerUpdateTimer = new Timer(PLAYER_UPDATE_INTERVAL, e -> {
            updatePlayerList();
        });
        playerUpdateTimer.start();
    }

    private void startGameStateMonitoring() {
        gameStateTimer = new Timer(GAME_STATE_CHECK_INTERVAL, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkGameState();
            }
        });

        gameStateTimer.setInitialDelay(1000);
        gameStateTimer.start();
    }

    private void checkGameState() {
        try {
            boolean gameStarted = model.isGameStarted(userid, lobbyId);

            if (gameStarted) {
                handleGameStartedForAll();
            }
        } catch (NotLoggedInException e) {
            gameStateTimer.stop();
            view.showErrorMessage("Session expired. Please login again.");
            handleLogout();
        } catch (LostConnectionException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.toLowerCase().contains("lobby")) {
                gameStateTimer.stop();
                handleLobbyNoLongerExists();
            } else {
                gameStateTimer.stop();
                view.showErrorMessage("Connection lost: " + e.getMessage());
                handleLobbyNoLongerExists();
            }
        } catch (Exception e) {
            System.err.println("Error checking game state: " + e.getMessage());
        }
    }
    private void handleCloseLobby() {
        isDisposing = true;

        try {
            stopTimers();

            boolean leftLobby = model.closeLobby(userid, lobbyId);

            if (leftLobby) {
                SwingUtilities.invokeLater(() -> {
                    parentView.removeLobbyRow(createdLobbyRow);
                });
                view.dispose();
            } else {
                view.showErrorMessage("Failed to close lobby. Please try again.");
            }

        } catch (NotLoggedInException e) {
            view.showErrorMessage("Session expired. Please login again.");
            handleLogout();
        } catch (LostConnectionException e) {
            view.showErrorMessage("Connection lost: " + e.getMessage());
            view.dispose();
        } catch (Exception e) {
            view.showErrorMessage("Unexpected error: " + e.getMessage());
            System.err.println("Error in handleCloseLobby: " + e.getMessage());
        }

        cleanup();
    }

    private void updatePlayerList() {
        try {
            List<String> players = model.getPlayersInLobby(userid, lobbyId);

            if (!isHost && (players == null || players.isEmpty())) {
                try {
                    List<String> verifyPlayers = model.getPlayersInLobby(userid, lobbyId);
                    if (verifyPlayers == null || verifyPlayers.isEmpty()) {
                        handleLobbyNoLongerExists();
                        return;
                    }
                } catch (Exception verifyEx) {
                    handleLobbyNoLongerExists();
                    return;
                }
            }

            if (players != null) {
                String hostName = getHostName();
                if (hostName != null && !hostName.isEmpty() && !hostName.equals("unknown")) {
                    if (!players.contains(hostName)) {
                        players.add(0, hostName);
                    }
                } else {
                    String fallbackName = parentController.getUsername();
                    if (fallbackName != null && !players.contains(fallbackName)) {
                        players.add(0, fallbackName);
                    }
                }

                int currentPlayerCount = players.size();
                if (currentPlayerCount > lastPlayerCount) {
                    System.out.println("New player joined. Resetting countdown...");
                    resetCountdownFromServer();
                }

                lastPlayerCount = currentPlayerCount;

                view.getPlayerListModel().clear();
                for (String player : players) {
                    view.getPlayerListModel().addElement(player);
                }
            }

        } catch (NotLoggedInException e) {
            view.showErrorMessage("Session expired. Please login again.");
            handleLogout();
        } catch (LostConnectionException e) {
            view.showErrorMessage("Connection lost: " + e.getMessage());
            if (!isHost) {
                handleLobbyNoLongerExists();
            }
        } catch (Exception e) {
            System.err.println("Error updating player list: " + e.getMessage());
            if (!isHost) {
                handleLobbyNoLongerExists();
            }
        }
    }

    private void handleLobbyNoLongerExists() {
        if (!isHost) {
            SwingUtilities.invokeLater(() -> {
                stopTimers();
                view.showInfoMessage("The lobby has been closed by the host.");
                view.dispose();
                cleanup();
            });
        }
    }

    private String getHostName() {
        try {
            String hostName = parentController.getUsername();

            if (hostName != null && !hostName.isEmpty()) {
                return hostName;
            }

            List<String> players = model.getPlayersInLobby(userid, lobbyId);
            if (players != null && !players.isEmpty()) {
                return players.get(0);
            }

        } catch (Exception e) {
            System.err.println("Error getting host name: " + e.getMessage());
        }

        return parentController.getUsername() != null ? parentController.getUsername() : "Host";
    }



}