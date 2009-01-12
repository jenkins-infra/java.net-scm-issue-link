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
            subscribe(mls.get("commits"));
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
