package com.sun.javanet.cvsnews2;

import javax.mail.internet.MimeMessage;
import java.text.ParseException;
import java.util.regex.Pattern;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class NewsParser {
    /**
     * Parses a changelog e-mail.
     */
    public abstract NewsItem parse(MimeMessage msg) throws ParseException;

    protected static final String TAG = "[a-zA-Z0-9_\\-.]+";
    protected static final Pattern TAGLINE = Pattern.compile("^ \\[NEWS: *"+TAG+"( +"+TAG+")*\\]");
}