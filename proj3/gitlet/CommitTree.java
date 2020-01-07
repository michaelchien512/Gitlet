package gitlet;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/** Java class to represent the tree of commits and branches.
 * @author Michael Chien */
public class CommitTree implements Serializable {

    /** THe constructor for the commit tree which creates the.
     * maps of all branches and all commits. */
    public CommitTree() {
        allbranches = new HashMap<>();
        allcommits = new HashMap<>();
        currentbranch = null;
        shortcommits = new HashMap<>();
    }

    /** Serializes the CommitTree and writes it to disk.
     * @param name name*/
    public void serialize(String name) {
        CommitTree commitTree = this;
        File outfile = new File(gitletpath + name);
        try {
            ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(outfile));
            out.writeObject(commitTree);
            out.close();
        } catch (IOException exp) {
            Utils.message("IO exception in commitTree");
            Utils.message(exp.getMessage());
            exp.printStackTrace();
            System.exit(0);
        }
    }

    /** Deserializes the CommitTree  and reads it from disk.
     * @param name name
     * @return commit */
    public CommitTree deserialize(String name) {
        CommitTree commitTree;
        File infile = new File(gitletpath + name);
        try {
            ObjectInputStream inp =
                    new ObjectInputStream(new FileInputStream(infile));
            commitTree = (CommitTree) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException exp) {
            Utils.message("IO exception in commitTree");
            Utils.message(exp.getMessage());
            exp.printStackTrace();
            System.exit(0);
            commitTree = null;
        }
        return commitTree;
    }

    /** Sets the current branch to the given branch.
     @param branch branch */
    public void setBranch(Branch branch) {
        this.currentbranch = branch;
    }
    /** Sets the current branch to the new commit head.
     * @param commitid commitid */
    public void setHead(String commitid) {
        this.currentbranch.setBranch(commitid);
    }
    /** Sets all branches to another set.
     * @param branches branches */
    public void setBranches(HashMap<String, Branch> branches) {
        this.allbranches = branches;
    }

    /** Find the split point between the given bran
     * ch and the current branch if there is one and return the common
     * ancestor between the two branches.
     * @param bname bname*/
    public String commonAncestor(String bname) {
        Branch givenbranch = allbranches.get(bname);
        ArrayList<String> currentbranchcommits = new ArrayList<>();
        ArrayList<String> givenbranchcommits = new ArrayList<>();
        Commit currentcommit = new Commit();
        currentcommit = currentcommit.deserialize(currentbranch.getHead());
        currentbranchcommits.add(currentcommit.getCommitSHA1());
        while (currentcommit.getParent() != null) {
            String parentsha1 = currentcommit.getParent();
            Commit parentcommit = currentcommit.deserialize(parentsha1);
            currentbranchcommits.add(parentsha1);
            currentcommit = parentcommit;
        }
        currentcommit = currentcommit.deserialize(givenbranch.getHead());
        givenbranchcommits.add(currentcommit.getCommitSHA1());
        while (currentcommit.getParent() != null) {
            String parentsha1 = currentcommit.getParent();
            Commit parentcommit = currentcommit.deserialize(parentsha1);
            givenbranchcommits.add(parentsha1);
            currentcommit = parentcommit;
        }
        for (String sha1 : givenbranchcommits) {
            if (currentbranchcommits.contains(sha1)) {
                return sha1;
            }
        }
        return "";
    }


    /** Gets all the branches.
     * @return branches */
    public HashMap<String, Branch> getAllbranches() {
        return this.allbranches;
    }
    /** Gets all the commits.
     * @return commits*/
    public HashMap<String, Commit> getAllcommits() {
        return this.allcommits;
    }
    /** Gets the current branch.
     * @return  branch*/
    public Branch getCurrentbranch() {
        return this.currentbranch;
    }
    /** Gets the short commits.
     * @return  commits*/
    public HashMap<String, Commit> getShortCommits() {
        return this.shortcommits;
    }


    /** The Map of all branches. */
    private HashMap<String, Branch> allbranches;
    /** The Map of all commits with the the Sha1 of the commit as KEY.
     * and the individual commit as the VALUE. */
    private HashMap<String, Commit> allcommits;
    /** The path to .gitlet. */
    private String gitletpath = ".gitlet/";
    /** The current branch.*/
    private Branch currentbranch;
    /** The map of all short commit ids. */
    private HashMap<String, Commit> shortcommits;

}
