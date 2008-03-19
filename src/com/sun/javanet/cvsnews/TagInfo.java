package com.sun.javanet.cvsnews;

import com.sun.javanet.cvsnews.util.XmlFile;
import com.thoughtworks.xstream.XStream;

import javax.swing.text.html.HTML.Tag;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;

/**
 * Persisted information about a tag.
 *
 * @author Kohsuke Kawaguchi
 */
public class TagInfo {
    /**
     * Description of this tag, if any.
     */
    private String description;
    /**
     * E-mail addresses that are subscribed to this tag.
     */
    private List<String> subscribers;

    private transient final XmlFile dataStore;

    public TagInfo(File home, String tag) {
        File tagFile = new File(new File(home, "tags"), tag);
        dataStore = new XmlFile(XSTREAM,tagFile);

        if(dataStore.exists()) {
            // load additional data from disk, if any
            try {
                dataStore.unmarshal(this);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Unable to read from "+dataStore, e);
            }
        }

        if(subscribers==null)
            subscribers = new ArrayList<String>();
    }

    public List<String> getSubscribers() {
        if(subscribers==null)
            return Collections.emptyList();
        else {
            ArrayList<String> r = new ArrayList<String>(subscribers);
            Collections.sort(r);
            return r;
        }
    }

    public void removeRecipient(String recipient) throws IOException {
        subscribers.remove(recipient);
        save();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) throws IOException {
        this.description = desc;
        save();
    }

    public void addSubscribers(List<String> names) throws IOException {
        subscribers.addAll(names);
        save();
    }

    public void removeSubscribers(List<String> names) throws IOException {
        subscribers.removeAll(names);
        save();
    }

    public void save() throws IOException {
        dataStore.mkdirs();
        dataStore.write(this);
    }


    private static final Logger LOGGER = Logger.getLogger(Tag.class.getName());

    private static final XStream XSTREAM = new XStream();

    static {
        XSTREAM.alias("tag", TagInfo.class);
    }
}
