package com.sun.javanet.cvsnews.cli;

import com.sun.javanet.cvsnews.Commit;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Subcommand that reads e-mail from stdin and adds news files.
 *
 * @author Kohsuke Kawaguchi
 */
public class UpdateCommand extends AbstractCommand implements Command {

    public int execute() throws Exception {
        System.out.println("Parsing stdin");
        Commit commit = parseStdin();

        Matcher m = ISSUE_MARKER.matcher(commit.log);
        while(m.find()) {
            System.out.println("Found issue "+m.group(1));
        }

        m = ID_MARKER.matcher(commit.log);
        while(m.find()) {
            System.out.println("Found issue "+m.group(2)+" for "+m.group(1));
        }

        return 0;
    }

    // look for strings like "issue #350" and "issue 350"
    private static final Pattern ISSUE_MARKER = Pattern.compile("\\bissue #?(\\d+)\\b");

    // look for full ID line like "JAXB-512"
    private static final Pattern ID_MARKER = Pattern.compile("\\b([A-Z-]+)-(\\d+)\\b");
}
