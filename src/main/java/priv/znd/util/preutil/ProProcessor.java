package priv.znd.util.preutil;

import java.util.HashMap;
import java.util.Map;

public interface ProProcessor {

    void init(String dataPath);
    Map<String, Object> doMessage(HashMap<String, Object> headers , Object message);

}
