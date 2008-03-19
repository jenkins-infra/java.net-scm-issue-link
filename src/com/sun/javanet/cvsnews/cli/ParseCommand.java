package com.sun.javanet.cvsnews.cli;

import com.sun.javanet.cvsnews2.Commit;

/**
 * Subcommand that reads e-mail from stdin and adds news files.
 *
 * @author Kohsuke Kawaguchi
 */
public class ParseCommand extends AbstractCommand implements Command {

    public int execute() throws Exception {
        Commit commit = parseStdin();

        System.out.println(commit);

        return 0;
    }

}
