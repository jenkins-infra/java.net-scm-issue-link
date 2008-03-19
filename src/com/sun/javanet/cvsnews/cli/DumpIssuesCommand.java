package com.sun.javanet.cvsnews.cli;

import com.sun.javanet.cvsnews.Commit;

import java.util.Set;

/**
 * Dumps issues found in the commit message.
 *
 * @author Kohsuke Kawaguchi
 */
public class DumpIssuesCommand extends AbstractIssueCommand {
    public int execute() throws Exception {
        System.out.println("Parsing stdin");
        Commit commit = parseStdin();
        Set<Issue> issues = parseIssues(commit);

        System.out.println("Found "+issues);

        return 0;
    }
}
