package com.sun.javanet.cvsnews.cli;

/**
 * A sub-command.
 *
 * <p>
 * args4j is used to fill options, then {@link #execute()} is invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Command {
    /**
     * @return
     *      exit code.
     */
    public int execute() throws Exception;
}
