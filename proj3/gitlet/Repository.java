package gitlet;

import java.io.Serializable;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Java class to represent a repository or current working directory.
 * The repository should be able to carry out most Gitlet commands.
 *
 * @author Michael Chien
 */
public class Repository implements Serializable {


    /**
     * The constructor for a repository allowing for a command to be inputted.
     */
    public Repository() {
        initialcommit = new Commit();
        stage = new StagingArea(initialcommit);
        committree = new CommitTree();
        untrackedfiles = new HashMap<>();
        modifiedfiles = new HashMap<>();
        deletedfiles = new HashMap<>();
    }



    /**
     * The INIT Command in Gitlet  which INITS gitlet and starts with
     * a single commit as well as one branch MASTER.
     * A .gitlet directory is also created
     * as well as a commits and stagingArea directory.
     */
    public void init() {
        File repo = Utils.join(".gitlet" + File.separator);
        if (repo.exists()) {
            Utils.message("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }
        repo.mkdirs();
        new File(".gitlet" + File.separator
                + "commits" + File.separator).mkdirs();
        new File(".gitlet" + File.separator
                + "blobs" + File.separator).mkdirs();
        File firstcommit = new File(".gitlet/commits/"
                + initialcommit.getCommitSHA1());
        Utils.writeObject(firstcommit, initialcommit);
        Branch master = new Branch("master", initialcommit.getCommitSHA1());
        HashMap<String, Branch> allbranches = committree.getAllbranches();
        HashMap<String, Commit> allcommits = committree.getAllcommits();
        committree.setBranch(master);
        allcommits.put(initialcommit.getCommitSHA1(), initialcommit);
        allbranches.put("master", master);
        stage.serialize("stage");
        committree.serialize("commitTree");
    }

    /**
     * The ADD command in Gitlet.
     * @param filename filename */
    public void add(String filename) {
        stage = stage.deserialize("stage");
        HashMap<String, String> stagedfiles = stage.getStagedFiles();
        HashMap<String, String> removedfiles = stage.getRemovedFiles();
        File file = new File(filename);
        if (!file.exists()) {
            Utils.message("File does not exist");
            System.exit(0);
        }
        byte[] addedfilecontents = Utils.readContents(file);
        String addedfileSHA1 = Utils.sha1(addedfilecontents);
        if (!isIdentical(filename, addedfileSHA1)) {
            File addedfile = new File(".gitlet/blobs/"
                    + addedfileSHA1);
            stagedfiles.put(filename, addedfileSHA1);
            Utils.writeContents(addedfile, addedfilecontents);
        }
        if (removedfiles.containsKey(filename)) {
            removedfiles.remove(filename);
            stagedfiles.remove(filename);
        }
        stage.serialize("stage");
    }

    /**
     * The COMMIT command in Gitlet which deserializes a stagingArea object
     * and then adds the files to be tracked
     * and commited then serializes the commit.
     * @param message message
     */
    public void commit(String message) {
        stage = stage.deserialize("stage");
        committree = committree.deserialize("commitTree");
        Commit currentcommit = new Commit();
        HashMap<String, String> stagedfiles =
                stage.getStagedFiles();
        HashMap<String, Commit> allcommits =
                committree.getAllcommits();
        HashMap<String, Commit> shortcommits =
                committree.getShortCommits();
        currentcommit = currentcommit.deserialize
                (committree.getCurrentbranch().getHead());
        HashMap<String, String> removefiles =
                stage.getRemovedFiles();
        HashMap<String, Branch> allbranches =
                committree.getAllbranches();
        HashMap<String, String> branchfiles =
                committree.getCurrentbranch().getBranchfiles();

        if (stagedfiles.size() == 0
                && stage.getRemovedFiles().size() == 0) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        }
        Commit newCommit =
                new Commit(message, currentcommit.getCommitSHA1(), stagedfiles);
        HashMap<String, String> trackedfiles = newCommit.getTrackedfiles();
        HashMap<String, String> newfiles = new HashMap<>();
        for (String filename : stagedfiles.keySet()) {
            trackedfiles.put(filename, stagedfiles.get(filename));
            branchfiles.put(filename, stagedfiles.get(filename));
        }
        for (String filename : removefiles.keySet()) {
            branchfiles.remove(filename);
            newfiles.remove(filename);
        }
        shortcommits.put(newCommit.getShortsha1(), newCommit);
        allcommits.put(newCommit.getCommitSHA1(), newCommit);
        File commitFile =
                new File(".gitlet/commits/" + newCommit.getCommitSHA1());
        Utils.writeObject(commitFile, newCommit);
        removefiles.clear();
        stagedfiles.clear();
        stage.serialize("stage");
        allbranches.remove(committree
                .getCurrentbranch().getName());
        committree.setHead(newCommit
                .getCommitSHA1());
        allbranches.put(committree
                        .getCurrentbranch().getName(),
                        committree.getCurrentbranch());
        committree.serialize("commitTree");
    }

    /**
     * The CHECKOUT Command for a FILE in Gitlet.
     * Checks if a file is in the latest commit
     * and then writes it to disk in the CWD.
     * @param filename filename
     */
    public void checkoutfile(String filename) {
        Commit latest = new Commit();
        committree = committree.deserialize("commitTree");
        latest = latest.deserialize(committree.getCurrentbranch().getHead());
        HashMap<String, String> latestfiles = latest.getFiles();
        if (latestfiles.containsKey(filename)) {
            String blob = latestfiles.get(filename);
            File blobfile = new File(".gitlet/blobs/" + blob);
            File checkoutfile = new File(filename);
            Utils.writeContents(checkoutfile,
                    Utils.readContentsAsString(blobfile));
        } else {
            Utils.message("File does not exist in that commit.");
            System.exit(0);
        }
        committree.serialize("commitTree");
    }

    /**
     * Checks out the most recent commit with the given file id
     * meaning we take the filename in the given commit id and write it to our
     * current working directory.
     * @param commitsha1 sha1
     * @param filename filename
     */
    public void checkoutcommit(String commitsha1, String filename) {
        committree = committree.deserialize("commitTree");
        Commit commit = new Commit();
        List<String> commitList = Utils.plainFilenamesIn(".gitlet/commits/");
        if (commitsha1.length() == 8) {
            HashMap<String, Commit> shortcommits = committree.getShortCommits();
            if (shortcommits.containsKey(commitsha1)) {
                commit = shortcommits.get(commitsha1);
                HashMap<String, String> commitfiles = commit.getTrackedfiles();
                if (commitfiles.containsKey(filename)) {
                    String blob = commitfiles.get(filename);
                    File blobfile = new File(".gitlet/blobs/" + blob);
                    File checkoutfile = new File(filename);
                    Utils.writeContents(checkoutfile,
                            Utils.readContentsAsString(blobfile));
                } else {
                    Utils.message("File does not exist in that commit.");
                    System.exit(0);
                }
            } else {
                Utils.message("No commit with that id exists.");
                System.exit(0);
            }
        } else {
            if (commitList.contains(commitsha1)) {
                commit = commit.deserialize(commitsha1);
                HashMap<String, String> commitfiles = commit.getFiles();
                if (commitfiles.containsKey(filename)) {
                    String blob = commitfiles.get(filename);
                    File blobfile = new File(".gitlet/blobs/" + blob);
                    File checkoutfile = new File(filename);
                    Utils.writeContents(checkoutfile,
                            Utils.readContentsAsString(blobfile));
                } else {
                    Utils.message("File does not exist in that commit.");
                    System.exit(0);
                }
            } else {
                Utils.message("No commit with that id exists.");
                System.exit(0);
            }
        }
        committree.serialize("commitTree");
    }


    /**
     * Gets the commit files from the head of the given branch.
     * and overwrites them in CWD. Checks for untrackedfiles.
     * The checkout branch is now set as the current branch
     * @param bname branch name
     */
    public void checkoutbranch(String bname) {
        Commit currentcommit = new Commit();
        Commit checkouthead = new Commit();
        stage = stage.deserialize("stage");
        committree = committree.deserialize("commitTree");
        HashMap<String, Branch> allbranches = committree.getAllbranches();
        if (!allbranches.containsKey(bname)) {
            Utils.message("No such branch exists.");
            System.exit(0);
        }
        Branch currentbranch = committree.getCurrentbranch();
        Branch checkoutbranch = allbranches.get(bname);
        HashMap<String, String> currentfiles =
                committree.getCurrentbranch().getBranchfiles();
        HashMap<String, String> headfiles =
                allbranches.get(bname).getBranchfiles();
        if (currentbranch.getName().equals(bname)) {
            Utils.message("No need to checkout the current branch.");
            System.exit(0);
        }
        checkuntracked();
        for (String filename : headfiles.keySet()) {
            String blob = headfiles.get(filename);
            File blobfile = new File(".gitlet/blobs/" + blob);
            File checkoutfile = new File(filename);
            if (checkoutfile.exists()) {
                Utils.writeContents(checkoutfile,
                        Utils.readContentsAsString(blobfile));
            } else {
                Utils.writeContents(checkoutfile,
                        Utils.readContentsAsString(blobfile));
            }
        }
        for (String filename : currentfiles.keySet()) {
            if (!headfiles.containsKey(filename)) {
                File file = new File(filename);
                if (file.exists()) {
                    Utils.restrictedDelete(file);
                }
            }
        }
        List<String> workingfiles =
                Utils.plainFilenamesIn(System.getProperty("user.dir"));
        for (String filename : workingfiles) {
            if (!headfiles.containsKey(filename)) {
                File file = new File(filename);
                if (file.exists()) {
                    Utils.restrictedDelete(file);
                }
            }
        }
        committree.setBranch(checkoutbranch);
        stage.clearStage();
        stage.serialize("stage");
        committree.serialize("commitTree");
    }

    /**
     * Prints the log for the commit.
     */
    public void log() {
        committree = committree.deserialize("commitTree");
        Commit logcommit = new Commit();
        logcommit = logcommit.deserialize(committree.
                getCurrentbranch().getHead());
        while (logcommit.getParent() != null
                || logcommit.ifMerge()) {
            if (logcommit.ifMerge()) {
                System.out.println(logcommit.printMerge());
                String parentsha1 = logcommit.getParent();
                logcommit =
                        logcommit.deserialize(parentsha1);
            } else {
                System.out.println(logcommit);
                String parentsha1 = logcommit.getParent();
                logcommit =
                        logcommit.deserialize(parentsha1);
            }
        }
        System.out.println(logcommit);
    }

    /**
     * Prints the log of all commits ever committed. */
    public void globallog() {
        committree = committree.deserialize("commitTree");
        HashMap<String, Commit> allcommits = committree.getAllcommits();
        for (String commitname : allcommits.keySet()) {
            System.out.println(allcommits.get(commitname));
        }
    }

    /**
     * Creates a new branch but DOES NOT switch to the branch.
     * @param bname branch name */
    public void branch(String bname) {
        committree = committree.deserialize("commitTree");
        HashMap<String, Branch> allbranches = committree.getAllbranches();
        if (allbranches.containsKey(bname)) {
            Utils.message("A branch with that name already exists.");
            System.exit(0);
        }
        Branch newbranch = new Branch(bname,
                committree.getCurrentbranch().getHead());
        HashMap<String, String> branchfiles = newbranch.getBranchfiles();
        branchfiles.putAll(committree
                .getCurrentbranch().getBranchfiles());
        allbranches.put(bname, newbranch);
        committree.serialize("commitTree");
    }

    /** Removes the given branch pointer but.
     * does NOT delete all commits made in the branch
     * @param bname branch name. */
    public void rmbranch(String bname) {
        committree = committree.deserialize("commitTree");
        HashMap<String, Branch> allbranches = committree.getAllbranches();
        if (!allbranches.containsKey(bname)) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (committree.getCurrentbranch().getName().equals(bname)) {
            Utils.message("Cannot remove the current branch.");
            System.exit(0);
        }
        allbranches.remove(bname);
        committree.serialize("commitTree");
    }

    /**
     * Prints the status of the git repository with
     * the branches , staged files , removed files , untracked files
     * and modifications.
     */
    public void status() {
        if (!new File(".gitlet/").exists()) {
            Utils.message("Not in an initiazlied Gitlet directory.");
            System.exit(0);
        }
        committree = committree.deserialize("commitTree");
        stage = stage.deserialize("stage");
        HashMap<String, Branch> branches = committree.getAllbranches();
        HashMap<String, String> files = stage.getStagedFiles();
        HashMap<String, String> rfiles = stage.getRemovedFiles();
        System.out.println("=== Branches ===");
        Object[] allbranches = branches.keySet().toArray();
        Arrays.sort(allbranches);
        for (Object name : allbranches) {
            if (committree.getCurrentbranch().getName().equals(name)) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        }
        System.out.println();
        Object[] stagedfiles = files.keySet().toArray();
        Arrays.sort(stagedfiles);
        System.out.println("=== Staged Files ===");
        for (Object name : stagedfiles) {
            System.out.println(name);
        }
        System.out.println();
        Object[] removedfiles = rfiles.keySet().toArray();
        Arrays.sort(removedfiles);
        System.out.println("=== Removed Files ===");
        for (Object name : removedfiles) {
            System.out.println(name);
        }
        System.out.println();

        modifiedfiles();
        deletedfiles();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String filename : deletedfiles.keySet()) {
            System.out.println(filename + " (deleted)");
        }
        for (String filename : modifiedfiles.keySet()) {
            System.out.println(filename + " (modifed)");
        }
        System.out.println();
        untrackedfiles();
        System.out.println("=== Untracked Files ===");
        for (String filename : untrackedfiles.keySet()) {
            System.out.println(filename);
        }
        System.out.println();

        deletedfiles.clear();
        modifiedfiles.clear();
        untrackedfiles.clear();
        committree.serialize("commitTree");
        stage.serialize("stage");
    }

