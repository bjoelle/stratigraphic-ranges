package test.integration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.io.File;

//import junit.framework.TestCase;
import org.junit.Test;

import org.junit.Assert;

import beast.base.inference.Logger;
import beast.base.inference.MCMC;
import beast.base.parser.XMLParser;
import beast.base.util.Randomizer;

/**
 * Check that srange's Beast xml file examples run and produce the expected result *
 */
public class CheckThatXmlExamplesRun_Test  {

    final static String INPUT_XML_FILE = "morph+dna_co_ncc.xml";

    final static String OUTPUT_DIR = "temp/test-output";
    final static String EXPECTED_OUTPUT_DIR = "resources/expected-test-output";

    final static String OUTPUT_TREES_FILE = "morph+dna_co_ncc.trees";
    final static String OUTPUT_LOG_FILE = "morph+dna_co.log";

    String input_dir;

    {
        // make sure output goes to test directory
        File testDir =  new File(OUTPUT_DIR);
        if (!testDir.exists()) {
            testDir.mkdir();
        }
        System.setProperty("file.name.prefix", OUTPUT_DIR+"/");

        input_dir = System.getProperty("user.dir") + "/examples";
    }

    private String getOutputFile(String filename) {

    	String outputDir = System.getProperty("user.dir") + "/" + OUTPUT_DIR;
    	String outputFile = outputDir + "/" + filename;
    	
    	return outputFile;
    }

    private String getExpectedOutputFile(String filename) {

    	String expectedOutputDir = (System.getProperty("user.dir") + "/" + EXPECTED_OUTPUT_DIR);
    	String expectedOutputFile = (expectedOutputDir + "/" + filename);
    	
    	return expectedOutputFile;
    }
    
    private void checkResult(String filename) throws Exception {
    	Path p1 = Paths.get(getOutputFile(filename));
    	Path p2 = Paths.get(getExpectedOutputFile(filename));

        long mismatch = Files.mismatch(p1, p2); // being same content, it should return -1.
        Assert.assertEquals(filename+" file differ!", -1, mismatch);
    }

    @Test
    public void runExample() throws Exception {
        Randomizer.setSeed(127);
        Logger.FILE_MODE = Logger.LogFileMode.overwrite;

        String fileName = input_dir + "/" + INPUT_XML_FILE;

        XMLParser parser = new XMLParser();
        beast.base.inference.Runnable runable = parser.parseFile(new File(fileName));
        if (runable instanceof MCMC) {
            MCMC mcmc = (MCMC) runable;
            mcmc.setInputValue("preBurnin", 0);
            mcmc.setInputValue("chainLength", 1000l);
            mcmc.run();
        }

        // Check if run result match expected result
        checkResult(OUTPUT_TREES_FILE);
        checkResult(OUTPUT_LOG_FILE);
    }
}
