package com.sun.javanet.cvsnews;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class GitHubCodeChange extends CodeChange {
    public GitHubCodeChange(String fileName, URL url) {
        super(fileName, url);
    }
}
