package com.sun.javanet.cvsnews.cli;

import com.sun.javanet.cvsnews.Commit;

import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Partial {@link Command} implementation that deals with issues in the issue tracker.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractIssueCommand extends AbstractCommand implements Command {
    /**
     * Discovers links to issues.
     */
    protected final Set<Issue> parseIssues(Commit commit) {
        Set<Issue> issues = new HashSet<Issue>();
        Matcher m = ISSUE_MARKER.matcher(commit.log);
        while(m.find())
            issues.add(new Issue(commit.project,m.group(1)));

        m = ID_MARKER.matcher(commit.log);
        while(m.find())
            issues.add(new Issue(m.group(1),m.group(2)));
        return issues;
    }

    /**
     * Issue in an issue tracker.
     */
    public static final class Issue {
        public final String projectName;
        public final int number;

        public Issue(String projectName, int number) {
            this.projectName = projectName.toLowerCase();
            this.number = number;
        }

        public Issue(String projectName, String number) {
            this(projectName,Integer.parseInt(number));
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UpdateCommand.Issue issue = (UpdateCommand.Issue) o;

            return number == issue.number && projectName.equals(issue.projectName);

        }

        public int hashCode() {
            int result;
            result = projectName.hashCode();
            result = 31 * result + number;
            return result;
        }

        public String toString() {
            return projectName+'-'+number;
        }
    }

    /**
     * Look for strings like "issue #350" and "issue 350"
     */
    private static final Pattern ISSUE_MARKER = Pattern.compile("\\bissue #?(\\d+)\\b");

    /**
     * Look for full ID line like "JAXB-512"
     */
    private static final Pattern ID_MARKER = Pattern.compile("\\b([A-Z-]+)-(\\d+)\\b");
}
