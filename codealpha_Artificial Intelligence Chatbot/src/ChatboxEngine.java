import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;


 */
public class ChatbotEngine {

    private final List<FAQEntry> knowledgeBase = new ArrayList<>();
    private final String unansweredLogPath;
    private static final double CONFIDENCE_THRESHOLD = 0.30;

    private String userName = null;

    public ChatbotEngine(String faqFilePath, String unansweredLogPath) throws IOException {
        this.unansweredLogPath = unansweredLogPath;
        loadKnowledgeBase(faqFilePath);
    }

   
    private void loadKnowledgeBase(String faqFilePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(faqFilePath), StandardCharsets.UTF_8);
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            String[] parts = trimmed.split("\\|\\|");
            if (parts.length < 2) continue;

            String question = parts[0].trim();
            String answer = parts[1].trim();
            String extraKeywords = parts.length > 2 ? parts[2].trim() : "";

            List<String> tokens = new ArrayList<>(NLPUtils.processText(question));
            if (!extraKeywords.isEmpty()) {
                for (String kw : extraKeywords.split(",")) {
                    tokens.addAll(NLPUtils.processText(kw.trim()));
                }
            }
            knowledgeBase.add(new FAQEntry(question, answer, tokens));
        }
    }

    
    public String getResponse(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return "I didn't quite catch that — could you type your question?";
        }

        String ruleBasedReply = tryRuleBasedIntent(userInput);
        if (ruleBasedReply != null) return ruleBasedReply;

        return matchFAQ(userInput);
    }

   
    private String tryRuleBasedIntent(String input) {
        String lower = input.toLowerCase().trim();

        if (lower.matches(".*\\b(hi|hello|hey|good morning|good evening|good afternoon)\\b.*")) {
            return userName != null
                    ? "Hello again, " + userName + "! How can I help you today?"
                    : "Hello! I'm JavaBot. How can I help you today? (You can also tell me your name.)";
        }

        Matcher nameMatcher = Pattern.compile("my name is ([a-zA-Z]+)").matcher(lower);
        if (nameMatcher.find()) {
            userName = capitalize(nameMatcher.group(1));
            return "Nice to meet you, " + userName + "! What can I help you with?";
        }

        if (lower.matches(".*\\b(bye|goodbye|see you|exit|quit)\\b.*")) {
            return "Goodbye" + (userName != null ? ", " + userName : "") + "! Have a great day.";
        }

        if (lower.matches(".*\\b(thanks|thank you|thx|appreciate it)\\b.*")) {
            return "You're welcome! Anything else I can help with?";
        }

        if (lower.matches(".*\\bwhat time is it\\b.*") || lower.matches(".*\\bcurrent time\\b.*")) {
            return "The current server time is " + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + ".";
        }

        if (lower.matches(".*\\b(help|menu|options)\\b.*") && lower.split("\\s+").length <= 3) {
            return "I can answer FAQs about accounts, passwords, orders, payments, refunds, and more. Just type your question!";
        }

        if (lower.matches(".*\\bhow are you\\b.*")) {
            return "I'm running smoothly, thanks for asking! How can I assist you?";
        }

        return null; 
    }

 
    private String matchFAQ(String userInput) {
        List<String> inputTokens = NLPUtils.processText(userInput);

        FAQEntry bestMatch = null;
        double bestScore = 0.0;

        for (FAQEntry entry : knowledgeBase) {
            double score = NLPUtils.cosineSimilarity(inputTokens, entry.getProcessedTokens());
            if (score > bestScore) {
                bestScore = score;
                bestMatch = entry;
            }
        }

        if (bestMatch != null && bestScore >= CONFIDENCE_THRESHOLD) {
            return bestMatch.getAnswer();
        }

        logUnanswered(userInput, bestMatch, bestScore);
        return "I'm not fully sure about that yet. I've logged your question so we can improve my answers. "
                + "Could you try rephrasing, or ask about accounts, orders, payments, or refunds?";
    }

    
    private void logUnanswered(String question, FAQEntry closest, double score) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(unansweredLogPath, true))) {
            bw.write(String.format("[unanswered] \"%s\" (closest=%.2f: %s)%n",
                    question, score, closest == null ? "none" : closest.getQuestion()));
        } catch (IOException e) {
            
            System.err.println("Could not write to unanswered log: " + e.getMessage());
        }
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public int getKnowledgeBaseSize() {
        return knowledgeBase.size();
    }
}
