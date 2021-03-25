package priv.znd.service;



import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;

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
    private HashMap<String, String> save; //参数化需要从报文提取的保存
    private HashMap<String,Object> headMaps; //报文头



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


/*public static MethodModel load(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        return objectMapper.readValue(new File(path),MethodModel.class);
    }*/

    /**
     * 发送请求
     * @param method
     * @param msgRule
     * @param header
     * @return
     */
    private Response sendhttprequest(String method, String msgRule, HashMap<String,String> header){
     //   Header Headers= new Header(header);
        return given().headers(header).queryParam(msgRule).log().all().request(method,this.url).then().log().all().extract().response();
    }

    private HashMap<String,Object> preHandle(Object handlemsg)
            throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {
        HashMap<String,Object>  remsg = null;
        Class clazz = null;
        if(preProcessor != null) {
            clazz = Class.forName(preProcessor);
            Object obj = clazz.newInstance();
            Method method = obj.getClass().getMethod("prehandl", HashMap.class, handlemsg.getClass());
            remsg = (HashMap<String, Object>) method.invoke(obj, "prehandl", headMaps, handlemsg);
        }
        else {
            remsg.put("headMaps", headMaps);
            remsg.put("req msg", handlemsg);
        }
        return remsg;
    }

    private HashMap<String,Object> afterHandle( Object handlemsg)
            throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {
        HashMap<String,Object>  remsg = null;
        Class clazz ;
        if(postProcessor != null ) {
            clazz = Class.forName(postProcessor);
            Object obj = clazz.newInstance();
            Method method = obj.getClass().getMethod("afterhandl", HashMap.class, handlemsg.getClass());
            remsg = (HashMap<String, Object>) method.invoke(obj, "afterhandl", headMaps, handlemsg);
        }
        else {
            remsg.put("headMaps", headMaps);
            remsg.put("resmsg", handlemsg);
        }
        return remsg;
    }
    



    public Response run(HashMap<String, Object> saveparam) {
        Response response = null ;
        response = given().filter((req,res,ctx)->{
            HashMap<String ,Object> result = null;
            try {
                if(bodymsg != null){
                    result = preHandle(bodymsg);
                    bodymsg = result.get("handlemsg").toString();
                    if(save != null && saveparam != null){
                        for (Map.Entry<String,  String> entry : save.entrySet()){
                            bodymsg.replace(entry.getKey(), entry.getValue());
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
                    result = preHandle(querymsg);
                    for(Map.Entry<String,Object> entry : ((HashMap<String, Object>) result.get("handlemsg")).entrySet()) {
                        String value = entry.getValue().toString();
                        if (value.toString().contains("${") && value.contains("}")
                                && save != null && saveparam != null){
                            req.queryParam(entry.getKey(), saveparam.get(value));
                        }
                        else
                            req.queryParam(entry.getKey(), value);
                    }
                }
                else if(formmsg != null){
                    result = preHandle(formmsg);
                    for(Map.Entry<String,Object> entry : ((HashMap<String, Object>) result.get("handlemsg")).entrySet()) {
                        String value = entry.getValue().toString();
                        if (value.contains("${") && value.contains("}")
                                && save != null && saveparam != null){
                                req.formParam(entry.getKey(), saveparam.get(value));
                            }
                        else
                            req.formParam(entry.getKey(),entry.getValue());
                    }
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            if(headMaps !=null){
                headMaps = (HashMap<String,  Object>) result.get("headMaps");
                for(Map.Entry<String,?> entry : headMaps.entrySet()){
                    req.header(entry.getKey(),entry.getValue());
                }
            }
            Response responseOnly = ctx.next(req,res);
            Map<String, ?> postRes = new HashMap<>();
            try {
                postRes = afterHandle(responseOnly);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
           Response responseReturn =new ResponseBuilder().clone(responseOnly)
                   .setBody(postRes.get("resmsg").toString())
                   .build();
            return responseReturn;
            }).log().all()
            .when().log().all().request(sendRule.get("httpmethod"),url)
            .then().extract().response();
        return response;
    }

}
