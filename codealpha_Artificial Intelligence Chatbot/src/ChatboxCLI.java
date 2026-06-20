import java.io.IOException;
import java.util.Scanner;


public class ChatbotCLI {
    public static void main(String[] args) throws IOException {
        ChatbotEngine engine = new ChatbotEngine("data/faqs.txt", "data/unanswered.txt");
        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================================");
        System.out.println(" JavaBot (CLI mode) — " + engine.getKnowledgeBaseSize() + " FAQ topics loaded");
        System.out.println(" Type your question, or 'bye' to exit.");
        System.out.println("=================================================");
        System.out.println("JavaBot: Hi! How can I help you today?");

        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine();
            String response = engine.getResponse(input);
            System.out.println("JavaBot: " + response);
            if (input.toLowerCase().matches(".*\\b(bye|goodbye|exit|quit)\\b.*")) {
                break;
            }
        }
        scanner.close();
    }
}
