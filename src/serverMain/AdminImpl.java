package serverMain;

import Server.AdminSide.AdminInterfacePOA;
import Server.CommonInterface.CallBackInterface;
import Server.CommonObjects.GameResult;
import Server.CommonObjects.GameRules;
import Server.CommonObjects.User;
import Server.Exceptions.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminImpl extends AdminInterfacePOA {
    private static final Logger logger = Logger.getLogger(AdminImpl.class.getName());
    private Connection connection;

    // Database config
    private static final String DB_URL      = "jdbc:mysql://127.0.0.1:3306/hangman";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";

    public AdminImpl() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL + "?useSSL=false", DB_USER, DB_PASSWORD);
            logger.info("Database connected.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "DB init failed", e);
        }
    }

    @Override
    public User login(CallBackInterface cb, String adminId, String password)
            throws LostConnectionException, AlreadyLoggedInException {
        String sql = "SELECT username, password FROM admin WHERE username = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, adminId);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new AlreadyLoggedInException("Invalid credentials");
            }
            User u = new User();
            u.userId      = rs.getString("username");
            u.password    = rs.getString("password");
            u.displayName = u.userId;  // no separate display name in schema
            return u;
        } catch (SQLException e) {
            throw new LostConnectionException("DB error during login");
        }
    }

    @Override
    public void createPlayer(String playerId, String password)
            throws LostConnectionException, NotLoggedInException {
        String sql = "INSERT INTO player(username,password) VALUES(?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerId);
            ps.setString(2, password);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new LostConnectionException("DB error creating player");
        }
    }

    @Override
    public void editUserDetails(String userId, String newPassword)
            throws LostConnectionException, NotLoggedInException, NoSuchUserFoundException {
        String sql = "UPDATE player SET password = ? WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, userId);
            if (ps.executeUpdate() == 0) {
                throw new NoSuchUserFoundException("No such player: " + userId);
            }
        } catch (SQLException e) {
            throw new LostConnectionException("DB error editing player");
        }
    }

    @Override
    public void deleteUser(String userId)
            throws LostConnectionException, NotLoggedInException, NoSuchUserFoundException {
        String sql = "DELETE FROM player WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId);
            if (ps.executeUpdate() == 0) {
                throw new NoSuchUserFoundException("No such player: " + userId);
            }
        } catch (SQLException e) {
            throw new LostConnectionException("DB error deleting player");
        }
    }

    @Override
    public User[] getUserList() throws LostConnectionException, NotLoggedInException {
        String sql = "SELECT username, password FROM player";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            java.util.List<User> users = new java.util.ArrayList<>();
            while (rs.next()) {
                User u = new User();
                u.userId      = rs.getString("username");
                u.password    = rs.getString("password");
                u.displayName = u.userId;
                users.add(u);
            }
            return users.toArray(new User[0]);
        } catch (SQLException e) {
            throw new LostConnectionException("DB error fetching players");
        }
    }

    @Override
    public User searchUser(String userId)
            throws LostConnectionException, NotLoggedInException, NoSuchUserFoundException {
        String sql = "SELECT username, password FROM player WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new NoSuchUserFoundException("No such player: " + userId);
            }
            User u = new User();
            u.userId      = rs.getString("username");
            u.password    = rs.getString("password");
            u.displayName = u.userId;
            return u;
        } catch (SQLException e) {
            throw new LostConnectionException("DB error searching player");
        }
    }

    @Override
    public GameRules changeRules(int waitTime, int roundDuration)
            throws LostConnectionException, NotLoggedInException {
        String sqlUpdate = "UPDATE durationconfig SET waitingDuration = ?, gameDuration = ?";
        String sqlSelect = "SELECT waitingDuration, gameDuration FROM durationconfig LIMIT 1";

        try (PreparedStatement ps = connection.prepareStatement(sqlUpdate)) {
            ps.setInt(1, waitTime);
            ps.setInt(2, roundDuration);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating durationconfig", e);
            throw new LostConnectionException("Database error changing rules");
        }

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sqlSelect)) {
            if (!rs.next()) {
                throw new LostConnectionException("No durationconfig row found");
            }
            GameRules gr = new GameRules();
            gr.waitTime      = rs.getInt("waitingDuration");
            gr.roundDuration = rs.getInt("gameDuration");
            return gr;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error reading durationconfig", e);
            throw new LostConnectionException("Database error fetching rules");
        }
    }
    @Override
    public GameResult[] getGameHistory()
            throws LostConnectionException, NotLoggedInException {
        String sqlLobbies =
                "SELECT lobbyID, winner FROM lobby";
        List<GameResult> results = new ArrayList<>();
        try (PreparedStatement lobStmt = connection.prepareStatement(sqlLobbies);
             ResultSet lobRs = lobStmt.executeQuery()) {

            // Sub-queries
            String sqlPlayers =
                    "SELECT username, scoreCount FROM playerinlobby WHERE lobbyID = ?";
            PreparedStatement plyStmt = connection.prepareStatement(sqlPlayers);

            String sqlRounds =
                    "SELECT COUNT(*) AS roundsPlayed FROM usedword WHERE lobbyID = ?";
            PreparedStatement roundsStmt = connection.prepareStatement(sqlRounds);

            // Shared gameDuration
            int durationSeconds = 0;
            try (ResultSet cfg = connection.createStatement()
                    .executeQuery("SELECT gameDuration FROM durationconfig LIMIT 1")) {
                if (cfg.next()) durationSeconds = cfg.getInt("gameDuration");
            }

            while (lobRs.next()) {
                int lid = lobRs.getInt("lobbyID");

                // players + total guesses
                plyStmt.setInt(1, lid);
                try (ResultSet plyRs = plyStmt.executeQuery()) {
                    List<String> players = new ArrayList<>();
                    int totalGuesses = 0;
                    while (plyRs.next()) {
                        players.add(plyRs.getString("username"));
                        totalGuesses += plyRs.getInt("scoreCount");
                    }

                    // rounds played
                    roundsStmt.setInt(1, lid);
                    int rounds = 0;
                    try (ResultSet rRs = roundsStmt.executeQuery()) {
                        if (rRs.next()) rounds = rRs.getInt("roundsPlayed");
                    }

                    GameResult gr = new GameResult();
                    gr.sessionId        = String.valueOf(lid);
                    gr.winner           = lobRs.getString("winner");
                    gr.players          = players.toArray(new String[0]);   // StringSequence → String[]
                    gr.roundsPlayed     = rounds;                          // now int
                    gr.totalGuessesMade = totalGuesses;                    // now int
                    gr.duration         = durationSeconds;                 // now int

                    results.add(gr);
                }
            }
            return results.toArray(new GameResult[0]);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching game history", e);
            throw new LostConnectionException("Database error fetching game history");
        }
    }

    @Override
    public GameResult[] getPlayerHistory(String playerId)
            throws LostConnectionException, NotLoggedInException, NoSuchUserFoundException {
        String sqlLobbies =
                "SELECT DISTINCT pil.lobbyID, l.winner " +
                        "FROM playerinlobby pil " +
                        "JOIN lobby l ON pil.lobbyID = l.lobbyID " +
                        "WHERE pil.username = ?";
        List<GameResult> results = new ArrayList<>();
        try (PreparedStatement lobStmt = connection.prepareStatement(sqlLobbies)) {
            lobStmt.setString(1, playerId);
            try (ResultSet lobRs = lobStmt.executeQuery()) {
                if (!lobRs.isBeforeFirst()) {
                    throw new NoSuchUserFoundException("No history for user: " + playerId);
                }

                String sqlPlayers =
                        "SELECT username, scoreCount FROM playerinlobby WHERE lobbyID = ?";
                PreparedStatement plyStmt = connection.prepareStatement(sqlPlayers);

                String sqlRounds =
                        "SELECT COUNT(*) AS roundsPlayed FROM usedword WHERE lobbyID = ?";
                PreparedStatement roundsStmt = connection.prepareStatement(sqlRounds);

                int durationSeconds = 0;
                try (ResultSet cfg = connection.createStatement()
                        .executeQuery("SELECT gameDuration FROM durationconfig LIMIT 1")) {
                    if (cfg.next()) durationSeconds = cfg.getInt("gameDuration");
                }

                while (lobRs.next()) {
                    int lid = lobRs.getInt("lobbyID");

                    // players + guesses
                    plyStmt.setInt(1, lid);
                    try (ResultSet plyRs = plyStmt.executeQuery()) {
                        List<String> players = new ArrayList<>();
                        int totalGuesses = 0;
                        while (plyRs.next()) {
                            players.add(plyRs.getString("username"));
                            totalGuesses += plyRs.getInt("scoreCount");
                        }

                        // rounds
                        roundsStmt.setInt(1, lid);
                        int rounds = 0;
                        try (ResultSet rRs = roundsStmt.executeQuery()) {
                            if (rRs.next()) rounds = rRs.getInt("roundsPlayed");
                        }

                        GameResult gr = new GameResult();
                        gr.sessionId        = String.valueOf(lid);
                        gr.winner           = lobRs.getString("winner");
                        gr.players          = players.toArray(new String[0]);
                        gr.roundsPlayed     = rounds;
                        gr.totalGuessesMade = totalGuesses;
                        gr.duration         = durationSeconds;

                        results.add(gr);
                    }
                }
            }
            return results.toArray(new GameResult[0]);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching player history", e);
            throw new LostConnectionException("Database error fetching player history");
        }
    }

    @Override
    public String ping(String userId) throws LostConnectionException, NotLoggedInException {
        return "ping:" + userId;
    }
}
