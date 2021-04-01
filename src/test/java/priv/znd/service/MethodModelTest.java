package priv.znd.service;

import com.alibaba.testable.core.annotation.MockMethod;
import com.alibaba.testable.core.model.MockScope;
import com.alibaba.testable.processor.annotation.EnablePrivateAccess;
import io.restassured.builder.ResponseBuilder;
import io.restassured.internal.RequestSpecificationImpl;
import io.restassured.internal.TestSpecificationImpl;
import io.restassured.response.Response;
import io.restassured.specification.RequestSenderOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author lddsp
 * @date 2021/3/24 15:53
 */
@EnablePrivateAccess
class MethodModelTest {
    private MethodModel methodModel = new MethodModel();

    @Test
    void preHandle(){
        HashMap<String, Object> headers = new HashMap<>();
        headers.put("session","xxxxxeeesesion");
        methodModel.setHeadMaps(headers);
        Map<String,Object>  remsg =  methodModel.preHandle("hhhhhh","");
        assertThat(String.valueOf(remsg.get("handlemsg")),equalTo("hhhhhh"));
        System.out.println(remsg.get("headMaps").getClass());
        Map<String, Object> reslutHeader = (Map<String, Object>) remsg.get("headMaps");
        assertThat(reslutHeader.get("session"),equalTo("xxxxxeeesesion"));
    }

    @Test
    void afterHandle(){
        Response res = new ResponseBuilder()
                .setBody("this is a response")
                .setStatusCode(200)
                .build();
       Response res1 = methodModel.afterHandle(res);
        assertThat(res1.getBody().asString(), equalTo("this is a response"));
    }

    @Test
    @Disabled
    void run() {
        methodModel.setProtocol("get");
        methodModel.setUrl("http://localhost:5195/docker.json");
        methodModel.setBodymsg("{\"eeee\":{\"aaaa\":\"ggggg2\"},\"ttt\":${qqqq}}");
        HashMap<String, Object> repalceParam = new HashMap<>();
        repalceParam.put("${qqqq}","vvvv");
        Response resp = methodModel.run(repalceParam,"");
        System.out.println(resp.getBody().asString());
        //resp.then().contentType(ContentType.JSON).body()
    }


}