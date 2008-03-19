package com.sun.javanet.cvsnews.cli;

import org.kohsuke.jnt.JNMailingList;
import org.kohsuke.jnt.JNMailingLists;
import org.kohsuke.jnt.JNProject;
import org.kohsuke.jnt.JavaNet;
import org.kohsuke.jnt.ProcessingException;
import static org.kohsuke.jnt.SubscriptionMode.NORMAL;

import java.io.File;

/**
 * Updates the subscriptions to the cvs/commits list of the various projects.
 *
 * @author Kohsuke Kawaguchi
 */
public class SubscriptionUpdateCommand extends AbstractCommand {
    public int execute() throws Exception {
        JavaNet user = JavaNet.connect(new File(HOME, ".java.net.scm_issue_link"));
        JavaNet admin = JavaNet.connect(new File(HOME, ".java.net.super_kohsuke"));

        for (JNProject p : user.getMyself().getMyProjects()) {
            System.out.println("Subscribing to "+p.getName());
            p = admin.getProject(p.getName());
            JNMailingLists mls = p.getMailingLists();
            // subscribe(mls.get("commits")); // TODO: subversion support
            subscribe(mls.get("cvs"));
        }
        
        return 0;
    }

    /**
     * Subscribes the daemon to the ML. Needs to run as a super user.
     */
    private void subscribe(JNMailingList ml) throws ProcessingException {
        if(ml==null)    return;
        ml.massSubscribe("scm_issue_link@dev.java.net",NORMAL);
    }
}
