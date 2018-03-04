package com.cloudbees.javanet.cvsnews.util;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.cloudbees.javanet.cvsnews.Commit;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Oleg Nenashev
 */
public class JiraIssueFilter {
    private Set<String> keywords;
    private Set<String> components;
    private Pattern pattern;

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
        this.pattern = keywords != null ? Pattern.compile("\\[.*(" + String.join("|", keywords) +").*\\]") : null;
    }

    @Nonnull
    public Set<String> getComponents() {
        return components != null ? components : Collections.emptySet();
    }

    public void setComponents(Set<String> components) {
        this.components = components;
    }

    @CheckForNull
    public Pattern getPattern() {
        return pattern;
    }

    public boolean matches(Issue issue, Commit commit) {
        if (!isMarkedAsFixed(commit.log)) {
            return false;
        }

        if (components == null) {
            // No components filter
            return true;
        }

        for (BasicComponent c : issue.getComponents()) {
            if (components.contains(c.getName())) {
                return true; // Component match
            }
        }
        return false;
    }

    public boolean isMarkedAsFixed(String log) {
        return pattern.matcher(log).find();
    }
}
