package com.sun.javanet.cvsnews.cli;

import com.sun.javanet.cvsnews.CodeChange;
import com.sun.javanet.cvsnews.Commit;
import org.kohsuke.jnt.JNIssue;
import org.kohsuke.jnt.JNProject;
import org.kohsuke.jnt.JavaNet;
import org.kohsuke.jnt.ProcessingException;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;

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
        boolean hasFisheye = FISHEYE_CVS_PROJECT.contains(commit.project);

        StringBuilder buf = new StringBuilder();
        buf.append("Code changed in "+commit.project+"\n");
        buf.append(MessageFormat.format("User: {0}\n",commit.userName));
        buf.append("Path:\n");
        for (CodeChange cc : commit.getCodeChanges()) {
            buf.append(MessageFormat.format(" {0} ({1})\n",cc.fileName,cc.revision));
            if(!hasFisheye)
                buf.append("   "+cc.url+"\n");
        }
        if(hasFisheye) {
            try {
                buf.append(MessageFormat.format(
                " http://fisheye5.cenqua.com/changelog/{0}/?cs={1}:{2}:{3}\n",
                    commit.project,
                    commit.userName,
                    commit.branch==null?"MAIN":commit.branch,
                    DATE_FORMAT.format(commit.getCodeChanges().get(0).determineTimstamp())));
            } catch (IOException e) {
                e.printStackTrace();
                buf.append("Failed to compute FishEye link "+e+"\n");
            } catch (InterruptedException e) {
                e.printStackTrace();
                buf.append("Failed to compute FishEye link "+e+"\n");
            }
        }
        buf.append("\n");
        buf.append("Log:\n");
        buf.append(commit.log);

        return buf.toString();
    }

    // taken from http://fisheye5.cenqua.com/
    private static final Set<String> FISHEYE_CVS_PROJECT = new HashSet<String>(Arrays.asList(
            "actions",
            "cejug-classifieds",
            "clickstream",
            "databinding",
            "dwr",
            "equinox",
            "fi",
            "flamingo",
            "genericjmsra",
            "genesis",
            "glassfish",
            "hudson",
            "hyperjaxb",
            "hyperjaxb2",
            "hyperubl",
            "javaserverfaces-sources",
            "jax-rpc",
            "jax-rpc-sources",
            "jax-rpc-tck",
            "jax-ws-sources",
            "jax-ws-tck",
            "jax-wsa",
            "jax-wsa-sources",
            "jax-wsa-tck",
            "jaxb",
            "jaxb-architecture-document",
            "jaxb-sources",
            "jaxb-tck",
            "jaxb-verification",
            "jaxb-workshop",
            "jaxb2-commons",
            "jaxb2-sources",
            "jaxmail",
            "jaxp-sources",
            "jaxwsunit",
            "jdic",
            "jdnc",
            "jwsdp",
            "jwsdp-samples",
            "l2fprod-common",
            "laf-plugin",
            "laf-widget",
            "lg3d",
            "ognl",
            "open-esb",
            "open-jbi-components",
            "osuser",
            "osworkflow",
            "quartz",
            "saaj",
            "sailfin",
            "sbfb",
            "schoolbus",
            "semblance",
            "shard",
            "sitemesh",
            "sjsxp",
            "skinlf",
            "stax-utils",
            "substance",
            "swing-layout",
            "swinglabs",
            "swinglabs-demos",
            "swingworker",
            "swingx",
            "tda",
            "tonic",
            "truelicense",
            "truezip",
            "txw",
            "webleaf",
            "webleaftest",
            "webwork",
            "wizard",
            "wsit",
            "xmlidfilter",
            "xom",
            "xsom",
            "xwork",
            "xwss"));

   private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
}
