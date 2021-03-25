package priv.znd.service;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
        Allure.description(testCaseSteps.getDecription());
        testCaseSteps.run(baseApiRun);
    }

    public List<Arguments> apiTest(){
        //保存参数化案例数据
        List<Arguments> testcases = new ArrayList<>();
        //加载所有的接口类
        baseApiRun = new BaseApiRun();
        if(System.getProperty("apiPath")!=null) {
            baseApiRun.load("src/main/resources/test_framework_service/api");
        }else
            baseApiRun.load(System.getProperty("apipath").trim());
        //读取测试用例
        String testcaseDir = (System.getProperty("testcasePath").trim() != null) ? System.getProperty("testcasePath").trim()
                :"src/main/resources/test_framework_service/testcase";



        Arrays.stream(new File(testcaseDir).list()).forEach(name ->{
            String realPath =testcaseDir+"/"+name;
                    try {
                        MethodObjectModel testcase = MethodObjectModel.load(realPath);
                        testcases.add(arguments(testcase,testcase.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
        return testcases;
    }

}