    /**
     * Removes the current file if it exists in the latest commit
     * and adds it to list of removed files to
     * be listed in next commit.
     * Also un-stages the file if it is currently staged.
     * @param filename filename
     */
    public void rm(String filename) {
        Commit latest = new Commit();
        stage = stage.deserialize("stage");
        committree = committree.deserialize("commitTree");
        latest = latest.deserialize(committree.getCurrentbranch().getHead());
        HashMap<String, String> removedfiles = stage.getRemovedFiles();
        HashMap<String, String> stagedfiles = stage.getStagedFiles();
        HashMap<String, String> trackedfiles = latest.getTrackedfiles();
        HashMap<String, String> branchfiles = committree.getAllbranches()
                .get(committree.getCurrentbranch().
                        getName()).getBranchfiles();
        if (!stagedfiles.containsKey(filename)
                && !branchfiles.containsKey(filename)) {
            Utils.message("No reason to remove the file.");
            System.exit(0);
        }
        if (stagedfiles.containsKey(filename)) {
            stagedfiles.remove(filename);
        }
        if (stagedfiles.containsKey(filename)) {
            stagedfiles.remove(filename);
        }
        if (branchfiles.containsKey(filename)) {
            removedfiles.put(filename, trackedfiles.get(filename));
            Utils.restrictedDelete(new File(filename));
        }
        stage.serialize("stage");
        committree.serialize("commitTree");
    }

