package priv.znd.service;



import com.alibaba.testable.core.annotation.MockMethod;
import com.alibaba.testable.core.model.MockScope;
import com.alibaba.testable.processor.annotation.EnablePrivateAccess;
import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试返回值
 * @author lddsp
 * @date 2021/3/28 21:27
 */


@EnablePrivateAccess
class BaseApiRunTest {
    BaseApiRun baseApiRun = new BaseApiRun();
    MethodModel methodModel = new MethodModel();
    private String path = "src/main/resources/service/api_unittest";
    private String path1 = "src/main/resources/service/api_unittest";


    public static class  Mock{
        /**
         * MethodModel的run方法的Mock 返回值
         * @param self
         * @param saveparam
         * @param dataPath
         * @return
         */
        @MockMethod(targetMethod = "run",scope = MockScope.ASSOCIATED)
        private Response runMock(MethodModel self,HashMap<String, Object> saveparam, String dataPath){
            String resStr = "{\"eeee\":\"aaaa\",\"rrrr\":\"dddd\",\"tttt\":\"ffff\"}";
            if(dataPath.equals("xml"))
                resStr="<class><student><name>cccc</name><age>21</age></student><student><name>bbbbbb</name><age>22</age></student></class>";
            else if(dataPath.equals("null"))
                return null;
            Response mockRes = new ResponseBuilder()
                    .setBody(resStr)
                    .setStatusCode(200)
                    .build();
            return mockRes;
        }
    }

    /**
     * 接口数据加载
     */
    @Test
    void load() {
        baseApiRun.load(path1);
        baseApiRun.getApis().stream().forEach(api->{
            assertThat( api.getMethods().size(),greaterThanOrEqualTo(1) );
        });

    }

    /**
     * 上下文依赖参数保存xml
     */
    @Test
    void setSaveparamxml() {
        String resxml = "<class><student><name>cccc</name><age>21</age></student><student><name>bbbbbb</name><age>22</age></student></class>";
        String pat = "<name>(.+?)</name>";
        HashMap<String, String> localSave = new HashMap<>();
        Response localRes = new ResponseBuilder()
                .setBody(resxml)
                .setStatusCode(200)
                .build();
        localSave.put("save",pat);
        baseApiRun.setSave(localSave);
        baseApiRun.setSaveparam(localRes);
        assertAll("多行匹配",
                ()->assertEquals("cccc",baseApiRun.getSaveparam().get("${save1}").toString()),
                ()->assertEquals("bbbbbb", baseApiRun.getSaveparam().get("${save2}").toString())
        );


    }

    /**
     * 上下文依赖参数保存Json
     */
    @Test
    void setSaveparamJson() {
        String res = "{\"eeee\":{\"aaaa\":\"ggggg\", \"vvvvv\":\"llll\", \"cccc\":{\"uuuu\":111, \"iii\":\"ppp\"},\"rrrr\":\"dddd\",\"tttt\":\"ffff\"}";
        String res1 = "{\"eeee\":{\"aaaa\":\"ggggg2\", \"vvvvv\":\"llll\", \"cccc\":{\"uuuu\":111, \"iii\":\"ppp\"}, \"eeee2\":{\"aaaa\":\"ggggg3\", \"vvvvv\":\"llll1\", \"cccc\":{\"uuuu\":1111, \"iii\":\"ppp1\"}, \"rrrr\":\"dddd\",\"tttt\":\"ffff\"}";
        String pat = "\"aaaa\":(.+?),";
        HashMap<String, String> localSave = new HashMap<>();
        Response localRes = new ResponseBuilder()
                .setBody(res)
                .setStatusCode(200)
                .build();
        Response localRes1 = new ResponseBuilder()
                .setBody(res1)
                .setStatusCode(200)
                .build();
        localSave.put("save",pat);
        baseApiRun.setSave(localSave);
        baseApiRun.setSaveparam(localRes);
        assertEquals("ggggg",baseApiRun.getSaveparam().get("${save1}"));
        baseApiRun.setSaveparam(localRes1);
       assertAll("多行匹配",
                ()->assertEquals("ggggg2",baseApiRun.getSaveparam().get("${save1}").toString()),
                ()->assertEquals("ggggg3", baseApiRun.getSaveparam().get("${save2}").toString())
        );
    }

   /* @Test
    void test() throws JSONException {
        String res = "{\"eeee\":{\"aaaa\":\"ggggg\", \"vvvvv\":\"llll\", \"cccc\":{\"uuuu\":111, \"iii\":\"ppp\"},\"rrrr\":\"dddd\",\"tttt\":\"ffff\"}";
        JSONObject jo = JSON.parseObject((res));
        System.out.println(jo.toString());
    }*/


