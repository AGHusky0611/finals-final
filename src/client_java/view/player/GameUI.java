package client_java.view.player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

public class GameUI extends JFrame {
    private JTextField answerField;
    private JLabel word, triesLeft;
    private JLabel hangmanImageLabel; // Reference to the hangman image label
    private ImageIcon image;

    public GameUI() {
        // ======== set up frame ========= //
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("res/logo/Hangman_Logo.jpg").getImage());
        setTitle("What's The Word?");
        setSize(1000, 700);
        setLayout(null);
        setLocationRelativeTo(null);
        setResizable(false);

        // ======== background designs ========= //
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

        JLabel roundNum = new JLabel("Round " + "1");
        roundNum.setBounds(50, 40, 150, 30);
        roundNum.setFont(new Font("Segoe UI", Font.PLAIN, 25));
        roundNum.setForeground(Color.white);

        JLabel countdown = new JLabel("30");
        countdown.setBounds(460, 35, 100, 60);
        countdown.setFont(new Font("Segoe UI", Font.PLAIN, 50));
        countdown.setForeground(Color.white);

        JPanel lettersPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setColor(new Color(0x40FFFFFF, true));
                graphics.fillRect(0, 0, getWidth(), getHeight());
                graphics.dispose();
            }
        };
        lettersPanel.setLayout(new BoxLayout(lettersPanel, BoxLayout.Y_AXIS));
        lettersPanel.setBounds(50, 150, 150, 200);
        lettersPanel.setOpaque(false);

        word = new JLabel("a _ r a _ _ d a _ _ a", SwingConstants.CENTER);
        word.setBounds(70, 400, 850, 50);
        word.setForeground(Color.white);
        word.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        word.setHorizontalAlignment(SwingConstants.CENTER);
        word.setVerticalAlignment(SwingConstants.CENTER);

        answerField = (JTextField) createRoundedField(false, 15, 40);
        answerField.setBounds(200, 470, 560, 50);
        answerField.setText("ANSWER..");
        answerField.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        answerField.setForeground(Color.GRAY);
        answerField.setBackground(new Color(30, 30, 30, 255));
        answerField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (answerField.getText().equals("ANSWER..")) {
                    answerField.setText("");
                    answerField.setForeground(Color.white);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (answerField.getText().isEmpty()) {
                    answerField.setText("ANSWER..");
                    answerField.setForeground(Color.GRAY);
                }
            }
        });

        // Initialize hangman image label
        hangmanImageLabel = new JLabel(changeImage(1));
        hangmanImageLabel.setBounds(650, 100, 300, 500);

        triesLeft = new JLabel("Tries left: " + "5");
        triesLeft.setBounds(50, 550, 200, 35);
        triesLeft.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        triesLeft.setForeground(Color.white);

        JLabel roundsWon = new JLabel("Rounds won: " + "0");
        roundsWon.setBounds(50, 590, 250, 35);
        roundsWon.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        roundsWon.setForeground(Color.white);

        JLabel tip = new JLabel("Tip: be the first to win 3 rounds");
        tip.setBounds(700, 610, 300, 30);
        tip.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        tip.setForeground(new Color(141, 141, 141, 255));

        panel.add(roundNum);
        panel.add(countdown);
        panel.add(lettersPanel);
        panel.add(word);
        panel.add(answerField);
        panel.add(hangmanImageLabel); // Add the hangman image label
        panel.add(triesLeft);
        panel.add(roundsWon);
        panel.add(tip);

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                panel.requestFocusInWindow();
            }
        });
        setContentPane(panel);
        setVisible(true);
        SwingUtilities.invokeLater(() -> panel.requestFocusInWindow());
    }

    private JComponent createRoundedField(boolean isPassword, int columns, int radius) {
        JComponent field = isPassword ? new JPasswordField(columns) : new JTextField(columns);

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

        field.setOpaque(false);
        field.setBorder(new EmptyBorder(5, 10, 5, 10));
        field.setBackground(Color.WHITE);
        return field;
    }

    public JTextField getAnswerField() { return answerField; }
    public JLabel getWord() { return word; }
    public JLabel getTriesLeft() { return triesLeft; }
    public void setAnswerField(KeyListener listener) {
        answerField.addKeyListener(listener);
    }
    public void setAnswerFieldInteract(boolean enabled) {
        answerField.setEnabled(enabled);
    }

    public ImageIcon changeImage(int stage) {
        String imagePath = "res/images/stage" + stage + ".png";
        image = new ImageIcon(imagePath);
        if (hangmanImageLabel != null) {
            hangmanImageLabel.setIcon(image);
        }
        return image;
    }
}