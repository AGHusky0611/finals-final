package client_java.view.admin;

import client_java.view.login.Login;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// CREATE PLAYERS/ HOMESCREEN
public class CreatePlayerUI extends JFrame {

    JPanel transparentPanel;
    private client_java.controller.admin.CreatePlayerController controller;

    public CreatePlayerUI() {
        controller = new client_java.controller.admin.CreatePlayerController(this);

        // set frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // temporary
        setIconImage(new ImageIcon("res/images/gameLogo.png").getImage());
        setTitle("What's The Word?");
        setSize(1000, 700);
        setLayout(null);
        setLocationRelativeTo(null);
        setResizable(false);

        // background of the frame
        ImageIcon backgroundIcon = new ImageIcon("res/images/background.png");
        Image backgroundImage = backgroundIcon.getImage();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setLayout(null);

        // logout button
        JButton logoutButton = createRoundedButton("logout", 30);
        logoutButton.setBounds(775, 25, 180, 45);
        logoutButton.setFocusPainted(false);
        logoutButton.setOpaque(false);
        logoutButton.setBackground(new Color(49, 111, 124));
        logoutButton.setForeground(Color.black);
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        logoutButton.addActionListener(e -> {
            new Login();
            dispose();
        });

        JLabel gameSettingsText = new JLabel("Game settings:");
        gameSettingsText.setBounds(20, 450, 200, 40);
        gameSettingsText.setForeground(new Color(174, 174, 174, 255));
        gameSettingsText.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel waitingTimeText = new JLabel("Waiting time: " + "10" + "s"); // placeholder
        waitingTimeText.setBounds(20, 485, 200, 40);
        waitingTimeText.setForeground(new Color(174, 174, 174, 255));
        waitingTimeText.setFont(new Font("Segoe UI", Font.PLAIN, 22));

        JLabel roundDurationText = new JLabel("Round duration: " + "30" + "s"); // placeholder
        roundDurationText.setBounds(20, 515, 200, 40);
        roundDurationText.setForeground(new Color(174, 174, 174, 255));
        roundDurationText.setFont(new Font("Segoe UI", Font.PLAIN, 22));

        // settings button
        JButton changeSettingsButton = new JButton("Change settings");
        changeSettingsButton.setBounds(20, 580, 250, 40);
        changeSettingsButton.setBackground(new Color(49, 111, 124));
        changeSettingsButton.setForeground(Color.black);
        changeSettingsButton.setFocusPainted(false);
        changeSettingsButton.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        changeSettingsButton.addActionListener(e -> {
            dispose();
            new GameSettingsUI();
        });

        // create player button
        JButton createPlayerAccountButton = new JButton("Create Player Account");
        createPlayerAccountButton.setBounds(310, 580, 620, 40);
        createPlayerAccountButton.setBackground(new Color(49, 111, 124));
        createPlayerAccountButton.setForeground(Color.black);
        createPlayerAccountButton.setFocusPainted(false);
        createPlayerAccountButton.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        createPlayerAccountButton.addActionListener(e -> {

            // new frame
            JDialog dialog = new JDialog();
            dialog.setTitle("Create player");
            dialog.setSize(400, 290);
            dialog.setLocationRelativeTo(null);
            dialog.setLayout(null);
            dialog.getContentPane().setBackground(new Color(192, 192, 192));
            dialog.setModal(true);

            // username field
            JTextField usernameField = (JTextField) createRoundedField(false, 15, 40);
            usernameField.setBounds(50, 30, 300, 40);
            usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            usernameField.setHorizontalAlignment(JTextField.LEFT);
            usernameField.setText("Username..");
            usernameField.setForeground(Color.GRAY);
            usernameField.addFocusListener(new java.awt.event.FocusAdapter() {
                // when user clicks on the field
                public void focusGained(java.awt.event.FocusEvent e1) {
                    if (usernameField.getText().equals("Username..")) {
                        usernameField.setText("");
                        usernameField.setForeground(Color.BLACK);
                    }
                }

                // when user clicks outside the field
                public void focusLost(java.awt.event.FocusEvent e2) {
                    if (usernameField.getText().isEmpty()) {
                        usernameField.setText("Username..");
                        usernameField.setForeground(Color.GRAY);
                    }
                }
            });
            dialog.add(usernameField);

            // password field
            JTextField passField = (JTextField) createRoundedField(false, 15, 40);
            passField.setBounds(50, 80, 300, 40);
            passField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            passField.setHorizontalAlignment(JTextField.LEFT);
            passField.setText("Password..");
            passField.setForeground(Color.GRAY);
            passField.addFocusListener(new java.awt.event.FocusAdapter() {
                // when user clicks on the field
                public void focusGained(java.awt.event.FocusEvent e1) {
                    if (passField.getText().equals("Password..")) {
                        passField.setText("");
                        passField.setForeground(Color.BLACK);
                    }
                }

                // when user clicks outside the field
                public void focusLost(java.awt.event.FocusEvent e2) {
                    if (passField.getText().isEmpty()) {
                        passField.setText("Password..");
                        passField.setForeground(Color.GRAY);
                    }
                }
            });
            dialog.add(passField);

            JLabel confirmLabel = new JLabel("Create player?");
            confirmLabel.setBounds(130, 140, 200, 30);
            confirmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
            dialog.add(confirmLabel);

            // cancel button
            JButton cancelButton = createRoundedButton("Cancel", 30);
            cancelButton.setBounds(50, 180, 120, 40);
            cancelButton.setBackground(new Color(161, 62, 62));
            cancelButton.setForeground(Color.white);
            cancelButton.setFocusPainted(false);
            cancelButton.setOpaque(false);
            cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            cancelButton.setBorder(BorderFactory.createEmptyBorder());
            cancelButton.addActionListener(e2 -> dialog.dispose());
            dialog.add(cancelButton);

            // yes button
            JButton yesButton = createRoundedButton("Yes", 30);
            yesButton.setBounds(230, 180, 120, 40);
            yesButton.setBackground(new Color(47, 123, 61));
            yesButton.setForeground(Color.white);
            yesButton.setFocusPainted(false);
            yesButton.setOpaque(false);
            yesButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            yesButton.setBorder(BorderFactory.createEmptyBorder());
            yesButton.addActionListener(e3 -> {
                String username = usernameField.getText();
                String password = passField.getText();if (!username.trim().equals("") && !username.trim().equals("Username..") &&
                        !password.trim().equals("") && !password.trim().equals("Password..")) {
                    System.out.println("player " + username + " created"); // placeholder
                    addAccountRow(username, password);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Please fill both fields to create an account.");
                }
            });
            dialog.add(yesButton);

            // when user clicks on the background, it loses focus on other elements
            dialog.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    dialog.requestFocusInWindow();
                }
            });

