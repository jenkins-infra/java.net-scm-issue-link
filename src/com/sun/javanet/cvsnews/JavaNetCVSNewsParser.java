package com.sun.javanet.cvsnews;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Parses {@link NewsItem} from java.net CVS changelog e-mail.
 *
 * @author Kohsuke Kawaguchi
 */
public class JavaNetCVSNewsParser extends NewsParser {
    public List<NewsItem> parse(MimeMessage msg) throws ParseException {

        List<NewsItem> list = new ArrayList<NewsItem>();
        List<CodeChange> codeChanges = new ArrayList<CodeChange>();

        try {
            Object content = msg.getContent();
            if(!(content instanceof String))
                throw new ParseException("Unrecognized content type "+content,-1);

            String project = getProjectName(msg);

            String branch = null;
            String user = null;
            Date date = null;
            boolean inLog = false;
            boolean inDescription = false;
            String title = null;
            StringWriter log = new StringWriter();
            String[] tags = null;

            boolean isEOF = false;

            String directory = null;    // set to the value of "Directory:" when parsing diffs
            String file = null;         // set to the value of "File [...]:" when parsing diffs
            String url = null;          // set to the value of "Url:" when parsing diffs

            BufferedReader in = new BufferedReader(new StringReader(content.toString()));
            do {
                String line=in.readLine();
                if(line==null) {
                    isEOF = true;
                    line = ""; // to finish off any pending news item
                }

                if(line.length()==0) {
                    inLog = false;
                    if(inDescription) {
                        inDescription = false;

                        // one news item is complete
                        NewsItem r = new NewsItem(user, date, title, log.toString());
                        r.tags.addAll(Arrays.asList(tags));
                        r.tags.remove("");

                        // auto tagging
                        r.tags.add(user);
                        if(branch!=null)
                            r.tags.add(branch);
                        r.tags.add(project);

                        list.add(r);
                    }
                }

                if(line.startsWith("Tag: ")) {
                    branch = line.substring(5).trim();
                    continue;
                }
                if(line.startsWith("User: ")) {
                    user = line.substring(6).trim();
                    continue;
                }
                if(line.startsWith("Date: ")) {
                    if(line.endsWith("+0000"))
                        line = line.substring(0,line.length()-5);
                    date = new Date(line.substring(6));
                    continue;
                }
                if(line.startsWith("Log:")) {
                    inLog = true;
                    continue;
                }
                if(inDescription) {
                    // this is a part of the log message
                    log.write(line.substring(1));   // cut off the first SP
                    log.write('\n');
                    continue;
                }
                if(inLog) {
                    if(TAGLINE.matcher(line).find()) {
                        int idx = line.indexOf(']');
                        title = line.substring(idx+1).trim();
                        tags = line.substring(7,idx).split(" ");
                        for( int i=0; i<tags.length; i++ )
                            tags[i] = tags[i].toLowerCase();

                        inDescription = true;
                    }
                } else {
                    Matcher m = DIRECTORY_LINE.matcher(line);
                    if(m.matches()) {
                        directory = m.group(1);
                        continue;
                    }

                    m = FILE_LINE.matcher(line);
                    if(m.matches()) {
                        // file always marks the start of new change
                        if(file!=null)
                            codeChanges.add(createCodeChange(directory,file,url,date));
                        file = m.group(2);
                        continue;
                    }

                    m = URL_LINE.matcher(line);
                    if(m.matches()) {
                        url = m.group(1);
                        continue;
                    }
                }
            } while(!isEOF);

            // wrap up the last change
            if(file!=null)
                codeChanges.add(createCodeChange(directory,file,url,date));

            // associate code changes to the news.
            for (NewsItem ni : list)
                ni.addCodeChanges(codeChanges);

            return list;
        } catch (IOException e) {
            // impossible
            throw new Error(e);
        } catch (MessagingException e) {
            // impossible
            throw new Error(e);
        }
    }

    private CodeChange createCodeChange(String directory, String file, String url, Date date) throws MalformedURLException {
        // compute the revision
        String rev = null;
        if(url!=null) {
            Matcher m = DIFF_REVISION.matcher(url);
            if(m.find()) {
                rev = m.group(2);
            } else {
                m = NEW_REVISION.matcher(url);
                if(m.find())
                    rev = m.group(1);
            }
        }

        return new CodeChange(directory+file,url==null?null:new URL(url),rev,date);
    }

    private String getProjectName(MimeMessage msg) throws MessagingException {
        InternetAddress ia = (InternetAddress) msg.getRecipients(RecipientType.TO)[0];
        String a = ia.getAddress();
        int idx = a.indexOf('@');
        int end = a.indexOf('.',idx);
        return a.substring(idx+1,end);
    }

    private static final Pattern DIRECTORY_LINE = Pattern.compile("^Directory: (/.+/)$");
    private static final Pattern FILE_LINE = Pattern.compile("^File \\[(changed|added|removed)\\]: (.+)$");
    private static final Pattern URL_LINE = Pattern.compile("^Url: (.+)$");

    private static final Pattern DIFF_REVISION = Pattern.compile("\\?r1=([0-9.]+)&r2=([0-9.]+)");
    private static final Pattern NEW_REVISION = Pattern.compile("\\?rev=([0-9.]+)&");
}

/* Sample changelog messages from java.net

File Changes:

Directory: /ws-test-harness/test-harness/test/testcases/jaxws/fromjava/server/
==============================================================================

File [removed]: AddWebservice.java

File [removed]: EndpointStopper.java


******************************************

Directory: /ws-test-harness/test-harness/bootstrap/src/com/sun/xml/ws/test/
===========================================================================

File [changed]: Bootstrap.java
Url: https://ws-test-harness.dev.java.net/source/browse/ws-test-harness/test-harness/bootstrap/src/com/sun/xml/ws/test/Bootstrap.java?r1=1.3&r2=1.4
Delta lines:  +11 -11


******************************************
Directory: /ws-test-harness/test-harness/src/com/sun/xml/ws/test/model/
=======================================================================

File [added]: WSDL.java
Url: https://ws-test-harness.dev.java.net/source/browse/ws-test-harness/test-harness/src/com/sun/xml/ws/test/model/WSDL.java?rev=1.1&content-type=text/vnd.viewcvs-markup
Added lines: 27
---------------
*/
