package serverMain;

import Server.AdminSide.AdminInterfacePOA;
import Server.CommonInterface.CallBackInterface;
import Server.CommonObjects.GameResult;
import Server.CommonObjects.GameRules;
import Server.CommonObjects.User;
import Server.Exceptions.AlreadyLoggedInException;
import Server.Exceptions.LostConnectionException;
import Server.Exceptions.NoSuchUserFoundException;
import Server.Exceptions.NotLoggedInException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminImpl extends AdminInterfacePOA {
    private static final Logger logger = Logger.getLogger(AdminImpl.class.getName());
    private Connection connection;

    // Database configuration
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/hangman";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public AdminImpl() {
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


    // needs more fixing and checking specifically sa view
    @Override
    public User login(CallBackInterface cb, String adminId, String password) throws LostConnectionException, AlreadyLoggedInException {
        String sql = "SELECT user_id, password, display_name FROM admins WHERE user_id = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, adminId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new AlreadyLoggedInException("Invalid admin credentials");
            }
            User admin = new User();
            admin.userId = rs.getString("user_id");
            admin.password = rs.getString("password");
            admin.displayName = rs.getString("display_name");
            return admin;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error during admin login", e);
            throw new LostConnectionException("Database connection issue");
        }
    }

    @Override
    public void createPlayer(String playerId, String password) throws LostConnectionException, NotLoggedInException {
        String sql = "INSERT INTO users (user_id, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId);
            stmt.setString(2, password);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating player", e);
            throw new LostConnectionException("Database connection issue");
        }
    }

    @Override
    public void editUserDetails(String userId, String newPassword) throws LostConnectionException, NotLoggedInException, NoSuchUserFoundException {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, userId);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new NoSuchUserFoundException("User not found: " + userId);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error editing user details", e);
            throw new LostConnectionException("Database connection issue");
        }
    }

    @Override
    public void deleteUser(String userId) throws LostConnectionException, NotLoggedInException, NoSuchUserFoundException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            int deleted = stmt.executeUpdate();
            if (deleted == 0) {
                throw new NoSuchUserFoundException("User not found: " + userId);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting user", e);
            throw new LostConnectionException("Database connection issue");
        }
    }

    @Override
    public User[] getUserList() throws LostConnectionException, NotLoggedInException {
        String sql = "SELECT user_id, password, display_name FROM users";
        List<User> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.userId = rs.getString("user_id");
                u.password = rs.getString("password");
                u.displayName = rs.getString("display_name");
                list.add(u);
            }
            return list.toArray(new User[0]);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching user list", e);
            throw new LostConnectionException("Database connection issue");
        }
    }

    @Override
    public User searchUser(String userId) throws LostConnectionException, NotLoggedInException, NoSuchUserFoundException {
        String sql = "SELECT user_id, password, display_name FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new NoSuchUserFoundException("User not found: " + userId);
            }
            User u = new User();
            u.userId = rs.getString("user_id");
            u.password = rs.getString("password");
            u.displayName = rs.getString("display_name");
            return u;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching for user", e);
            throw new LostConnectionException("Database connection issue");
        }
    }

    // Everything here below will be changed and needs fixing

    @Override
    public GameRules changeRules(int waitingDuration, int gameDuration) throws LostConnectionException, NotLoggedInException {
        return null; // based on the project specifications, the only configurations that the admins needs to be able to do are lobby waiting time and round duration
    }


    @Override
    public GameResult[] getGameHistory() throws LostConnectionException, NotLoggedInException {
        return new GameResult[0];
    }

    @Override
    public GameResult[] getPlayerHistory(String playerId) throws LostConnectionException, NotLoggedInException, NoSuchUserFoundException {
        return new GameResult[0];
    }

    @Override
    public String ping(String userId) throws LostConnectionException, NotLoggedInException {
        return "ping:";
    }
}
