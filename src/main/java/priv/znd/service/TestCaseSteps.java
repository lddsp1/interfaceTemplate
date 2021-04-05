package priv.znd.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
    private String description;
    private List<HashMap<String,Object>> steps = new ArrayList<>();
    private List<HashMap<String,Object>> execresult = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(TestCaseSteps.class);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<HashMap<String, Object>> getSteps() {
        return steps;
    }

    public void setStep(List<HashMap<String, Object>> steps) {
        this.steps = steps;
    }


    @JsonIgnoreProperties (ignoreUnknown = true)
    public static TestCaseSteps load(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(new File(path), TestCaseSteps.class);
    }

    public List<HashMap<String,Object>> run(BaseApiRun baseApiRun){
        steps.stream().forEach(step -> {
           HashMap<String,Object> result =  baseApiRun.run(step);
           execresult.add(result);
        });
        return execresult;
    }
    }
