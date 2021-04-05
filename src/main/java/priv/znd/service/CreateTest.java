package priv.znd.service;

import io.qameta.allure.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static Logger logger = LoggerFactory.getLogger(CreateTest.class);


    @ParameterizedTest(name = "{index} {1}")
    @MethodSource()
    @Description()
    @Step("Type {testCaseSteps.name}")
    void apiTest(TestCaseSteps testCaseSteps ,String name){
        logger.info("{}---案例执行开始",name);
        Allure.description(testCaseSteps.getDescription());
        List<HashMap<String,Object>> results = testCaseSteps.run(baseApiRun);
        SoftAssertions softAssert = new SoftAssertions();
        results.stream().forEach(
                result->softAssert.assertThat(result.get("resultflag"))
                        .as("%s执行结果:\n%s",result.get("stepname"),result.get("resultmsg").toString())
                        .isEqualTo(true)
        );
        softAssert.assertAll();
        logger.info("{}---案例执行结束",name);
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
        logger.info("导入测试案例的路径:{}",testcaseDir);
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
