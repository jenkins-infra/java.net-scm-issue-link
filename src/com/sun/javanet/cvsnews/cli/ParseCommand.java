package com.sun.javanet.cvsnews.cli;

import com.sun.javanet.cvsnews.JavaNetCVSNewsParser;
import com.sun.javanet.cvsnews.NewsItem;
import com.sun.javanet.cvsnews.TagInfo;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import java.text.ParseException;

/**
 * Subcommand that reads e-mail from stdin and adds news files.
 *
 * @author Kohsuke Kawaguchi
 */
public class ParseCommand extends AbstractCommand implements Command {

    public int execute() throws Exception {
        List<NewsItem> newsItems = parseStdin();
        if(newsItems.isEmpty()) {
            System.err.println("No news in this e-mail");
            return 0;
        }

        File base = new File("news");

        for (NewsItem item : newsItems) {
            File existing = item.getDataFile(base);
            if(existing.exists()) {
                // is this an addition to an existing change?
                NewsItem old = NewsItem.load(existing);
                old.merge(item);
                item = old;
            }

            System.out.println(item.id);
            item.save(base);

            {
                System.err.println("Sending out a notification");
                MimeMessage email = item.createNotifcation();
                boolean hasReceiver=false;
                for( String tag : item.tags) {
                    // send out e-mails to subscribers
                    TagInfo ti = new TagInfo(new File("."),tag);
                    for( String recipient : ti.getSubscribers() ) {
                        try {
                            email.addRecipient(RecipientType.BCC,new InternetAddress(recipient));
                            hasReceiver = true;
                        } catch (MessagingException e) {
                            // invalid e-mail address
                            ti.removeRecipient(recipient);
                        }
                    }
                }
                if(hasReceiver)
                    Transport.send(email);
                else
                    System.err.println("No subscriber");
            }
        }

        return 0;
    }

}
