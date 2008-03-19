package com.sun.javanet.cvsnews;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Highlighter} implementations.
 * @author Kohsuke Kawaguchi
 */
public class Highlighters {

    public static final Highlighter HIGHLIGHTER = new Aggregator(
        new Regexp(Pattern.compile("[^0-9]([0-9]{7})[^0-9]"),1,"<a href='http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=$1'>","</a>")
    );

    /**
     * Combines multiple {@link Highlighter} into a single one.
     */
    public static final class Aggregator extends Highlighter {
        private final Highlighter[] highlighters;

        public Aggregator(Highlighter... highlighters) {
            this.highlighters = highlighters;
        }

        public void highlightSubject(String subject, Collection<Markup> result) {
            for (Highlighter h : highlighters)
                h.highlightSubject(subject,result);
        }

        public void highlightDescription(String description, Collection<Markup> result) {
            for (Highlighter h : highlighters)
                h.highlightDescription(description,result);
        }
    }

    /**
     * Highlights text according to regexp.
     */
    public static final class Regexp extends Highlighter {
        private final Pattern pattern;
        private final int groupNumber;
        private final String startTag;
        private final String endTag;

        public Regexp(Pattern pattern, int groupNumber, String startTag, String endTag) {
            this.pattern = pattern;
            this.groupNumber = groupNumber;
            this.startTag = startTag;
            this.endTag = endTag;
        }

        public void highlightSubject(String subject, Collection<Markup> result) {
            highlightDescription(subject,result);
        }

        public void highlightDescription(String description, Collection<Markup> result) {
            Matcher m = pattern.matcher(description);
            while(m.find()) {
                result.add(new Markup(m.start(groupNumber),computeReplacement(m,startTag)));
                result.add(new Markup(m.end(groupNumber),  computeReplacement(m,endTag)));
            }
        }

        /**
         * Computes the actual replacement text by applying group capture.
         */
        private String computeReplacement(Matcher m, String replacement) {
            StringBuilder result = new StringBuilder();
            int cursor=0;
            while (cursor < replacement.length()) {
                char nextChar = replacement.charAt(cursor);
                if (nextChar == '\\') {
                    cursor++;
                    nextChar = replacement.charAt(cursor);
                    result.append(nextChar);
                    cursor++;
                } else if (nextChar == '$') {
                    // Skip past $
                    cursor++;

                    // The first number is always a group
                    int refNum = (int)replacement.charAt(cursor) - '0';
                    if ((refNum < 0)||(refNum > 9))
                        throw new IllegalArgumentException(
                            "Illegal group reference");
                    cursor++;

                    // Capture the largest legal group string
                    boolean done = false;
                    while (!done) {
                        if (cursor >= replacement.length()) {
                            break;
                        }
                        int nextDigit = replacement.charAt(cursor) - '0';
                        if ((nextDigit < 0)||(nextDigit > 9)) { // not a number
                            break;
                        }
                        int newRefNum = (refNum * 10) + nextDigit;
                        if (m.groupCount() < newRefNum) {
                            done = true;
                        } else {
                            refNum = newRefNum;
                            cursor++;
                        }
                    }

                    // Append group
                    if (m.group(refNum) != null)
                        result.append(m.group(refNum));
                } else {
                    result.append(nextChar);
                    cursor++;
                }
            }
            return result.toString();
        }
    }
}
