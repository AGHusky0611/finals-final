package serverMain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManagement {
    // Token -> Username mapping
    private static final Map<String, String> activeSessions = new ConcurrentHashMap<>();
    // Username -> Token mapping
    private static final Map<String, String> userToTokenMap = new ConcurrentHashMap<>();

    public static synchronized String createSession(String username, String token) {
        // Invalidate any existing session for this user
        invalidateUserSession(username);

        activeSessions.put(token, username);
        userToTokenMap.put(username, token);
        return token;
    }

    public static synchronized void invalidateUserSession(String username) {
        String token = userToTokenMap.get(username);
        if (token != null) {
            activeSessions.remove(token);
            userToTokenMap.remove(username);
        }
    }

    public static synchronized void invalidateToken(String token) {
        String username = activeSessions.get(token);
        if (username != null) {
            activeSessions.remove(token);
            userToTokenMap.remove(username);
        }
    }

    public static synchronized String getUsername(String token) {
        return activeSessions.get(token);
    }

    public static boolean isValidToken(String token) {
        return activeSessions.containsKey(token);
    }

    public static synchronized boolean isUserActive(String username) {
        return userToTokenMap.containsKey(username);
    }
}