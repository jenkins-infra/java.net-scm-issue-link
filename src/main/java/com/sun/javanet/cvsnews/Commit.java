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

package com.sun.javanet.cvsnews;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A CVS commit.
 *
 * @author Kohsuke Kawaguchi
 */
public class Commit<CC extends CodeChange> {
    /**
     * The user who commmitted the change.
     */
    public final String userName;

    /**
     * Timestamp when the news item was created.
     */
    public final Date date;

    /**
     * Commit message
     */
    public final String log;

    /**
     * Source code changes associated with this.
     * Created on demand, and copy-on-write.
     */
    private volatile List<CC> codeChanges;

    /**
     * Java.net project in which this change was mde.
     */
    public final String project;

    public Commit(String project, String userName, Date date, String log) {
        this.userName = userName;
        this.project = project;
        this.date = date;

        this.log = log;
    }

    /**
     * Adds a new code change to the list.
     */
    public synchronized void addCodeChange(CC cc) {
        List<CC> r = new ArrayList<CC>();
        if(codeChanges!=null)
            r.addAll(codeChanges);
        r.add(cc);
        codeChanges = r;
    }

    /**
     * Adds new code changes to the list.
     */
    public synchronized void addCodeChanges(Collection<? extends CC> cc) {
        List<CC> r = new ArrayList<CC>();
        if(codeChanges!=null)
            r.addAll(codeChanges);
        r.addAll(cc);
        codeChanges = r;
    }

    /**
     * Gets a read-only view of all the code changes.
     */
    public List<CC> getCodeChanges() {
        List<CC> r = codeChanges;
        if(r==null) return Collections.emptyList();
        else        return r;
    }
}