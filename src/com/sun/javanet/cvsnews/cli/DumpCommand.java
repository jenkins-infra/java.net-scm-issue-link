package com.sun.javanet.cvsnews.cli;

import com.sun.javanet.cvsnews.NewsItem;

/**
 * @author Kohsuke Kawaguchi
 */
public class DumpCommand extends AbstractCommand {
    public int execute() throws Exception {
        for (NewsItem item : parseStdin()) {
            // TODO
        }
        return 0;
    }
}
