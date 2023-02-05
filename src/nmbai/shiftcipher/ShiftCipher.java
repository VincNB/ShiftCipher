package nmbai.shiftcipher;

import java.util.*;

/**
 * Class used for encrypting Strings using the shift cipher algorithm where each letter in the String is substituted
 * by another letter that is n places in a direction in the alphabet, where n is the Key and the direction is
 * determined by the ShiftMode with ENCRYPT being left-shifted and DECRYPT being right-shifted. Numbers that cause
 * the placement to overflow or underflow the alphabet are wrapped around. e.g. 'Y' + 2 will result in 'A'.
 */
public class ShiftCipher {
    public static final int ALPHA_LEN = 26;
    private static final int UPPERCASE_A = 'A';
    private static final int LOWERCASE_A = 'a';
    private final StringBuilder builder = new StringBuilder();
    private final Map<ShiftMode, Integer> keys = new EnumMap<>(ShiftMode.class);
    private ShiftMode shiftMode = ShiftMode.IDLE;
    private int key;
    public ShiftCipher() {
        this(0);
    }

    public ShiftCipher(int key) {
        setKey(key);
    }

    public int getKey() {
        return key;
    }

    /**
     * Sets the key to be used by the ShiftCipher operations. Keys will be reduced using key % ALPHA_LEN
     *
     * @param key used in operations
     */
    public void setKey(int key) {
        this.key = Math.floorMod(key, ALPHA_LEN);
        for (ShiftMode mode : ShiftMode.values()) {
            keys.put(mode, mode.multiplier * this.key);
        }
    }

    public ShiftMode getShiftMode() {
        return shiftMode;
    }

    /**
     * Sets the cipher operation to either encrypt or decrypt. Valid parameters are ShiftMode.IDLE, ShiftMode.ENCRYPT,
     * and ShiftMode.DECRYPT.
     *
     * @param shiftMode operation mode
     */
    public void setShiftMode(ShiftMode shiftMode) {
        this.shiftMode = Objects.requireNonNull(shiftMode);
    }

    /**
     * Updates a word by encrypting or decrypting it based on the current key and the ShiftMode, or leaving it unchanged
     * if the ShiftMode is IDLE.
     *
     * @param word to be updated
     * @return the newly encrypted or decrypted word
     */
    public String update(String word) {
        builder.setLength(0); // clears builder
        for (int i = 0; i < word.length(); i++) {
            int ch = word.charAt(i);
            int val = getSubtractedValue(ch);
            if (val != 0) {
                ch -= val;
                ch += keys.get(shiftMode);
                ch = Math.floorMod(ch, ALPHA_LEN);
                ch += val;
            }
            builder.append((char) ch);
        }

        return builder.toString();
    }

    /**
     * Returns a value for a character that, when subtracted from the character, the result will be that character's
     * zero-based index in the English alphabet, or 0 if it is not an alphabetic letter
     */
    private int getSubtractedValue(int ch) {
        int val = 0;
        if (Character.isLowerCase(ch)) {
            val = LOWERCASE_A;
        } else if (Character.isUpperCase(ch)) {
            val = UPPERCASE_A;
        }
        return val;
    }

    @Override
    public String toString() {
        return String.format("ShiftCipher[mode=%s, key=%d]", this.shiftMode, this.key);
    }

    public enum ShiftMode {
        IDLE(0),
        ENCRYPT(1),
        DECRYPT(-1);

        private final int multiplier;

        ShiftMode(int multiplier) {
            this.multiplier = multiplier;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ROOT);
        }
    }
}
