package enigma;

import java.util.ArrayList;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Benjamin Jong
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _rotors = (ArrayList<Rotor>) allRotors;
        _numRotors = numRotors;
        _pawls = pawls;
        _plugboard = null;
        _using = new Rotor[_numRotors];
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        int count = 0;

        for (int i = 0; i < rotors.length; i += 1) {
            boolean inserted = false;
            for (int j = 0; j < count; j++) {
                if (rotors[i].equals(_using[j].name())) {
                    throw new EnigmaException("Using more than 1 " + _using[j]);
                }
            }
            for (Rotor rotor : _rotors) {
                if (rotor.name().toUpperCase().equals(rotors[i])) {
                    _using[count] = rotor;
                    _using[count].set(0);
                    count += 1;
                    inserted = true;
                    break;
                }
            }

            if (count == 1 && !_using[count - 1].reflecting()) {
                throw new EnigmaException("Not a reflector");
            }
            if (count > 0 && count <= _numRotors - _pawls
                    && _using[count - 1].rotates()) {
                throw new EnigmaException("Wrong place for non-fixed rotor");
            }
            if (!inserted) {
                throw new EnigmaException("Rotor " + rotors[i] + " not found.");
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of four
     *  upper-case letters. The first letter refers to the leftmost
     *  rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        for (int i = 0; i < setting.length(); i += 1) {
            _using[i + 1].set(setting.charAt(i));
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        int numRotorsUsed = _using.length;
        Rotor rotor = _using[numRotorsUsed - 1];

        if (_using[numRotorsUsed - 2].atNotch()) {
            _using[numRotorsUsed - 3].advance();
        }
        if (_using[numRotorsUsed - 2].atNotch() || rotor.atNotch()) {
            _using[numRotorsUsed - 2].advance();
        }

        rotor.advance();
        if (_plugboard != null) {
            c = _plugboard.permute(c);
        }
        c = rotor.convertForward(c);
        for (int i = numRotorsUsed - 2; i >= 0; i -= 1) {
            Rotor currentRotor = _using[i];
            c = currentRotor.convertForward(c);
        }
        for (int i = 1; i < numRotorsUsed; i += 1) {
            c = _using[i].convertBackward(c);
        }
        if (_plugboard != null) {
            c = _plugboard.permute(c);
        }
        return _alphabet.toChar(c);
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String converted = "";

        msg = msg.toUpperCase();
        while (msg.length() > 0) {
            char ch = msg.charAt(0);
            if (ch != ' ') {
                ch = (char) convert(_alphabet.toInt(ch));
                converted += ch;
            }
            msg = msg.substring(1);
        }
        return converted;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Collection of all available rotors to use. */
    private ArrayList<Rotor> _rotors;

    /** Array of rotors being used. */
    private Rotor[] _using;

    /** Plugboard holds characters that swap in Enigma. */
    private Permutation _plugboard;

    /**
     *  _pawls - number of pawls.
     *  _numRotors - number of rotors used.
     */
    private int _pawls, _numRotors;
}