    /**
     * Finds and prints all the commits with the given message.
     * @param message message */
    public void find(String message) {
        Commit curr = new Commit();
        committree = committree.deserialize("commitTree");
        HashMap<String, Commit> allcommits = committree.getAllcommits();
        int count = allcommits.size();
        for (String commitname : allcommits.keySet()) {
            curr = curr.deserialize(commitname);
            if (curr.getMessage().equals(message)) {
                System.out.println(curr.getCommitSHA1());
            } else {
                count -= 1;
            }
        }
        if (count == 0) {
            Utils.message("Found no commit with that message.");
            System.exit(0);
        }
    }

    /**
     * Checks out all the files tracked by the given commit id.
     * Removes tracked files that are not present in that commit
     * Moves the current branch's head to that commit.
     * The staging area is cleared.
     * @param commitid id */
    public void reset(String commitid) {
        stage = stage.deserialize("stage");
        committree = committree.deserialize("commitTree");
        HashMap<String, Commit> allcommits = committree.getAllcommits();
        HashMap<String, Branch> allbranches = committree.getAllbranches();
        if (!allcommits.containsKey(commitid)) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }
        Commit current = new Commit();
        current = current.deserialize
                (committree.getCurrentbranch().getHead());
        HashMap<String, String> currentfiles = current.getFiles();
        Commit given = allcommits.get(commitid);
        checkuntracked();
        HashMap<String, String> givenfiles = given.getTrackedfiles();
        for (String filename : givenfiles.keySet()) {
            File blob = new File(".gitlet/blobs/"
                    + givenfiles.get(filename));
            if (new File(filename).exists()) {
                Utils.writeContents(new File(filename),
                        Utils.readContentsAsString(blob));
            } else {
                Utils.writeContents(new File(filename),
                        Utils.readContentsAsString(blob));
            }
        }
        for (String filename : currentfiles.keySet()) {
            if (!givenfiles.containsKey(filename)) {
                File file = new File(filename);
                if (file.exists()) {
                    Utils.restrictedDelete(new File(filename));
                }
            }
        }
        for (String filename : stage.getStagedFiles().keySet()) {
            if (!givenfiles.containsKey(filename)) {
                File file = new File(filename);
                if (file.exists()) {
                    Utils.restrictedDelete(new File(filename));
                }
            }
        }
        stage.clearStage();
        stage.serialize("stage");
        Branch currentbranch = committree.getCurrentbranch();
        committree.setHead(commitid);
        HashMap<String, String> combinedfiles = currentfiles;
        combinedfiles.putAll(givenfiles);
        HashMap<String, String> branchfiles = currentbranch.getBranchfiles();
        branchfiles.clear();
        List<String> workingfiles =
                Utils.plainFilenamesIn(System.getProperty("user.dir"));
        for (String filename : workingfiles) {
            branchfiles.put(filename, combinedfiles.get(filename));
        }
        allbranches.replace(currentbranch.getName(), currentbranch);
        committree.setBranches(allbranches);
        committree.serialize("commitTree");
    }

    /**
     * Given a branch "bname" merges that branch into the current branch.
     * Need to keep track of 3 things.
     * 1. Current branch commit files.
     * 2. Given branch commit files.
     * 3. Split commit files.
     * @param bname branch name.*/
    public void merge(String bname) {
        premerge(bname);
        Commit splitcommit = new Commit();
        committree = committree.deserialize("commitTree");
        stage = stage.deserialize("stage");
        boolean conflict = false;
        HashMap<String, Branch> allbranches = committree.getAllbranches();
        Branch currentbranch = committree.getCurrentbranch();
        Branch givenbranch = allbranches.get(bname);
        String splitcommitsha1 = committree.commonAncestor(bname);
        splitcommit = splitcommit.deserialize(splitcommitsha1);
        HashMap<String, String> branchfiles = currentbranch.getBranchfiles();
        HashMap<String, String> splitfiles = splitcommit.getFiles();
        HashMap<String, String> givenfiles = givenbranch.getBranchfiles();
        HashMap<String, String> currentfiles = currentbranch.getBranchfiles();
        HashMap<String, String> stagedfiles = stage.getStagedFiles();
        HashMap<String, String> combinedfiles = new HashMap<>();
        HashMap<String, String> removefiles = stage.getRemovedFiles();
        List<String> workingfiles =
                Utils.plainFilenamesIn(System.getProperty("user.dir"));
        combinedfiles.putAll(splitfiles);
        combinedfiles.putAll(givenfiles);
        combinedfiles.putAll(currentfiles);
        String message = "";
        for (String filename : combinedfiles.keySet()) {
            String splitsha1 = splitfiles.get(filename);
            if (splitsha1 != null) {
                conflict = domerge1(branchfiles,
                        splitfiles, currentfiles, combinedfiles,
                        givenbranch, workingfiles,
                        filename, conflict);
            } else {
                conflict = domerge2(currentfiles, stagedfiles,
                        combinedfiles, givenfiles,
                        givenbranch, filename, conflict);
            }
        }
        branchfiles.clear();
        workingfiles = Utils.plainFilenamesIn(System.getProperty("user.dir"));
        for (String filename : workingfiles) {
            branchfiles.put(filename, combinedfiles.get(filename));
        }
        message = String.format("Merged %s into %s.",
                givenbranch.getName(), currentbranch.getName());
        if (conflict) {
            Utils.message("Encountered a merge conflict.");
            mergecommit(message, givenbranch.getName());
        } else {
            mergecommit(message, givenbranch.getName());
        }
        allbranches.replace(currentbranch.getName(), currentbranch);
        committree.setBranches(allbranches);
        committree.serialize("commitTree");
        stage.serialize("stage");
    }

    /** Merge helper.
     *
     * @param branchfiles a
     * @param splitfiles b
     * @param currentfiles c
     * @param combinedfiles e
     * @param givenbranch h
     * @param workingfiles i
     * @param filename j
     * @param conflict k
     * @return l
     */
    public boolean domerge1(HashMap<String, String> branchfiles,
                        HashMap<String, String> splitfiles,
                        HashMap<String, String> currentfiles,
                        HashMap<String, String> combinedfiles,
                        Branch givenbranch,
                        List<String> workingfiles,
                         String filename,
                         boolean conflict) {
        HashMap<String, String> givenfiles = givenbranch.getBranchfiles();
        HashMap<String, String> stagedfiles = stage.getStagedFiles();
        HashMap<String, String> removefiles = stage.getRemovedFiles();
        String currentsha1 = currentfiles.get(filename);
        String givensha1 = givenfiles.get(filename);
        String splitsha1 = splitfiles.get(filename);
        if (splitfiles.containsKey(filename)) {
            if (!isModified(splitsha1, currentsha1)
                    && isModified(splitsha1, givensha1)
                    && currentfiles.containsKey(filename)
                    && givenfiles.containsKey(filename)) {
                checkoutcommit(givenbranch.getHead(), filename);
                stagedfiles.put(filename, combinedfiles.get(filename));
            } else if (isModified(splitsha1, currentsha1)
                    && !isModified(splitsha1, givensha1)) {
                return conflict;
            } else if (unmodified(currentsha1, givensha1)) {
                return conflict;
            } else if (workingfiles.contains(filename)
                    && !currentfiles.containsKey(filename)
                    && !givenfiles.containsKey(filename)) {
                return conflict;
            } else if (!isModified(splitsha1, currentsha1)
                    && !givenfiles.containsKey(filename)) {
                Utils.restrictedDelete(filename);
                branchfiles.remove(filename);
                removefiles.put(filename, combinedfiles.get(filename));
            } else if (!isModified(splitsha1, givensha1)
                    && !currentfiles.containsKey(filename)) {
                return conflict;
            } else if (isModified(givensha1, currentsha1)) {
                conflict = true;
                mergeConflict(filename, givenbranch);
            }
        }
        return conflict;
    }

    /** Merge helper.
     *
     * @param currentfiles a
     * @param stagedfiles b
     * @param combinedfiles c
     * @param givenfiles d
     * @param givenbranch e
     * @param filename f
     * @param conflict g
     * @return h
     */
    public boolean domerge2(
                         HashMap<String, String> currentfiles,
                         HashMap<String, String> stagedfiles,
                         HashMap<String, String> combinedfiles,
                         HashMap<String, String> givenfiles,
                         Branch givenbranch,
                         String filename,
                         boolean conflict) {
        String currentsha1 = currentfiles.get(filename);
        String givensha1 = givenfiles.get(filename);
        if (currentfiles.containsKey(filename)
                && !givenfiles.containsKey(filename)) {
            return conflict;
        } else if (!currentfiles.containsKey(filename)
                && givenfiles.containsKey(filename)) {
            checkoutcommit(givenbranch.getHead(), filename);
            stagedfiles.put(filename, combinedfiles.get(filename));
        } else if (isModified(givensha1, currentsha1)) {
            conflict = true;
            mergeConflict(filename, givenbranch);
        }
        return conflict;
    }



    /**
     * Merge conflict method that writes into the files.
     * @param filename name
     * @param givenbranch branch */
    private void mergeConflict(String filename, Branch givenbranch) {
        Commit curr = new Commit();
        Commit given = new Commit();
        curr = curr.deserialize(committree.getCurrentbranch().getHead());
        given = given.deserialize(givenbranch.getHead());
        HashMap<String, String> currentfiles = curr.getFiles();
        HashMap<String, String> givenfiles = given.getFiles();
        String result = "<<<<<<< HEAD\n";
        if (currentfiles.containsKey(filename)) {
            File ccf = new File(".gitlet/blobs/" + currentfiles.get(filename));
            String ccfc = Utils.readContentsAsString(ccf);
            result += ccfc;
        }
        result += "=======\n";
        if (givenfiles.containsKey(filename)) {
            File gcf = new File(".gitlet/blobs/" + givenfiles.get(filename));
            String gcfc = Utils.readContentsAsString(gcf);
            result += gcfc;
        }
        result += ">>>>>>>\n";
        Utils.writeContents(new File(filename), result);
    }

    /**
     * The merge commit method !!
     * different because we have a set of parents!
     * @param message message
     * @param givenbranchname name */
    public void mergecommit(String message, String givenbranchname) {
        stage = stage.deserialize("stage");
        committree = committree.deserialize("commitTree");
        Commit currentcommit = new Commit();
        HashMap<String, String> stagedfiles = stage.getStagedFiles();
        HashMap<String, Commit> allcommits = committree.getAllcommits();
        currentcommit = currentcommit.deserialize
                (committree.getCurrentbranch().getHead());
        HashMap<String, String> trackedFiles =
                currentcommit.getTrackedfiles();
        HashMap<String, String> removefiles =
                stage.getRemovedFiles();
        HashMap<String, Branch> allbranches =
                committree.getAllbranches();
        for (String filename : stagedfiles.keySet()) {
            trackedFiles.put(filename, stagedfiles.get(filename));
        }
        Branch given = allbranches.get(givenbranchname);
        Commit newCommit = new Commit(message, currentcommit.getCommitSHA1(),
                given.getHead(), stagedfiles);
        HashMap<String, String> trackedfiles =
                newCommit.getTrackedfiles();
        for (String filename : stagedfiles.keySet()) {
            trackedfiles.put(filename, stagedfiles.get(filename));
        }
        allcommits.put(newCommit.getCommitSHA1(), newCommit);
        File commitFile = new File(".gitlet/commits/"
                + newCommit.getCommitSHA1());
        Utils.writeObject(commitFile, newCommit);
        removefiles.clear();
        stagedfiles.clear();
        stage.serialize("stage");
        allbranches.remove(committree.getCurrentbranch().getName());
        committree.setHead(newCommit.getCommitSHA1());
        allbranches.put(committree.getCurrentbranch().getName(),
                committree.getCurrentbranch());
        committree.serialize("commitTree");
    }

    /**
     * Checks necessary conditions before merge can occur.
     * @param bname Branch name.
     */
    public void premerge(String bname) {
        committree = committree.deserialize("commitTree");
        stage = stage.deserialize("stage");
        HashMap<String, String> stagedfiles = stage.getStagedFiles();
        HashMap<String, String> removefiles = stage.getRemovedFiles();
        HashMap<String, Branch> allbranches = committree.getAllbranches();
        Branch currentbranch = committree.getCurrentbranch();
        if (!allbranches.containsKey(bname)) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (currentbranch.getName().equals(bname)) {
            Utils.message("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String splitcommitsha1 = committree.commonAncestor(bname);
        if (splitcommitsha1.equals(allbranches.get(bname).getHead())) {
            Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitcommitsha1.equals(currentbranch.getHead())) {
            checkoutbranch(bname);
            committree.setHead(splitcommitsha1);
            Utils.message("Current branch fast-forwarded.");
            System.exit(0);
        }
        if (stagedfiles.size() != 0 || removefiles.size() != 0) {
            Utils.message("You have uncommitted changes.");
            System.exit(0);
        }
        checkuntracked();
    }

    /**
     * Checks if a file is identical to the file in the last commit.
     * @param filename filename.
     * @param addfileSHA1 addfilesha1.
     * @return boolean. */
    public boolean isIdentical(String filename, String addfileSHA1) {
        committree = committree.deserialize("commitTree");
        Commit curr = new Commit();
        curr = curr.deserialize(committree.getCurrentbranch().getHead());
        HashMap<String, String> latestcommitfiles = curr.getFiles();
        if (latestcommitfiles.containsKey(filename)) {
            String commitSHA1 = latestcommitfiles.get(filename);
            if (commitSHA1.equals(addfileSHA1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if there are untracked files in the
     * current working directory.
     */
    private void checkuntracked() {
        stage = stage.deserialize("stage");
        committree = committree.deserialize("commitTree");
        List<String> workingfiles =
                Utils.plainFilenamesIn(System.getProperty("user.dir"));
        HashMap<String, String> stagedfiles = stage.getStagedFiles();
        HashMap<String, String> branchfiles = committree.getAllbranches().get
                (committree.getCurrentbranch().getName()).
                getBranchfiles();
        for (String filename : workingfiles) {
            if (!branchfiles.containsKey(filename)
                    && !stagedfiles.containsKey(filename)
                    && !filename.equals(".DS_Store")
                    && !filename.equals("proj3.iml")) {
                Utils.message("There is an untracked file in "
                        +  "the way; delete it or add it first.");
                System.exit(0);
            }
        }
    }

    /**
     * Checks if a file is modified or not is the same in two branches.
     * @param file1 file1.
     * @param file2 file2.
     * @return boolean. */

    private boolean isModified(String file1, String file2) {
        if (file1 == null && file2 == null) {
            return true;
        }
        return !file1.equals(file2);
    }

    /**
     * @param file1 is first file we compared.
     * @param file2 is second file we compare.
     * @return boolean. */
    private boolean unmodified(String file1, String file2) {
        if (file1 == null && file2 == null) {
            return true;
        }
        return file1.equals(file2);
    }

    /**
     * Gets untracked files for status.
     */
    private void untrackedfiles() {
        stage = stage.deserialize("stage");
        committree = committree.deserialize("commitTree");
        List<String> workingfiles =
                Utils.plainFilenamesIn(System.getProperty("user.dir"));
        HashMap<String, String> stagedfiles = stage.getStagedFiles();
        HashMap<String, String> branchfiles = committree.getAllbranches()
                .get(committree.getCurrentbranch().getName()).getBranchfiles();

        for (String filename : workingfiles) {
            if (!branchfiles.containsKey(filename)
                    && !stagedfiles.containsKey(filename)
                    && !filename.equals(".DS_Store")
                    && !filename.equals("proj3.iml")) {
                untrackedfiles.put(filename,
                        Utils.readContentsAsString(new File(filename)));
            }
        }
    }
    /** Gets deleted files for ec. */
    private void deletedfiles() {
        List<String> workingfiles =
                Utils.plainFilenamesIn(System.getProperty("user.dir"));
        HashMap<String, String> stagedfiles =
                stage.getStagedFiles();
        HashMap<String, String> branchfiles = committree.getAllbranches()
                .get(committree.getCurrentbranch().getName()).getBranchfiles();
        HashMap<String, String> removefiles =
                stage.getRemovedFiles();
        for (String filename : stagedfiles.keySet()) {
            if (!workingfiles.contains(filename)) {
                deletedfiles.put(filename,
                        Utils.readContentsAsString(new File(filename)));
            }
        }
        for (String filename : branchfiles.keySet()) {
            if (!removefiles.containsKey(filename)
                    && !workingfiles.contains(filename)) {
                deletedfiles.put(filename, branchfiles.get(filename));
            }
        }
    }
    /** Gets modified files for ec. */
    public void modifiedfiles() {
        List<String> workingfiles =
                Utils.plainFilenamesIn(System.getProperty("user.dir"));
        HashMap<String, String> stagedfiles = stage.getStagedFiles();
        HashMap<String, String> branchfiles =
                committree.getCurrentbranch().getBranchfiles();
        for (String filename : workingfiles) {
            String sha1 =
                    Utils.sha1(Utils.readContentsAsString(new File(filename)));
            if (branchfiles.containsKey(filename)
                    && isModified(branchfiles.get(filename), sha1)
                    && !stagedfiles.containsKey(filename)) {
                modifiedfiles.put(filename,
                        Utils.readContentsAsString(new File(filename)));
            } else if (stagedfiles.containsKey(filename)
                    && isModified(stagedfiles.get(filename), sha1)) {
                modifiedfiles.put(filename,
                        Utils.readContentsAsString(new File(filename)));
            }
        }
    }
    public void addremote(String name, String directory) {
        File dir = Utils.join(directory + File.separator + name + File.separator);
        if (dir.exists()) {
            Utils.message("A remote with that name already exists.");
            System.exit(0);
        }
        Utils.writeContents(dir, Utils.serialize(dir));
    }
    




    /**
     * The stagingArea of the repository.
     */
    private StagingArea stage;
    /**
     * The Initial commit.
     */
    private Commit initialcommit;
    /**
     * The commitTree of the repository.
     */
    private CommitTree committree;
    /** Untracked files. */
    private HashMap<String, String> untrackedfiles;
    /** Modified files. */
    private HashMap<String, String> modifiedfiles;
    /** Deleted files. */
    private HashMap<String, String> deletedfiles;
}
