package client_java.controller.player;

import Server.Exceptions.LostConnectionException;
import Server.Exceptions.NotLoggedInException;
import client_java.model.player.CreateLobbyModel;
import client_java.model.player.LobbyHostModel;
import client_java.view.player.CreateLobbyDialog;
import client_java.view.player.HomeScreenUI;
import client_java.view.player.LobbyHostDialog;

import javax.swing.*;

public class CreateLobbyController {
    private CreateLobbyDialog view;
    private CreateLobbyModel model;
    private HomeScreenUI parentView;
    private HomeScreenController parentController;

    public CreateLobbyController(CreateLobbyDialog view, CreateLobbyModel model,
                                 HomeScreenUI parentView, HomeScreenController parentController) {
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
            cleanup();
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
            int lobbyId = model.createLobby(lobbyName);
            model.joinLobby(lobbyId);
            model.setLobbyId(lobbyId);

            String username = model.retrieveUsernameFromServer();
            JPanel createdLobbyRow = parentView.addLobbyRow(username, lobbyName, 1);
            view.dispose();
            showLobbyHostDialog(lobbyName, username, createdLobbyRow);

        } catch (NotLoggedInException e) {
            view.showErrorMessage("Session expired. Please login again.");
            parentController.handleLogout();
            view.dispose();
        } catch (LostConnectionException e) {
            view.showErrorMessage("Connection lost: " + e.getMessage());
        } catch (Exception e) {
            view.showErrorMessage("Unexpected error: " + e.getMessage());
        }

        cleanup();
    }

    private void showLobbyHostDialog(String lobbyName, String username, JPanel createdLobbyRow) {
        LobbyHostDialog lobbyHostView = new LobbyHostDialog(parentView, lobbyName, username);
        LobbyHostModel lobbyHostModel = new LobbyHostModel(model.getUserToken(), lobbyName, username, model.getLobbyId());

        LobbyHostController lobbyHostController = new LobbyHostController(lobbyHostView, lobbyHostModel, parentView, parentController, createdLobbyRow, true);
        lobbyHostView.setVisible(true);
    }

    private void cleanup() {
        if (model != null) {
            model.cleanup();
        }
    }

    public CreateLobbyModel getModel() {
        return model;
    }
}