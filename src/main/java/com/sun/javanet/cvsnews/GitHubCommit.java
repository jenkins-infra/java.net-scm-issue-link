package com.sun.javanet.cvsnews;

import java.util.Date;

/**
 * @author Kohsuke Kawaguchi
 */
public class GitHubCommit extends Commit<GitHubCodeChange> {
    public final String commitSha1;
    public GitHubCommit(String commitSha1, String project, String userName, Date date, String log) {
        super(project, userName, date, log);
        this.commitSha1 = commitSha1;
    }
}
