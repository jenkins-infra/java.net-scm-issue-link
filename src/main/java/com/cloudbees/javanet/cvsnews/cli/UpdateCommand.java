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
import com.cloudbees.javanet.cvsnews.util.Config;
import com.cloudbees.javanet.cvsnews.util.JiraTransition;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Subcommand that reads e-mail from stdin and updates the issue tracker.
 *
 * @author Kohsuke Kawaguchi
 */
public class UpdateCommand extends AbstractIssueCommand {

    private static final Logger LOGGER = Logger.getLogger(UpdateCommand.class.getName());

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

            final Config config = Config.loadConfig();
            String msg = createUpdateMessage(commit, config);

            for (Issue issue : issues) {
                if (config.getJiraModel().getProjects().contains(issue.projectName)) {
                    System.out.println("Updating "+issue);
                    // update JIRA

                    final JiraRestClient service = config.connectClient();

                    String id = issue.projectName.toUpperCase() + "-" + issue.number;

                    // if an issue doesn't exist an exception will be thrown
                    com.atlassian.jira.rest.client.api.domain.Issue i = service.getIssueClient().getIssue(id).claim();

                    // is this commit already reported?
                    Iterable<Comment> comments = i.getComments();
                    if (isAlreadyCommented(commit, config, comments))
                        continue;

                    // add comment
                    service.getIssueClient().addComment(i.getCommentsUri(),Comment.valueOf(msg)).claim();

                    // Apply transitions
                    // comment set here doesn't work. see http://jira.atlassian.com/browse/JRA-11278
                    if (issues.size() == 1) {
                        // TODO: apply filters
                        for (JiraTransition transition : config.getJiraModel().getTransitions()) {
                            if (transition.getFilter().matches(i, commit)) {
                                LOGGER.log(Level.INFO, "Issue {0}: applying transition to {1}", new Object[] {id, transition});
                                service.getIssueClient().transition(i, new TransitionInput(transition.getId())).claim();
                            }
                        }
                    }
                }
            }
        }

        return 0;
    }

    /**
     * Returns true if the given commit is already mentioned in one of the comments.
     */
    private boolean isAlreadyCommented(Commit commit, Config config, Iterable<Comment> comments) {
        String msg = createUpdateMessage(commit, config);

        for (Comment comment : comments) {
            if (!comment.getAuthor().getName().equals(config.getUserName()))
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

    private String createUpdateMessage(Commit _commit, Config config) {
        StringBuilder buf = new StringBuilder();
        buf.append("Code changed in "+_commit.project+"\n");
        buf.append(MessageFormat.format("User: {0}\n",_commit.userName));
        buf.append("Path:\n");

        if (_commit instanceof GitHubCommit) {
            GitHubCommit commit = (GitHubCommit) _commit;

            for (CodeChange cc : commit.getCodeChanges()) {
                buf.append(MessageFormat.format(" {0}\n",cc.fileName));
            }
            buf.append(config.getJiraUrl() + "commit/").append(commit.repository).append('/').append(commit.commitSha1);
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
}
