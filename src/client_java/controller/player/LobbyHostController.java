package client_java.controller.player;


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
    private String lobbyName;
    private String userid;
    private String username;

    public LobbyHostController(String lobbyName, String userid, String username) {
        this.lobbyName = lobbyName;
        this.userid = userid;
        this.username = username;

        initialize();
    }

    private void initialize() {

    }



}