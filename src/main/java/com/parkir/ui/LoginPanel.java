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
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_APP);

        add(createBrandPanel(), BorderLayout.WEST);
        add(createFormPanel(), BorderLayout.CENTER);
    }

    // Sisi kiri: identitas aplikasi
    private JPanel createBrandPanel() {
        JPanel brand = new JPanel();
        brand.setBackground(UITheme.BG_DARK);
        brand.setPreferredSize(new Dimension(420, 0));
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(BorderFactory.createEmptyBorder(0, 48, 0, 48));

        JLabel lblApp = new JLabel("PARKIR SYSTEM");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblApp.setForeground(Color.WHITE);
        lblApp.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTag = new JLabel("Kelola parkir, tarif, dan laporan.");
        lblTag.setFont(UITheme.FONT_BODY);
        lblTag.setForeground(UITheme.SIDEBAR_TEXT);
        lblTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(Box.createVerticalGlue());
        brand.add(lblApp);
        brand.add(Box.createVerticalStrut(8));
        brand.add(lblTag);
        brand.add(Box.createVerticalGlue());
        return brand;
    }

    // Sisi kanan: form login
    private JPanel createFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.BG_APP);

        JPanel card = UITheme.createCard();
        card.setPreferredSize(new Dimension(380, 400));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblHead = new JLabel("Masuk");
        lblHead.setFont(UITheme.FONT_TITLE);
        lblHead.setForeground(UITheme.TEXT_MAIN);
        lblHead.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Gunakan akun Anda untuk melanjutkan");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(UITheme.TEXT_MAIN);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfUsername = new JTextField();
        UITheme.styleField(tfUsername);
        tfUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tfUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UITheme.FONT_BOLD);
        lblPass.setForeground(UITheme.TEXT_MAIN);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfPassword = new JPasswordField();
        UITheme.styleField(tfPassword);
        tfPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tfPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnLogin = UITheme.createPrimaryButton("Masuk");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

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

        card.add(lblHead);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(28));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(6));
        card.add(tfUsername);
        card.add(Box.createVerticalStrut(16));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(6));
        card.add(tfPassword);
        card.add(Box.createVerticalStrut(10));
        card.add(lblError);
        card.add(Box.createVerticalStrut(14));
        card.add(btnLogin);
        card.add(Box.createVerticalGlue());

        wrapper.add(card);
        return wrapper;
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