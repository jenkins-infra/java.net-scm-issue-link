package com.sun.javanet.cvsnews;

/**
 * Represents a track back.
 *
 * See http://www.sixapart.com/pronet/docs/trackback_spec for
 * meaning of fields.
 *
 * @author Kohsuke Kawaguchi
 */
public class Trackback {
    /**
     * Blog where the source belongs.
     * In practice, this can be used as a top-most title.
     */
    public String blogName;

    /**
     * The title of the track back.
     */
    public String title;

    /**
     * URL of the link source. Mandatory.
     */
    public String url;

    /**
     * More lengthy text about what this track back is about.
     */
    public String excerpt;

    public Trackback() {
    }

    public Trackback(String blogName, String title, String url, String excerpt) {
        this.blogName = blogName;
        this.title = title;
        this.url = url;
        this.excerpt = excerpt;
    }

    /**
     * Gets the title that falls back to URL.
     */
    public String getDisplayTitle() {
        if(title!=null) return title;
        else            return url;
    }
}
