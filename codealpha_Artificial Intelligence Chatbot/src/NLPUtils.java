import java.util.*;
import java.util.regex.*;


public class NLPUtils {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a","an","the","is","are","was","were","be","been","being","am",
            "do","does","did","doing","will","would","shall","should","can",
            "could","may","might","must","i","you","he","she","it","we","they",
            "me","him","her","us","them","my","your","his","its","our","their",
            "to","of","in","on","at","by","for","with","about","against",
            "between","into","through","during","before","after","above","below",
            "from","up","down","out","off","over","under","again","further",
            "then","once","here","there","when","where","why","how","all","any",
            "both","each","few","more","most","other","some","such","no","nor",
            "not","only","own","same","so","than","too","very","just","and",
            "but","or","if","this","that","these","those","please","hi","hello"
    ));

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-zA-Z']+");

    
    public static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null) return tokens;
        Matcher m = TOKEN_PATTERN.matcher(text.toLowerCase());
        while (m.find()) {
            tokens.add(m.group());
        }
        return tokens;
    }

    
    public static List<String> removeStopwords(List<String> tokens) {
        List<String> result = new ArrayList<>();
        for (String t : tokens) {
            if (!STOPWORDS.contains(t)) result.add(t);
        }
        return result;
    }

    
    public static String stem(String word) {
        if (word.length() <= 3) return word;
        String w = word;
        if (w.endsWith("ies") && w.length() > 4) return w.substring(0, w.length() - 3) + "y";
        if (w.endsWith("es") && w.length() > 4) return w.substring(0, w.length() - 2);
        if (w.endsWith("s") && !w.endsWith("ss")) return w.substring(0, w.length() - 1);
        if (w.endsWith("ing") && w.length() > 5) return w.substring(0, w.length() - 3);
        if (w.endsWith("ed") && w.length() > 4) return w.substring(0, w.length() - 2);
        return w;
    }

    
    public static List<String> processText(String text) {
        List<String> tokens = removeStopwords(tokenize(text));
        List<String> stemmed = new ArrayList<>();
        for (String t : tokens) stemmed.add(stem(t));
        return stemmed;
    }

   
    public static Map<String, Integer> termFrequency(List<String> tokens) {
        Map<String, Integer> tf = new HashMap<>();
        for (String t : tokens) tf.merge(t, 1, Integer::sum);
        return tf;
    }

   
    public static double cosineSimilarity(List<String> a, List<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        Map<String, Integer> tfA = termFrequency(a);
        Map<String, Integer> tfB = termFrequency(b);

        Set<String> vocabulary = new HashSet<>();
        vocabulary.addAll(tfA.keySet());
        vocabulary.addAll(tfB.keySet());

        double dot = 0, magA = 0, magB = 0;
        for (String term : vocabulary) {
            int x = tfA.getOrDefault(term, 0);
            int y = tfB.getOrDefault(term, 0);
            dot += x * y;
            magA += x * x;
            magB += y * y;
        }
        if (magA == 0 || magB == 0) return 0.0;
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }
}
