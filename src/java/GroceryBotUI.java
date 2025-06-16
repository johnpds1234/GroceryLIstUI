import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class GroceryBotUI extends JFrame implements ActionListener {
    private JTextField inputField;
    private JButton generateButton;
    private JTextArea responseArea;

    public GroceryBotUI() {
        setTitle("GroceryBot");
        setSize(600, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        Font mainFont = new Font("SansSerif", Font.PLAIN, 16);
        Font buttonFont = new Font("SansSerif", Font.BOLD, 16);

        inputField = new JTextField("Enter a meal request...");
        inputField.setFont(mainFont);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        generateButton = new JButton("Generate");
        generateButton.setFont(buttonFont);
        generateButton.setBackground(new Color(66, 133, 244));
        generateButton.setForeground(Color.BLACK);
        generateButton.setFocusPainted(false);
        generateButton.addActionListener(this);

        responseArea = new JTextArea();
        responseArea.setFont(mainFont);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setEditable(false);
        responseArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        responseArea.setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(inputField, BorderLayout.CENTER);
        topPanel.add(generateButton, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(responseArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String prompt = inputField.getText();
        responseArea.setText("Thinking like a Michelin chef...\n");
        String result = runOllama(prompt);
        responseArea.setText(result);
    }

    private String runOllama(String prompt) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL("http://localhost:XXXXXX/api/generate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String payload = String.format("""
                {
                  "model": "mistral",
                  "prompt": "%s",
                  "stream": false
                }
            """, prompt.replace("\"", "\\\""));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes());
                os.flush();
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }

            String json = response.toString();
            int start = json.indexOf("\"response\":\"") + 11;
            int end = json.indexOf("\",\"done\"");
            if (start >= 0 && end > start) {
                return json.substring(start, end)
                           .replace("\\n", "\n")
                           .replace("\\\"", "\"");
            } else {
                return "Failed to parse response.";
            }

        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
    }

    public static void main(String[] args) {
        new GroceryBotUI();
    }
}

