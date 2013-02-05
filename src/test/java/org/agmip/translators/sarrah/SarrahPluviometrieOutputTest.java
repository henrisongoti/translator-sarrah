package org.agmip.translators.sarrah;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.agmip.util.JSONAdapter;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
/**
 *
 * @author Meng Zhang
 */
public class SarrahPluviometrieOutputTest {

    SarrahPluviometrieOutput output;
//    SarrahPluviometrieInput input;
    URL resource;

    @Before
    public void setUp() throws Exception {
        output = new SarrahPluviometrieOutput();
//        input = new SarrahPluviometrieInput();
    }

    @Test
    public void test() throws IOException, Exception {
        String jsonStr;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("mach_fast.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        jsonStr = br.readLine();
        output.writeFile("", JSONAdapter.fromJSON(jsonStr));
        File file2 = output.getOutputFile();
        if (file2 != null) {
            assertTrue(file2.exists());
            assertTrue(file2.getName().equals("Pluviometrie_AgMIP.txt"));
            assertTrue(file2.delete());
        }
    }
}