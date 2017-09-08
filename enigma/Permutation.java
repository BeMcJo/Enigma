package enigma;

import java.util.ArrayList;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Benjamin Jong
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters not
     *  included in any cycle map to themselves. Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = new ArrayList<>();
        addCycles(cycles);
    }

    /** Add the cycles (c0->c1->...->cm->c0) to the permutation, where CYCLEs is
     *  c0c1...cm.
     *  @param cycles - string of cycles*/
    void addCycles(String cycles) {
        while (cycles.length() > 2) {
            int start = cycles.indexOf('('), ending = cycles.indexOf(')');
            if (start == -1 || ending == -1 || start > ending) {
                throw new EnigmaException("Improper Cycle Format");
            }

            String cycle = cycles.substring(start + 1, ending);
            _cycles.add(cycle);
            if (cycles.indexOf(' ') == -1) {
                break;
            }
            cycles = cycles.substring(ending + 1);
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char c = _alphabet.toChar(p);
        String targetCycle = "";
        int index = -1;

        for (String cycle: _cycles) {
            index = cycle.indexOf(c);
            if (index != -1) {
                targetCycle = cycle;
                break;
            }
        }
        if (index != -1) {
            c = targetCycle.charAt((1 + index) % targetCycle.length());
        }
        p = _alphabet.toInt(c);
        return wrap(p);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char ch = _alphabet.toChar(c);
        String targetCycle = "";
        int index = -1;

        for (String cycle: _cycles) {
            index = cycle.indexOf(ch);
            if (index != -1) {
                targetCycle = cycle;
                break;
            }
        }
        if (index != -1) {
            ch = targetCycle.charAt((targetCycle.length() - 1 + index)
                                    % targetCycle.length());
        }
        c = _alphabet.toInt(ch);
        return wrap(c);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int index;

        for (String cycle: _cycles) {
            index = cycle.indexOf(p);
            if (index != -1) {
                p = cycle.charAt((1 + index) % cycle.length());
                break;
            }
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    int invert(char c) {
        int index;

        for (String cycle: _cycles) {
            index = cycle.indexOf(c);
            if (index != -1) {
                c = cycle.charAt((cycle.length() - 1 + index) % cycle.length());
                break;
            }
        }
        return c;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (String cycle: _cycles) {
            for (int i = 0; i < cycle.length(); i += 1) {
                if (cycle.charAt(i) == cycle.charAt((i + 1) % cycle.length())) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Collection of cycles. */
    private ArrayList<String> _cycles;
}

