/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 */

package com.cloudbees.javanet.cvsnews;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class NewsParser {
    /**
     * Parses a changelog e-mail.
     */
    public abstract List<? extends Commit> parse(MimeMessage msg) throws ParseException;

    protected final String getProjectName(MimeMessage msg) throws MessagingException {
        InternetAddress ia = (InternetAddress) msg.getRecipients(Message.RecipientType.TO)[0];
        String a = ia.getAddress();
        int idx = a.indexOf('@');
        int end = a.indexOf('.',idx);
        return a.substring(idx+1,end);
    }

    protected final Date parseDateLine(String line) throws ParseException {
        Date date;
        if(line.endsWith("+0000"))
                        line = line.substring(0,line.length()-5);
        date = DATE_FORMAT.parse(line.substring(6));
        return date;
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    protected static final String TAG = "[a-zA-Z0-9_\\-.]+";
    protected static final Pattern TAGLINE = Pattern.compile("^ \\[NEWS: *"+TAG+"( +"+TAG+")*\\]");
    protected static final Pattern URL_LINE = Pattern.compile("^Url: (.+)$");
}