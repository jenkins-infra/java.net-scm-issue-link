package com.cloudbees.javanet.cvsnews;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Date;

/**
 * @author Kohsuke Kawaguchi
 */
public class GitHubCommit extends Commit<GitHubCodeChange> {
    public final String commitSha1;
    /**
     * E-mail address of the author.
     */
    public final String author;

    public final String url;

    public GitHubCommit(String commitSha1, String url, String project, String author, Date date, String log) throws AddressException {
        super(project, toUserName(author), date, log);
        this.commitSha1 = commitSha1;
        this.author = author;
        this.url = url;
    }

    private static String toUserName(String author) throws AddressException {
        String p = new InternetAddress(author).getPersonal();
        if (p==null)    return author;  // use the e-mail adress as the fall back
        return p;
    }
}
