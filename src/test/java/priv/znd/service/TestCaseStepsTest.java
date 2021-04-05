package priv.znd.service;

import com.alibaba.testable.core.annotation.MockMethod;
import com.alibaba.testable.core.model.MockScope;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author lddsp
 * @date 2021/3/26 6:55
 */
class TestCaseStepsTest {
    private BaseApiRun baseApiRun = new BaseApiRun();
    TestCaseSteps testCaseSteps;
    private String path = "src/main/resources/service/testcase_sample/test_sample.yaml";

    public static class Mock {
        /**
         * 进行执行得run得mock
         * @param self
         * @param step
         * @return
         */
        @MockMethod(targetMethod = "run",scope = MockScope.ASSOCIATED)
        private HashMap<String, Object> runResult(BaseApiRun self, HashMap<String, Object> step) {
            HashMap<String, Object> getResult = new HashMap<>();
            getResult.put("resultmsg", "执行成功\n");
            getResult.put("resultflag", true);
            getResult.put("stepname", "testName");
            return getResult;
        }
    }



    @Test
    void load() throws IOException {

        TestCaseSteps testCaseSteps = new TestCaseSteps().load(path);
        assertEquals("testName" , testCaseSteps.getName());
       // assertEquals("testdescri" , testCaseSteps.getDescription());
        testCaseSteps.getSteps().stream().forEach(step->{
            //System.out.println(step.getClass());
            assertEquals("apiName" , step.get("apiObject"));
            assertEquals("dataPath", step.get("dataPath"));
        });
    }

    @Test
    void run() throws IOException {
        testCaseSteps = TestCaseSteps.load(path);
        List<HashMap<String,Object>> testResult = testCaseSteps.run(baseApiRun);
       for(HashMap<String,Object> paramMap : testResult){
           assertEquals("执行成功\n",paramMap.get("resultmsg"));
           assertEquals(true, paramMap.get("resultflag"));
           assertEquals("testName", paramMap.get("stepname"));
       }
    }
}