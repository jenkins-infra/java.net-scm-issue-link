package com.cloudbees.javanet.cvsnews.util;

/**
 * @author Oleg Nenashev
 */
public class JiraTransition {

    private int id;
    private String name;
    private JiraIssueFilter filter;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JiraIssueFilter getFilter() {
        return filter;
    }

    public void setFilter(JiraIssueFilter filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", id, name);
    }
}
