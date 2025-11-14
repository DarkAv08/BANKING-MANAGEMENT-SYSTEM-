import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Account Record (Data Model)
 * Note: Must be public for the ObjectMapper to access it, and defined outside the main class.
 */
class Account {
    public int id;
    public String name;
    public double balance;

    // Default constructor required for ObjectMapper
    public Account() {} 

    public Account(int id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %d) - $%.2f", name, id, balance);
    }
}

/**
 * Main Swing GUI Application for the Banking System.
 */
public class BankingSystemGUI extends JFrame {

    private static final String API_BASE_URL = "http://localhost:8080/api";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // UI Components
    private JPanel accountsPanel;
    private JComboBox<Integer> fromAccountComboBox;
    private JComboBox<Integer> toAccountComboBox;
    private JTextField amountField;
    private JTextField newNameField;
    private JTextField newDepositField;
    private JButton refreshButton;
    private JButton transferButton;
    private JButton createButton;

    private List<Account> currentAccounts = new ArrayList<>();

    public BankingSystemGUI() {
        super("Advanced Java: Swing Banking Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Use a modern look and feel if available
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set System Look and Feel.");
        }

        initializeUI();
        pack();
        setLocationRelativeTo(null); // Center the window
        setVisible(true);

        // Fetch initial data on startup
        fetchAccounts(); 
    }

