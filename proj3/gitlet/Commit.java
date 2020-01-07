package gitlet;

import java.io.File;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Java class that will represent the commit in gitlet.
 * @author  Michael Chien */
public class Commit implements Serializable {

    /** The constructor for a commit, a commit should take in.
     * @param message the commits message.
     * @param parent the parent of the commit.
     * @param files and a Hashmap of all files.
     *  A commt should also have a timestamp of the commit as well as a
     *  SHA1 id uniquely representing a commit. The SHA1 id is made of
     *  its message + parent + timestamp + files */
    public Commit(String message, String parent,
                  HashMap<String, String> files) {
        if (message == null || message.equals("")) {
            Utils.message("Please enter a commit message.");
            System.exit(0);
        }
        _message = message;
        _parent = parent;
        _files = files;
        timestamp = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z")
                .format(new Date());
        commitSHA1 = Utils.sha1(_message + _parent
                + timestamp + _files);
        trackedfiles = new HashMap<>();
        shortsha1 = commitSHA1.substring(0, 8);

    }

    /** The Constructor for an initial commit. */
    public Commit() {
        _message = "initial commit";
        _parent = null;
        _files = new HashMap<>();
        timestamp = "Thu Jan 4 00:00:00 1970 -0800";
        commitSHA1 = Utils.sha1(_message + _parent + timestamp);
        trackedfiles = new HashMap<>();
        shortsha1 = commitSHA1.substring(0, 8);
    }
    /** The Constructor for a merge commit.
     * @param message message
     * @param  currentparent parent
     * @param givenparent parent
     * @param files files*/
    public Commit(String message, String currentparent,
                  String givenparent, HashMap<String, String> files) {
        _message = message;
        _parent = currentparent;
        _givenparent = givenparent;
        _files = files;
        timestamp = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z")
                .format(new Date());
        commitSHA1 = Utils.sha1(_message + _parent
                + _givenparent + timestamp + _files);
        trackedfiles = new HashMap<>();
        shortsha1 = commitSHA1.substring(0, 8);
        merge = true;
    }


    /** Deserializes a commit.
     * @param name name
     * @return string*/
    public Commit deserialize(String name) {
        Commit commit;
        File infile = new File(commitpath + name);
        try {
            ObjectInputStream inp =
                    new ObjectInputStream(new FileInputStream(infile));
            commit = (Commit) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException exp) {
            commit = null;
            Utils.message("IO exception in commit");
            Utils.message(exp.getMessage());
            exp.printStackTrace();
            System.exit(0);
        }
        return commit;
    }
    @Override
    /** Prints log.
     */
    public String toString() {
        String log = "";
        log += "===\n";
        log += "commit" + " " + this.getCommitSHA1() + "\n";
        log += "Date:" + " " + this.getTimestamp() + "\n";
        log += this.getMessage();
        log += "\n";
        return log;
    }

    /** Prints merge commit.
     * @return string
     */
    public String printMerge() {
        String log = "";
        log += "===\n";
        log += "commit" + " " + this.getCommitSHA1() + "\n";
        log += "Merge:" + " " + this.getParent().substring(0, 7) + " "
                + this.getmergeParent2().substring(0, 7) + " " + "\n";
        log += "Date:" + " " + this.getTimestamp() + "\n";
        log += this.getMessage();
        log += "\n";
        return log;
    }


    /** Gets the message of the commit.
     * @return string*/
    public String getMessage() {
        return this._message;
    }
    /** Gets the parent of the current commit.
     * @return  string*/
    public String getParent() {
        return this._parent;
    }
    /** Gets the Hashmap of all files.
     * @return  string*/
    public HashMap<String, String> getFiles() {
        return this._files;
    }
    /** Gets the timestamp of the commit.
     * @return  string*/
    public String getTimestamp() {
        return this.timestamp;
    }
    /** Gets the unique SHA1 id of the commit.
     * @return  string*/
    public String getCommitSHA1() {
        return this.commitSHA1;
    }
    /** Gets the trackedfiles of the commit.
     * @return string*/
    public HashMap<String, String> getTrackedfiles() {
        return this.trackedfiles;
    }
    /** Gets the short commit id.
     * @return string*/
    public String getShortsha1() {
        return this.shortsha1; }
    /** Gets merge parent2.
     * @return string*/
    public String getmergeParent2() {
        return this._givenparent;
    }
    /** See if its a merge commit.
     * @return boolean*/
    public boolean ifMerge() {
        return this.merge;
    }

    /** The message of the commit. */
    private String _message;
    /** The parent of the current commit (used for normal commits ). */
    private String _parent;
    /*** The Hashmap of all files. */
    private HashMap<String, String> _files;
    /** The sha1 id of the commit. */
    private String commitSHA1;
    /** The timestamp of the commit. */
    private String timestamp;
    /** The commit path for easy access. */
    private String commitpath = ".gitlet/commits/";
    /** The trackedfiles for the current commit. */
    private HashMap<String, String> trackedfiles;
    /** THe given parent of the given branch for merge commits. */
    private String _givenparent;
    /** The abbreviated sha1 of the commit. */
    private String shortsha1;
    /** To see if its a merge commit. */
    private boolean merge;
}





