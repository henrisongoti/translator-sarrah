package org.agmip.translators.sarrah;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.agmip.util.MapUtil.*;
import org.agmip.util.MapUtil.BucketEntry;

/**
 * Sarrah Model Output translator for Pluviometrie file
 *
 * @author Meng Zhang
 */
public class SarrahStationOutput extends SarrahCommonOutput {

    @Override
    public void writeTemplate(Map map) throws IOException {

        if (!loadTemplate("Station")) {
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
                data.put("wst_loc_1", getObjectOr(wthData, "wst_loc_1", defValC));
                data.put("wst_id", getObjectOr(wthData, "wst_id", defValC));
                data.put("wst_lat", getObjectOr(wthData, "wst_lat", defValC));
                data.put("wst_long", getObjectOr(wthData, "wst_long", defValC));
                data.put("wst_elev", getObjectOr(wthData, "wst_elev", defValC));
                data.put("CodeTypeStation", getObjectOr(wthData, "sarrah_CodeTypeStation", defValC));
                dataArr.add(data);
            }
        }

        context.put("dataArr", dataArr);
    }
}
