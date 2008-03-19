package com.sun.javanet.cvsnews;

import com.sun.javanet.cvsnews.Highlighter.Markup;
import com.sun.javanet.cvsnews.util.NullStream;
import com.sun.javanet.cvsnews.util.XmlFile;
import com.thoughtworks.xstream.XStream;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * A note worthy CVS commit.
 *
 * @author Kohsuke Kawaguchi
 */
public final class NewsItem implements Comparable<NewsItem> {
    /**
     * The user who commmitted the change.
     */
    public final String userName;

    /**
     * Timestamp when the news item was created.
     */
    public final Date date;

    /**
     * One line change summary.
     */
    public final String subject;

    /**
     * A somewhat more detailed description.
     */
    public final String description;

    /**
     * For backward compatibility only. New news items won't get UUID.
     */
    public UUID uuid;

    /**
     * Unique hash code for this news item.
     *
     * This value can be uniquely computed from the changelog without talking
     * to the server, allowing collaboration between remote disjoint systems.
     */
    public String id;

    /**
     * Associated tags.
     *
     * Should be all lower case.
     */
    public final Set<String> tags = new TreeSet<String>();

    /**
     * Trackbacks to this entry. Created on demand, and copy-on-write.
     */
    private volatile List<Trackback> trackbacks;

    /**
     * Source code changes associated with this.
     * Created on demand, and copy-on-write. 
     */
    private volatile List<CodeChange> codeChanges;

    public NewsItem(String userName, Date date, String subject, String description) {
        this.userName = userName;
        this.date = date;

        subject = subject.trim();
        if(subject.endsWith("."))
            subject = subject.substring(0,subject.length()-1);
        this.subject = subject;
        this.description = description;
        this.id = id();
    }

    public static NewsItem load(File file) throws IOException {
        return (NewsItem)new XmlFile(XSTREAM,file).read();
    }

    /**
     * Saves this {@link NewsItem}.
     */
    public void save(File baseDir) throws IOException {
        File file = getDataFile(baseDir);
        file.getParentFile().mkdirs();

        new XmlFile(XSTREAM,file).write(this);
    }

    /**
     * Where do we store the data file?
     */
    public File getDataFile(File baseDir) {
        File file = new File(baseDir, id.substring(0,2)+'/'+ id.substring(2,4)+'/'+ id.substring(4)+".xml");
        return file;
    }

    public int compareTo(NewsItem that) {
        int r = this.date.compareTo(that.date);
        if(r!=0)     return -r;

        return this.subject.compareTo(that.subject);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final NewsItem that = (NewsItem) o;

        if (!date.equals(that.date)) return false;
        if (!subject.equals(that.subject)) return false;

        return true;
    }

    public int hashCode() {
        return date.hashCode();
    }

    /**
     * Gets the human readable string that represents how old this news is.
     */
    public String getDateString() {
        return getTimeSpanString(System.currentTimeMillis() - date.getTime());
    }

    /**
     * Returns the timestamp formatted in xs:dateTime.
     */
    public String getXSDateTimeString() {
        return XS_DATETIME_FORMATTER.format(date);
    }

    private static String getTimeSpanString(long duration) {
        duration /= 1000;
        if(duration<60)
            return combine(duration,"second");
        duration /= 60;
        if(duration<60)
            return combine(duration,"minute");
        duration /= 60;
        if(duration<24)
            return combine(duration,"hour");
        duration /= 24;
        if(duration<30)
            return combine(duration,"day");
        duration /= 30;
        if(duration<12)
            return combine(duration,"month");
        duration /= 12;
        return combine(duration,"year");
    }

    private static String combine(long n, String suffix) {
        String s = Long.toString(n)+' '+suffix;
        if(n!=1)
            s += 's';
        return s;
    }

    public MimeMessage createNotifcation() throws MessagingException {
        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
        msg.setSubject("[CVS-NEWS] "+ subject);
        for (String t : tags) {
            msg.addHeader("X-CVS-NEWS",t);
        }
        msg.addHeader("X-CVS-NEWS-ID", id);
        msg.setText("See http://cvs-news.sfbay/"+ id +"/\n\n"+ description);
        msg.setFrom(new InternetAddress("cvs-news@kohsuke.sfbay.sun.com")); // needs to be configurable
        return msg;
    }