            // focuses on window instead of other elements
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowOpened(java.awt.event.WindowEvent e) {
                    dialog.requestFocusInWindow();
                }
            });
            dialog.setVisible(true);
        });

        transparentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setColor(new Color(0x40FFFFFF, true));
                graphics.fillRect(0, 0, getWidth(), getHeight());
                graphics.dispose();
            }
        };
        transparentPanel.setLayout(new BoxLayout(transparentPanel, BoxLayout.Y_AXIS));
        transparentPanel.setBounds(310, 150, 620, 430);
        transparentPanel.setOpaque(false);

        // search field
        JTextField searchField = (JTextField) createRoundedField(false, 15, 40);
        searchField.setBounds(310, 100, 620, 37);
        searchField.setText(" Search player..");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setForeground(Color.GRAY);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            // when user clicks on field
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals(" Search player..")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            @Override
            // when user clicks outside the field
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(" Search player..");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        JLabel playersText = new JLabel("PLAYERS");
        playersText.setBounds(310, 50, 200, 40);
        playersText.setForeground(new Color(174, 174, 174, 255));
        playersText.setFont(new Font("Segoe UI", Font.PLAIN, 22));

        panel.add(logoutButton);
        panel.add(gameSettingsText);
        panel.add(waitingTimeText);
        panel.add(roundDurationText);
        panel.add(changeSettingsButton);
        panel.add(createPlayerAccountButton);
        panel.add(transparentPanel);
        panel.add(searchField);
        panel.add(playersText);

        // when user clicks on the background, it loses focus on other elements
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                panel.requestFocusInWindow();
            }
        });

        setContentPane(panel);
        setVisible(true);
        SwingUtilities.invokeLater(() -> {
            getContentPane().requestFocusInWindow();
        });

        // Refresh player list on UI load
        controller.refreshPlayerList();
    }

    // adds a row panel for every account created
    public JPanel addAccountRow(String username, String status) {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new GridLayout(1, 3));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        rowPanel.setBackground(new Color(186, 185, 185, 255));
        rowPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(5, 40, 5, 10)
        ));

        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        usernameLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel playerStatus = new JLabel(status + "   ");
        playerStatus.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        playerStatus.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        deleteButton.setFocusPainted(false);
        deleteButton.setOpaque(true);
        deleteButton.setForeground(Color.white);
        deleteButton.setBackground(new Color(161, 62, 62));
        deleteButton.setHorizontalAlignment(SwingConstants.CENTER);
        deleteButton.setBorder(BorderFactory.createEmptyBorder());

        rowPanel.add(usernameLabel);
        rowPanel.add(playerStatus);
        rowPanel.add(deleteButton);

        rowPanel.putClientProperty("username", username);
        rowPanel.putClientProperty("status", status);

        rowPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                String storedUsername = (String) rowPanel.getClientProperty("username");
                String storedStatus = (String) rowPanel.getClientProperty("status");

                JDialog playerDetails = new JDialog();
                playerDetails.setTitle("Account");
                playerDetails.setSize(400, 200);
                playerDetails.setLocationRelativeTo(null);
                playerDetails.setLayout(null);
                playerDetails.getContentPane().setBackground(new Color(192, 192, 192));
                playerDetails.setModal(true);

                JLabel userText = new JLabel("Username: " + storedUsername);
                userText.setBounds(20,15,350,30);
                userText.setFont(new Font("Segoe UI", Font.PLAIN, 22));

                JLabel statusText = new JLabel("Status: " + storedStatus);
                statusText.setBounds(20,50,350,30);
                statusText.setFont(new Font("Segoe UI", Font.PLAIN, 22));

                JButton okButton = createRoundedButton("OK",30);
                okButton.setBounds(150, 100, 100, 35);
                okButton.setFocusPainted(false);
                okButton.setOpaque(false);
                okButton.setBackground(new Color(60, 60, 60));
                okButton.setForeground(Color.white);
                okButton.setFont(new Font("Segoe UI", Font.PLAIN, 22));
                okButton.addActionListener(e1 -> {
                    playerDetails.dispose();
                });

                playerDetails.add(userText);
                playerDetails.add(statusText);
                playerDetails.add(okButton);
                playerDetails.setVisible(true);
            }
        });

        transparentPanel.add(rowPanel);
        transparentPanel.revalidate();
        transparentPanel.repaint();

        return rowPanel;
    }

    public void clearPlayerList() {
        transparentPanel.removeAll();
        transparentPanel.revalidate();
        transparentPanel.repaint();
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // a design for textfields and password fields
    private JComponent createRoundedField(boolean isPassword, int columns, int radius) {
        JComponent field;

        if (isPassword) {
            // password field
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
            // textfield
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
    private JButton createRoundedButton(String text, int radius) {
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

    // temporary
    public static void main(String[] args) {
        CreatePlayerUI run = new CreatePlayerUI();
    }
}