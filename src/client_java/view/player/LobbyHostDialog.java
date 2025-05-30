package client_java.view.player;

import javax.swing.*;
import java.awt.*;

public class LobbyHostDialog extends JDialog {
    private DefaultListModel<String> playerListModel;
    private JList<String> playerList;
    private JLabel countdownLabel;
    private JButton startButton;
    private JButton leaveButton;
    private HomeScreenUI parentView;
    private JScrollPane playerScrollPane;

    public LobbyHostDialog(HomeScreenUI parent, String lobbyName, String hostUsername) {
        super(parent, "Lobby: " + lobbyName, true);
        this.parentView = parent;
        initializeDialog(hostUsername);
    }

    private void initializeDialog(String hostUsername) {
        setSize(600, 400);
        getContentPane().setBackground(new Color(192, 192, 192, 255));
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        setupPlayersSection(hostUsername);
        setupCountdownLabel();
        setupButtons();
    }

    private void setupPlayersSection(String hostUsername) {
        JLabel playersText = new JLabel("Players");
        playersText.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        playersText.setBounds(20, 20, 350, 30);
        add(playersText);

        playerListModel = new DefaultListModel<>();
        playerListModel.addElement(hostUsername);
        playerList = new JList<>(playerListModel);
        playerList.setBackground(new Color(192, 192, 192, 255));

        playerScrollPane = new JScrollPane(playerList);
        playerScrollPane.setBounds(20, 70, 550, 150);
        playerScrollPane.setBackground(new Color(192, 192, 192, 255));
        add(playerScrollPane);
    }

    private void setupCountdownLabel() {
        countdownLabel = new JLabel("loading...", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        countdownLabel.setBounds(130, 240, 340, 30);
        add(countdownLabel);
    }

    private void setupButtons() {
        // Start button
        startButton = parentView.createRoundedButton("Start", 30);
        startButton.setBounds(330, 300, 120, 40);
        startButton.setBackground(new Color(47, 123, 61));
        startButton.setForeground(Color.white);
        startButton.setFocusPainted(false);
        startButton.setOpaque(false);
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(startButton);

        // Leave button
        leaveButton = parentView.createRoundedButton("Leave", 30);
        leaveButton.setBounds(140, 300, 120, 40);
        leaveButton.setBackground(new Color(161, 62, 62));
        leaveButton.setForeground(Color.white);
        leaveButton.setFocusPainted(false);
        leaveButton.setOpaque(false);
        leaveButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(leaveButton);
    }

    public DefaultListModel<String> getPlayerListModel() {
        return playerListModel;
    }

    public JList<String> getPlayerList() {
        return playerList;
    }

    public JLabel getCountdownLabel() {
        return countdownLabel;
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getLeaveButton() {
        return leaveButton;
    }

    public void updateCountdown(int seconds) {
        if (seconds > 0) {
            countdownLabel.setText("Please wait for " + seconds + "...");
        } else {
            countdownLabel.setText("Time's up! Starting the game...");
        }
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

}