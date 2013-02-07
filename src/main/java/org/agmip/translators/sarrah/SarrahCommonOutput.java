package org.agmip.translators.sarrah;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.agmip.core.types.TranslatorOutput;
import static org.agmip.util.MapUtil.*;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * Sarrah Experiment Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public abstract class SarrahCommonOutput implements TranslatorOutput {

    // Default value for each type of value (R: real number; C: String; I: Integer; D: Date)
    protected String defValR = "0.00";
    protected String defValC = "";
    protected String defValI = "0";
    protected String defValD = "-99";
    protected String defValBlank = "";
    // construct the error message in the output
    protected StringBuilder sbError;
    protected File outputFile;
    protected VelocityContext context;
    protected Template template;
    protected String fileName;

    /**
     * Output SarraH model input file
     *
     * @param path The output path
     * @param map The Ace data set
     * @throws IOException
     */
    @Override
    public void writeFile(String path, Map map) throws IOException {
        setDefVal();
        initVelocity();
        writeTemplate(map);
        outputFile(path);
    }

    /**
     * Fill data into the template of SarraH model input file
     *
     * @param map The Ace data set
     * @throws IOException
     */
    protected abstract void writeTemplate(Map map) throws IOException;

    /**
     * initialize the template object
     */
    private void initVelocity() {
        Velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, "src\\main\\resources\\templates");
        Velocity.init();
        context = new VelocityContext();
    }

    /**
     * Output the content into file on hard drive by given path
     *
     * @param path The output path
     */
    private void outputFile(String path) throws IOException {
        if (template != null) {
            outputFile = new File(revisePath(path) + fileName);
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            template.merge(context, bw);
            bw.flush();
            bw.close();
        } else {
            System.out.println("Template is invalid!");
        }
    }

    /**
     * Load the template by given template name. Must be called before write
     * content to the template
     *
     * @param tempName The tempalte name without extention
     * @return True for load successfully; Flase for any exception happened
     */
    protected boolean loadTemplate(String tempName) {
        try {
            template = Velocity.getTemplate(tempName + ".txt");
            this.fileName = tempName + "_AgMIP.txt";
            return true;
        } catch (ResourceNotFoundException rnfe) {
            // couldn't find the template
        } catch (ParseErrorException pee) {
            // syntax error: problem parsing the template
        } catch (MethodInvocationException mie) {
            // something invoked in the template
            // threw an exception
        } catch (Exception e) {
        }

        return false;
    }

    /**
     * Format the number with maximum length and type
     *
     * @param bits Maximum length of the output string
     * @param m the experiment data holder
     * @param key the key of field in the map
     * @param defVal the default return value when error happens
     * @return formated string of number
     */
    protected String formatNumStr(int bits, HashMap m, Object key, String defVal) {

        String ret = "";
        String str = getObjectOr(m, key, defVal);
        double decimalPower;
        long decimalPart;
        double input;
        String[] inputStr = str.split("\\.");
        if (str.trim().equals("")) {
            return String.format("%" + bits + "s", defVal);
        } else if (inputStr[0].length() > bits) {
            //throw new Exception();
            sbError.append("! Waring: There is a variable [").append(key).append("] with oversized number [").append(ret).append("] (Limitation is ").append(bits).append(" bits)\r\n");
            return String.format("%" + bits + "s", defVal);
        } else {
            ret = inputStr[0];

            if (inputStr.length > 1 && inputStr[0].length() < bits) {

                if (inputStr[1].length() <= bits - inputStr[0].length() - 1) {
                    ret = ret + "." + inputStr[1];
                } else {
                    try {
                        input = Math.abs(Double.valueOf(str));
                    } catch (Exception e) {
                        // TODO throw exception
                        return str;
                    }
                    //decimalPower = Math.pow(10, Math.min(bits - inputStr[0].length(), inputStr[1].length()) - 1);
                    decimalPower = Math.pow(10, bits - inputStr[0].length() - 1);
                    decimalPart = Double.valueOf(Math.round(input * decimalPower) % decimalPower).longValue();
                    ret = ret + "." + (decimalPart == 0 && (bits - inputStr[0].length() < 2) ? "" : decimalPart);
                }
            }
            if (ret.length() < bits) {
                ret = String.format("%1$" + bits + "s", ret);
            }
        }

        return ret;
    }

    /**
     * Format the output string with maximum length
     *
     * @param bits Maximum length of the output string
     * @param m the experiment data holder
     * @param key the key of field in the map
     * @param defVal the default return value when error happens
     * @return formated string of number
     */
    protected String formatStr(int bits, HashMap m, Object key, String defVal) {

        String ret = getObjectOr(m, key, defVal).trim();
        if (ret.equals("")) {
            return ret;
        } else if (ret.length() > bits) {
            //throw new Exception();
            sbError.append("! Waring: There is a variable [").append(key).append("] with oversized content [").append(ret).append("], only first ").append(bits).append(" bits will be applied.\r\n");
            return ret.substring(0, bits);
        }

        return ret;
    }

    /**
     * Translate data str from "yyyymmdd" to "dd/MM/yyyy"
     *
     * @param str date string with format of "yyyymmdd"
     * @return result date string with format of "yyddd"
     */
    protected String formatDateStr(String str) {

        return formatDateStr(str, "0");
    }

    /**
     * Translate data str from "yyyymmdd" to "dd/MM/yyyy" plus days you want
     *
     * @param startDate date string with format of "yyyymmdd"
     * @param strDays the number of days need to be added on
     * @return result date string with format of "yyddd"
     */
    protected String formatDateStr(String startDate, String strDays) {

        // Initial Calendar object
        Calendar cal = Calendar.getInstance();
        int days;
        startDate = startDate.replaceAll("/", "");
        try {
            days = Double.valueOf(strDays).intValue();
            // Set date with input value
            cal.set(Integer.parseInt(startDate.substring(0, 4)), Integer.parseInt(startDate.substring(4, 6)) - 1, Integer.parseInt(startDate.substring(6)));
            cal.add(Calendar.DATE, days);
            // translatet to dd/MM/yyyy format
            return String.format("%1$02d/%2$02d/%3$04d", cal.get(Calendar.DATE), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
        } catch (Exception e) {
            // if tranlate failed, then use default value for date
            return "";
        }

    }

    /**
     * Revise output path
     *
     * @param path the output path
     * @return revised path
     */
    public static String revisePath(String path) {
        if (!path.trim().equals("")) {
//            path = path.replaceAll("/", File.separator);
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            File f = new File(path);
            if (f.isFile()) {
                f = f.getParentFile();
            }
            if (f != null && !f.exists()) {
                f.mkdirs();
            }
        }
        return path;
    }

    /**
     * Get output file object
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * Set default value for missing data
     *
     */
    protected void setDefVal() {
        sbError = new StringBuilder();
        outputFile = null;
        context = null;
        template = null;
        fileName = "needSetName.txt";
    }
}
