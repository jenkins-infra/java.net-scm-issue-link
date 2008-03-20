package com.sun.javanet.cvsnews.cli;

import com.sun.javanet.cvsnews.Commit;
import com.sun.javanet.cvsnews.CVSParser;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.text.ParseException;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractCommand implements Command {
    /**
     * Parses stdin into {@link Commit}.
     */
    protected final Commit parseStdin() throws MessagingException, ParseException {
        MimeMessage msg = new MimeMessage(
            Session.getInstance(System.getProperties()), System.in);

        System.err.println("Subject: "+msg.getSubject());

        return new CVSParser().parse(msg);
    }

    protected static final File HOME = new File(System.getProperty("user.home"));
}
