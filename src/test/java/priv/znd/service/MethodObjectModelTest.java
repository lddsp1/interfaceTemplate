package priv.znd.service;

import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author lddsp
 * @date 2021/3/28 21:53
 */
class MethodObjectModelTest {
    private String path = "src/main/resources/service/api_sample/api_sample.yaml";
    private static MethodObjectModel methodObjectModel;


    @Test
    void load() throws IOException {
        methodObjectModel = MethodObjectModel.load(path);
        assertThat(methodObjectModel.getName(),equalTo("apiObjecName"));
        assertThat(methodObjectModel.getMethods().get("apiName").getClass().toString(),equalTo("class priv.znd.service.MethodModel"));
    }

}