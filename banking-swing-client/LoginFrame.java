import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Initial login screen for the banking client application.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    public LoginFrame() {
        super("Banking Client Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set System Look and Feel.");
        }

        initializeUI();
        pack();
        setSize(400, 250); // Fixed size for the login window
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel header = new JLabel("Secure Banking Login", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(header, gbc);

        // Username Field
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        usernameField = new JTextField("admin");
        panel.add(usernameField, gbc);

        // Password Field
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        passwordField = new JPasswordField("password");
        panel.add(passwordField, gbc);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(60, 60, 120));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(this::handleLogin);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(loginButton, gbc);
        
        // Message Label
        messageLabel = new JLabel("Use 'admin' / 'password'", SwingConstants.CENTER);
        messageLabel.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(messageLabel, gbc);

        add(panel, BorderLayout.CENTER);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Simple client-side authentication for this demo
        if ("admin".equals(username) && "password".equals(password)) {
            messageLabel.setText("Login Successful! Starting system...");
            messageLabel.setForeground(new Color(0, 128, 0));
            
            // Close the login window
            this.dispose(); 
            // Open the main application
            SwingUtilities.invokeLater(BankingSystemGUI::new); 
        } else {
            messageLabel.setText("Invalid Username or Password.");
            messageLabel.setForeground(Color.RED);
        }
    }

    public static void main(String[] args) {
        // Start the application with the login screen
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}