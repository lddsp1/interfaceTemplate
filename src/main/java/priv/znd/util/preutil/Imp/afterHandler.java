package priv.znd.util.preutil.Imp;

import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;
import priv.znd.util.preutil.PostProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lddsp
 * @date 2021/3/26 5:13
 */
public class afterHandler implements PostProcessor {

    @Override
    public Response doMessage(Response resp) {
        //如果要修改响应报文内容
        /* Response responseReturn =new ResponseBuilder().clone(resp)
                   .setBody(resp.getBody().toString())
                   .build();*/
        return resp;
    }
}
