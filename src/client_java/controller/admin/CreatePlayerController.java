package client_java.controller.admin;

import client_java.view.admin.CreatePlayerUI;
import client_java.model.admin.CreatePlayerModel;

import java.util.HashSet;
import java.util.Set;

public class CreatePlayerController {
    private CreatePlayerUI view;
    private CreatePlayerModel model;

    public CreatePlayerController(CreatePlayerUI view) {
        this.view = view;
        this.model = new CreatePlayerModel();
    }

    public void refreshPlayerList() {
//        try {
//            String[] players = model.getRegisteredPlayers();
//            String[] connectedPlayers = model.getConnectedPlayers();
//            Set<String> connectedSet = new HashSet<>();
//            for (String p : connectedPlayers) {
//                connectedSet.add(p);
//            }
//            view.clearPlayerList();
//            for (String player : players) {
//                boolean isConnected = connectedSet.contains(player);
//                view.addAccountRow(player, isConnected ? "connected" : "disconnected");
//            }
//        } catch (Exception e) {
//            view.showErrorMessage("Failed to fetch registered players: " + e.toString());
//        }
    }
}
