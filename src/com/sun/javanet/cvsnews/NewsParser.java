package com.sun.javanet.cvsnews;

import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class NewsParser {
    /**
     * Parses a news item from the changelog e-mail.
     *
     * @return
     *      empty if this e-mail didn't have any notable change.
     */
    public abstract List<NewsItem> parse(MimeMessage msg) throws ParseException;


    /**
     * Parses a commit message as a string, instead of parsing changelog e-mail.
     *
     * @return
     *      empty if this e-mail didn't have any notable change.
     *      Otherwise return {@link NewsItem}s, but note that those news items
     *      are not fully populated (for example date and user are impossible to find.)
     *      tag sets are also not complete. But they can be used to
     *      obtain the permalink ID {@link NewsItem#id}. 
     */
    public static List<NewsItem> parse(String text) throws ParseException {
        List<NewsItem> list = new ArrayList<NewsItem>();

        try {
            boolean inDescription = false;
            String title = null;
            StringWriter log = new StringWriter();
            String[] tags = null;

            boolean isEOF = false;

            BufferedReader in = new BufferedReader(new StringReader(text));
            do {
                String line=in.readLine();
                if(line==null) {
                    isEOF = true;
                    line = ""; // to finish off any pending news item
                }

                if(inDescription) {
                    // this is a part of the log message
                    log.write(line);   // cut off the first SP
                    log.write('\n');
                    continue;
                }
                if(TAGLINE.matcher(line).find()) {
                    int idx = line.indexOf(']');
                    title = line.substring(idx).trim();
                    tags = line.substring(7,idx).split(" ");
                    for( int i=0; i<tags.length; i++ )
                        tags[i] = tags[i].toLowerCase();

                    inDescription = true;
                }
            } while(!isEOF);

            if(inDescription) {
                // news item is complete
                NewsItem r = new NewsItem(null, null, title, log.toString());
                r.tags.addAll(Arrays.asList(tags));
                r.tags.remove("");

                list.add(r);
            }

            return list;
        } catch (IOException e) {
            // impossible
            throw new Error(e);
        }
    }

    protected static final String TAG = "[a-zA-Z0-9_\\-.]+";
    protected static final Pattern TAGLINE = Pattern.compile("^ \\[NEWS: *"+TAG+"( +"+TAG+")*\\]");
}
