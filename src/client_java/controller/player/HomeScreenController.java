package client_java.controller.player;

import Server.CommonObjects.LobbyInfo;
import Server.Exceptions.LostConnectionException;
import Server.Exceptions.NotLoggedInException;
import client_java.controller.login.LoginController;
import client_java.model.login.LoginModel;
import client_java.model.player.*;
import client_java.view.login.Login;
import client_java.view.player.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

public class HomeScreenController {
    String userId;
    String username;
    private HomeScreenUI view;
    private HomeScreenModel model;
    private LobbyHostModel lobbyHostModel;
    private LobbyHostController lobbyHostController;
    private Timer countdownTimer;
    private int countdown;
    private DefaultListModel<String> playerListModel;
    private Timer lobbyUpdateTimer;
    private static final int LOBBY_UPDATE_INTERVAL = 3000; // 3 seconds

    private List<HomeScreenModel.LobbyData> allLobbies;

    public HomeScreenController(HomeScreenUI view, HomeScreenModel model, String userId, String username) {
        this.userId = userId;
        this.username = username;
        this.view = view;
        this.model = model;
        initialize();
    }

    public void initialize() {
        getCountdownTimer();
        getSearchField();
        getLeaderboardButtonAction();
        getLogoutButtonAction();
        getCreateLobbyButtonAction();
        initializeLobbyList();
        initializeLobbyUpdates();
    }

    private void initializeLobbyUpdates() {
        lobbyUpdateTimer = new Timer(LOBBY_UPDATE_INTERVAL, e -> {
            SwingUtilities.invokeLater(() -> {
                initializeLobbyList();
            });
        });
        lobbyUpdateTimer.start();
    }

    public void getLeaderboardButtonAction() {
        view.getViewLeaderboardButton().addActionListener(e -> {
            view.setVisible(false);
            cleanup();
            LeaderboardUI leaderboardView = new LeaderboardUI();
            LeaderboardModel leaderboardModel = new LeaderboardModel(userId);
            new LeaderboardController(leaderboardView, leaderboardModel, userId, username, this);
            leaderboardView.setVisible(true);
        });
    }

    public void getLogoutButtonAction() {
        view.getLogoutButton().addActionListener(e -> {
            handleLogout();
        });
    }

    public void getCreateLobbyButtonAction() {
        view.getCreateLobbyButton().addActionListener(e -> {
            CreateLobbyDialog createLobbyView = new CreateLobbyDialog(view);
            CreateLobbyModel createLobbyModel = new CreateLobbyModel(userId);

            CreateLobbyController createLobbyController = new CreateLobbyController(createLobbyView, createLobbyModel, view, this);
            createLobbyView.setVisible(true);
        });
    }

    public void getSearchField() {
        view.getSearchField().addActionListener(e -> {
            performLobbySearch();
        });

        view.getSearchField().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> performLobbySearch());
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> performLobbySearch());
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> performLobbySearch());
            }
        });
    }

    private void performLobbySearch() {
        String searchText = view.getSearchField().getText().trim();

        if (allLobbies == null) {
            return;
        }

        List<HomeScreenModel.LobbyData> filteredLobbies;

        if (searchText.isEmpty()) {
            filteredLobbies = allLobbies;
        } else {
            filteredLobbies = allLobbies.stream()
                    .filter(lobby -> lobby.lobbyName.toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
        }
        displayLobbies(filteredLobbies);
    }

    public void getCountdownTimer() {
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown--;
                if (countdown == 0) {
                    countdownTimer.stop();
                    if (view.getCountdownLabel() != null) {
                        view.getCountdownLabel().setText("Game Started!");
                    }
                }
            }
        });
    }

    private void initializeLobbyList() {
        try {
            allLobbies = model.getLobbyList();
            displayLobbies(allLobbies);
            view.refreshLobbyList();
        } catch (LostConnectionException e) {
            JOptionPane.showMessageDialog(view,
                    "Failed to load lobby list: " + e.message,
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayLobbies(List<HomeScreenModel.LobbyData> lobbies) {
        // Clear existing lobbies
        view.getTransparentPanel().removeAll();

        if (lobbies.isEmpty()) {
            // Show appropriate message based on whether it's a search result or no lobbies exist
            String searchText = view.getSearchField().getText().trim();
            String message = searchText.isEmpty() ?
                    "No active lobbies available" :
                    "No lobbies found matching '" + searchText + "'";

            JLabel noLobbiesLabel = new JLabel(message);
            noLobbiesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            noLobbiesLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noLobbiesLabel.setForeground(Color.WHITE);
            JPanel messagePanel = new JPanel(new BorderLayout());
            messagePanel.setOpaque(false);
            messagePanel.add(noLobbiesLabel, BorderLayout.CENTER);
            messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            view.getTransparentPanel().add(messagePanel);
        } else {
            // Add each lobby as a row
            for (HomeScreenModel.LobbyData lobby : lobbies) {
                JPanel row = view.addLobbyRow("Host: " + lobby.hostUsername, lobby.lobbyName, lobby.playerCount);
                row.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        joinLobby(lobby.lobbyId);
                    }
                });
            }
        }
        view.getTransparentPanel().revalidate();
        view.getTransparentPanel().repaint();
    }

    private void navigateToLobbyAsPlayer(int lobbyId) {
        try {
            HomeScreenModel.LobbyData lobbyData = findLobbyById(lobbyId);
            if (lobbyData == null) {
                JOptionPane.showMessageDialog(view, "Lobby not found. It may have been closed.",
                        "Lobby Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            lobbyHostModel = new LobbyHostModel(userId, lobbyData.lobbyName, lobbyData.hostUsername, lobbyId);
            LobbyHostDialog lobbyView = new LobbyHostDialog(this.view, lobbyData.lobbyName, lobbyData.hostUsername);
            lobbyHostController = new LobbyHostController(lobbyView, lobbyHostModel, this.view, this, null, false);
            lobbyView.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Failed to navigate to lobby: " + e.getMessage(),
                    "Navigation Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private HomeScreenModel.LobbyData findLobbyById(int lobbyId) {
        if (allLobbies == null) {
            try {
                allLobbies = model.getLobbyList();
            } catch (LostConnectionException e) {
                return null;
            }
        }
        return allLobbies.stream().filter(lobby -> lobby.lobbyId == lobbyId).findFirst().orElse(null);
    }

    private void joinLobby(int lobbyId) {
        try {
            // First attempt to join the lobby on the server
            model.joinLobby(lobbyId);
            navigateToLobbyAsPlayer(lobbyId);

        } catch (NotLoggedInException e) {
            JOptionPane.showMessageDialog(view,
                    "Session expired. Please login again.",
                    "Authentication Error",
                    JOptionPane.ERROR_MESSAGE);
            handleLogout();
        } catch (LostConnectionException e) {
            JOptionPane.showMessageDialog(view,
                    "Connection lost: " + e.message,
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view,
                    "Failed to join lobby: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void handleLogout() {
        model.logout();
        view.dispose();
        cleanup();
        Login loginView = new Login();
        LoginModel loginModel = new LoginModel();
        new LoginController(loginView, loginModel);
        JOptionPane.showMessageDialog(loginView, "Successfully logged out", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void cleanup() {
        if (lobbyUpdateTimer != null) {
            lobbyUpdateTimer.stop();
        }
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
    }

    public void refreshLobbyList() {
        view.refreshLobbyList();
    }

    public void showHomeScreen() {
        view.setVisible(true);
    }

    // Getters for access from other controllers
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public HomeScreenUI getView() {
        return view;
    }

    public HomeScreenModel getModel() {
        return model;
    }
}