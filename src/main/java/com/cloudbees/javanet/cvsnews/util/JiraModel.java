package com.cloudbees.javanet.cvsnews.util;

import java.util.HashSet;
import java.util.List;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
public class JiraModel {
    private HashSet<String> projects;
    private List<JiraTransition> transitions;

    public HashSet<String> getProjects() {
        return projects;
    }

    public void setProjects(HashSet<String> projects) {
        this.projects = projects;
    }

    public List<JiraTransition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<JiraTransition> transitions) {
        this.transitions = transitions;
    }
}
