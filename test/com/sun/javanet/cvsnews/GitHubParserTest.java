package com.sun.javanet.cvsnews;

import junit.framework.TestCase;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * @author Kohsuke Kawaguchi
 */
public class GitHubParserTest extends TestCase {
    public void testParse() throws Exception {
        new GitHubParser().parse(new MimeMessage(Session.getInstance(System.getProperties()),
                getClass().getResourceAsStream("github.txt")));
    }
}
