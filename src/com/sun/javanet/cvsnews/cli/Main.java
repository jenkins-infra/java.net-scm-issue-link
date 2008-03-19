package com.sun.javanet.cvsnews.cli;

import java.util.List;
import java.util.Collections;
import java.util.Arrays;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.CmdLineException;

/**
 * Reads CVS changelog e-mail from stdin and writes a news file to the current directory.
 *
 * @author Kohsuke Kawaguchi
 */
public class Main {
    public static void main(String[] args) throws Exception {
        System.exit(run(args));
    }
    public static int run(String[] args) throws Exception {

        Command com;
        List<String> commandArgs;
        if(args.length==0) {
            com = new ParseCommand();   // compatibility
            commandArgs = Collections.emptyList();
        } else {
            try {
                Class c = Class.forName(capitalize(args[0])+"Command");
                com = (Command)c.newInstance();
            } catch (ClassNotFoundException e) {
                System.err.println("No such command: "+args[0]);
                return -1;
            }
            commandArgs = Arrays.asList(args).subList(1,args.length);
        }

        CmdLineParser p = new CmdLineParser(com);
        try {
            p.parseArgument(commandArgs.toArray(new String[0]));
            return com.execute();
        } catch (CmdLineException e) {
            p.printUsage(System.err);
            return -1;
        }
    }

    private static String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0))+text.substring(1);
    }
}
