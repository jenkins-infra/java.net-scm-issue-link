package com.sun.javanet.cvsnews.cli;

import com.sun.javanet.cvsnews.NewsItem;
import com.sun.javanet.cvsnews.JavaNetCVSNewsParser;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.text.ParseException;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractCommand implements Command {
    /**
     * Parses stdin into {@link NewsItem}s.
     */
    protected final List<NewsItem> parseStdin() throws MessagingException, ParseException {
        MimeMessage msg = new MimeMessage(
            Session.getInstance(System.getProperties()), System.in);

        System.err.println("Subject: "+msg.getSubject());

        return new JavaNetCVSNewsParser().parse(msg);
    }
}