    /**
     * compareJson测试
     */
    @Test
    void compareJson() {
        String expected = "{\"eeee\":\"aaaa\",\"rrrr\":\"dddd\",\"tttt\":\"ffff\"}";
        String expectedno = "{\"eeee\":\"aaaaww\",\"rrrr\":\"dddd\",\"tttt\":\"ffff\"}";
        String expectedlike = "{\"eeee\":\"\",\"rrrr\":\"dddd\",\"tttt\":\"ffff\"}";
        String actual = "{\"eeee\":\"aaaa\",\"rrrr\":\"dddd\",\"tttt\":\"ffff\"}";
        HashMap<String, Object> resultJson = baseApiRun.compareJson(expected,actual,"compareJson");
        HashMap<String, Object> resultJsonno = baseApiRun.compareJson(expectedno,actual,"compareJsonno");
        HashMap<String, Object> resultJsonnlike = baseApiRun.compareJson(expectedlike,actual,"compareJsonlike");
        assertAll("测试Json",
                ()->assertEquals("compareJson",resultJson.get("stepname")),
                ()->assertEquals("执行成功\n",resultJson.get("resultmsg")),
                ()->assertEquals(true,resultJson.get("resultflag")),
                ()->assertEquals("compareJsonno",resultJsonno.get("stepname")),
                ()->assertEquals("eeee预期值: aaaaww, 实际值: aaaa",resultJsonno.get("resultmsg")),
                ()->assertEquals(false,resultJsonno.get("resultflag")),
                ()->assertEquals("compareJsonlike",resultJsonnlike.get("stepname")),
                ()->assertEquals("执行成功\n",resultJsonnlike.get("resultmsg")),
                ()->assertEquals(true,resultJsonnlike.get("resultflag"))
        );
    }

    /**
     * xml的对比
     */
    @Test
    void compareXml() {
        String expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?><class><student><name>vvv</name><age>21</age></student><student><name>bbbbbb</name><age>22</age></student></class>";
        String expectedno = "<class><student><name>aaa</name><age>21</age></student><student><name>bbbbbb</name><age>22</age></student></class>";
        String expectedlike = "<class><student><name></name><age>21</age></student><student><name>bbbbbb</name><age>22</age></student></class>";
        String actual = "<?xml version=\"1.0\" encoding=\"utf-8\"?><class><student><name>vvv</name><age>21</age></student><student><name>bbbbbb</name><age>22</age></student></class>";
        HashMap<String, Object> resultxml = baseApiRun.compareXml(expected,actual,"comparexml");
        HashMap<String, Object> resultxmlno = baseApiRun.compareXml(expectedno,actual,"comparexmlno");
        HashMap<String, Object> resultxmlnlike = baseApiRun.compareXml(expectedlike,actual,"comparexmllike");

        assertAll("测试Xml",
                ()->assertEquals("comparexml",resultxml.get("stepname")),
                ()->assertEquals("执行成功\n",resultxml.get("resultmsg")),
                ()->assertEquals(true,resultxml.get("resultflag")),
                ()->assertEquals("comparexmlno",resultxmlno.get("stepname")),
                ()->assertEquals("节点/class/student/name/text()预期结果aaa,预期结果vvv;",resultxmlno.get("resultmsg")),
                ()->assertEquals(false,resultxmlno.get("resultflag")),
                ()->assertEquals("comparexmllike",resultxmlnlike.get("stepname")),
                ()->assertEquals("执行成功\n",resultxmlnlike.get("resultmsg")),
                ()->assertEquals(true,resultxmlnlike.get("resultflag"))
        );

    }

