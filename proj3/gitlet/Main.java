package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Michael Chien
 */
public class Main {


    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        Repository repo = new Repository();
        if (args.length == 0) {
            Utils.message("Please enter a command.");
            System.exit(0);
        }
        try {
            switch (args[0]) {
            case "init": repo.init();
                break;
            case "add": repo.add(args[1]);
                break;
            case "commit": repo.commit(args[1]);
                break;
            case "checkout": if (args.length == 3 && args[1].equals("--")) {
                    repo.checkoutfile(args[2]);
                    break;
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        throw new IllegalArgumentException("bad");
                    }
                    repo.checkoutcommit(args[1], args[3]);
                    break;
                } else {
                    repo.checkoutbranch(args[1]);
                    break;
                }
            case "log": repo.log();
                break;
            case "global-log": repo.globallog();
                break;
            case "branch": repo.branch(args[1]);
                break;
            case "rm-branch": repo.rmbranch(args[1]);
                break;
            case "status":
                repo.status();
                break;
            case "rm": repo.rm(args[1]);
                break;
            case "find": repo.find(args[1]);
                break;
            case "reset": repo.reset(args[1]);
                break;
            case "merge": repo.merge(args[1]);
                break;
                case "add-remote": repo.addremote(args[1], args[2]);
                break;
            default: Utils.message("No command with that name exists.");
                System.exit(0);
            }
        }   catch (IllegalArgumentException
                | ArrayIndexOutOfBoundsException exp) {
            Utils.message("Incorrect operands.");
            exp.getMessage();
            System.exit(0);
        }
    }
}


