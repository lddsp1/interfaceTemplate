package priv.znd.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author lddsp
 * @date 2021/3/9 7:03
 */
public class TestCaseSteps {
    private String name;
    private String decription;
    private List<HashMap<String,Object>> steps = new ArrayList<>();
    private List<HashMap<String,Object>> execresult = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(TestCaseSteps.class);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDecription() {
        return decription;
    }

    public void setDecription(String decription) {
        this.decription = decription;
    }

    public List<HashMap<String, Object>> getStep() {
        return steps;
    }

    public void setStep(List<HashMap<String, Object>> step) {
        this.steps = steps;
    }

    public TestCaseSteps load(String path) throws IOException {
        logger.info("导入测试案例集");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        return objectMapper.readValue(new File(path), TestCaseSteps.class);
    }

    public List<HashMap<String,Object>> run(BaseApiRun baseApiRun){

        steps.stream().forEach(step -> {
           logger.info("执行测试用例:"+step.get("stepname"));
           HashMap<String,Object> result =  baseApiRun.run(step);
           execresult.add(result);
        });
        return execresult;
    }
    }
