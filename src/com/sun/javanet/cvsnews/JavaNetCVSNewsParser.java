package com.sun.javanet.cvsnews;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
/**
 * Parses {@link Commit} from java.net CVS changelog e-mail.
 *
 * @author Kohsuke Kawaguchi
 */
public class JavaNetCVSNewsParser extends NewsParser {
    public Commit parse(MimeMessage msg) throws ParseException {
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
            StringWriter log = new StringWriter();

            String directory = null;    // set to the value of "Directory:" when parsing diffs
            String file = null;         // set to the value of "File [...]:" when parsing diffs
            String url = null;          // set to the value of "Url:" when parsing diffs

            BufferedReader in = new BufferedReader(new StringReader(content.toString()));
            String line;
            while((line=in.readLine())!=null) {
                if(line.length()==0) {
                    inLog = false;
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
                    date = DATE_FORMAT.parse(line.substring(6));
                    continue;
                }
                if(line.startsWith("Log:")) {
                    inLog = true;
                    continue;
                }

                if(inLog) {
                    // this is a part of the log message
                    log.write(line.substring(1));   // cut off the first SP
                    log.write('\n');
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
                            codeChanges.add(createCodeChange(directory,file,url));
                        file = m.group(2);
                        continue;
                    }

                    m = URL_LINE.matcher(line);
                    if(m.matches())
                        url = m.group(1);
                }
            }

            // wrap up the last change
            if(file!=null)
                codeChanges.add(createCodeChange(directory,file,url));

            Commit item = new Commit(project, user, branch, date, log.toString());
            item.addCodeChanges(codeChanges);

            return item;
        } catch (IOException e) {
            // impossible
            throw new Error(e);
        } catch (MessagingException e) {
            // impossible
            throw new Error(e);
        }
    }

    private CodeChange createCodeChange(String directory, String file, String url) throws MalformedURLException {
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

        return new CodeChange(directory+file,url==null?null:new URL(url),rev);
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

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
