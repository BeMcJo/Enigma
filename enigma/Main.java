package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Benjamin Jong
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        String line, converted;
        int processed = 0;
        Machine m = readConfig();

        while (_input.hasNextLine()) {
            line = _input.nextLine();
            if (line.length() > 0 && line.charAt(0) == '*') {
                setUp(m, line.substring(2));
                processed = 1;
            } else {
                if (processed == 0) {
                    throw new EnigmaException("Machine was not set up");
                }
                processed += 1;
                converted = m.convert(line);
                printMessageLine(converted);
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alphabet, line;
            int numRotors, numPawls;
            ArrayList<Rotor> rotors = new ArrayList<>();

            alphabet = _config.nextLine();

            if (alphabet.indexOf(' ') != -1 || alphabet.length() == 0) {
                throw new EnigmaException("Not a proper alphabet");
            }
            _alphabet = new Alphabet(alphabet);

            if (!_config.hasNextInt()) {
                throw new EnigmaException("No existence of numRotors");
            }
            numRotors = _config.nextInt();

            if (!_config.hasNextInt()) {
                throw new EnigmaException("No existence of numRotors");
            }
            numPawls = _config.nextInt();

            if (numRotors <= numPawls) {
                throw new EnigmaException("Can't have less slots than pawls");
            }
            _config.nextLine();
            while (_config.hasNextLine()) {
                line = _config.nextLine();
                if (line.charAt(1) == ' ') {
                    rotors.get(rotors.size() - 1).permutation().addCycles(line);
                } else {
                    Rotor adding = readRotor(line.substring(1));
                    for (Rotor r: rotors) {
                        if (adding.name().equals(r.name())) {
                            throw new EnigmaException("Duplicate Rotor Name");
                        }
                    }
                    rotors.add(adding);
                }
            }
            return new Machine(new Alphabet(alphabet), numRotors,
                    numPawls, rotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config.
     *  @param rotorConfig - string containing name,
     *                        type, notches, and cycles.*/
    private Rotor readRotor(String rotorConfig) {
        try {
            String name, notches, cycles;
            int index = rotorConfig.indexOf(' ');
            Rotor rotor;

            name = rotorConfig.substring(0, index);
            rotorConfig = rotorConfig.substring(index + 1);
            index = rotorConfig.indexOf(' ');
            notches = rotorConfig.substring(0, index);
            index = rotorConfig.indexOf('(');
            cycles = rotorConfig.substring(index);

            if (notches.charAt(0) == 'R') {
                rotor = new Reflector(name,
                        new Permutation(cycles, _alphabet));
            } else if (notches.charAt(0) == 'M') {
                if (notches.length() < 2) {
                    throw new EnigmaException("Improper Moving Rotor Format");
                }

                rotor = new MovingRotor(name,
                        new Permutation(cycles, _alphabet),
                            notches.substring(1));
            } else if (notches.charAt(0) == 'N') {
                rotor = new FixedRotor(name,
                        new Permutation(cycles, _alphabet));
            } else {
                throw new EnigmaException("Improper Rotor Type");
            }
            return rotor;
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        int numRotors = M.numRotors(), index;
        String[] rotors = new String[numRotors];
        String rotorSettings;

        for (int i = 0; i < numRotors; i += 1) {
            index = settings.indexOf(' ');
            if (index != -1) {
                rotors[i] = settings.substring(0, index);
                settings = settings.substring(index + 1);
            } else {
                rotors[i] = settings;
            }
        }
        index = settings.indexOf(' ');
        if (index == -1) {
            rotorSettings = settings;
            M.setPlugboard(new Permutation("", _alphabet));
        } else {
            M.setPlugboard(new Permutation(settings.substring(index + 1),
                    _alphabet));
            rotorSettings = settings.substring(0, index);
        }
        M.insertRotors(rotors);
        M.setRotors(rotorSettings);
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        String groupedMsg = "";
        int count = 0;

        for (int i = 0; i < msg.length(); i += 1) {
            char ch = msg.charAt(i);
            if (ch != ' ') {
                groupedMsg += ch;
                count += 1;
            }
            if (count == 5) {
                count = 0;
                groupedMsg += ' ';
            }
        }
        _output.println(groupedMsg);
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
}
