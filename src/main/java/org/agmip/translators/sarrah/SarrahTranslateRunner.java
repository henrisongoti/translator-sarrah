package org.agmip.translators.sarrah;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The output runner for Multi-thread mode translation
 *
 * @author Meng Zhang
 */
public class SarrahTranslateRunner implements Callable<File> {

    private SarrahCommonOutput translator;
    private Map data;
    private String outputDirectory;
    private static Logger LOG = LoggerFactory.getLogger(SarrahTranslateRunner.class);

    public SarrahTranslateRunner(SarrahCommonOutput translator, Map data, String outputDirectory) {
        this.translator = translator;
        this.data = data;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public File call() throws Exception {
        LOG.debug("Starting new thread!");
        try {
            translator.writeFile(outputDirectory, data);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        
        File ret = translator.getOutputFile();
        
        if (ret == null) {
            LOG.debug("Job canceled!");
            throw new NoOutputFileException();
        } else {
            LOG.debug("Job done for " + ret.getName());
            return ret;
        }
    }
    
    class NoOutputFileException extends RuntimeException {
        
    }
}