    public String getHighlightedSubject() {
        List<Markup> markups = new ArrayList<Markup>();
        Highlighters.HIGHLIGHTER.highlightSubject(subject,markups);
        return Highlighter.annotate(subject,markups);
    }

    public String getHighlightedDescription() {
        List<Markup> markups = new ArrayList<Markup>();
        Highlighters.HIGHLIGHTER.highlightDescription(description,markups);
        return Highlighter.annotate(description,markups);
    }

    /**
     * Adds a new track back.
     *
     * Synchronization to serialize two concurrent invocations.
     * Readers don't need to synchronize as this is copy-on-write.
     */
    public synchronized void addTrackback(Trackback tb) {
        List<Trackback> r = new ArrayList<Trackback>();
        if(trackbacks!=null)
            r.addAll(trackbacks);
        r.add(tb);
        trackbacks = r;
    }

    public synchronized void addTrackbacks(Collection<Trackback> tbs) {
        List<Trackback> r = new ArrayList<Trackback>();
        if(trackbacks!=null)
            r.addAll(trackbacks);
        r.addAll(tbs);
        trackbacks = r;
    }

    /**
     * Gets a read-only view of all the trackbacks.
     */
    public List<Trackback> getTrackbacks() {
        List<Trackback> r = trackbacks;
        if(r==null) return Collections.emptyList();
        else        return r;
    }

    /**
     * Adds a new code change to the list.
     */
    public synchronized void addCodeChange(CodeChange cc) {
        List<CodeChange> r = new ArrayList<CodeChange>();
        if(codeChanges!=null)
            r.addAll(codeChanges);
        r.add(cc);
        codeChanges = r;
    }

    /**
     * Adds new code changes to the list.
     */
    public synchronized void addCodeChanges(Collection<? extends CodeChange> cc) {
        List<CodeChange> r = new ArrayList<CodeChange>();
        if(codeChanges!=null)
            r.addAll(codeChanges);
        r.addAll(cc);
        codeChanges = r;
    }

    /**
     * Gets a read-only view of all the code changes.
     */
    public List<CodeChange> getCodeChanges() {
        List<CodeChange> r = codeChanges;
        if(r==null) return Collections.emptyList();
        else        return r;
    }

    /**
     * Incorporates all the additional properties in another {@link NewsItem}
     * into this news item.
     */
    public void merge(NewsItem that) {
        // concurrency unsafe if this happens in multi-thread environment.
        // need COW
        this.tags.addAll(that.tags);
        this.addTrackbacks(that.getTrackbacks());
        this.addCodeChanges(that.getCodeChanges());
    }


    private Object readResolve() {
        if(id ==null)
            id = id(); // for backward compatibility
        return this;
    }

    /**
     * Computes the hash of this news item.
     */
    private String id() {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }

        try {
            md5.reset();    // technically not necessary, but hey, just to be safe
            Writer w = new OutputStreamWriter(new DigestOutputStream(new NullStream(), md5),"UTF-8");
            w.write(subject);
            w.write(description);
            w.close();
            // don't use date because CVS occasionally mess up with time zone differences

            byte[] digest = md5.digest();
            return toHexString(digest,0,digest.length);
        } catch (IOException e) {
            throw new Error(e); // impossible
        }
    }

    private static String toHexString(byte[] data, int start, int len) {
        StringBuffer buf = new StringBuffer();
        for( int i=0; i<len; i++ ) {
            int b = data[start+i]&0xFF;
            if(b<16)    buf.append('0');
            buf.append(Integer.toHexString(b));
        }
        return buf.toString();
    }

    protected static final SimpleDateFormat XS_DATETIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final XStream XSTREAM = new XStream();
    static {
        XSTREAM.alias("news",NewsItem.class);
        XSTREAM.alias("trackback",Trackback.class);
        XSTREAM.alias("codeChange",CodeChange.class);
    }
}
