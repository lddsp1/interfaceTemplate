package priv.znd.service;

import io.restassured.response.Response;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xmlunit.diff.ComparisonResult.DIFFERENT;

/**
 * @author lddsp
 * @date 2021/3/7 23:02
 */
public class BaseApiRun {

    /**
     * 保存所有的接口信息
     */
    private List<MethodObjectModel> apis = new ArrayList<MethodObjectModel>();
    //private HashMap<String,Response> result;
    private HashMap<String,String> save; //参数化需要从报文提取的保存
    private HashMap<String,Object> saveparam = new HashMap<>(); //参数化数据保存
    private HashMap<String,String> respondbody = new HashMap<>();//保存响应体
    private static final Logger logger = LoggerFactory.getLogger(BaseApiRun.class);

    public List<MethodObjectModel> getApis() {
        return apis;
    }

    public HashMap<String, Object> getSaveparam() {
        return saveparam;
    }

    public void setSaveparam(HashMap<String, Object> saveparam) {
        this.saveparam = saveparam;
    }

    /**
     *
     * @param response
     */
    private void setSaveparam(Response response) {
        if(save != null){
            String resStr = response.getBody().asString();
            for (Map.Entry<String, String> entry : save.entrySet()){
                String token = entry.getValue();
                Pattern r = Pattern.compile(token);
                Matcher m = r.matcher(resStr);
                if(m.find()){
                    for(int i = 1;i<= m.groupCount();i++){
                        saveparam.put("${"+entry.getKey()+i+"}",m.group(i).trim());
                        logger.info("保存上下文依赖:{"+entry.getKey()+i+"}"+m.group(i).trim());
                    }
                }
            }
        }
    }


