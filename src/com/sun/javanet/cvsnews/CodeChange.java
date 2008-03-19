package com.sun.javanet.cvsnews;

import java.net.URL;
import java.util.Date;

/**
 * Change of the source code.
 *
 * @author Kohsuke Kawaguchi
 */
public class CodeChange {
    /**
     * The file that was changed.
     *
     * <p>
     * This is a path-like '/' separated name, but the exact meaning
     * depends on the SCM in use.
     */
    public final String fileName;

    /**
     * Link target that represents this file or the diff.
     */
    public final URL url;

    /**
     * Revision of the new file.
     * <p>
     * Again the meaning really depends on SCM. Can be null.
     */
    public final String revision;
    
    /**
     * Time of the commit.
     */
    public final Date date;

    public CodeChange(String fileName, URL url, String revision, Date date) {
        this.fileName = fileName;
        this.url = url;
        this.revision = revision;
        // use a new time instance to make XML look nicer.
        // otherwise XStream will try to unify instances
        this.date = new Date(date.getTime());
    }
}
