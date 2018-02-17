Jenkins JIRA Issue linking tool
===

This tool parses CVS/Subversion/GitHub commit e-mails and optionally updates JIRA tickets.
The tool has been originally developed for the [Jenkins project](https://jenkins.io/)'s bugtracker.

## Usage

### Command-line

```sh
cat MESSAGE_TO_PARSE | java -jar parser.jar <command>
```

Supported commands:

* `Update` - Updates issues in the JIRA bugtracker. 
  * It will require JIRA credentials from the configuration file (see below)
* `DumpIssues` - Dry run, just list the discovered issues

### Configuration file

The tool uses the `.java.net.scm_issue_link` file from the 
current user's home directory for configuration.
This is a property file with the following fields:

* `jiraUrl` - URL of the JIRA server (optional).
Defaults to Jenkins bugtracker: http://issues.jenkins-ci.org/
* `userName` - JIRA User name
* `password` - JIRA Password

### Behaviour

The tool has the following ticket update logic:

* The tool will only match tickets for `JENKINS` and `INFRA` projects
* JIRA comments will be added to all referenced issues
* If the commit message contains one of the `fixed|FIXED|FIX|FIXES` keywords,
the ticket will be transferred to the _Resolved_ state (ID is `5`, hardcoded for now)
