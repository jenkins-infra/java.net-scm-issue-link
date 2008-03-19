package com.sun.javanet.cvsnews.cli;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import com.sun.javanet.cvsnews.NewsItem;

/**
 * Converts all file layout (as of 2006/11) to new ones.
 *
 * @author Kohsuke Kawaguchi
 */
public class ConvertCommand implements Command {
    @Argument
    List<File> sources = new ArrayList<File>();

    @Option(name="-d",usage="Output data directory")
    File out = new File("news");

    public int execute() throws Exception {
        for (File src : sources) {
            System.out.println(src);
            NewsItem.load(src).save(out);
        }
        return 0;
    }
}
