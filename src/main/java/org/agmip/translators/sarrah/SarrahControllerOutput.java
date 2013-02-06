package org.agmip.translators.sarrah;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

    private ArrayList<File> files;
    private ArrayList<Future<File>> futFiles;
    private ArrayList<Class<? extends SarrahCommonOutput>> fileTypes;
    private ExecutorService executor;
    private static Logger LOG = LoggerFactory.getLogger(SarrahControllerOutput.class);

    /**
     * Output SarraH model input files
     *
     * @param path The output path
     * @param map The Ace data set
     * @throws IOException
     */
    @Override
    public void writeFile(String path, Map map) throws IOException {

        // Initialization
        files = new ArrayList();
        futFiles = new ArrayList();
        loadAutoClasses();
        executor = Executors.newFixedThreadPool(64);
        SarrahCommonOutput outputTran;

        // Set data to translator runner
        for (int i = 0; i < fileTypes.size(); i++) {
            try {
                outputTran = fileTypes.get(i).getConstructor().newInstance();
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

    /**
     * Get all output files
     *
     * @return The list of output files
     */
    public ArrayList<File> getOutputFiles() {
        return files;
    }

    /**
     * Get all output files
     *
     * @return The array of output files
     */
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

    /**
     * Load default output translator classes
     */
    private void loadDefClasses() {
        fileTypes = new ArrayList();
        fileTypes.add(SarrahMeteorologieOutput.class);
        fileTypes.add(SarrahPersonalDataOutput.class);
        fileTypes.add(SarrahPluviometrieOutput.class);
    }

    /**
     * Scan the package and Load all output translator classes
     */
    private void loadAutoClasses() {

        String pk = this.getClass().getPackage().getName();
        String path = pk.replace('.', '/');
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource(path);
        File dir = new File(url.getFile().replaceAll("test-classes", "classes"));

        if (!dir.exists()) {
            return;
        }

        fileTypes = new ArrayList();
        for (File f : dir.listFiles()) {
            if (!f.isDirectory()) {
                String name = f.getName();
                if (name.endsWith(".class")) {
                    try {
                        Class c = Class.forName(pk + "." + name.substring(0, name.length() - 6));
                        if (c.getSuperclass().equals(SarrahCommonOutput.class)) {
                            fileTypes.add(c);
                        }
                    } catch (ClassNotFoundException e) {
                    }
                }
            }
        }

        if (fileTypes.isEmpty()) {
            loadDefClasses();
        }
    }
}
