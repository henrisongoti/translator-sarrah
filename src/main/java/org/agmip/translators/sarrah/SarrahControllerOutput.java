package org.agmip.translators.sarrah;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.agmip.core.types.TranslatorOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class for output all type of file at one time, will use mutil-thread mode
 * to load all Sarrah output translator to do the job
 *
 * @author Meng Zhang
 */
public class SarrahControllerOutput implements TranslatorOutput {

    ArrayList<File> files;
    private ArrayList<Future<File>> futFiles;
    Class<? extends SarrahCommonOutput>[] fileTypes;
    private ExecutorService executor;
    private static Logger LOG = LoggerFactory.getLogger(SarrahControllerOutput.class);

    @Override
    public void writeFile(String path, Map map) throws IOException {
        
        // Initialization
        files = new ArrayList();
        futFiles = new ArrayList();
        fileTypes = new Class[]{
            SarrahMeteorologieOutput.class,
            SarrahPersonalDataOutput.class,
            SarrahPluviometrieOutput.class
        };
        executor = Executors.newFixedThreadPool(64);
        SarrahCommonOutput outputTran;

        // Set data to translator runner
        for (int i = 0; i < fileTypes.length; i++) {
            try {
                outputTran = fileTypes[i].getConstructor().newInstance();
                futFiles.add(executor.submit(new SarrahTranslateRunner(outputTran, map, path)));
            } catch (Exception ex) {
                continue;
            }

        }
        
        // Get output result files into output array for zip package
        for (int i = 0; i < futFiles.size(); i++) {
            try {
                File f = futFiles.get(i).get();
                if (f != null) {
                    files.add(f);
                }
            } catch (InterruptedException ex) {
                LOG.error(ex.getMessage());
            } catch (ExecutionException ex) {
                if (!ex.getMessage().contains("NoOutputFileException")) {
                    LOG.error(ex.getMessage());
                }
            }
        }

        // Release executor
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        executor = null;
    }
    
    public ArrayList<File> getOutputFiles() {
        return files;
    }
    
    public File[] getOutputFileArr() {
        
        if (files == null) {
            return new File[0];
        }
        File[] ret = new File[files.size()];
        for (int i = 0; i < files.size(); i++) {
            ret[i] = files.get(i);
        }
        return ret;
    }
}
