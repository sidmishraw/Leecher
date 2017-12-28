/**
 * Project: Leecher
 * Package: leecher
 * File: LeecherDriver.java
 * 
 * @author sidmishraw
 *         Last modified: Dec 25, 2017 7:30:39 PM
 */
package leecher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h2>Leecher</h2>
 * 
 * <p>
 * Leecher is an application that will post a dummy commit to a GitHub repository every X minutes.
 * </p>
 * 
 * <p>
 * The way to start Leecher is
 * <code>
 *      leecher start leeching x
 * </code>
 * </p>
 * 
 * 
 * @author sidmishraw
 *
 *         Qualified Name: leecher.LeecherDriver
 *
 */
public class LeecherDriver {
    
    /**
     * The logger using SLF4J
     */
    private static final Logger logger             = LoggerFactory.getLogger(LeecherDriver.class);
    
    /**
     * The file with configurations for Leecher
     */
    private static final File   PROPS_FILE         = Paths.get("conf.props").toFile();
    /**
     * The random file that Leecher writes and commits
     */
    private static final File   RANDOMFILE         = Paths.get("theRandomFile.log").toFile();
    
    /**
     * The GitHub URL for leecher -- default
     */
    private static String       LEECHER_GITHUB_URL = "https://github.com/sidmishraw/leechers-log.git";
    
    /**
     * @param args
     *            the first element of the args is the minutes gap
     */
    public static void main(String[] args) {
        int minutes = Integer.parseInt(args[0]); // number of minutes to wait for
        logger.info(String.format("Leecher will leech every %s mins!", minutes));
        setup();
        spawnLeecher(minutes); // execute the leecher
    }
    
    /**
     * Reads the configuration from the <code>conf.props</code> file and sets up Leecher.
     */
    private static void setup() {
        try {
            Properties props = new Properties();
            props.load(new InputStreamReader(new FileInputStream(PROPS_FILE)));
            LEECHER_GITHUB_URL = props.getProperty(LEECHER_GITHUB_URL, LEECHER_GITHUB_URL); // get the GitHub repo from
                                                                                            // the props file if
                                                                                            // possible
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    /**
     * <p>
     * Spawns the Leecher that operates every X minutes
     * </p>
     * 
     * @param minutes
     *            the X minutes the leecher needs to operate after
     */
    private static void spawnLeecher(int minutes) {
        try {
            while (true) {
                addARandomEntryToFile(RANDOMFILE);
                commitTheFile(RANDOMFILE);
                Thread.sleep(minutes * 60 * 1000); // wait for X minutes
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    /**
     * <p>
     * Adds a random content into the file.
     * </p>
     * 
     * @param randomContentsFile
     *            the file with random contents
     */
    private static void addARandomEntryToFile(File randomContentsFile) {
        try (BufferedWriter bfos = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(randomContentsFile, true)))) {
            bfos.write(String.format("Leecher logs: This is an random entry by Leecher --- %s -- %d \n",
                    new Date().toString(), new Date().getTime()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    /**
     * <p>
     * Commits the file with random contents.
     * </p>
     * 
     * @param randomContentsFile
     *            the file with random contents
     */
    private static void commitTheFile(File randomContentsFile) {
        try {
            logger.info("Starting commit phase!");
            Process gitProcess = Runtime.getRuntime().exec(String.format("git add -A")); // git add the files for
                                                                                         // staging
            gitProcess.waitFor();
            if (gitProcess.exitValue() != 0) {
                // the process failed, probably because there was no git repository made
                logger.info("git repo doesn't exist yet, making git repo...");
                Runtime.getRuntime().exec("git init").waitFor(); // init git repo
                logger.info("git init");
                Runtime.getRuntime().exec(String.format("git remote add bulbasaur %s", LEECHER_GITHUB_URL)).waitFor(); // add
                                                                                                                       // remote
                                                                                                                       // repo
                logger.info(String.format("git remote add bulbasaur %s", LEECHER_GITHUB_URL));
                Runtime.getRuntime().exec(String.format("git add -A")).waitFor(); // re-add all the files to the git
                                                                                  // staging
                logger.info(String.format("git add -A"));
            }
            Process commit = Runtime.getRuntime().exec(
                    new String[] { "git", "commit", "-m", String.format("\"logging at %d\"", new Date().getTime()) }); // commit
                                                                                                                       // the
                                                                                                                       // files
            commit.waitFor();
            if (commit.exitValue() != 0) {
                // failed to commit
                String errString = readErrorStream(commit.getErrorStream());
                logger.error("EXACT ERROR :: " + errString);
                System.exit(1);
            }
            logger.info(String.format("git commit -m \"logging at %d\"", new Date().getTime()));
            Runtime.getRuntime().exec(String.format("git push bulbasaur master")).waitFor(); // push the commit to
                                                                                             // GitHub
            logger.info(String.format("git push bulbasaur master"));
            logger.info("Finished commit phase!");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    /**
     * Reads the contents of the error stream and returns the string contents
     * 
     * @param errStream
     *            the error stream to read from
     * @return the string contents of the error stream
     */
    private static String readErrorStream(InputStream errStream) {
        StringBuffer errBuffer = new StringBuffer();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(errStream))) {
            String line = null;
            while (null != (line = br.readLine())) {
                errBuffer.append(line);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return errBuffer.toString();
    }
}
