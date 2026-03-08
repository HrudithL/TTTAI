import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Handles persisting a paused game to disk and reading it back on the next run.
 *
 * Save file: ~/.tttai_save.txt
 *
 * Format (key=value, one per line):
 *   mode=HVS_AI | HVS_H
 *   aiType=MINIMAX | RANDOM   (only when mode=HVS_AI)
 *   p1Name=...
 *   p2Name=...
 *   currentPlayer=0|1
 *   board=XXOO... (64 chars, '.'=empty, sheet-major)
 */
public class SaveManager {

    public static final String SAVE_PATH =
            System.getProperty("user.home") + File.separator + ".tttai_save.txt";

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public static boolean hasSave() {
        return Files.exists(Paths.get(SAVE_PATH));
    }

    /**
     * Saves the current game to disk so it can be resumed on the next boot.
     *
     * @param gameState live GameState (for board + currentPlayer)
     * @param mode      "HVS_AI" or "HVS_H"
     * @param aiType    "MINIMAX", "RANDOM", or "" (when HVS_H)
     * @param p1Name    display name for player 1
     * @param p2Name    display name for player 2
     */
    public static void save(GameState gameState,
                             String mode, String aiType,
                             String p1Name, String p2Name) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_PATH))) {
            pw.println("mode="    + mode);
            pw.println("aiType="  + aiType);
            pw.println("p1Name="  + p1Name);
            pw.println("p2Name="  + p2Name);
            // board + currentPlayer added by GameState itself
            pw.print(gameState.saveToString());
        } catch (IOException e) {
            System.err.println("Could not write save file: " + e.getMessage());
        }
    }

    /**
     * Loads all saved key/value pairs, including board state.
     * @return Properties map – never null; empty if file unreadable.
     */
    public static Properties load() {
        Properties props = new Properties();
        try (BufferedReader br = new BufferedReader(new FileReader(SAVE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                int eq = line.indexOf('=');
                if (eq > 0) props.setProperty(line.substring(0, eq), line.substring(eq + 1));
            }
        } catch (IOException e) {
            System.err.println("Could not read save file: " + e.getMessage());
        }
        return props;
    }

    /** Deletes the save file (called after a fresh New Game wipes the state). */
    public static void deleteSave() {
        try { Files.deleteIfExists(Paths.get(SAVE_PATH)); }
        catch (IOException ignored) { }
    }
}
