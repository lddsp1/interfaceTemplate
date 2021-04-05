package priv.znd.service;

import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
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
import priv.znd.util.jsonutil.CustomContComparator;
import priv.znd.util.xmlutil.CunstomXmlDvaluator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author lddsp
 * @date 2021/3/7 23:02
 */
public class BaseApiRun {

    private List<MethodObjectModel> apis = new ArrayList<MethodObjectModel>(); //保存所有的接口信息
    //private HashMap<String,Response> result;
    private HashMap<String, Object> save; //参数化需要从报文提取的保存
    private HashMap<String,Object> saveparam = new HashMap<>(); //参数化数据保存
    private HashMap<String,String> respondbody = new HashMap<>();//保存响应体
    private static final Logger logger = LoggerFactory.getLogger(BaseApiRun.class);

    public List<MethodObjectModel> getApis() {
        return apis;
    }

    public HashMap<String, Object> getSaveparam() {
        return saveparam;
    }

    public void setApis(List<MethodObjectModel> apis) {
        this.apis = apis;
    }

    public HashMap<String,  Object> getSave() {
        return save;
    }

    public void setSave(HashMap<String, Object> save) {
        this.save = save;
    }



    /**
     *上下文案例依赖参数保存
     * @param response
     */
    private void setSaveparam(Response response, HashMap<String, Object> save) {
        if(save != null){
            logger.info("需要保存的上下文依赖:{}",save);
            String resStr = response.getBody().asString();
            for (Map.Entry<String, Object> entry : save.entrySet()){
                String token = entry.getValue().toString();
                Pattern r = Pattern.compile(token);
                Matcher m = r.matcher(resStr);
                int i = 1;
                while(m.find()){
                    saveparam.put("${"+entry.getKey()+i+"}",m.group(1).trim());
                    i++;
                }
            }
            logger.info("案例间的依赖保存有{}",saveparam);
        }
    }


    /**
     * 加载所有的api object对象，并保存到一个列表里
     * @param dir //到文件夹路径
     */
    public void load(String dir){
        logger.info("加载接口对象的路径:{}",dir);
        Arrays.stream(new File(dir).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                //todo: 筛选文件，
                return true;
            }
        })).forEach(path -> {
            try {
                apis.add(MethodObjectModel.load(dir+"/"+path));//导入当前路径的所有接口
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 进行Json的结果比较
     * @param expected 预期值
     * @param actual 实际值
     * @return 比较结果
     * @throws JSONException
     */
    private HashMap<String, Object> compareJson
            (String expected, String actual,String caseName)
            throws JSONException {
        HashMap<String, Object>  jsonResult =new HashMap<>();
        JSONCompareResult result = JSONCompare.compareJSON(expected, actual,
                new CustomContComparator(JSONCompareMode.LENIENT));
        boolean  Success = result.passed();
        //System.out.println("比较结果：" + Success);
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
        jsonResult.put("stepname",caseName);
        return jsonResult;
    }

    /**
     * Xml响应报文对比
     * @param expected 预期值
     * @param actual 实际值
     * @param caseName 案例名称
     * @return 比较结果
     */
    private HashMap<String, Object> compareXml (String expected, String actual,String caseName){
        HashMap<String, Object> xmlResult = new HashMap<>();
        Diff xmlDiff = DiffBuilder.compare(expected).withTest(actual)
                .checkForSimilar()
                .ignoreElementContentWhitespace()
                .ignoreComments()
                .ignoreWhitespace()
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes))
                .withDifferenceEvaluator(new CunstomXmlDvaluator())
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
                    xmlResult.put("resultmsg", result.trim());
                }
            }
        }else
            xmlResult.put("resultmsg","执行成功\n");
        xmlResult.put("resultflag",!Success);
        xmlResult.put("stepname",caseName);
        return xmlResult;
    }


    /**
     *根据测试用例提供的api Object和对应的action，检索对应的api，并调用对应的方法
     * @param step 案例的执行步骤
     * @return 返回案例的执行结果包含
     * resultmsg 执行信息
     * resultflag 执行结果（true|false）
     * stepname 案例名称
     */
    public HashMap<String, Object> run(HashMap<String, Object> step){
        AtomicReference<HashMap<String, Object>> metherResult = new AtomicReference<>(new HashMap<>());
        apis.stream().forEach(api ->{
            Response onlyRespone ;
            HashMap<String, Object> comResult =new HashMap<>();
            if(api.getName().equals(step.get("apiObject").toString())){
            String contextPath = (step.get("dataPath") != null)?step.get("dataPath").toString():"";
            onlyRespone = api.getMethods().get(step.get("action")).run(saveparam,contextPath);
            String asserresp = null;
            if(onlyRespone != null) {
                asserresp = onlyRespone.getBody().asString();
            }else {
                if(logger.isDebugEnabled())
                    logger.error("{}响应结果为空",step.get("stepname").toString());
            }
                if (asserresp != null || StringUtils.isNotEmpty(asserresp)) {
                    logger.info("{}执行结果的响应报文:{}", step.get("stepname").toString(), asserresp);
                    setSaveparam(onlyRespone, api.getMethods().get(step.get("action")).getSave());
                if (step.get("assertjson") != null) {
                    try {
                        comResult = compareJson(step.get("assertjson").toString(), asserresp, step.get("stepname").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        comResult = null;
                        logger.error("预期响应报文json格式有误:" + step.get("assertjson"));
                    }
                } else if (step.get("assertxml") != null) {
                    comResult = compareXml( step.get("assertxml").toString(), asserresp, step.get("stepname").toString());

                } else {
                    comResult.put("resultmsg", "执行成功\n");
                    comResult.put("resultflag", true);
                    comResult.put("stepname", step.get("stepname"));
                }

            } else{
                    comResult.put("resultmsg", "执行失败，响应报文为null\n");
                    comResult.put("resultflag", false);
                    comResult.put("stepname", step.get("stepname"));
            }
                logger.info("{}步骤执行信息:{}",step.get("stepname"),comResult.get("resultmsg"));
                metherResult.set(comResult);
            }
        });
        return metherResult.get();
    }
}
