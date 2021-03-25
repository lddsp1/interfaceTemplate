package priv.znd.service;



import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.znd.util.preutil.Imp.afterHandler;
import priv.znd.util.preutil.Imp.prohandler;
import priv.znd.util.preutil.PostProcessor;
import priv.znd.util.preutil.ProProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static io.restassured.RestAssured.given;

public class MethodModel {
    private String name; //接口
    private String url; //请求地址
    private String protocol; //请求规则
    private HashMap<String, String> sendRule; //http或https
    private String msgRule; //格式application/json或者application/xml
    private String bodymsg; //请求报文
    private HashMap<String,Object> querymsg; //查询报文
    private HashMap<String,Object> formmsg; //表单数据
    private List parammsg; //列表数据
    private String preProcessor; //是否进行报文前处理
    private String postProcessor; //是否进行报文后处理
    private HashMap<String, String> save; //参数化需要修改请求的值
    private HashMap<String,Object> headMaps; //报文头
    private static final Logger logger = LoggerFactory.getLogger(MethodModel.class);



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol.toLowerCase();
    }

    public HashMap<String, String> getSendRule() {
        return sendRule;
    }

    public void setSendRule(HashMap<String, String> sendRule) {
        this.sendRule = sendRule;
    }

    public String getmsgRule() {
        return msgRule;
    }

    public void setmsgRule(String msgRule) {
        this.msgRule = msgRule;
    }

    public HashMap<String, String> getSave() {
        return save;
    }

    public void setSave(HashMap<String, String> save) {
        this.save = save;
    }

    public HashMap<String, ?> getHeadMaps() {
        return headMaps;
    }

    public void setHeadMaps(HashMap<String, Object> headMaps) {
        this.headMaps = headMaps;
    }

    public String getPreProcessor() {
        return preProcessor;
    }

    public void setPreProcessor(String preProcessor) {
        this.preProcessor = preProcessor;
    }

    public String getMsgRule() {
        return msgRule;
    }

    public void setMsgRule(String msgRule) {
        this.msgRule = msgRule;
    }

    public String getBodymsg() {
        return bodymsg;
    }

    public void setBodymsg(String bodymsg) {
        this.bodymsg = bodymsg;
    }

    public HashMap<String,  Object> getQuerymsg() {
        return querymsg;
    }

    public void setQuerymsg(HashMap<String, Object> querymsg) {
        this.querymsg = querymsg;
    }

    public HashMap<String, Object> getFormmsg() {
        return formmsg;
    }

    public void setFormmsg(HashMap<String, Object> formmsg) {
        this.formmsg = formmsg;
    }

    public List<?> getParammsg() {
        return parammsg;
    }

    public void setParammsg(List parammsg) {
        this.parammsg = parammsg;
    }

    public String getPostProcessor() {
        return postProcessor;
    }

    public void setPostProcessor(String postProcessor) {
        this.postProcessor = postProcessor;
    }


    /**
     * 发送请求报文前处理,进行参数化或加密，或添加证书
     * @param handlemsg 请求报文
     * @param dataPath  参数化的路径
     * @return
     */
    private Map<String,Object> preHandle(Object handlemsg,String dataPath){
        Map<String,Object>  remsg = new HashMap<>();
        ProProcessor proProcessor = new prohandler();
        proProcessor.init(dataPath);
        proProcessor.doMessage(headMaps, handlemsg);
        return remsg;
    }

    /**
     * 响应报文处理，进行解密类操作
     * @param handlemsg 响应报文
     * @return
     */
    private Response afterHandle(Response handlemsg){
        PostProcessor postProcessor = new afterHandler();
        return postProcessor.doMessage(handlemsg);
    }


    /**
     * 发送RestAssured请求
     * @param saveparam 上下文依赖参数
     * @param dataPath  参数化参数全目录路径
     * @return
     */
    public Response run(HashMap<String, Object> saveparam, String dataPath) {
        Response response ;
        response = given().filter((req,res,ctx)->{
            Map<String ,Object> result = new HashMap<>();
                if(bodymsg != null){
                    result = preHandle(bodymsg, dataPath);
                    bodymsg = result.get("handlemsg").toString();
                    if(save != null && saveparam != null){
                        for (Map.Entry<String,  Object> entry : saveparam.entrySet()){
                            bodymsg.replace(entry.getKey(), entry.getValue().toString());
                            logger.info("请求报文:"+bodymsg);
                        }
                        req.body(bodymsg);
                    }

                }
            /*    else if(parammsg != null){
                    result = preHandle("pre", parammsg);
                    parammsg = (List) result.get("parammsg");
                    if (save != null && saveparam != null){
                        for (Map.Entry<String,  String> entry : save.entrySet()){
                            Collections.replaceAll(parammsg, entry.getKey(), entry.getValue());
                        }
                        req.param(parammsg);
                    }
                }*/
                else if(querymsg != null){
                    result = preHandle(querymsg, dataPath);
                    for(Map.Entry<String,Object> entry : ((HashMap<String, Object>) result.get("handlemsg")).entrySet()) {
                        String value = entry.getValue().toString();
                        if (value.contains("${") && value.contains("}")
                                && save != null && saveparam != null){
                            req.queryParam(entry.getKey(), saveparam.get(value));
                            logger.info("queryParam的:"+entry.getKey()+"\nvalue:"+ saveparam.get(value));
                        }
                        else
                            req.queryParam(entry.getKey(), value);
                    }
                }
                else if(formmsg != null){
                    result = preHandle(formmsg, dataPath);
                    for(Map.Entry<String,Object> entry : ((HashMap<String, Object>) result.get("handlemsg")).entrySet()) {
                        String value = entry.getValue().toString();
                        if (value.contains("${") && value.contains("}")
                                && save != null && saveparam != null){
                                req.formParam(entry.getKey(), saveparam.get(value));
                                logger.info("formParam的:"+entry.getKey()+"\nvalue:"+ saveparam.get(value));
                            }
                        else
                            req.formParam(entry.getKey(),entry.getValue());
                    }
                }
            if(headMaps !=null){
                headMaps = (HashMap<String,  Object>) result.get("headMaps");
                for(Map.Entry<String,?> entry : headMaps.entrySet()){
                    logger.info("报文头为:"+entry.getKey()+"\nvalue:");
                    req.header(entry.getKey(),entry.getValue());
                }
            }
            Response responseOnly = ctx.next(req,res);
            Response postRes = afterHandle(responseOnly);
            logger.info("响应报文为:" + postRes.getBody().asString());
            return postRes;
            }).log().all()
            .when().log().all().request(sendRule.get("httpmethod"),url)
            .then().extract().response();
        return response;
    }
}
