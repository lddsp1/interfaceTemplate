package priv.znd.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public abstract class MethodObjectModel {
    private String name;
    private HashMap<String, MethodModel> methods;
    private HashMap<String, String> save = new HashMap<>();
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

    public HashMap<String, String> getSave() {
        return save;
    }

    public void setSave(HashMap<String, String> saveParams) {
        this.save = save;
    }

    /**
     * 加载所有apiobject对象
     * @param path
     * @return
     * @throws IOException
     */
    public static MethodObjectModel load(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            return objectMapper.readValue(new File(path), MethodObjectModel.class);
    }

    public void run(MethodModel methodModel) {
    //  Response req = methodModel.run(save);

    }


}
