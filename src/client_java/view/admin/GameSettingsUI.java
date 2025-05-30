package client_java.view.admin;

import javax.swing.*;
import java.awt.*;

// GAME SETTINGS

public class GameSettingsUI extends JFrame {
    private int waitingTimeValue = 10; // placeholder
    private int roundDurationValue = 30; // placeholder
    private JLabel waitingTime;
    private JLabel roundDuration;

    public GameSettingsUI() {
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

        JPanel transparentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setColor(new Color(0x40FFFFFF, true));
                graphics.fillRect(0, 0, getWidth(), getHeight());
                graphics.dispose();
            }
        };
        transparentPanel.setLayout(null);
        transparentPanel.setBounds(170, 120, 640, 400);
        transparentPanel.setOpaque(false);

        JLabel changeWTLabel = new JLabel("Change waiting time");
        changeWTLabel.setBounds(50, 60, 300, 30);
        changeWTLabel.setForeground(Color.white);
        changeWTLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        transparentPanel.add(changeWTLabel);

        // buttons for WAITING TIME
        JButton minusOneWT = createRoundedButton("-1", 40);
        JButton minusFiveWT = createRoundedButton("-5", 40);
        JButton minusTenWT = createRoundedButton("-10", 40);
        waitingTime = new JLabel(String.valueOf(waitingTimeValue), SwingConstants.CENTER);
        JButton plusOneWT = createRoundedButton("+1", 40);
        JButton plusFiveWT = createRoundedButton("+5", 40);
        JButton plusTenWT = createRoundedButton("+10", 40);

        setupButton(minusOneWT, 220, 100);
        setupButton(minusFiveWT, 140, 100);
        setupButton(minusTenWT, 60, 100);
        setupButton(plusOneWT, 360, 100);
        setupButton(plusFiveWT, 440, 100);
        setupButton(plusTenWT, 520, 100);

        waitingTime.setBounds(300, 95, 50, 40);
        waitingTime.setForeground(Color.white);
        waitingTime.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        transparentPanel.add(minusOneWT);
        transparentPanel.add(minusFiveWT);
        transparentPanel.add(minusTenWT);
        transparentPanel.add(waitingTime);
        transparentPanel.add(plusOneWT);
        transparentPanel.add(plusFiveWT);
        transparentPanel.add(plusTenWT);

        JLabel changePRDLabel = new JLabel("Change per round duration");
        changePRDLabel.setBounds(50, 190, 300, 39);
        changePRDLabel.setForeground(Color.white);
        changePRDLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        transparentPanel.add(changePRDLabel);

        // buttons for PER ROUND DURATION
        JButton minusOnePRD = createRoundedButton("-1", 40);
        JButton minusFivePRD = createRoundedButton("-5", 40);
        JButton minusTenPRD = createRoundedButton("-10", 40);
        roundDuration = new JLabel(String.valueOf(roundDurationValue), SwingConstants.CENTER);
        JButton plusOnePRD = createRoundedButton("+1", 40);
        JButton plusFivePRD = createRoundedButton("+5", 40);
        JButton plusTenPRD = createRoundedButton("+10", 40);

        setupButton(minusOnePRD, 220, 240);
        setupButton(minusFivePRD, 140, 240);
        setupButton(minusTenPRD, 60, 240);
        setupButton(plusOnePRD, 360, 240);
        setupButton(plusFivePRD, 440, 240);
        setupButton(plusTenPRD, 520, 240);

        roundDuration.setBounds(300, 235, 50, 40);
        roundDuration.setForeground(Color.white);
        roundDuration.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        transparentPanel.add(minusOnePRD);
        transparentPanel.add(minusFivePRD);
        transparentPanel.add(minusTenPRD);
        transparentPanel.add(roundDuration);
        transparentPanel.add(plusOnePRD);
        transparentPanel.add(plusFivePRD);
        transparentPanel.add(plusTenPRD);

        // cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(220, 575, 250, 40);
        cancelButton.setBackground(new Color(49, 111, 124));
        cancelButton.setForeground(Color.black);
        cancelButton.setFocusPainted(false);
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        cancelButton.addActionListener(e -> {
            dispose();
            new CreatePlayerUI();
        });

        // OK button
        JButton okButton = new JButton("Ok");
        okButton.setBounds(520, 575, 250, 40);
        okButton.setBackground(new Color(49, 111, 124));
        okButton.setForeground(Color.black);
        okButton.setFocusPainted(false);
        okButton.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        okButton.addActionListener(e -> {
            System.out.println("Final Waiting Time: " + waitingTimeValue);
            System.out.println("Final Round Duration: " + roundDurationValue);
            dispose();
            new CreatePlayerUI();
        });

        panel.add(transparentPanel);
        panel.add(cancelButton);
        panel.add(okButton);

        setContentPane(panel);
        setVisible(true);
        SwingUtilities.invokeLater(() -> getContentPane().requestFocusInWindow());
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

    // designs of the buttons
    private void setupButton(JButton button, int x, int y) {
        button.setBounds(x, y, 70, 35);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        button.setBackground(Color.white);
        button.setOpaque(false);
        button.setFocusable(false);
    }

    // temporary
    public static void main(String[] args) {
        new GameSettingsUI();
    }
}