package serverMain;

import Server.Exceptions.*;
import Server.PlayerSide.PlayerInterfacePOA;

import java.rmi.RemoteException;
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
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             CallableStatement stmt = conn.prepareCall("{CALL getActiveLobbies()}")) {

            ResultSet rs = stmt.executeQuery();
            List<String> lobbies = new ArrayList<>();

            while (rs.next()) {
                lobbies.add(rs.getInt("lobbyID") + ":" +
                        rs.getString("lobbyName") + ":" +
                        rs.getInt("playerCount"));
            }

            // Return empty array instead of null if no lobbies
            return lobbies.isEmpty() ? new String[0] : lobbies.toArray(new String[0]);

        } catch (SQLException e) {
            throw new LostConnectionException("Database error");
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
    public String getLobbyHost(int lobbyId) throws LostConnectionException {
        return "";
    }

    @Override
    public int createLobby(String userToken, String lobbyName)
            throws LostConnectionException, NotLoggedInException, IllegalArgumentException {

        // Validate session
        if (!SessionManagement.isValidToken(userToken)) {
            throw new NotLoggedInException("Invalid or expired token");
        }
        String username = SessionManagement.getUsername(userToken);
        if (username == null) {
            throw new NotLoggedInException("No user associated with this token");
        }

        // Validate lobby name
        lobbyName = lobbyName.trim();
        if (lobbyName.isEmpty() || lobbyName.length() > 100) {
            throw new IllegalArgumentException("Lobby name must be 1-100 characters");
        }

        Connection conn = null;
        try {
            conn = connection;
            conn.setAutoCommit(false); // Start transaction

            // Create lobby and get ID
            int lobbyId;
            try (CallableStatement stmt = conn.prepareCall("{CALL createLobby(?, ?, ?)}")) {
                stmt.setString(1, lobbyName);
                stmt.setString(2, username);
                stmt.registerOutParameter(3, Types.INTEGER);
                stmt.execute();
                lobbyId = stmt.getInt(3);

                if (lobbyId <= 0) {
                    throw new SQLException("Lobby creation failed: ID not returned");
                }
            }

            // Add creator to lobby
            try (PreparedStatement joinStmt = conn.prepareStatement(
                    "INSERT INTO playerinlobby (username, lobbyID, scoreCount) VALUES (?, ?, 0)")) {
                joinStmt.setString(1, username);
                joinStmt.setInt(2, lobbyId);
                joinStmt.executeUpdate();
            }

            conn.commit();
            logger.info("Lobby created - ID: " + lobbyId + ", Name: " + lobbyName);
            return lobbyId;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Rollback failed", ex);
            }

            if (e.getMessage().contains("Duplicate entry")) {
                throw new IllegalArgumentException("Lobby name already exists");
            }
            if (e.getMessage().contains("connection")) {
                throw new LostConnectionException("Database connection lost");
            }

            logger.log(Level.SEVERE, "Lobby creation failed", e);
            throw new LostConnectionException("Failed to create lobby: " + e.getMessage());

        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {
                logger.log(Level.WARNING, "Auto-commit reset failed", e);
            }
        }
    }

    @Override
    public boolean joinLobby(String userToken, int lobbyId) throws LostConnectionException, NotLoggedInException {
        return false;
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
                stmt.executeUpdate();
            }

            // Check if lobby is empty now
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM playerinlobby WHERE lobbyID = ?")) {
                checkStmt.setInt(1, lobbyId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    // Delete empty lobby
                    try (PreparedStatement deleteStmt = conn.prepareStatement(
                            "DELETE FROM lobby WHERE lobbyID = ?")) {
                        deleteStmt.setInt(1, lobbyId);
                        deleteStmt.executeUpdate();
                    }
                }
            }

            conn.commit();
            logger.info("Player " + username + " left lobby " + lobbyId);

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
    public boolean removePlayerFromLobby(int lobbyId, String username) throws LostConnectionException {
        return false;
    }

    @Override
    public String[] getPlayersInLobby(String token, int lobbyId) throws LostConnectionException, NotLoggedInException {
        return new String[0];
    }

    @Override
    public boolean startGame(String userToken, int lobbyId) throws LostConnectionException, NotLoggedInException {
        return false;
    }

    @Override
    public boolean isGameStarted(String userToken, int lobbyId) throws LostConnectionException, NotLoggedInException {
        return false;
    }

    @Override
    public boolean closeLobby(String userToken, int lobbyId) throws LostConnectionException, NotLoggedInException {
        return false;
    }

    @Override
    public int getWaitingDuration(String userToken, int lobbyId) throws LostConnectionException, NotLoggedInException {
        return 0;
    }

    @Override
    public String getHostUsername(String userId, int lobbyId) throws LostConnectionException, NotLoggedInException {
        return "";
    }

    public void deleteLobby(int lobbyID) throws LostConnectionException {
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

}