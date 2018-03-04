package com.cloudbees.javanet.cvsnews.util;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jenkinsci.jira.JIRA;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
    private final JiraModel jiraModel;

    public Config(Properties props, JiraModel jiraModel) throws MalformedURLException {
        userName = props.getProperty("userName");
        password = props.getProperty("password");
        jiraUrl = new URL(props.getProperty("jiraUrl", "http://issues.jenkins-ci.org/"));
        this.jiraModel = jiraModel;
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

    public JiraModel getJiraModel() {
        return jiraModel;
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

        // TODO: add support external rules
        final JiraModel rules;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try(InputStream istream = JiraModel.class.getResourceAsStream("jenkinsJiraModel.yml")) {
            if (istream == null) {
                throw new FileNotFoundException("Cannot find resource: jenkinsJiraModel.yml");
            }
            rules = mapper.readValue(istream, JiraModel.class);
        }

        return new Config(props, rules);
    }
}
