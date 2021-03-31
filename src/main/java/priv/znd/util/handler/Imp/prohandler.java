package priv.znd.util.handler.Imp;

import priv.znd.util.handler.ProProcessor;

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
        System.out.println("prohandler运行init");

    }
    @Override
    public Map<String, Object> doMessage(Map<String, Object> headers, Object message) {
        System.out.println("prohandler运行doMessage");
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("handlemsg",message);
        result.put("headMaps",headers);

        return result;
    }


}
