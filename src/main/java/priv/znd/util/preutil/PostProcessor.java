package priv.znd.util.preutil;

import io.restassured.response.Response;


/**
 * @author lddsp
 * @date 2021/3/25 21:00
 */
public interface PostProcessor {
    Response doMessage(Response resp);
}