    // --- UI Setup ---

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 1. Header
        JLabel header = new JLabel("Banking Management System", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(new Color(60, 60, 120)); // Indigo-like color
        add(header, BorderLayout.NORTH);

        // 2. Main Content Area (West: Balances, Center: Forms)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Panel for Balances (takes 1/3 width)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        contentPanel.add(buildBalancePanel(), gbc);

        // Panel for Forms (Transfer and Create, takes 2/3 width)
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        contentPanel.add(buildFormsPanel(), gbc);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel buildBalancePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Current Account Balances"));
        panel.setPreferredSize(new Dimension(300, 400));
        
        accountsPanel = new JPanel();
        accountsPanel.setLayout(new BoxLayout(accountsPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(accountsPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshButton = new JButton("Refresh Balances");
        refreshButton.setBounds(1,0,20,20);
        refreshButton.setBackground(new Color(60, 60, 120)); // Indigo
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(this::handleRefresh);
        
        panel.add(refreshButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildFormsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 15, 15));
        panel.add(buildTransferPanel());
        panel.add(buildCreationPanel());
        return panel;
    }

    private JPanel buildTransferPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 100, 0)), "Transfer Funds"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: From Account
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(new JLabel("Source Account (ID):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        fromAccountComboBox = new JComboBox<>();
        panel.add(fromAccountComboBox, gbc);

        // Row 2: To Account
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("Destination Account (ID):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        toAccountComboBox = new JComboBox<>();
        panel.add(toAccountComboBox, gbc);

        // Row 3: Amount
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        panel.add(new JLabel("Amount ($):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        amountField = new JTextField();
        panel.add(amountField, gbc);

        // Row 4: Button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0;
        transferButton = new JButton("Execute Transfer");
        transferButton.setBackground(new Color(0, 100, 0)); // Dark Green
        transferButton.setForeground(Color.BLACK);
        transferButton.setFocusPainted(false);
        transferButton.addActionListener(this::handleTransfer);
        panel.add(transferButton, gbc);

        return panel;
    }

    private JPanel buildCreationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 100, 0)), "Open New Account"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(new JLabel("Holder Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        newNameField = new JTextField();
        panel.add(newNameField, gbc);

        // Row 2: Initial Deposit
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("Initial Deposit ($):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        newDepositField = new JTextField("100.00"); // Default to minimum
        panel.add(newDepositField, gbc);

        // Row 3: Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0;
        createButton = new JButton("Create Account");
        createButton.setBackground(new Color(0, 128, 0)); // Slightly brighter green
        createButton.setForeground(Color.BLACK);
        createButton.setFocusPainted(false);
        createButton.addActionListener(this::handleCreateAccount);
        panel.add(createButton, gbc);
        
        return panel;
    }
    
    // --- Helper Methods ---

    /**
     * Updates the accounts panel and the transfer combo boxes.
     */
    private void updateUI(List<Account> accounts) {
        currentAccounts = accounts;
        
        // 1. Update Balances Panel
        accountsPanel.removeAll();
        for (Account acc : accounts) {
            JLabel balanceLabel = new JLabel(String.format("ID %d: %s | $%.2f", acc.id, acc.name, acc.balance));
            balanceLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
            balanceLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            balanceLabel.setBackground(new Color(240, 240, 255)); // Light blue/indigo background
            balanceLabel.setOpaque(true);
            accountsPanel.add(balanceLabel);
            accountsPanel.add(Box.createVerticalStrut(5));
        }
        accountsPanel.revalidate();
        accountsPanel.repaint();

        // 2. Update Combo Boxes - FIX IMPLEMENTED HERE: Use two separate models
        DefaultComboBoxModel<Integer> fromModel = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Integer> toModel = new DefaultComboBoxModel<>();

        for (Account acc : accounts) {
            fromModel.addElement(acc.id);
            toModel.addElement(acc.id);
        }
        
        // Assign the two unique models
        fromAccountComboBox.setModel(fromModel);
        toAccountComboBox.setModel(toModel);
        
        // Select the first account if available
        if (!accounts.isEmpty()) {
            fromAccountComboBox.setSelectedIndex(0);
            // Crucially, we set the destination index to the second account (index 1) 
            // to avoid the immediate "transfer to self" error and provide a better UX.
            toAccountComboBox.setSelectedIndex(Math.min(1, accounts.size() - 1));
        }
    }

    /**
     * Shows a message dialog.
     */
    private void showMessage(String message, String title, int type) {
        JOptionPane.showMessageDialog(this, message, title, type);
    }
    
    /**
     * Parses the JSON response body.
     */
    private String getErrorMessage(HttpResponse<String> response) throws IOException {
        try {
            // Spring returns a JSON map like {"error": "message"}
            java.util.Map<String, String> errorMap = mapper.readValue(response.body(), new TypeReference<>() {});
            return errorMap.getOrDefault("error", "Unknown API error.");
        } catch (Exception e) {
            // Fallback if the body is not standard JSON
            return "API returned status " + response.statusCode() + " but body was malformed.";
        }
    }

    // --- Action Handlers ---

    private void handleRefresh(ActionEvent e) {
        fetchAccounts();
    }

    private void handleTransfer(ActionEvent e) {
        // SwingWorker handles long operations without freezing the UI
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // Ensure the application is still running before attempting to use the API
                if (!isSpringBackendRunning()) {
                    return "Error: Spring Boot Backend is not running on port 8080.";
                }
                
                try {
                    int fromId = (Integer) fromAccountComboBox.getSelectedItem();
                    int toId = (Integer) toAccountComboBox.getSelectedItem();
                    double amount = Double.parseDouble(amountField.getText());

                    if (fromId == toId) {
                        // This check is still necessary if the user manually selects the same account
                        return "Error: Cannot transfer money to the same account. Please choose a different destination.";
                    }

                    return performTransfer(fromId, toId, amount);

                } catch (NumberFormatException ex) {
                    return "Error: Please enter a valid numerical amount.";
                } catch (NullPointerException ex) {
                    return "Error: Please select both source and destination accounts.";
                } catch (Exception ex) {
                    return "An unexpected local error occurred: " + ex.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.startsWith("Success")) {
                        showMessage("Transfer successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        amountField.setText("");
                        fetchAccounts(); // Refresh balances immediately
                    } else if (result.startsWith("Error")) {
                        showMessage(result.substring(7).trim(), "Transaction Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    showMessage("Background task failed: " + ex.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void handleCreateAccount(ActionEvent e) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                if (!isSpringBackendRunning()) {
                    return "Error: Spring Boot Backend is not running on port 8080.";
                }
                
                try {
                    String name = newNameField.getText();
                    double deposit = Double.parseDouble(newDepositField.getText());
                    
                    return performAccountCreation(name, deposit);

                } catch (NumberFormatException ex) {
                    return "Error: Please enter a valid numerical deposit amount (Min $100).";
                } catch (Exception ex) {
                    return "An unexpected local error occurred: " + ex.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.startsWith("Success")) {
                        showMessage(result.substring(7).trim(), "Account Created", JOptionPane.INFORMATION_MESSAGE);
                        newNameField.setText("");
                        newDepositField.setText("100.00");
                        fetchAccounts(); // Refresh to see the new account
                    } else if (result.startsWith("Error")) {
                        showMessage(result.substring(7).trim(), "Creation Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    showMessage("Background task failed: " + ex.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // --- API Methods (Blocking calls, must be run in background thread) ---

    private boolean isSpringBackendRunning() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/accounts"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            // Connection refused or general network error
            return false;
        }
    }
    
    private void fetchAccounts() {
        new SwingWorker<List<Account>, Void>() {
            @Override
            protected List<Account> doInBackground() throws Exception {
                if (!isSpringBackendRunning()) {
                    throw new IOException("Spring Boot Backend is not running on port 8080.");
                }
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE_URL + "/accounts"))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return mapper.readValue(response.body(), new TypeReference<List<Account>>() {});
                } else {
                    throw new IOException("Failed to fetch accounts. Status: " + response.statusCode());
                }
            }

            @Override
            protected void done() {
                try {
                    List<Account> accounts = get();
                    updateUI(accounts);
                } catch (InterruptedException | ExecutionException ex) {
                    String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    showMessage("Error fetching accounts: " + message, "Connection Error", JOptionPane.ERROR_MESSAGE);
                    updateUI(new ArrayList<>()); // Clear UI on error
                }
            }
        }.execute();
    }
    
    private String performTransfer(int fromId, int toId, double amount) throws IOException, InterruptedException {
        String jsonBody = String.format(
            "{\"fromAccountId\":%d, \"toAccountId\":%d, \"amount\":%.2f}",
            fromId, toId, amount
        );
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/transfer"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return "Success: " + mapper.readValue(response.body(), new TypeReference<java.util.Map<String, String>>() {}).get("message");
        } else {
            return "Error: " + getErrorMessage(response);
        }
    }

    private String performAccountCreation(String name, double deposit) throws IOException, InterruptedException {
        String jsonBody = String.format(
            "{\"name\":\"%s\", \"initialDeposit\":%.2f}",
            name, deposit
        );
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/accounts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) { // 201 CREATED status
            java.util.Map<String, Object> responseMap = mapper.readValue(response.body(), new TypeReference<java.util.Map<String, Object>>() {});
            String message = (String) responseMap.get("message");
            int newId = (Integer) responseMap.get("accountId");
            return "Success: " + message + " New ID: " + newId;
        } else {
            return "Error: " + getErrorMessage(response);
        }
    }

    // --- Main Method ---

    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread (Swing standard practice)
        SwingUtilities.invokeLater(BankingSystemGUI::new);
    }
}