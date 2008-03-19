package com.sun.javanet.cvsnews.cli;

import com.sun.javanet.cvsnews.Commit;

import java.util.Set;
import java.util.HashSet;

/**
 * Subcommand that reads e-mail from stdin and adds news files.
 *
 * @author Kohsuke Kawaguchi
 */
public class UpdateCommand extends AbstractIssueCommand {

    public int execute() throws Exception {
        System.out.println("Parsing stdin");
        Commit commit = parseStdin();
        Set<Issue> issues = parseIssues(commit);

        System.out.println("Found "+issues);

        return 0;
    }

    private final static Set<String> PARTICIPATING_PROJECTS = new HashSet<String>(); 
}