    /**
     * 测试请求Json,无断言
     */
    @Test
    void runJsonNo() {
        baseApiRun.load(path1);
        HashMap<String, Object> step = new HashMap<>();
        step.put("apiObject","wework");
        step.put("action","get_token");
        step.put("stepname","unittestcase");
        step.put("dataPath","json");
        HashMap<String, Object> result = baseApiRun.run(step);
        assertAll("测试请求Json,无断言",
                ()->assertEquals("unittestcase",result.get("stepname")),
                ()->assertEquals("执行成功\n",result.get("resultmsg")),
                ()->assertEquals(true,result.get("resultflag"))
                );
    }
    /**
     * 测试请求Json，测试断言正常
     */
    @Test
    void runJsonSucess() {
        baseApiRun.load(path1);
        HashMap<String, Object> step = new HashMap<>();
        step.put("apiObject","wework");
        step.put("action","get_token");
        step.put("stepname","unittestcase");
        step.put("assertjson","{\"eeee\":\"\",\"rrrr\":\"dddd\",\"tttt\":\"ffff\"}");
        step.put("dataPath","json");
        HashMap<String, Object> result = baseApiRun.run(step);
        assertAll("测试请求Json,断言正常",
                ()->assertEquals("unittestcase",result.get("stepname")),
                ()->assertEquals("执行成功\n",result.get("resultmsg")),
                ()->assertEquals(true,result.get("resultflag"))
        );
    }
    /**
     * 测试请求Json，测试JSON断言失败
     */
    @Test
    void runJsonFail() {
        baseApiRun.load(path1);
        HashMap<String, Object> step = new HashMap<>();
        step.put("apiObject","wework");
        step.put("action","get_token");
        step.put("stepname","unittestcase");
        step.put("assertjson","{\"eeee\":\"ggg\",\"rrrr\":\"dddd\",\"tttt\":\"ffff\"}");
        step.put("dataPath","json");
        HashMap<String, Object> result = baseApiRun.run(step);
        //System.out.println(result.get("resultmsg"));
        assertAll("测试请求Json,断言",
                ()->assertEquals("unittestcase",result.get("stepname")),
                ()->assertEquals("eeee预期值: ggg, 实际值: aaaa",result.get("resultmsg")),
                ()->assertEquals(false,result.get("resultflag"))
        );

    }
    /**
     * 测试请求Xml,无断言
     */
    @Test
    void runxml() {
        baseApiRun.load(path);
        HashMap<String, Object> step = new HashMap<>();
        step.put("apiObject","tags");
        step.put("action","add");
        step.put("stepname","unittestcasexml");
        step.put("dataPath","xml");
        HashMap<String, Object> result = baseApiRun.run(step);
        assertAll("测试请求xml,无断言",
                ()->assertEquals("unittestcasexml",result.get("stepname")),
                ()->assertEquals("执行成功\n",result.get("resultmsg")),
                ()->assertEquals(true,result.get("resultflag"))
        );

    }


    /**
     * 测试请求Xml,断言通过
     */
    @Test
    void runxmlsucess(){
        String expected = "<class><student><name></name><age>21</age></student><student><name>bbbbbb</name><age>22</age></student></class>";

        baseApiRun.load(path);
        HashMap<String, Object> step = new HashMap<>();
        step.put("apiObject","tags");
        step.put("action","add");
        step.put("stepname","unittestcasexml");
        step.put("assertxml",expected);
        step.put("dataPath","xml");
        HashMap<String, Object> result = baseApiRun.run(step);
        assertAll("测试请求XMl,无断言",
                ()->assertEquals("unittestcasexml",result.get("stepname")),
                ()->assertEquals("执行成功\n",result.get("resultmsg")),
                ()->assertEquals(true,result.get("resultflag"))
        );
    }

    /**
     * 测试请求Xml,断言失败
     */
    @Test
    void runxmlFail(){
        String expected = "<class><student><name>bbbb</name><age>21</age></student><student><name>bbbbbb</name><age>22</age></student></class>";

        baseApiRun.load(path);
        HashMap<String, Object> step = new HashMap<>();
        step.put("apiObject","tags");
        step.put("action","add");
        step.put("stepname","unittestcasexml");
        step.put("assertxml",expected);
        step.put("dataPath","xml");
        HashMap<String, Object> result = baseApiRun.run(step);
      //  System.out.println(result);
        assertAll("测试请求XMl,无断言",
                ()->assertEquals("unittestcasexml",result.get("stepname")),
                ()->assertEquals("节点/class/student/name/text()预期结果bbbb,预期结果cccc;",result.get("resultmsg")),
                ()->assertEquals(false,result.get("resultflag"))
        );
    }

    /**
     * responed is null
     */
    @Test
    void runNull(){
        String expected = "<class><student><name>bbbb</name><age>21</age></student><student><name>bbbbbb</name><age>22</age></student></class>";
        baseApiRun.load(path);
        HashMap<String, Object> step = new HashMap<>();
        step.put("apiObject","tags");
        step.put("action","add");
        step.put("stepname","unittestcasexml");
        step.put("assertxml",expected);
        step.put("dataPath","null");
        HashMap<String, Object> result = baseApiRun.run(step);
        //System.out.println(result);
        assertAll("测试请求XMl,无断言",
                ()->assertEquals("unittestcasexml",result.get("stepname")),
                ()->assertEquals("执行失败，响应报文为null\n",result.get("resultmsg")),
                ()->assertEquals(false,result.get("resultflag"))
        );
    }
    
}