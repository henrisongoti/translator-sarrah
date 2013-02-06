package org.agmip.translators.sarrah;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.agmip.util.MapUtil.*;
import org.agmip.util.MapUtil.BucketEntry;

/**
 * Sarrah Model Output translator for Meteorologie file
 *
 * @author Meng Zhang
 */
public class SarrahMeteorologieOutput extends SarrahCommonOutput {

    /**
     * Fill data into the template of Meteorologie file
     *
     * @param map The Ace data set
     * @throws IOException
     */
    @Override
    public void writeTemplate(Map map) throws IOException {

        if (!loadTemplate("Meteorologie")) {
            return;
        }

        ArrayList<HashMap> dataArr = new ArrayList();
        HashMap data;
        ArrayList<HashMap> wthDatas = getObjectOr(map, "weathers", new ArrayList());

        for (int i = 0; i < wthDatas.size(); i++) {

            HashMap wthData = wthDatas.get(i);
            BucketEntry wth = new BucketEntry(wthData);
            ArrayList<HashMap<String, String>> dailyDataArr = wth.getDataList();

            for (int j = 0; j < dailyDataArr.size(); j++) {
                data = new HashMap();
                HashMap<String, String> dailyData = dailyDataArr.get(j);
                data.put("wst_id", getObjectOr(wthData, "wst_id", defValC));
                data.put("w_date", formatDateStr(getObjectOr(dailyData, "w_date", defValC)));
                data.put("tmax", getObjectOr(dailyData, "tmax", defValC));
                data.put("tmin", getObjectOr(dailyData, "tmin", defValC));
                data.put("tavd", getObjectOr(dailyData, "tavd", defValC));
                data.put("rhmnd", getObjectOr(dailyData, "rhmnd", defValC));
                data.put("rhmxd", getObjectOr(dailyData, "rhmxd", defValC));
//                data.put("HMoy", defValC);
                data.put("wind", getObjectOr(dailyData, "wind", defValC));
                data.put("sunh", getObjectOr(dailyData, "sunh", defValC));
                data.put("srad", getObjectOr(dailyData, "srad", defValC));
                data.put("evap", getObjectOr(dailyData, "evap", defValC));

                dataArr.add(data);
            }
        }

        context.put("dataArr", dataArr);
    }
}
