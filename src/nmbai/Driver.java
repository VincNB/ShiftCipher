package nmbai;

import nmbai.shiftcipher.ShiftCipher;
import nmbai.text.SearchDictionary;
import nmbai.text.WordFilter;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Driver {
    private static final String INSTRUCTIONS = """
            Expecting arguments:
                "-e <input file> <key> <output file>" to encrypt input file with key and save to output file
                "-d <input file> <key> <output file>" to decrypt input file with key and save to output file
                "-c <input file>" to crack input file and output to console
            e.g.
                -e testInput.txt 13 testOutput.txt
                -c testOutput.txt""";
    private static final String ENCRYPT_ARG = "-e";
    private static final String DECRYPT_ARG = "-d";
    private static final String CRACK_ARG = "-c";
    private final ShiftCipher cipher = new ShiftCipher();

    private final String[] args;

    public Driver(String[] args) {
        this.args = args;
    }

    public String run() {
        String result = INSTRUCTIONS;
        if (args.length != 0) {
            switch (args[0]) {
                case ENCRYPT_ARG -> {
                    cipher.setShiftMode(ShiftCipher.ShiftMode.ENCRYPT);
                    result = operate();
                }
                case DECRYPT_ARG -> {
                    cipher.setShiftMode(ShiftCipher.ShiftMode.DECRYPT);
                    result = operate();
                }
                case CRACK_ARG -> result = bruteForce();
            }
        }
        return result;
    }

    private String operate() {
        StringBuilder result = new StringBuilder();
        if (args.length == 4) {
            String inputFile = args[1];
            String outputFile = args[3];
            if (setKey()) {
                List<String> lines = Collections.emptyList();
                try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                    lines = reader.lines().collect(Collectors.toList());
                } catch (FileNotFoundException ex) {
                    result.append(String.format("File %s could not be found.", inputFile));
                } catch (IOException ex) {
                    result.append(String.format("An IOException occurred while reading file %s.", inputFile));
                    lines = Collections.emptyList();
                }
                Queue<String> queue = new ArrayDeque<>(lines.size());
                lines.forEach(line -> queue.offer(cipher.update(line))); //update file

                if (!queue.isEmpty()) { //write file
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                        while (!queue.isEmpty()) {
                            writer.write(queue.poll());
                            writer.write('\n');
                        }
                        result.append(String.format("Finished %s of %s with key %d; saved to file %s.",
                                cipher.getShiftMode().toString(), inputFile, cipher.getKey(), outputFile));
                    } catch (IOException ex) {
                        result.append(String.format("An IOException occurred while writing file %s.", outputFile));
                    }
                }
            } else {
                result.append("Could not set key - expecting an integer argument.");
            }
        } else {
            result.append("Expecting four arguments.");
        }
        return result.toString();
    }

    private String bruteForce() {
        StringBuilder result = new StringBuilder();
        if (args.length == 2) {
            String inputFile = args[1];
            List<String> lines = Collections.emptyList();
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                lines = reader.lines().collect(Collectors.toList());
            } catch (FileNotFoundException ex) {
                result.append(String.format("File %s could not be found.", inputFile));
            } catch (IOException ex) {
                result.append(String.format("An IOException occurred while reading file %s.", inputFile));
                lines = Collections.emptyList();
            }
            if (!lines.isEmpty()) {
                int softCap = 100;
                List<String> words = new ArrayList<>();
                WordFilter filter = new WordFilter();
                SearchDictionary dictionary = new SearchDictionary();
                for (int i = 0; i < lines.size() && words.size() < softCap; i++) {
                    String[] split = lines.get(i).split(" ");
                    for (String s : split) {

                        String filtered = filter.filter(s);
                        if (filtered.length() >= dictionary.getMinWordLength() && filtered.length() <= dictionary.getMaxWordLength()) {
                            words.add(filtered);
                        }
                    }
                }
                final int hitsNeeded = Math.max(1, words.size() / 2);
                final int missesNeeded = Math.max(1, words.size() - hitsNeeded);
                boolean cracked = false;
                cipher.setShiftMode(ShiftCipher.ShiftMode.DECRYPT);
                for (int key = 0; key < ShiftCipher.ALPHA_LEN && !cracked; key++) {
                    int hitsLeft = hitsNeeded;
                    int missesLeft = missesNeeded;
                    cipher.setKey(key);
                    boolean keyDone = false;
                    for (int i = 0; i < words.size() && !keyDone; i++) {
                        String updated = cipher.update(words.get(i));
                        if (dictionary.contains(updated)) {
                            hitsLeft--;
                            cracked = (hitsLeft == 0);
                        } else {
                            missesLeft--;
                            keyDone = (missesLeft == 0);
                        }
                        keyDone |= cracked;
                    }
                }
                if (cracked) {
                    result.append(String.format("%nCracked file %s with key %d.%n", inputFile, cipher.getKey()));
                    lines.forEach(s -> System.out.println(cipher.update(s)));
                } else {
                    result.append(String.format("Unable to crack file %s.%n", inputFile));
                }
            }
        } else {
            result.append("Expecting two arguments.");
        }
        return result.toString();
    }

    private boolean setKey() {
        boolean success = false;
        try {
            int key = Integer.parseInt(args[2]);
            cipher.setKey(key);
            success = true;
        } catch (NumberFormatException ignored) {
            //intentionally empty
        }
        return success;
    }

}
