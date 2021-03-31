package priv.znd.util.handler.Imp;

import io.restassured.response.Response;
import priv.znd.util.handler.PostProcessor;


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
        System.out.println("afterHandler运行domessage");
        return resp;
    }
}
