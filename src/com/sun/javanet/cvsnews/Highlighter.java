package com.sun.javanet.cvsnews;

import java.util.Collection;
import java.util.List;
import java.util.Collections;

/**
 * Highlights text.
 * @author Kohsuke Kawaguchi
 */
public abstract class Highlighter {
    public abstract void highlightSubject(String subject, Collection<Markup> result);
    public abstract void highlightDescription(String description, Collection<Markup> result);

    public static String annotate(String s, List<Markup> result) {
        Collections.sort(result);
        int j=0; // next Markup to be written

        StringBuilder buf = new StringBuilder();
        for( int i=0; i<s.length(); i++ ) {
            while(j<result.size() && result.get(j).pos==i) {
                buf.append(result.get(j++).text);
            }

            char ch = s.charAt(i);
            switch(ch) {
            case '<':
                buf.append("&lt;");
                break;
            case '&':
                buf.append("&amp;");
                break;
            case '\t':
                // \t is most likely used as indentation, so better preserve it.
                buf.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                break;
            case '\n':
                buf.append("<br>");
                break;
            default:
                buf.append(ch);
                break;
            }
        }
        return buf.toString();
    }

    /**
     * Represents inserted mark up.
     */
    public class Markup implements Comparable<Markup> {
        public final int pos;
        public final String text;

        public Markup(int pos, String text) {
            this.pos = pos;
            this.text = text;
        }

        public int compareTo(Markup that) {
            return this.pos-that.pos;
        }
    }
}
