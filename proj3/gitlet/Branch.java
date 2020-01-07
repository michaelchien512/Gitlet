package gitlet;
import java.io.Serializable;
import java.util.HashMap;

/** Java class that represents a branch of the entire commit tree.
 * @author Michael Chien */
public class Branch implements Serializable {
    /** Constructor for a branch which takes in
     * a branchname and a commit sha1 string it points to.
     * @param head head
     * @param name name*/
    public Branch(String name, String head) {
        _name = name;
        _head = head;
        branchfiles = new HashMap<>();
    }

    /** Sets the branch to commitid.
     * @param commitid commitid */
    public void setBranch(String commitid) {
        this._head = commitid;
    }
    /** Getss the head of the branch that points to the commit.
     * @return string*/
    public String getHead() {
        return _head;
    }
    /** Gets the name of the branch.
     * @return string*/
    public String getName() {
        return _name;
    }
    /** Gets that branchs files.
     * @return  string */
    public HashMap<String, String> getBranchfiles() {
        return branchfiles;
    }
    /** The head of the branch that points to the commit. */
    private String _head;
    /** The name of the branch. */
    private String _name;
    /** All trackedfiles in a branch. */
    private HashMap<String, String> branchfiles;
}
