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
public class SarrahControllerOutputTest {

    SarrahControllerOutput output;
//    SarrahControllerInput input;
    URL resource;

    @Before
    public void setUp() throws Exception {
        output = new SarrahControllerOutput();
//        input = new SarrahControllerInput();
    }

    @Test
    public void test() throws IOException, Exception {
        String jsonStr;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("mach_fast.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        jsonStr = br.readLine();
        output.writeFile("output", JSONAdapter.fromJSON(jsonStr));
        File[] files = output.getOutputFileArr();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                assertTrue(files[i].exists());
                assertTrue(files[i].delete());
            }
        }
        File dir = new File("output");
        dir.delete();
    }
}