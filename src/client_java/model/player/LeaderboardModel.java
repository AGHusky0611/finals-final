package client_java.model.player;

import Server.CommonObjects.*;
import Server.Exceptions.*;
import client_java.util.PlayerServerConnection;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardModel {
    private List<String> players;
    private List<Integer> wins;
    private String userid;

    public LeaderboardModel(String userid) {
        this.players = new ArrayList<>();
        this.wins = new ArrayList<>();
        this.userid = userid;
    }

    public void fetchLeaderboardData() throws LostConnectionException {
        try {
            String[] leaderboardData = PlayerServerConnection.getPlayerServerConnection().getLeaderboard(userid);

            players.clear();
            wins.clear();

            for (String entry : leaderboardData) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    players.add(parts[0]);
                    wins.add(Integer.parseInt(parts[1]));
                }
            }
        } catch (LostConnectionException e) {
            throw e;
        } catch (Exception e) {
            PlayerServerConnection.handleConnectionError(e);
            throw new LostConnectionException("Failed to fetch leaderboard data");
        }
    }

    public List<String> getPlayers() {
        return new ArrayList<>(players);
    }

    public List<Integer> getWins() {
        return new ArrayList<>(wins);
    }

    public void setLeaderboardRanking(List<String> players, List<Integer> wins) {
        this.players = new ArrayList<>(players);
        this.wins = new ArrayList<>(wins);
    }
}