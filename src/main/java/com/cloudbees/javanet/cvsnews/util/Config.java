package com.cloudbees.javanet.cvsnews.util;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import org.jenkinsci.jira.JIRA;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Manages credentials used by the library.
 * @author Oleg Nenashev
 */
public class Config {

    public static final String CONFIG_FILE_NAME = ".java.net.scm_issue_link";

    private final URL jiraUrl;
    private final String userName;
    private final String password;

    public Config(Properties props) throws MalformedURLException {
        userName = props.getProperty("userName");
        password = props.getProperty("password");
        jiraUrl = new URL(props.getProperty("jiraUrl", "http://issues.jenkins-ci.org/"));
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public URL getJiraUrl() {
        return jiraUrl;
    }

    /**
     * Connects client using the settings
     * @return Connected JIRA client
     * @throws IOException Connection failed
     */
    public JiraRestClient connectClient() throws IOException {
        return JIRA.connect(jiraUrl, userName, password);
    }

    @Nonnull
    public static File getDefaultCredentialsFile() {
        File homeDir = new File(System.getProperty("user.home"));
        return new File(homeDir, CONFIG_FILE_NAME);
    }

    public static Config loadConfig() throws IOException {
        final Properties props = new Properties();
        final File config = getDefaultCredentialsFile();
        if (config.exists()) {
            try (FileReader r = new FileReader(config)) {
                props.load(r);
            }
        }
        return new Config(props);
    }
}
