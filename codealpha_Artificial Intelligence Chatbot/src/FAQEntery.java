import java.util.List;


public class FAQEntry {
    private final String question;
    private final String answer;
    private final List<String> processedTokens;

    public FAQEntry(String question, String answer, List<String> processedTokens) {
        this.question = question;
        this.answer = answer;
        this.processedTokens = processedTokens;
    }

    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public List<String> getProcessedTokens() { return processedTokens; }
}
