package client_java.view.player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class CreateLobbyDialog extends JDialog {
    private JTextField nameField;
    private JButton cancelButton;
    private JButton yesButton;
    private HomeScreenUI parentView;

    public CreateLobbyDialog(HomeScreenUI parent) {
        super(parent, "Confirm", true);
        this.parentView = parent;
        initializeDialog();
    }

    private void initializeDialog() {
        setSize(400, 250);
        setLocationRelativeTo(getParent());
        setLayout(null);
        getContentPane().setBackground(new Color(192, 192, 192));

        setupNameField();
        setupConfirmLabel();
        setupCancelButton();
        setupYesButton();
    }

    private void setupNameField() {
        nameField = (JTextField) parentView.createRoundedField(false, 15, 40);
        nameField.setBounds(50, 30, 300, 40);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        nameField.setHorizontalAlignment(JTextField.LEFT);
        nameField.setText("Enter lobby name..");
        nameField.setForeground(Color.GRAY);

        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (nameField.getText().equals("Enter lobby name..")) {
                    nameField.setText("");
                    nameField.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (nameField.getText().isEmpty()) {
                    nameField.setText("Enter lobby name..");
                    nameField.setForeground(Color.GRAY);
                }
            }
        });

        add(nameField);
    }

    private void setupConfirmLabel() {
        JLabel confirmLabel = new JLabel("Create lobby?");
        confirmLabel.setBounds(130, 90, 200, 30);
        confirmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        add(confirmLabel);
    }

    private void setupCancelButton() {
        cancelButton = parentView.createRoundedButton("Cancel", 30);
        cancelButton.setBounds(50, 150, 120, 40);
        cancelButton.setBackground(new Color(161, 62, 62));
        cancelButton.setForeground(Color.white);
        cancelButton.setFocusPainted(false);
        cancelButton.setOpaque(false);
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        cancelButton.setBorder(BorderFactory.createEmptyBorder());
        add(cancelButton);
    }

    private void setupYesButton() {
        yesButton = parentView.createRoundedButton("Yes", 30);
        yesButton.setBounds(230, 150, 120, 40);
        yesButton.setBackground(new Color(47, 123, 61));
        yesButton.setForeground(Color.white);
        yesButton.setFocusPainted(false);
        yesButton.setOpaque(false);
        yesButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        yesButton.setBorder(BorderFactory.createEmptyBorder());
        add(yesButton);
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public JButton getYesButton() {
        return yesButton;
    }

    public String getLobbyName() {
        String name = nameField.getText();
        return name.equals("Enter lobby name..") ? "" : name;
    }

    public boolean isValidLobbyName(String name) {
        name = getLobbyName();
        return !name.isEmpty() && !name.equals("Enter lobby name..");
    }

    public void showInvalidNameMessage() {
        JOptionPane.showMessageDialog(this, "Please enter a valid lobby name.");
    }

    public void showErrorMessage(String s) {
    }
}