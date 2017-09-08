package enigma;

import static enigma.EnigmaException.*;

/* Extra Credit Only */

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Benjamin Jong
 */
class Alphabet {

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        alphabet = chars;
    }

    /** Returns the size of the alphabet. */
    int size() {
        return alphabet.length();
    }

    /** Returns true if C is in this alphabet. */
    boolean contains(char c) {
        return alphabet.indexOf(c) != -1;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        if (index < 0 || index >= size()) {
            throw new EnigmaException("Alphabet Index: Out of bounds");
        }
        return alphabet.charAt(index);
    }

    /** Returns the index of character C, which must be in the alphabet. */
    int toInt(char c) {
        if (!contains(c)) {
            throw new EnigmaException("Char (" + c + ") not in alphabet");
        }
        return alphabet.indexOf(c);
    }

    /** String of characters in the alphabet. */
    private String alphabet;
}
