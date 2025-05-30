package client_java.view.login;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Login extends JFrame {
    private JTextField usernameField;
    private JPasswordField passField;
    private JButton loginButton;

    public Login() {
        setupFrame();
        setupBackground();
        setupUsernameField();
        setupPasswordField();
        setupLoginButton();
        setVisible(true);
    }

    private void setupFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("res/logo/Hangman_Logo.jpg").getImage());
        setTitle("What's The Word?");
        setSize(1000, 700);
        setLayout(null);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void setupBackground() {
        ImageIcon backgroundIcon = new ImageIcon("res/images/loginBackground.png");
        Image backgroundImage = backgroundIcon.getImage();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setLayout(null);
        setContentPane(panel);
    }

    private void setupUsernameField() {
        usernameField = createRoundedTextField(15, 20);
        usernameField.setBounds(430, 360, 470, 50);
        usernameField.setText("Username..");
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        usernameField.setForeground(Color.GRAY);

        usernameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (usernameField.getText().equals("Username..")) {
                    usernameField.setText("");
                    usernameField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (usernameField.getText().isEmpty()) {
                    usernameField.setText("Username..");
                    usernameField.setForeground(Color.GRAY);
                }
            }
        });

        add(usernameField);
    }

    private void setupPasswordField() {
        passField = createRoundedPasswordField(15, 20);
        passField.setBounds(430, 425, 470, 50);
        passField.setText("Password..");
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        passField.setForeground(Color.GRAY);

        passField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                String password = new String(passField.getPassword());
                if (password.equals("Password..")) {
                    passField.setText("");
                    passField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String password = new String(passField.getPassword());
                if (password.isEmpty()) {
                    passField.setText("Password..");
                    passField.setForeground(Color.GRAY);
                }
            }
        });

        add(passField);
    }

    private void setupLoginButton() {
        loginButton = createRoundedButton("Login", 30);
        loginButton.setBounds(530, 520, 260, 45);
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(false);
        loginButton.setBackground(new Color(49, 111, 124));
        loginButton.setForeground(Color.black);
        loginButton.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        add(loginButton);
    }

    private JTextField createRoundedTextField(int columns, int radius) {
        JTextField field = new JTextField(columns) {
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
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(5, 10, 5, 10));
        field.setBackground(Color.WHITE);
        return field;
    }

    private JPasswordField createRoundedPasswordField(int columns, int radius) {
        JPasswordField field = new JPasswordField(columns) {
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
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(5, 10, 5, 10));
        field.setBackground(Color.WHITE);
        return field;
    }

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

    // Getters
    public String getUsername() {
        return usernameField.getText();
    }

    public char[] getPassword() {
        return passField.getPassword();
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Login();
        });
    }
}