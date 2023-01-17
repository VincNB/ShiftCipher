package nmbai.text;

public class WordFilter {
    private final StringBuilder builder = new StringBuilder();

    /**
     * Cleans word by setting all characters to lowercase alpha characters
     * or returns an empty string if word contains symbols or numbers.
     *
     * @param word to filter
     * @return clean word or an empty string
     */
    public String filter(String word) {
        builder.setLength(0);
        for (int i = 0; i < word.length(); i++) {
            int ch = word.charAt(i);
            if (Character.isLowerCase(ch)) {
                builder.append((char) ch);
            } else if (Character.isUpperCase(ch)) {
                builder.append(Character.toLowerCase((char) ch));
            } else {
                builder.setLength(0);
                break;
            }
        }
        return builder.toString();
    }
}
