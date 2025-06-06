package client_java.controller.player;


import Server.Exceptions.LostConnectionException;
import Server.Exceptions.NotLoggedInException;
import Server.PlayerSide.PlayerInterface;
import client_java.model.player.CreateLobbyModel;
import client_java.model.player.LobbyHostModel;
import client_java.util.PlayerServerConnection;
import client_java.view.player.CreateLobbyDialog;
import client_java.view.player.HomeScreenUI;
import client_java.view.player.LobbyHostDialog;

import javax.swing.*;

/**
 * REMEMBER! - use usertokens not username for access in the server!!!!!
 * logic for the create lobby controller
 * - if the create button is pressed -> the controller calls the model.createlobby retrieving a lobbyId
 * - then the server creates a row for homescreen that will display a row with the host and lobby name
 * - calls the lobbyHost controller to create the lobby view
 */
public class CreateLobbyController {
    private String userid;
    private int lobbyId;

    private CreateLobbyDialog view;
    private CreateLobbyModel model;
    private HomeScreenUI parentView;
    private HomeScreenController parentController;
    
    public CreateLobbyController(CreateLobbyDialog view, CreateLobbyModel model, HomeScreenUI parentView,
                                 HomeScreenController parentController,
                                 String userid) {
        this.userid = userid;

        this.view = view;
        this.model = model;

        this.parentView = parentView;
        this.parentController = parentController;
        initialize();
    }

    private void initialize() {
        setupCancelButtonAction();
        setupCreateButtonAction();
    }

    private void setupCancelButtonAction() {
        view.getCancelButton().addActionListener(e -> {
            view.dispose();
            model.cleanup();
        });
    }

    private void setupCreateButtonAction() {
        view.getYesButton().addActionListener(e -> {
            handleCreateLobby();
        });
    }


    private void handleCreateLobby() {
        String lobbyName = view.getLobbyName();

        if (!model.isValidLobbyName(lobbyName)) {
            view.showInvalidNameMessage();
            return;
        }

        try {
            lobbyId = model.createLobby(lobbyName);
            boolean joined = model.joinLobby(lobbyId);
            if (!joined) {
                view.showErrorMessage("Failed to join lobby");
                return;
            }

            String hostUsername = model.getHostUsername(userid, lobbyId);
            System.out.println("[DEBUG - retrieval of host]: " + hostUsername);
            JPanel createdLobbyRow = parentView.addLobbyRow(hostUsername, lobbyName, 1);
            view.dispose();

            showLobbyHostDialog(lobbyName, hostUsername, createdLobbyRow);

        } catch (NotLoggedInException e) {
            view.showErrorMessage("Session expired. Please login again.");
            parentController.handleLogout();
            view.dispose();
        } catch (LostConnectionException e) {
            view.showErrorMessage("Connection lost: " + e.getMessage());
        } catch (Exception e) {
            view.showErrorMessage("Unexpected error: " + e.getMessage());
        }

        model.cleanup();
    }

    private void showLobbyHostDialog(String lobbyName, String username, JPanel createdLobbyRow) {
        LobbyHostDialog lobbyHostView = new LobbyHostDialog(parentView, lobbyName, username);
        LobbyHostModel lobbyHostModel = new LobbyHostModel();

        LobbyHostController lobbyHostController = new LobbyHostController(lobbyHostView, lobbyHostModel,
                parentView, parentController,
                createdLobbyRow, true, userid, lobbyId);

        lobbyHostView.setVisible(true);
    }
}