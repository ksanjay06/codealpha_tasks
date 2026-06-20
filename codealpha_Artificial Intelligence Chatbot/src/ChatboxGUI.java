import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class ChatbotGUI extends JFrame {

    private final ChatbotEngine engine;
    private final JTextPane chatPane;
    private final JTextField inputField;
    private final StyledDocument doc;

    private static final Color BOT_COLOR = new Color(0x2F, 0x55, 0x96);
    private static final Color USER_COLOR = new Color(0x1E, 0x7E, 0x34);
    private static final Color BG_COLOR = new Color(0xF5, 0xF7, 0xFA);

    public ChatbotGUI(ChatbotEngine engine) {
        super("JavaBot — AI FAQ Chatbot");
        this.engine = engine;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 680);
        setMinimumSize(new Dimension(420, 480));
        setLocationRelativeTo(null);

        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(BG_COLOR);
        chatPane.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatPane.setMargin(new Insets(12, 12, 12, 12));
        doc = chatPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton sendButton = new JButton("Send");
        sendButton.setFocusPainted(false);
        sendButton.setBackground(BOT_COLOR);
        sendButton.setForeground(Color.WHITE);

        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JLabel header = new JLabel("  JavaBot — Ask me a question", JLabel.LEFT);
        header.setOpaque(true);
        header.setBackground(BOT_COLOR);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.setPreferredSize(new Dimension(100, 40));

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        ActionListener sendAction = e -> handleSend();
        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);

        appendMessage("JavaBot", greeting(), BOT_COLOR);
        inputField.requestFocusInWindow();
    }

    private String greeting() {
        return "Hi! I'm JavaBot \uD83E\uDD16. I can answer FAQs about accounts, orders, "
                + "payments, refunds, and more (" + engine.getKnowledgeBaseSize() + " topics loaded). "
                + "Type your question below to get started!";
    }

    private void handleSend() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        appendMessage("You", text, USER_COLOR);
        inputField.setText("");

        
        SwingUtilities.invokeLater(() -> {
            String reply = engine.getResponse(text);
            appendMessage("JavaBot", reply, BOT_COLOR);
        });
    }

    private void appendMessage(String sender, String message, Color color) {
        try {
            SimpleAttributeSet senderStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(senderStyle, color);
            StyleConstants.setBold(senderStyle, true);

            SimpleAttributeSet bodyStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(bodyStyle, Color.DARK_GRAY);

            SimpleAttributeSet timeStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(timeStyle, Color.GRAY);
            StyleConstants.setItalic(timeStyle, true);
            StyleConstants.setFontSize(timeStyle, 11);

            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));

            doc.insertString(doc.getLength(), sender + "  ", senderStyle);
            doc.insertString(doc.getLength(), timestamp + "\n", timeStyle);
            doc.insertString(doc.getLength(), message + "\n\n", bodyStyle);

            chatPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ChatbotEngine engine = new ChatbotEngine("data/faqs.txt", "data/unanswered.txt");
                new ChatbotGUI(engine).setVisible(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to load knowledge base: " + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
