package priv.znd.service;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author lddsp
 * @date 2021/3/28 23:07
 */
public class MethodObjectModel {
    private String name;
    private HashMap<String, MethodModel> methods =  new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(MethodObjectModel.class);


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, MethodModel> getMethods() {
        return methods;
    }

    public void setMethods(HashMap<String, MethodModel> methods) {
        this.methods = methods;
    }




    /**
     * 加载所有apiobject对象
     * @param path
     * @return
     * @throws IOException
     */
    @JsonIgnoreProperties (ignoreUnknown = true)
    public static MethodObjectModel load(String path) throws IOException {
        logger.info("加载apiObject对象:{}", path);
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            return objectMapper.readValue(new File(path), MethodObjectModel.class);
    }

    /*public void run(MethodModelcopy MethodModelcopy) {
    //  Response req = MethodModelcopy.run(save);

    }*/


}