    /**
     * 加载所有的api object对象，并保存到一个列表里
     * @param dir
     */
    public void load(String dir){
        logger.info("加载接口内容");
        Arrays.stream(new File(dir).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                //todo: filter
                return true;
            }
        })).forEach(path-> {
            try {
                apis.add(MethodObjectModel.load(dir+"/"+path));//导入当前路径的所有接口
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 进行Json的结果比较
     * @param expected
     * @param actual
     * @return
     * @throws JSONException
     */
    private HashMap<String, Object> compareJson
            (HashMap<String, Object> expected, String actual,String caseName)
            throws JSONException {
        JSONCompareMode compareMode = JSONCompareMode.LENIENT;
        String expectedStr;
        HashMap<String, Object>  jsonResult =new HashMap<>();
       if (expected.get("equals") != null ){
            compareMode = JSONCompareMode.NON_EXTENSIBLE;
            expectedStr = expected.get("equals").toString();
        }else
           expectedStr = expected.get("contain").toString();
        JSONCompareResult result = JSONCompare.compareJSON(expectedStr, actual,
                compareMode);
        boolean  Success = result.passed();
        String msg = result.getMessage();
        if(!Success) {
            msg = msg.replaceAll("Could not find match for element", "无法找到指定元素");
            msg = msg.replaceAll("Expected", "预期值");
            msg = msg.replaceAll("got", "实际值");
            msg = msg.replaceAll("Expected", "预期有");
            msg = msg.replaceAll("but got", "但实际有");
            msg = msg.replaceAll("but none found", "响应结果无");
            msg = msg.replaceAll("Unexpected", "预期结果无该值");
            msg = msg.replaceAll("\n", "").replaceAll(";", ";\n").replaceAll("    ", ",");
            jsonResult.put("resultmsg", msg);
        }else
            jsonResult.put("resultmsg","执行成功\n");
        jsonResult.put("resultflag",Success);
        return jsonResult;
    }

    /**
     * Xml响应报文对比
     * @param expected
     * @param actual
     * @param caseName
     * @return
     */
    private HashMap<String, Object> compareXml
            (HashMap<String, Object> expected, String actual,String caseName){
        HashMap<String, Object> xmlResult = new HashMap<>();
        Diff xmlDiff = DiffBuilder.compare(expected.get("assertxml").toString()).withTest(actual)
                .checkForSimilar()
                .ignoreElementContentWhitespace()
                .ignoreComments()
                .ignoreWhitespace()
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes))
                .withDifferenceEvaluator(new DifferenceEvaluator() {
                    /**
                     * 对整形参数值进行处理比如1.0和1.00
                     * @param comparison 比较器
                     * @param outcome 比较结果
                     * @return
                     */
                    @Override
                    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                        if(DIFFERENT.equals(outcome)) {
                            int a = 0;
                            int b = 0;
                            if (comparison.getControlDetails().getValue() instanceof Integer) {
                                a = (Integer) comparison.getControlDetails().getValue();
                            }
                                    /*if (comparison.getControlDetails().getValue() instanceof Integer) {
                                        b = (Integer) comparison.getControlDetails().getValue();
                                    }*/
                            if(comparison.getTestDetails().getValue() instanceof Integer){
                                b = (Integer) comparison.getTestDetails().getValue();
                            }
                            if (("CHILD_NODELIST_LENGTH".equals(comparison.getType().toString()) && a < b) ||
                                    (("CHILD_NODELIST_SEQUENCE".equals(comparison.getType().toString()))) ||
                                    (("CHILD_LOOKUP".equals(comparison.getType().toString()))
                                            && comparison.getControlDetails().getXPath() == null)) {
                                outcome = null;
                            }
                        }
                        return outcome;

                    }
                })
                .build();
        boolean  Success = xmlDiff.hasDifferences();
        //Iterable<Difference> msgIters = xmlDiff.getDifferences();
        StringBuffer msg = new StringBuffer();

        if(Success){
            Iterator<Difference> msgIters = xmlDiff.getDifferences().iterator();
            while (msgIters.hasNext()) {
                String result = "";
                Difference d = msgIters.next();
                //执行结果
                Comparison.Detail detail = d.getComparison().getTestDetails();
                //预期结果
                Comparison.Detail controllDetail = d.getComparison().getControlDetails();
                //执行的结果节点
                Node node = detail.getTarget();
                //预期结果的节点
                Node controllNode = controllDetail.getTarget();
                //结果处理
                //预期结果为空不做处理，有子节点不做校验
                if (controllNode != null && !"".equals(controllNode.getTextContent().trim())) {
                    String controllNodePath = controllDetail.getXPath();
                    if (controllNodePath != null && controllNodePath.trim().length() > 0) {
                        controllNodePath = controllNodePath.replaceAll("\\[1\\]", "");
                    }
                    if (controllNodePath.lastIndexOf("text()") != -1) {
                        NamedNodeMap attrs = controllNode.getParentNode().getAttributes();
                        String str = "";
                        if (attrs != null && attrs.getLength() > 0) {
                            for (int i = 0; i < attrs.getLength(); i++) {
                                Node tempNode = attrs.item(i);
                                if (tempNode != null) {
                                    str += "/" + tempNode.toString();
                                }
                            }
                        }
                        controllNodePath.replaceAll("/text\\(\\)", str);
                    } else if (controllNode.getAttributes() != null
                            && controllNode.getAttributes().getLength() > 0) {
                        NamedNodeMap attrs = controllNode.getAttributes();
                        if (attrs != null && attrs.getLength() > 0) {
                            String str = "";
                            for (int i = 0; i < attrs.getLength(); i++) {
                                Node tmpNode = attrs.item(i);
                                if (tmpNode != null) {
                                    str += "/" + tmpNode.toString();
                                }
                            }
                            controllNodePath += str;
                        }
                    }
                    result += "节点"+controllNodePath+"预期结果"+controllNode.getTextContent().trim()+",";
                    result += node !=null ? "预期结果"+node.getTextContent().trim()+";\n":"实际结果";
                    xmlResult.put("resultmsg", result);
                }
            }
        }else
            xmlResult.put("resultmsg","执行成功\n");
        xmlResult.put("resultflag",Success);
        xmlResult.put("stepname",caseName);
        return xmlResult;
    }



    /**
     *根据测试用例提供的api Object和对应的action，检索对应的api，并调用对应的方法
     */
    public HashMap<String, Object> run(HashMap<String, Object> step){
        AtomicReference<HashMap<String, Object>> metherResult = new AtomicReference<>(new HashMap<>());
        apis.stream().forEach(api ->{
            Response onlyRespone ;
            HashMap<String, Object> comResult =new HashMap<>();
            if(api.getName().equals(step.get("apiObject").toString())){
            String contextPath = (step.get("dataPath") != null)?step.get("dataPath").toString():"";
            onlyRespone = api.getMethods().get(step.get("action")).run(saveparam,contextPath);
            String asserresp = onlyRespone.getBody().asString();
            if (asserresp != null || "".equals(asserresp)) {
                save = api.getSave();
                setSaveparam(onlyRespone);
//                respondbody.put(step.get("stepname").toString(),onlyRespone.getBody().asString());
                logger.info(step.get("stepname").toString() + "执行结果的响应报文:" + asserresp);
                if (step.get("assertjson") != null) {
                    try {
                        comResult = compareJson((HashMap<String, Object>) step.get("assertjson"), asserresp, step.get("stepname").toString());
                        metherResult.set(comResult);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        comResult = null;
                        metherResult.set(comResult);
                        logger.error("预期响应报文json格式有误:" + step.get("assertjson"));
                    }
                } else if (step.get("assertxml") != null) {
                    comResult = compareXml((HashMap<String, Object>) step.get("assertxml"), asserresp, step.get("stepname").toString());
                    metherResult.set(comResult);
                } else {
                    comResult.put("resultmsg", "执行成功\n");
                    comResult.put("resultflag", true);
                    comResult.put("stepname", step.get("stepname"));
                    metherResult.set(comResult);
                }
                if (step.get("containkey") != null) {
                    // onlyRespone.then().body()
                }
            }
            }
        });
        return metherResult.get();
    }


}
