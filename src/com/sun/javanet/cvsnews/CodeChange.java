package com.sun.javanet.cvsnews;

import java.net.URL;

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

    public CodeChange(String fileName, URL url, String revision) {
        this.fileName = fileName;
        this.url = url;
        this.revision = revision;
    }

    public String toString() {
        return fileName+':'+revision;
    }
}