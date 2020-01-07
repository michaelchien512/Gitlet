package gitlet;

import java.io.File;
import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;

/** StagingArea for gitlet.
 * @author Michael Chien
 */
public class StagingArea implements Serializable {

    /** Constructor for the stagingArea which takes in the latestcommit.
     * @param latestcommit commit*/
    public StagingArea(Commit latestcommit) {
        stagedFiles = new HashMap<>();
        removedFiles = new HashMap<>();
        untrackedFiles = new HashSet<>();
        alltrackedFiles = new HashMap<>();
    }


    /** Clears the stagingArea. */
    public void clearStage() {
        this.stagedFiles.clear();
    }

    /** Clears removed files. */
    public void clearRemove() {
        this.removedFiles.clear();
    }

    /** Serializes the stagingArea.
     * @param name name*/
    public void serialize(String name) {
        StagingArea stage = this;
        File outfile = new File(".gitlet/" + name);
        try {
            ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(outfile));
            out.writeObject(stage);
            out.close();
        } catch (IOException exp) {
            Utils.message("IO exception in stage");
            Utils.message(exp.getMessage());
            exp.printStackTrace();
            System.exit(0);
        }
    }

    /** Deserializes the stagingArea.
     * @param name name
     * @return stagingarea */
    public StagingArea deserialize(String name) {
        StagingArea stage;
        File infile = new File(".gitlet/" + name);
        try {
            ObjectInputStream inp =
                    new ObjectInputStream(new FileInputStream(infile));
            stage = (StagingArea) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException exp) {
            Utils.message("IO exception in stage");
            Utils.message(exp.getMessage());
            exp.printStackTrace();
            System.exit(0);
            stage = null;
        }
        return stage;
    }




    /** Get the stagedFiles from the stagingArea.
     * @return files */
    public HashMap<String, String> getStagedFiles() {
        return stagedFiles;
    }
    /** Get the removedFiles from the stagingArea.
     * @return files */
    public HashMap<String, String> getRemovedFiles() {
        return removedFiles;
    }
    /** Gets all untrackedFiles from the stagingArea.
     * @return files */
    public HashSet<String> getUntrackedFiles() {
        return untrackedFiles;
    }
    /** Gets all tracked files.
     * @return files */
    public HashMap<String, String> getAlltrackedFiles() {
        return alltrackedFiles;
    }

    /** Hashmap of all stagedFiles with filename.
     * as its KEY and SHA1ID as its VALUE.*/
    private HashMap<String, String> stagedFiles;
    /** Hashmap of all removedFiles with filename.
     * as its KEY and SHA1ID as its VALUE.*/
    private HashMap<String, String> removedFiles;
    /** Hashmap of all untrackedFiles with filename.
     * as its KEY and SHA1ID as its VALUE. */
    private HashSet<String> untrackedFiles;
    /** All tracked files. */
    private HashMap<String, String> alltrackedFiles;
}
