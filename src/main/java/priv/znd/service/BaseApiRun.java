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
    List<MethodObjectModel> apis = new ArrayList<MethodObjectModel>();
    //private HashMap<String,Response> result;
    private HashMap<String,String> save; //参数化需要从报文提取的保存
    private HashMap<String,Object> saveparam = new HashMap<>(); //参数化数据保存
    private HashMap<String,String> respondbody = new HashMap<>();//保存响应体
    private static final Logger logger = LoggerFactory.getLogger(BaseApiRun.class);


    public HashMap<String, Object> getSaveparam() {
        return saveparam;
    }

    public void setSaveparam(HashMap<String, Object> saveparam) {
        this.saveparam = saveparam;
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
     *根据测试用例提供的api Object和对应的action，检索对应的api，并调用对应的方法
     */
    public HashMap<String, Object> run(HashMap<String, Object> step){
        HashMap<String,Object> metherResult = null;
            apis.stream().forEach(api ->{
            if(api.getName().equals(step.get("apiObject").toString())){
                String contextPath = (step.get("dataPath") != null)?step.get("dataPath").toString():"";
                Response onlyRespone = api.getMethods().get(step.get("action")).run(saveparam,contextPath);
                save = api.getSave();
                setSaveparam(onlyRespone);
                respondbody.put(step.get("stepname").toString(),onlyRespone.getBody().asString());
                logger.info(step.get("stepname").toString()+"执行结果的响应报文:"+onlyRespone.getBody().asString());
            }
            if(step.get("assertjson") != null){
                String asserresp = respondbody.get(step.get("assertstep").toString());
                /**
                *  JSONCompareMode.LENIENT 可数据扩展，相同数据顺序可以不一样
                *  JSONCompareMode.NON_EXTENSIBLE 不可数据扩展，相同数据顺序可以不一样
                *  JSONCompareMode.STRICT 不可数据扩展，相同数据顺序必须一样
                *  JSONCompareMode.STRICT_ORDER 可数据扩展，相同数据顺序必须一样
                **/
                try {
                    JSONCompareResult result = JSONCompare.compareJSON(step.get("assertjson").toString(),asserresp,
                            JSONCompareMode.NON_EXTENSIBLE);
                    boolean  Success = result.passed();
                    String msg = result.getMessage();
                    if(!Success){
                        msg = msg.replaceAll("Could not find match for element","无法找到指定元素");
                        msg = msg.replaceAll("Expected","预期值");
                        msg = msg.replaceAll("got","实际值");
                        msg = msg.replaceAll("Expected","预期有");
                        msg = msg.replaceAll("but got","但实际有");
                        msg = msg.replaceAll("\n","").replaceAll(";",";\n");
                        metherResult.put("resultmsg",msg);
                    }else
                        metherResult.put("resultmsg","执行成功\n");
                    metherResult.put("resultflag",Success);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if(step.get("assertxml") != null){
                boolean totalFlag = true;
                String asserresp = respondbody.get(step.get("assertstep").toString());
                Diff xmlDiff = DiffBuilder.compare(step.get("assertxml").toString()).withTest(asserresp)
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
                            totalFlag = false;
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
                            metherResult.put("resultmsg", result);
                        }
                    }
                }else
                    metherResult.put("resultmsg","执行成功\n");
                metherResult.put("resultflag",Success);
            }
        });
            metherResult.put("stepname",step.get("stepname"));
        return metherResult;
    }


}
