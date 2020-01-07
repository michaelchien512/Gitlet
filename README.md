# Gitlet

Gitlet is a version-control system analogous to Git. It is capable of the basic Git commands such as adding, commiting, restoring versions of files, maintaing sequences of commits through branches, and much more listed down below.
Gitlet uses several of the same object git uses to keep track of data such as blobs, trees, and commits. Each commit and blob 
is given a unique integer ID using a cryptographic hash function called SHA-1 (Secure Hash 1) capable of produdcing a 160-bit 
integer hash function from a sequence of bytes. 

<img width="569" alt="Screen Shot 2020-01-07 at 12 59 01 AM" src="https://user-images.githubusercontent.com/47373165/71882269-fb9ae600-30e8-11ea-8063-f84349684aa8.png">

## Commands 
**-init**
Usage: java gitlet.Main init
Description: Creates a new Gitlet version-control system in the current directory. This system will automatically start with one commit.

**-add**\
Usage: java gitlet.Main add [file name]
Description: Adds a copy of the file as it currently exists to the staging area (see the description of the commit command). For this reason, adding a file is also called staging the file. 

**-commit**\
Usage: java gitlet.Main commit [message]

Description: Saves a snapshot of certain files in the current commit and staging area so they can be restored at a later time, creating a new commit. The commit is said to be tracking the saved files. By default, each commit's snapshot of files will be exactly the same as its parent commit's snapshot of files; it will keep versions of files exactly as they are, and not update them. A commit will only update files it is tracking that have been staged at the time of commit, in which case the commit will now include the version of the file that was staged instead of the version it got from its parent. A commit will save and start tracking any files that were staged but weren't tracked by its parent. Finally, files tracked in the current commit may be untracked in the new commit as a result of the rm command (below).

**-rm**
Usage: java gitlet.Main rm [file name]
Description: Unstage the file if it is currently staged. If the file is tracked in the current commit, mark it to indicate that it is not to be included in the next commit (presumably you would store this mark somewhere in the .gitlet directory), and remove the file from the working directory if the user has not already done so (do not remove it unless it is tracked in the current commit).

**-log**
Usage: java gitlet.Main log
Description: Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits. (In regular Git, this is what you get with git log --first-parent). This set of commit nodes is called the commit's history. For every node in this history, the information it should display is the commit id, the time the commit was made, and the commit message. Here is an example of the exact format:

   ===
   commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
   Date: Thu Nov 9 20:00:05 2017 -0800
   A commit message.

   ===
   commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
   Date: Thu Nov 9 17:01:33 2017 -0800
   Another commit message.

   ===
   commit e881c9575d180a215d1a636545b8fd9abfb1d2bb
   Date: Wed Dec 31 16:00:00 1969 -0800
   initial commit


**-global-log**
Usage: java gitlet.Main global-log
Description: Like log, except displays information about all commits ever made. The order of the commits does not matter.

**-find**
Usage: java gitlet.Main find [commit message]
Description: Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks, as for the commit command below.
Runtime: Should be linear relative to the number of commits.

status
Usage: java gitlet.Main status
Description: Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged or marked for untracking. An example of the exact format it should follow is as follows.

=== Branches ===
*master
other-branch

=== Staged Files ===
wug.txt
wug2.txt

=== Removed Files ===
goodbye.txt

=== Modifications Not Staged For Commit ===
junk.txt (deleted)
wug3.txt (modified)

=== Untracked Files ===
random.stuff
There is an empty line between each section. Entries should be listed in lexicographic order, using the Java string-comparison order (the asterisk doesn't count). A file in the working directory is "modified but not staged" if it is

Tracked in the current commit, changed in the working directory, but not staged; or
Staged for addition, but with different contents than in the working directory; or
Staged for addition, but deleted in the working directory; or
Not staged for removal, but tracked in the current commit and deleted from the working directory.

The final category ("Untracked Files") is for files present in the working directory but neither staged for addition nor tracked. This includes files that have been staged for removal, but then re-added without Gitlet's knowledge. Ignore any subdirectories that may have been introduced, since Gitlet does not deal with them.

The last two sections (modifications not staged and untracked files) are extra credit. Feel free to leave them blank (leaving just the headers).

Runtime: Make sure this depends only on the amount of data in the working directory plus the number of files staged to be added or deleted plus the number of branches.
Failure cases: None
Dangerous?: No
Our line count: ~45
checkout
Checkout is a kind of general command that can do a few different things depending on what its arguments are. There are 3 possible use cases. In each section below, you'll see 3 bullet points. Each corresponds to the respective usage of checkout.

Usages:

java gitlet.Main checkout -- [file name]
java gitlet.Main checkout [commit id] -- [file name]
java gitlet.Main checkout [branch name]
Descriptions:

Takes the version of the file as it exists in the head commit, the front of the current branch, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.

Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.

Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch (see Failure cases below).


branch
Usage: java gitlet.Main branch [branch name]
Description: Creates a new branch with the given name, and points it at the current head node. A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node. This command does NOT immediately switch to the newly created branch (just as in real Git). Before you ever call branch, your code should be running with a default branch called "master".
Runtime: Should be constant relative to any significant measure.

