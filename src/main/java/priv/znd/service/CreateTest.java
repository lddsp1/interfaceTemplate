package priv.znd.service;

import io.qameta.allure.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author lddsp
 * @date 2021/3/8 8:41
 */
public class CreateTest {
    private static BaseApiRun baseApiRun;


    @ParameterizedTest(name = "{index} {1}")
    @MethodSource()
    @Description()
    @Step("Type {testCaseSteps.name}")
    void apiTest(TestCaseSteps testCaseSteps ,String name){
        Allure.description(testCaseSteps.getDescription());
        List<HashMap<String,Object>> results = testCaseSteps.run(baseApiRun);
        SoftAssertions softAssert = new SoftAssertions();
        results.stream().forEach(
                result->softAssert.assertThat(result.get("resultflag"))
                        .as("%s执行结果:\n%s",result.get("stepname"),result.get("resultmsg").toString())
                        .isEqualTo(true)
        );
        softAssert.assertAll();
    }


    static List<Arguments> apiTest(){
        //保存参数化案例数据
        List<Arguments> testcases = new ArrayList<>();
        //加载所有的接口类
        baseApiRun = new BaseApiRun();
        if(System.getProperty("apiPath")!=null) {
            baseApiRun.load(System.getProperty("apipath").trim());
        }else
            baseApiRun.load("src/main/resources/service/api_sample");
        //读取测试用例
        String testcaseDir = (System.getProperty("testcasePath") != null) ? System.getProperty("testcasePath").trim()
                :"src/main/resources/service/testcase_sample";
        Arrays.stream(new File(testcaseDir).list()).forEach(name ->{
            String realPath =testcaseDir+"/"+name;
            try {
                TestCaseSteps testcase = TestCaseSteps.load(realPath);
                testcases.add(arguments(testcase,testcase.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        );
        return testcases;
    }

}
