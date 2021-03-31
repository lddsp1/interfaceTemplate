package priv.znd.util.handler;

import java.util.HashMap;
import java.util.Map;

public interface ProProcessor {

    void init(String dataPath);
    Map<String, Object> doMessage(Map<String, Object> headers , Object message);

}
