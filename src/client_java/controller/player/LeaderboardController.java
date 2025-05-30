package client_java.controller.player;

import Server.Exceptions.LostConnectionException;
import client_java.model.player.HomeScreenModel;
import client_java.model.player.LeaderboardModel;
import client_java.view.player.HomeScreenUI;
import client_java.view.player.LeaderboardUI;
import javax.swing.*;
import java.util.List;

public class LeaderboardController {
    private final LeaderboardUI view;
    private final LeaderboardModel model;
    private final HomeScreenController homeScreenController;

    public LeaderboardController(LeaderboardUI leaderboardUI, LeaderboardModel leaderboardModel, String userid, String username, HomeScreenController homeScreenController) {
        this.view = leaderboardUI;
        this.model = leaderboardModel;
        this.homeScreenController = homeScreenController;
        initialize();
    }

    private void initialize() {
        loadLeaderboardData();
        setupBackButton();
    }

    private void loadLeaderboardData() {
        try {
            model.fetchLeaderboardData();
            updateLeaderboardView();
        } catch (LostConnectionException e) {
            JOptionPane.showMessageDialog(view,
                    "Failed to load leaderboard: " + e.message,
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupBackButton() {
        view.getBackButton().addActionListener(e -> {
            view.dispose();
            homeScreenController.showHomeScreen();

        });
    }

    private void updateLeaderboardView() {
        List<String> players = model.getPlayers();
        List<Integer> wins = model.getWins();

        if (!players.isEmpty() && !wins.isEmpty()) {
            view.setLeaderBoardRanking(players, wins);
        }
    }
}