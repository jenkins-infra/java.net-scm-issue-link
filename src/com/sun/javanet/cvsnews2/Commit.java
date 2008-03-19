package com.sun.javanet.cvsnews2;

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
public final class Commit {
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
    private volatile List<CodeChange> codeChanges;

    /**
     * Java.net project in which this change was mde.
     */
    public final String project;

    /**
     * Branch
     */
    public final String branch;

    public Commit(String project, String userName, String branch, Date date, String log) {
        this.userName = userName;
        this.project = project;
        this.branch = branch;
        this.date = date;

        this.log = log;
    }

    /**
     * Adds a new code change to the list.
     */
    public synchronized void addCodeChange(CodeChange cc) {
        List<CodeChange> r = new ArrayList<CodeChange>();
        if(codeChanges!=null)
            r.addAll(codeChanges);
        r.add(cc);
        codeChanges = r;
    }

    /**
     * Adds new code changes to the list.
     */
    public synchronized void addCodeChanges(Collection<? extends CodeChange> cc) {
        List<CodeChange> r = new ArrayList<CodeChange>();
        if(codeChanges!=null)
            r.addAll(codeChanges);
        r.addAll(cc);
        codeChanges = r;
    }

    /**
     * Gets a read-only view of all the code changes.
     */
    public List<CodeChange> getCodeChanges() {
        List<CodeChange> r = codeChanges;
        if(r==null) return Collections.emptyList();
        else        return r;
    }
}