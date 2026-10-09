package com.parkir.ui;

import com.parkir.exception.ParkirException;
import com.parkir.model.User;
import com.parkir.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {
    private final AuthService authService;
    private final Consumer<User> onLoginBerhasil;

    private JTextField tfUsername;
    private JPasswordField tfPassword;
    private JLabel lblError;
    private JButton btnLogin;

    public LoginPanel(AuthService authService, Consumer<User> onLoginBerhasil) {
        this.authService = authService;
        this.onLoginBerhasil = onLoginBerhasil;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_APP);

        JPanel card = UITheme.createCard();
        card.setPreferredSize(new Dimension(380, 420));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Judul Aplikasi
        JLabel lblApp = new JLabel("SISTEM PARKIR");
        lblApp.setFont(UITheme.FONT_TITLE);
        lblApp.setForeground(UITheme.PRIMARY);
        lblApp.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Silakan masuk dengan akun Anda");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(UITheme.TEXT_MAIN);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfUsername = new JTextField();
        tfUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tfUsername.setFont(UITheme.FONT_BODY);

        // Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UITheme.FONT_BOLD);
        lblPass.setForeground(UITheme.TEXT_MAIN);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfPassword = new JPasswordField();
        tfPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tfPassword.setFont(UITheme.FONT_BODY);

        // Label Error
        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tombol Login
        btnLogin = UITheme.createPrimaryButton("Masuk");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // JLabel lblHint = new JLabel("Default admin: admin / admin123");
        // lblHint.setFont(UITheme.FONT_SMALL);
        // lblHint.setForeground(UITheme.TEXT_MUTED);
        // lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Pasang listener
        btnLogin.addActionListener(e -> executeLogin());
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeLogin();
                }
            }
        };
        tfUsername.addKeyListener(enterKey);
        tfPassword.addKeyListener(enterKey);

        // Tata letak card
        card.add(Box.createVerticalStrut(10));
        card.add(lblApp);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(24));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(4));
        card.add(tfUsername);
        card.add(Box.createVerticalStrut(14));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(4));
        card.add(tfPassword);
        card.add(Box.createVerticalStrut(8));
        card.add(lblError);
        card.add(Box.createVerticalStrut(12));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(14));
        card.add(lblHint);

        add(card);
    }

    private void executeLogin() {
        String u = tfUsername.getText();
        String p = new String(tfPassword.getPassword());

        try {
            User user = authService.login(u, p);
            lblError.setText(" ");
            tfPassword.setText("");
            onLoginBerhasil.accept(user);
        } catch (ParkirException e) {
            lblError.setText(e.getMessage());
        } catch (Exception e) {
            lblError.setText("Terjadi kesalahan sistem");
        }
    }

    public void resetForm() {
        tfUsername.setText("");
        tfPassword.setText("");
        lblError.setText(" ");
        tfUsername.requestFocusInWindow();
    }
}
