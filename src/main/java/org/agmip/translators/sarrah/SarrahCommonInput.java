package org.agmip.translators.sarrah;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.agmip.core.types.TranslatorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sarrah Experiment Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public abstract class SarrahCommonInput implements TranslatorInput {

    private static final Logger log = LoggerFactory.getLogger(SarrahCommonInput.class);
    protected String[] flg = {"", "", ""};
    protected int flg4 = 0;
    protected String defValR = "-99.0";
    protected String defValC = "";
    protected String defValI = "-99";
    protected String defValD = "20110101";
    protected String jsonKey = "unknown";

    /**
     * DSSAT Data Output method for Controller using
     *
     * @param m The holder for BufferReader objects for all files
     * @return result data holder object
     */
    protected abstract HashMap readFile(HashMap m) throws IOException;

    /**
     * DSSAT XFile Data input method, always return the first data object
     *
     * @param arg0 file name
     * @return result data holder object
     */
    @Override
    public HashMap readFile(String arg0) {

        HashMap ret = new HashMap();
        String filePath = arg0;

        try {
            // read file by file
            ret = readFile(getBufferReader(filePath));

        } catch (FileNotFoundException fe) {
            log.warn("File not found under following path : [" + filePath + "]!");
            return ret;
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return ret;
//        return readFileById(arg0, 0);
    }

    /**
     * Set reading flgs for reading lines
     *
     * @param line the string of reading line
     */
    protected void judgeContentType(String line) {
        // Section Title line
        if (line.startsWith("*")) {

            setTitleFlgs(line);
            flg4 = 0;

        } // Data title line
        else if (line.startsWith("@")) {

            flg[1] = line.substring(1).trim().toLowerCase();
            flg[2] = "title";
            flg4++;

        } // Comment line
        else if (line.startsWith("!")) {

            flg[2] = "comment";

        } // Data line
        else if (!line.trim().equals("")) {

            flg[2] = "data";

        } // Continued blank line
        else if (flg[2].equals("blank")) {

            flg[0] = "";
            flg[1] = "";
            flg[2] = "blank";
            flg4 = 0;

        } else {

//            flg[0] = "";
            flg[1] = "";
            flg[2] = "blank";
        }
    }

    /**
     * Set reading flgs for title lines (the line marked with *)
     *
     * @param line the string of reading line
     */
    protected abstract void setTitleFlgs(String line);

    /**
     * Take the data str from input map and translate it from "yyddd" to
     * "yyyymmdd"
     *
     * @param m input map which might contain date value in it
     * @param id date string with format of "yyddd"
     */
    protected void translateDateStr(HashMap m, String id) {

        if (m.get(id) != null) {
            m.put(id, translateDateStr((String) m.get(id)));
        }
    }

    /**
     * Translate data str from "yyddd" to "yyyymmdd"
     *
     * @param str date string with format of "yyddd"
     * @return result date string with format of "yyyymmdd"
     */
    protected String translateDateStr(String str) {

        return translateDateStr(str, "0");
    }

    /**
     * Translate data str from "yyddd" or "doy" to "yyyymmdd"
     *
     * @param m the experiment data holder
     * @param id the key name of date field in the map
     * @param pdate the related planting date
     */
    protected void translateDateStrForDOY(HashMap m, String id, String pdate) {

        if (m.get(id) != null) {
            m.put(id, translateDateStrForDOY((String) m.get(id), pdate));
        }
    }

    /**
     * Translate data str from "yyddd" or "doy" to "yyyymmdd"
     *
     * @param str date string with format of "yyddd"
     * @param pdate the related planting date
     * @return result date string with format of "yyyymmdd"
     */
    protected String translateDateStrForDOY(String str, String pdate) {

        if (str != null && str.length() <= 3) {
            if (!pdate.equals("") && pdate.length() >= 2) {
                try {
                    str = String.format("%1$2s%2$03d", pdate.substring(0, 2), Integer.parseInt(str));
                } catch (NumberFormatException e) {
                    return "";
                }
            }
        }

        return translateDateStr(str, "0");
    }

    /**
     * Translate data str from "yyddd" to "yyyymmdd" plus days you want
     *
     * @param startDate date string with format of "yyydd"
     * @param strDays the number of days need to be added on
     * @return result date string with format of "yyyymmdd"
     */
    protected String translateDateStr(String startDate, String strDays) {

        // Initial Calendar object
        Calendar cal = Calendar.getInstance();
        int days;
        int year;
        if (startDate == null || startDate.length() > 5 || startDate.length() < 4) {
            //throw new Exception("");
            return ""; //defValD; // P.S. use blank string instead of -99
        }
        try {
            startDate = String.format("%05d", Integer.parseInt(startDate));
            days = Double.valueOf(strDays).intValue();
            // Set date with input value
            year = Integer.parseInt(startDate.substring(0, 2));
            year += year <= 15 ? 2000 : 1900; // P.S. 2015 is the cross year for the current version 
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(startDate.substring(2)));
            cal.add(Calendar.DATE, days);
            // translatet to yyddd format
            return String.format("%1$04d%2$02d%3$02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        } catch (Exception e) {
            // if tranlate failed, then use default value for date
            // sbError.append("! Waring: There is a invalid date [").append(startDate).append("]");
            return ""; //formatDateStr(defValD); // P.S. use blank string instead of -99
        }

    }

    /**
     * Divide the data in the line into a map (Default invalid value is null,
     * which means not to be sore in the json)
     *
     * @param line The string of line read from data file
     * @param formats The definition of length for each data field (String
     * itemName : Integer length)
     * @return the map contains divided data with keys from original string
     */
    protected HashMap readLine(String line, LinkedHashMap<String, Integer> formats) {

        return readLine(line, formats, null);
    }

    /**
     * Divide the data in the line into a map
     *
     * @param line The string of line read from data file
     * @param formats The definition of length for each data field (String
     * itemName : Integer length)
     * @param invalidValue The text will replace the original reading when its
     * value is invalid
     * @return the map contains divided data with keys from original string
     */
    protected HashMap readLine(String line, LinkedHashMap<String, Integer> formats, String invalidValue) {

        HashMap ret = new HashMap();
        int length;
        String tmp;

        for (String key : formats.keySet()) {
            // To avoid to be over limit of string lenght
            length = Math.min((Integer) formats.get(key), line.length());
            if (!((String) key).equals("") && !((String) key).startsWith("null")) {
                tmp = line.substring(0, length).trim();
                // if the value is in valid keep blank string in it
                if (checkValidValue(tmp)) {
                    ret.put(key, tmp);
                } else {
                    if (invalidValue != null) {
                        ret.put(key, invalidValue);   // P.S. "" means missing or invalid value
                    }
                }
            }
            line = line.substring(length);
        }

        return ret;
    }

    /**
     * Check if input is a valid value
     *
     * @return check result
     */
    protected boolean checkValidValue(String value) {
        if (value == null || value.trim().equals(defValC) || value.equals(defValI) || value.equals(defValR)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get BufferReader for each type of file
     *
     * @param filePath the full path of the input file
     * @return result the holder of BufferReader for different type of files
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected static HashMap getBufferReader(String filePath) throws FileNotFoundException, IOException {

        HashMap result = new HashMap();
        InputStream in;
        HashMap mapW = new HashMap();
        HashMap mapS = new HashMap();
        HashMap mapC = new HashMap();
        HashMap mapX = new HashMap();
        HashMap mapA = new HashMap();
        HashMap mapT = new HashMap();
        String[] tmp = filePath.split("[\\/]");

        // If input File is ZIP file
        if (filePath.toUpperCase().endsWith(".ZIP")) {

            // Get experiment name
            ZipEntry entry;
            ArrayList<String> exnames = new ArrayList();
            String exname = "";
            in = new ZipInputStream(new FileInputStream(filePath));
            while ((entry = ((ZipInputStream) in).getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    if (entry.getName().matches(".+\\.\\w{2}[Xx]")) {
                        exname = entry.getName().replaceAll("[Xx]$", "");
                        exnames.add(exname);
//                        break;
                    }
                }
            }

            // Read Files
            in = new ZipInputStream(new FileInputStream(filePath));

            while ((entry = ((ZipInputStream) in).getNextEntry()) != null) {
                if (!entry.isDirectory()) {

                    if (exnames.contains(entry.getName().replaceAll("[Xx]$", ""))) {
//                        result.put("X", getBuf(in, (int) entry.getSize()));
                        mapX.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (entry.getName().toUpperCase().endsWith(".WTH")) {
                        mapW.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (entry.getName().toUpperCase().endsWith(".SOL")) {
                        mapS.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (exnames.contains(entry.getName().replaceAll("[Aa]$", ""))) {
//                        result.put("A", getBuf(in, (int) entry.getSize()));
                        mapA.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (exnames.contains(entry.getName().replaceAll("[Tt]$", ""))) {
//                        result.put("T", getBuf(in, (int) entry.getSize()));
                        mapT.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (entry.getName().toUpperCase().endsWith(".OUT")) {
                        result.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (entry.getName().toUpperCase().endsWith(".CUL")) {
                        mapC.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (entry.getName().toUpperCase().endsWith(".JSON")) {
                        result.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    }
                }
            }
        } // If input File is not ZIP file
        else {
            in = new FileInputStream(filePath);
            File f = new File(filePath);
            if (filePath.matches(".+\\.\\w{2}[Xx]")) {
                mapX.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
//                result.put("X", new BufferedReader(new InputStreamReader(in)));
            } else if (filePath.toUpperCase().endsWith(".WTH")) {
                mapW.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
            } else if (filePath.toUpperCase().endsWith(".SOL")) {
                mapS.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
            } else if (filePath.matches(".+\\.\\w{2}[Aa]")) {
                mapA.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
//                result.put("A", new BufferedReader(new InputStreamReader(in)));
            } else if (filePath.matches(".+\\.\\w{2}[Tt]")) {
                mapT.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
//                result.put("T", new BufferedReader(new InputStreamReader(in)));
            } else if (f.getName().toUpperCase().endsWith(".OUT")) {
                result.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
            } else if (f.getName().toUpperCase().endsWith(".CUL")) {
                mapC.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
            } else if (filePath.toUpperCase().endsWith(".JSON")) {
                result.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
            }
        }

        result.put("W", mapW);
        result.put("S", mapS);
        result.put("X", mapX);
        result.put("A", mapA);
        result.put("T", mapT);
        result.put("C", mapC);
        result.put("Z", tmp[tmp.length - 1]);

        return result;
    }

    /**
     * Get BufferReader object from Zip entry
     *
     * @param in The input stream of zip file
     * @param entry The current entry of zip input stream
     * @return result The char array for current entry
     * @throws IOException
     */
    private static char[] getBuf(InputStream in, ZipEntry entry) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        char[] buf;
        long size = entry.getSize();;

        if (size > 0 && size <= Integer.MAX_VALUE) {
            buf = new char[(int) size];
            br.read(buf);
        } else {
            char[] b = new char[1024];
            CharArrayWriter cw = new CharArrayWriter();
            int chunk;
            while ((chunk = br.read(b)) > 0) {
                cw.write(b, 0, chunk);
            }
            buf = cw.toCharArray();
        }

        return buf;
    }

    public static void setDataVersionInfo(HashMap m) {
        m.put("data_source", "SARRAH");
        m.put("crop_model_version", "");
    }

    /**
     * copy item from one map to another map
     *
     * @param to the map which data will be copied to
     * @param from the map which data will be copied from
     * @param key the key name which will be copied
     *
     */
    public static void copyItem(HashMap to, HashMap from, String key) {
        copyItem(to, from, key, key, false);
    }

    /**
     * copy item from one map to another map original data might be delete based
     * on last boolean value
     *
     * @param to the map which data will be copied to
     * @param from the map which data will be copied from
     * @param key the key name which will be copied
     * @param deleteFlg decide if delete the original data(true for delete)
     *
     */
    public static void copyItem(HashMap to, HashMap from, String key, boolean deleteFlg) {
        copyItem(to, from, key, key, deleteFlg);
    }

    /**
     * copy item from one map to another map by using different key original
     * data might be delete based on last boolean value
     *
     * @param to the map which data will be copied to
     * @param from the map which data will be copied from
     * @param toKey the key name used in target holder
     * @param fromKey the key name used in original holder
     * @param deleteFlg decide if delete the original data(true for delete)
     *
     */
    public static void copyItem(HashMap to, HashMap from, String toKey, String fromKey, boolean deleteFlg) {
        if (from.get(fromKey) != null) {
            if (deleteFlg && from.get(fromKey) != null) {
                to.put(toKey, from.remove(fromKey));
            } else {
                to.put(toKey, from.get(fromKey));
            }
        }
    }
}
