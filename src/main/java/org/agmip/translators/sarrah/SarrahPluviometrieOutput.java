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
public class SarrahPluviometrieOutput extends SarrahCommonOutput {

    @Override
    public void writeTemplate(String string, Map map) throws IOException {

        if (!initTemplate("Pluviometrie")) {
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
                data.put("pred", getObjectOr(dailyData, "pred", defValC));

                dataArr.add(data);
            }
        }

        context.put("dataArr", dataArr);
    }
}
