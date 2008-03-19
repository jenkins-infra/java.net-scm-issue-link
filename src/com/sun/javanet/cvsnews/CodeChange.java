package com.sun.javanet.cvsnews;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * Obtains the CVS repository's exact timestamp for this change.
     */
    public Date determineTimstamp() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(
            "cvs",
            "-d:pserver:guest@cvs.dev.java.net:/cvs",
            "rlog",
            "-N",
            "-r",
            revision,
            fileName);
        pb.redirectErrorStream(true);

        Process proc = pb.start();
        proc.getOutputStream().close();
        BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line;
        Date result=null;
        StringWriter out = new StringWriter();
        while((line=r.readLine())!=null) {
            out.write(line);
            out.write('\n');
            if(result==null) {
                Matcher m = DATE_PATTERN.matcher(line);
                if(m.matches()) {
                    try {
                        result = DATE_FORMAT.parse(m.group(1));
                    } catch (ParseException e) {
                        throw new IOException("Failed to parse "+m.group(1));
                    }
                }
            }
        }

        // wait for the completion
        proc.waitFor();

        throw new IOException("cvs output:\n"+out);
    }

    private static final Pattern DATE_PATTERN = Pattern.compile("^date: (..../../.. ..:..:..);.+");

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
}