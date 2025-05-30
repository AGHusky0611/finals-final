package client_java.view.player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class HomeScreenUI extends JFrame {
    private JPanel transparentPanel;
    private JLabel countdownLabel;
    private JButton logoutButton;
    private JTextField searchField;
    private JButton viewLeaderboardButton;
    private JButton createLobbyButton;

    public HomeScreenUI() {
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("res/logo/Hangman_Logo.jpg").getImage());
        setTitle("What's The Word?");
        setSize(1000, 700);
        setLayout(null);
        setLocationRelativeTo(null);
        setResizable(false);

        // Background panel
        ImageIcon backgroundIcon = new ImageIcon("res/images/background.png");
        Image backgroundImage = backgroundIcon.getImage();

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setLayout(null);

        // Header label
        JLabel header = new JLabel("<html><div style='text-align: left;'>WELCOME TO \"WHAT'S THE WORD?\"<br>Please join or create a lobby to start!</div></html>");
        header.setBounds(70, 3, 500, 100);
        header.setForeground(new Color(174, 174, 174));
        header.setFont(new Font("Segoe UI", Font.PLAIN, 22));

        // Logout button
        logoutButton = createRoundedButton("logout", 30);
        logoutButton.setBounds(775, 25, 180, 45);
        configureButton(logoutButton);

        // Search field
        searchField = (JTextField) createRoundedField(false, 15, 40);
        searchField.setBounds(70, 100, 840, 37);
        configureSearchField();

        // Transparent panel for lobby list
        transparentPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setColor(new Color(0x40FFFFFF, true));
                graphics.fillRect(0, 0, getWidth(), getHeight());
                graphics.dispose();
            }
        };
        transparentPanel.setLayout(new BoxLayout(transparentPanel, BoxLayout.Y_AXIS));
        transparentPanel.setOpaque(false);

        JScrollPane scrollPane = createScrollPane();

        // Leaderboard button
        viewLeaderboardButton = new JButton("View Leaderboard");
        viewLeaderboardButton.setBounds(70, 570, 420, 40);
        configureButton(viewLeaderboardButton);

        // Create lobby button
        createLobbyButton = new JButton("Create Lobby");
        createLobbyButton.setBounds(490, 570, 420, 40);
        configureButton(createLobbyButton);

        // Add components to panel
        panel.add(header);
        panel.add(logoutButton);
        panel.add(searchField);
        panel.add(scrollPane);
        panel.add(viewLeaderboardButton);
        panel.add(createLobbyButton);
        setContentPane(panel);

        setVisible(true);
        SwingUtilities.invokeLater(() -> getContentPane().requestFocusInWindow());
    }

    private JScrollPane createScrollPane() {
        JScrollPane scrollPane = new JScrollPane(transparentPanel);
        scrollPane.setBounds(70, 150, 840, 420);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));
        return scrollPane;
    }

    private void configureSearchField() {
        searchField.setText(" Search a lobby..");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setForeground(Color.GRAY);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            // when user clicks on the field
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals(" Search a lobby..")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            // when user clicks outside the field
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(" Search a lobby..");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void configureButton(JButton button) {
        button.setBackground(new Color(49, 111, 124));
        button.setForeground(Color.black);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 22));
    }

    public JComponent createRoundedField(boolean isPassword, int columns, int radius) {
        JComponent field;

        if (isPassword) {
            field = new JPasswordField(columns) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D graphics = (Graphics2D) g.create();
                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    graphics.setColor(getBackground());
                    graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                    super.paintComponent(g);
                    graphics.dispose();
                }

                @Override
                protected void paintBorder(Graphics g) {
                    Graphics2D graphics = (Graphics2D) g.create();
                    graphics.setColor(Color.GRAY);
                    graphics.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                    graphics.dispose();
                }
            };
        } else {
            field = new JTextField(columns) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D graphics = (Graphics2D) g.create();
                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    graphics.setColor(getBackground());
                    graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                    super.paintComponent(g);
                    graphics.dispose();
                }

                @Override
                protected void paintBorder(Graphics g) {
                    Graphics2D graphics = (Graphics2D) g.create();
                    graphics.setColor(Color.GRAY);
                    graphics.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                    graphics.dispose();
                }
            };
        }

        field.setOpaque(false);
        ((JComponent) field).setBorder(new EmptyBorder(5, 10, 5, 10));
        field.setBackground(Color.WHITE);
        return field;
    }

    // a design for buttons
    public JButton createRoundedButton(String text, int radius) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(getBackground());
                graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                super.paintComponent(g);
                graphics.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setColor(getForeground());
                graphics.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                graphics.dispose();
            }
        };
        return button;
    }

    public JPanel addLobbyRow(String username, String lobbyName, int playerCount) {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new GridLayout(1, 2));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        rowPanel.setBackground(new Color(186, 185, 185, 255));
        rowPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(5, 40, 5, 40)
        ));

        JLabel userAndLobbyLabel = new JLabel("<html><div style='text-align: left;'>" + username + "<br>Lobby: " + lobbyName + "</div></html>");
        userAndLobbyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        userAndLobbyLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel countLabel = new JLabel("players: " + playerCount);
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        rowPanel.add(userAndLobbyLabel);
        rowPanel.add(countLabel);

        transparentPanel.add(rowPanel);
        transparentPanel.revalidate();
        transparentPanel.repaint();

        return rowPanel;
    }

    public void removeLobbyRow(JPanel row) {
        transparentPanel.remove(row);
        transparentPanel.revalidate();
        transparentPanel.repaint();
    }

    // Getters for UI components
    public JButton getLogoutButton() {
        return logoutButton;
    }

    public JButton getViewLeaderboardButton() {
        return viewLeaderboardButton;
    }

    public JButton getCreateLobbyButton() {
        return createLobbyButton;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JLabel getCountdownLabel() {
        return countdownLabel;
    }

    public JPanel getTransparentPanel() {return transparentPanel;}

    // Add to HomeScreenUI.java
    public void refreshLobbyList() {
        transparentPanel.revalidate();
        transparentPanel.repaint();
    }
}