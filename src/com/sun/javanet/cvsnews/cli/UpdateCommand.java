package com.sun.javanet.cvsnews.cli;

import com.sun.javanet.cvsnews.CodeChange;
import com.sun.javanet.cvsnews.Commit;
import org.kohsuke.jnt.JNIssue;
import org.kohsuke.jnt.JNProject;
import org.kohsuke.jnt.JavaNet;
import org.kohsuke.jnt.ProcessingException;

import java.io.File;
import java.text.MessageFormat;
import java.util.Set;

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

        String msg = createUpdateMessage(commit);

        JavaNet con = JavaNet.connect(new File(HOME, ".java.net.scm_issue_link"));
        for (Issue issue : issues) {
            JNProject p = con.getProject(issue.projectName);
            if(!con.getMyself().getMyProjects().contains(p))
                // not a participating project
                continue;

            System.out.println("Updating "+issue);
            try {
                JNIssue i = p.getIssueTracker().get(issue.number);
                i.update(msg);
            } catch (ProcessingException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private String createUpdateMessage(Commit commit) {
        StringBuilder buf = new StringBuilder();
        buf.append("==[Code change]==\n");
        buf.append(MessageFormat.format("User: {0}\n",commit.userName));
        buf.append("Path:\n");
        for (CodeChange cc : commit.getCodeChanges()) {
            buf.append(MessageFormat.format(" {0} ({1})\n",cc.fileName,cc.revision));
            buf.append("   "+cc.url+"\n");
        }
        buf.append("Log:\n");
        buf.append(commit.log);

        return buf.toString();
    }
}
