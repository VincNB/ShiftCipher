package nmbai.text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SearchDictionary {
    private static final String DEFAULT_FILE_NAME = "dictionary.txt";
    private final String fileName;
    private final Set<String> words = new HashSet<>();
    private boolean fileLoaded = false;
    private int minWordLength;
    private int maxWordLength;

    public SearchDictionary() {
        this(DEFAULT_FILE_NAME);
    }

    public SearchDictionary(String fileName) {
        this.fileName = fileName;
    }

    private void initialize() {
        boolean errorCaught = false;
        minWordLength = Integer.MAX_VALUE;
        maxWordLength = Integer.MIN_VALUE;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line);
                minWordLength = Math.min(line.length(), minWordLength);
                maxWordLength = Math.max(line.length(), maxWordLength);
            }
        } catch (FileNotFoundException ignored) {
            System.out.printf("File %s could not be found.%n", fileName);
            errorCaught = true;
        } catch (IOException ex) {
            System.out.printf("IOException occurred when reading file %s.%n%s%n", fileName, ex.getMessage());
            errorCaught = true;
        } finally {
            if (errorCaught) {
                words.clear();
            }
        }
        fileLoaded = true;
    }

    /**
     * Returns true if word is contained in dictionary file used
     * @param word to check
     * @return true if word is contained in loaded file, false if it is not
     */
    public boolean contains(String word) {
        if (!fileLoaded) {
            initialize();
        }
        return words.contains(word);
    }

    /**
     * Returns the length of the smallest word in the dictionary file supplied
     * @return length of the smallest word
     */
    public int getMinWordLength() {
        if (!fileLoaded) {
            initialize();
        }
        return minWordLength;
    }

    /**
     * Returns the length of the largest word in the dictionary file supplied
     * @return length of the largest word
     */
    public int getMaxWordLength() {
        if (!fileLoaded) {
            initialize();
        }
        return maxWordLength;
    }
}
