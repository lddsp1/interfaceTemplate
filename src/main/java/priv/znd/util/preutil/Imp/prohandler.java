package priv.znd.util.preutil.Imp;

import priv.znd.util.preutil.ProProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lddsp
 * @date 2021/3/26 4:36
 */
public class prohandler implements ProProcessor {
    Map<String,String> contextParams = new HashMap<>();

    @Override
    public void init(String dataPath) {

    }
    @Override
    public Map<String, Object> doMessage(HashMap<String, Object> headers, Object message) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("handlemsg",message);
        result.put("headMaps",headers);

        return result;
    }


}
