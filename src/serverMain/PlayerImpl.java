package serverMain;

import Server.Exceptions.*;
import Server.PlayerSide.PlayerInterfacePOA;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerImpl extends PlayerInterfacePOA {
    private static final Logger logger = Logger.getLogger(PlayerImpl.class.getName());
    private Connection connection;

    // Database configuration
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/hangman";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public PlayerImpl() {
        initializeDatabaseConnection();
    }

    private void initializeDatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(
                    DB_URL + "?useSSL=false&autoReconnect=true",
                    DB_USER, DB_PASSWORD);
            logger.info("[SERVER]: Database connection established");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[SERVER]: Database connection failed", e);
        }
    }

    @Override
    public String login(String username, String password) throws LostConnectionException, NoSuchUserFoundException {
        logger.info(String.format("[CLIENT - %s] Request: login", username));

        String userType = "player";
        try {
            // 1. Authenticate user
            if (!authenticateUser(username, password, userType)) {
                throw new NoSuchUserFoundException("[SERVER]: Invalid username or password");
            }

            // 2. Generate token
            String userToken = generateToken();

            // 3. Handle existing session (if any)
            if (SessionManagement.isUserActive(username)) {  // Add this method to SessionManager
                System.out.println("[SERVER]: Logging out previous session for user: " + username);
                SessionManagement.invalidateUserSession(username);  // Add this method
            }

            // 4. Create new session using SessionManager
            SessionManagement.createSession(username, userToken);
            System.out.println("[SERVER]: user " + username + " Logged in successfully with token: " + userToken + " @ " + getDateTime());

            return userToken;
        } catch (NoSuchUserFoundException e) {
            logger.log(Level.SEVERE, "[SERVER]: Login failed for user: " + username, e);
            throw new NoSuchUserFoundException("[SERVER]: Login failed");
        } catch (LostConnectionException e) {
            logger.log(Level.SEVERE, "[SERVER]: Login failed for user: " + username, e);
            throw new LostConnectionException("[SERVER]: Server is unreachable");
        }
    }

    @Override
    public String adminLogin(String username, String password) throws LostConnectionException, NoSuchUserFoundException {
        return "";
    }

    private boolean authenticateUser(String username, String password, String userType) throws NoSuchUserFoundException, LostConnectionException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Query the player table for matching credentials
            String sql = "SELECT username FROM player WHERE username = ? AND password = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password); // In production, use hashed passwords!
            rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            throw new LostConnectionException("Database error during authentication");
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    @Override
    public void logout(String userToken) throws LostConnectionException, NotLoggedInException {
        String username = SessionManagement.getUsername(userToken);
        if (username == null) {
            throw new NotLoggedInException("User is not logged in");
        }

        try {
            // Log the logout attempt
            logger.info(String.format("[CLIENT - %s] Request: logout", username));

            // Invalidate the session
            SessionManagement.invalidateToken(userToken);

            // Log successful logout
            logger.info(String.format("[CLIENT - %s] Logout successful", username));
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("[CLIENT - %s] Logout failed", username), e);
            throw new LostConnectionException("Logout failed due to server error");
        }
    }

    @Override
    public String[] getLeaderboard(String userId) throws LostConnectionException {
        try {
            CallableStatement stmt = connection.prepareCall("{CALL retrieveLeaderboard()}");
            ResultSet rs = stmt.executeQuery();

            java.util.ArrayList<String> leaderboardList = new java.util.ArrayList<>();

            while (rs.next()) {
                String username = rs.getString("username");
                int winCount = rs.getInt("winCount");

                // Format: "username:winCount" or however you want to format it
                leaderboardList.add(username + ":" + winCount);
            }

            rs.close();
            stmt.close();

            // Convert ArrayList to String array
            return leaderboardList.toArray(new String[0]);

        } catch (SQLException e) {
            // Handle SQL exceptions - check if it's a connection issue
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new LostConnectionException("Database connection lost: " + e.getMessage());
            }

            // Log the error and return empty array for other SQL errors
            System.err.println("Error fetching leaderboard: " + e.getMessage());
            e.printStackTrace();
            return new String[0];
        }
    }

    @Override
    public String[] getLobbyList(String userToken) throws LostConnectionException {
        logger.info(String.format("[CLIENT - %s] Request: getLobbyList",
                SessionManagement.getUsername(userToken)));

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             CallableStatement stmt = conn.prepareCall("{CALL getActiveLobbies()}")) {

            ResultSet rs = stmt.executeQuery();
            List<String> lobbies = new ArrayList<>();

            while (rs.next()) {
                lobbies.add(String.join(":",
                        String.valueOf(rs.getInt("lobbyID")),
                        rs.getString("lobbyName"),
                        String.valueOf(rs.getInt("playerCount")),
                        rs.getString("hostUsername") // Added host username
                ));
            }

            logger.info(String.format("[SERVER] Returning %d active lobbies", lobbies.size()));
            return lobbies.toArray(new String[0]);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[SERVER] Database error in getLobbyList", e);
            if (e.getMessage().contains("connection") || e.getErrorCode() == 0) {
                throw new LostConnectionException("Database connection error");
            }
            throw new LostConnectionException("Failed to retrieve lobby list: " + e.getMessage());
        }
    }

    private String generateToken() throws LostConnectionException {
        try {
            // Generate a random UUID and replace dashes with empty strings
            return UUID.randomUUID().toString().replace("-", "");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to generate token", e);
            throw new LostConnectionException("Server is unreachable");
        }
    }

    @Override
    public String getUsernameByToken(String token) throws LostConnectionException, NotLoggedInException {
        try {
            if (!SessionManagement.isValidToken(token)) {
                throw new NotLoggedInException("[SERVER]: Invalid or expired token");
            }

            String username = SessionManagement.getUsername(token);

            if (username == null) {
                throw new NotLoggedInException("[SERVER]: No user associated with this token");
            }

            logger.info("[SERVER]: Retrieved username '" + username + "' for token: " + token);
            return username;

        } catch (NotLoggedInException e) {
            logger.log(Level.WARNING, "[SERVER]: Token validation failed: " + token, e);
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[SERVER]: Error retrieving username for token: " + token, e);
            throw new LostConnectionException("[SERVER]: Server error while retrieving user information");
        }
    }

    @Override
    public String guess(String shownWord, int lobbyId, char guessChar, String userId) throws LostConnectionException, NotLoggedInException {
        return "";
    }

    @Override
    public void prepareNextRound(int lobbyId) throws LostConnectionException {

    }

    @Override
    public void declareWinner(int lobbyId, String username) throws LostConnectionException {

    }

    @Override
    public void returnToLobby(int lobbyId, String username) throws LostConnectionException {

    }

    @Override
    public String[] getReturnedPlayers(int lobbyId) throws LostConnectionException {
        return new String[0];
    }

    @Override
    public boolean startNextRound(String userToken, int lobbyId) throws NotLoggedInException, LostConnectionException {
        return false;
    }

    @Override
    public String getLobbyHost(String userid, int lobbyId) throws LostConnectionException {
        try {
            if (!SessionManagement.isValidToken(userid)) {
                throw new NotLoggedInException("Invalid or expired token");
            }

            String sql = "SELECT hostUsername FROM lobby WHERE lobbyID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, lobbyId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getString("hostUsername");
                }
                return ""; // Lobby not found
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting lobby host", e);
            throw new LostConnectionException("Database error");
        } catch (NotLoggedInException e) {
            logger.log(Level.WARNING, "Unauthorized access to get lobby host", e);
            return ""; // Return empty string for unauthorized access
        }
    }

    @Override
    public int createLobby(String userToken, String lobbyName) throws LostConnectionException, NotLoggedInException {
        if (!SessionManagement.isValidToken(userToken)) {
            throw new NotLoggedInException("[SERVER]: Invalid or expired token");
        }
        String username = SessionManagement.getUsername(userToken);
        if (username == null) {
            throw new NotLoggedInException("[SERVER]: No user associated with this token");
        }

        lobbyName = lobbyName.trim();
        if (lobbyName.isEmpty()) {
            throw new IllegalArgumentException("[SERVER]: Lobby name cannot be empty");
        }

        try {
            try (CallableStatement stmt = connection.prepareCall("{CALL createLobby(?, ?, ?)}")) {
                stmt.setString(1, lobbyName);
                stmt.setString(2, username);
                stmt.registerOutParameter(3, Types.INTEGER);

                stmt.execute();

                int lobbyId = stmt.getInt(3);
                if (lobbyId > 0) {
                    logger.info("[SERVER]: Lobby '" + lobbyName + "' created with ID " + lobbyId + " by user: " + username);
                    return lobbyId;
                } else {
                    throw new SQLException("Lobby creation failed: ID not returned");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[SERVER]: SQL error while creating lobby", e);

            if (e.getMessage().contains("Communications link failure") || e.getMessage().contains("connection")) {
                throw new LostConnectionException("Database connection lost: " + e.getMessage());
            }

            throw new LostConnectionException("[SERVER]: SQL error: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[SERVER]: Unexpected error while creating lobby", e);
            throw new LostConnectionException("[SERVER]: Server error while creating lobby");
        }
    }

    @Override
    public int joinLobby(String userToken, int lobbyId) throws LostConnectionException, NotLoggedInException {
        if (!SessionManagement.isValidToken(userToken)) {
            throw new NotLoggedInException("[SERVER]: Invalid or expired token");
        }
        String username = SessionManagement.getUsername(userToken);
        System.out.println("[SERVER]: " + username + " Joining lobby: " + lobbyId);
        if (username == null) {
            throw new NotLoggedInException("[SERVER]: No user associated with this token");
        }

        try (CallableStatement stmt = connection.prepareCall("{CALL joinLobby(?, ?)}")) {
            stmt.setString(1, username);
            stmt.setInt(2, lobbyId);

            boolean hasResultSet = stmt.execute();
            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    while (rs.next()) {
                    }
                }
            }
            while (stmt.getMoreResults()) {
                try (ResultSet rs = stmt.getResultSet()) {
                    while (rs.next()) {
                    }
                }
            }
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            logger.info("[SERVER]: User '" + username + "' joined lobby ID: " + lobbyId);
            return lobbyId;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[SERVER]: SQL error while joining lobby: " + e.getMessage(), e);
            logger.log(Level.SEVERE, "[SERVER]: SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());

            if (e.getMessage().contains("Communications link failure") || e.getMessage().contains("connection")) {
                throw new LostConnectionException("Database connection lost: " + e.getMessage());
            }

            throw new RuntimeException("SQL error in joinLobby: " + e.getMessage(), e);
        }
    }


    @Override
    public synchronized void leaveLobby(String userId, int lobbyId) throws LostConnectionException, NotLoggedInException {
        if (!SessionManagement.isValidToken(userId)) {
            throw new NotLoggedInException("Invalid or expired token");
        }

        String username = SessionManagement.getUsername(userId);
        if (username == null) {
            throw new NotLoggedInException("No user associated with this token");
        }

        Connection conn = null;
        try {
            conn = connection;
            conn.setAutoCommit(false);

            // Remove player from lobby
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM playerinlobby WHERE username = ? AND lobbyID = ?")) {
                stmt.setString(1, username);
                stmt.setInt(2, lobbyId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected == 0) {
                    conn.rollback();
                    throw new NotLoggedInException("Player was not in the specified lobby");
                }
            }

            // Check if lobby is empty now
            boolean lobbyIsEmpty = false;
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM playerinlobby WHERE lobbyID = ?")) {
                checkStmt.setInt(1, lobbyId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    lobbyIsEmpty = rs.getInt(1) == 0;
                }
            }

            if (lobbyIsEmpty) {
                // Delete empty lobby
                try (PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM lobby WHERE lobbyID = ?")) {
                    deleteStmt.setInt(1, lobbyId);
                    deleteStmt.executeUpdate();
                }
            }

            conn.commit();
            logger.info("Player " + username + " left lobby " + lobbyId + (lobbyIsEmpty ? " and lobby was deleted" : ""));

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Failed to rollback transaction", ex);
            }

            if (e.getMessage().contains("Communications link failure")) {
                throw new LostConnectionException("Database connection lost");
            }
            throw new LostConnectionException("Failed to leave lobby: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to reset auto-commit", e);
            }
        }
    }

    @Override
    public int removeAllPlayersFromLobby(int lobbyID) throws LostConnectionException {
        return 0;
    }

    @Override
    public boolean removePlayerFromLobby(String userId, int lobbyId) throws LostConnectionException {
        try {
            // First get the username from the token
            String username = getUsernameByToken(userId);

            try (CallableStatement stmt = connection.prepareCall("{CALL removePlayerFromLobby(?, ?)}")) {
                stmt.setString(1, username);
                stmt.setInt(2, lobbyId);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (NotLoggedInException e) {
            throw new LostConnectionException("User not logged in");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error removing player from lobby", e);
            throw new LostConnectionException("Database error");
        }
    }

    @Override
    public String[] getPlayersInLobby(String userId, int lobbyId) throws LostConnectionException, NotLoggedInException {
        try {
            // First verify the requesting user's session
            if (!SessionManagement.isValidToken(userId)) {
                throw new NotLoggedInException("[SERVER]: Invalid or expired token");
            }

            try (CallableStatement stmt = connection.prepareCall("{CALL getPlayersInLobby(?)}")) {
                stmt.setInt(1, lobbyId);
                ResultSet rs = stmt.executeQuery();
                List<String> activePlayers = new ArrayList<>();

                while (rs.next()) {
                    String username = rs.getString("username");
                    // Verify each player's session is still active
                    if (SessionManagement.isUserActive(username)) {
                        activePlayers.add(username);
                    } else {
                        // Remove inactive players from lobby
                        try (PreparedStatement removeStmt = connection.prepareStatement(
                                "DELETE FROM playerinlobby WHERE username = ? AND lobbyID = ?")) {
                            removeStmt.setString(1, username);
                            removeStmt.setInt(2, lobbyId);
                            removeStmt.executeUpdate();
                        }
                    }
                }
                return activePlayers.toArray(new String[0]);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting players in lobby", e);
            throw new LostConnectionException("Database error");
        }
    }

    @Override
    public boolean startGame(String userToken, int lobbyId) throws LostConnectionException, NotLoggedInException {
        return false;
    }

    @Override
    public boolean isGameStarted(String userToken, int lobbyId) throws LostConnectionException, NotLoggedInException {
        String sql = "SELECT gameStarted FROM lobby WHERE lobbyID = ? AND isOpen = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, lobbyId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("gameStarted") == 1;
                }

                return false; // Lobby not found or closed
            }

        } catch (SQLException e) {
            System.err.println("Error checking game status: " + e.getMessage());
            throw new LostConnectionException("Database error: " + e.getMessage());
        }
    }

    @Override
    public boolean closeLobby(String userId, int lobbyId) throws LostConnectionException, NotLoggedInException {
        if (!SessionManagement.isValidToken(userId)) {
            throw new NotLoggedInException("[SERVER]: Invalid or expired token");
        }
        String username = SessionManagement.getUsername(userId);
        if (username == null) {
            throw new NotLoggedInException("[SERVER]: No user associated with this token");
        }

        String sql = "CALL closeLobby(?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, lobbyId);
            stmt.setString(2, username);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error closing lobby: " + e.getMessage());
            throw new LostConnectionException("Database error: " + e.getMessage());
        }
    }

    @Override
    public int getWaitingDuration(String userToken, int lobbyId) throws LostConnectionException, NotLoggedInException {
        return 0;
    }

    @Override
    public void deleteLobby(String userId,int lobbyID) throws LostConnectionException {
        try {
            CallableStatement stmt = connection.prepareCall("{CALL deleteLobby(?)}");
            stmt.setInt(1, lobbyID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new LostConnectionException("Database error");
        }
    }

    private LocalDateTime getDateTime() {
        return LocalDateTime.now();
    }

    public void cleanupExpiredLobbySessions() {
        try (CallableStatement stmt = connection.prepareCall("{CALL cleanupExpiredLobbySessions()}")) {
            stmt.execute();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error cleaning up expired lobby sessions", e);
        }
    }

}