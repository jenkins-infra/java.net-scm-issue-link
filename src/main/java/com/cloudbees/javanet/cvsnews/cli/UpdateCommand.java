/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 */

package com.cloudbees.javanet.cvsnews.cli;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.cloudbees.javanet.cvsnews.CodeChange;
import com.cloudbees.javanet.cvsnews.Commit;
import com.cloudbees.javanet.cvsnews.GitHubCommit;
import org.jenkinsci.jira.JIRA;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Subcommand that reads e-mail from stdin and updates the issue tracker.
 *
 * @author Kohsuke Kawaguchi
 */
public class UpdateCommand extends AbstractIssueCommand {
    private final File credential = new File(HOME, ".java.net.scm_issue_link");

    public int execute() throws Exception {
        System.out.println("Parsing stdin");
        return execute(parse(System.in));
    }

    public int execute(Collection<? extends Commit> commits) throws Exception {
        for (Commit commit : commits) {
            Set<Issue> issues = parseIssues(commit);

            System.out.println("Found "+issues);
            if(issues.isEmpty())
                continue;   // no issue link

            String msg = createUpdateMessage(commit);

            boolean markedAsFixed = FIXED.matcher(commit.log).find();

            for (Issue issue : issues) {
                if (PROJECTS.contains(issue.projectName)) {
                    System.out.println("Updating "+issue);
                    // update JIRA
                    JiraRestClient service = JIRA.connect(new URL("http://issues.jenkins-ci.org/"));

                    Properties props = new Properties();
                    props.load(new FileInputStream(credential));

                    String id = issue.projectName.toUpperCase() + "-" + issue.number;

                    String userName = props.getProperty("userName");

                    // if an issue doesn't exist an exception will be thrown
                    com.atlassian.jira.rest.client.api.domain.Issue i = service.getIssueClient().getIssue(id).claim();

                    // is this commit already reported?
                    Iterable<Comment> comments = i.getComments();
                    if (isAlreadyCommented(commit,userName,comments))
                        continue;


                    // add comment
                    service.getIssueClient().addComment(i.getCommentsUri(),Comment.valueOf(msg));

                    // resolve.
                    // comment set here doesn't work. see http://jira.atlassian.com/browse/JRA-11278
                    if (markedAsFixed && issues.size()==1) {
                        service.getIssueClient().transition(i,new TransitionInput(5)); /*this is apparently the ID for "resolved"*/
//                            service.progressWorkflowAction(securityToken,id,"5" ,
//                                new RemoteFieldValue[]{new RemoteFieldValue("comment",new String[]{"closing comment"})});
                    }
                }
            }
        }

        return 0;
    }

    /**
     * Returns true if the given commit is already mentioned in one of the comments.
     */
    private boolean isAlreadyCommented(Commit commit, String userName, Iterable<Comment> comments) {
        String msg = createUpdateMessage(commit);

        for (Comment comment : comments) {
            if (!comment.getAuthor().getName().equals(userName))
                continue;

            // TODO: do this for Subversion and CVS, although GitHub is the only place where
            // we can possibly get multiple notifications for the same commit
            // (when a ref moves and includes existing commits)
            if (commit instanceof GitHubCommit) {
                GitHubCommit ghc = (GitHubCommit) commit;
                if (comment.getBody().contains(ghc.commitSha1+"\nLog"))
                    return true;
            }
        }
        return false;
    }

    private String createUpdateMessage(Commit _commit) {
        StringBuilder buf = new StringBuilder();
        buf.append("Code changed in "+_commit.project+"\n");
        buf.append(MessageFormat.format("User: {0}\n",_commit.userName));
        buf.append("Path:\n");

        if (_commit instanceof GitHubCommit) {
            GitHubCommit commit = (GitHubCommit) _commit;

            for (CodeChange cc : commit.getCodeChanges()) {
                buf.append(MessageFormat.format(" {0}\n",cc.fileName));
            }
            buf.append("http://jenkins-ci.org/commit/").append(commit.repository).append('/').append(commit.commitSha1);
        } else {
            throw new AssertionError("Unrecognized commit type "+_commit.getClass());
        }

        buf.append("\n");
        buf.append("Log:\n");
        buf.append(_commit.log);

        return buf.toString();
    }

    /**
     * Marked for marking bug as fixed.
     */
    private static final Pattern FIXED = Pattern.compile("\\[.*(fixed|FIXED|FIX|FIXES).*\\]");

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final Set<String> PROJECTS = new HashSet<String>(Arrays.asList("jenkins","infra"));
}
