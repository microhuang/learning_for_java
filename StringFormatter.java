/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xxx.yyy.zzz;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * 文本模版格式化
 * java风格
 * %[argument_index$][flags][width][.precision]conversion
 * python风格
 * %[(name)][flags][width].[precision]typecode
 * 前端格式
 * {%[(name)][flags][width].[precision]typecode}
 */
public class StringFormatter 
{
    private String template="";
    
    /**
     * 前端可用标签
     */
    private final Map<String,Map<String,Object>> labels = new HashMap();
    
    /**
     * 添加可用标签
     * @param label，前端标签
     * @param value，前端默认值
     * @return 
     */
    public boolean add(final String label, final Object value)
    {
        if(label==null || "".equals(label.trim()))
            return false;
        
        if(!labels.containsKey(label))
            labels.put(label.trim(), new HashMap(){{
                put("index", labels.size()+1);//格式化用：%index$
                put("label", label.trim());//前端用：%(name)$
                put("default", value);//前端用，示例值
            }});
        
        return true;
    }
    
    /**
     * 给标签设值
     * @param label
     * @param value
     * @return 
     */
    public boolean set(final String label, final Object value)
    {
        if(label==null || "".equals(label.trim()))
            return false;
        
        if(labels.containsKey(label.trim()))
            labels.get(label.trim()).put("value", value);
        
        return true;
    }
    
    public boolean template(final String temp)
    {
        template = temp;
        return true;
    }
    
    /**
     * 得到所有可用标签
     * @return 
     */
    public Map<String,Map<String,Object>> getLabels()
    {
        return labels;
    }
    
    /**
     * 得到实际使用标签
     * @return 
     */
    public List getFields()
    {
        List fields = new ArrayList();
        Pattern p = Pattern.compile("%\\((.*?)\\)");
        Matcher m = p.matcher(template);
        while(m.find()){
            if(labels.containsKey(m.group(1)))
                fields.add(m.group(1));
        }
        return fields;
    }
    
    /**
     * 处理模版字符
     * @param temp
     * @return 
     */
    public String format(String temp)
    {
//        temp = "a {%(title)s} b, c {%(amount)d} d";
        //temp = "a %1s b, c %2$d d";
        Object[] params = new Object[labels.size()];
//        temp = temp.replace("{", "").replace("}", "");//todo:xxx
        for (Map.Entry<String, Map<String, Object>> entry : labels.entrySet()) {
            temp = temp.replace("%("+entry.getKey()+")", "%"+entry.getValue().get("index")+"$");
            params[Integer.parseInt(entry.getValue().get("index").toString())-1] = entry.getValue().get("value");
        }
        //直接使用String格式化
        try
        {
            return String.format(temp, params);
        }
        catch(Exception ex)
        {
            return temp;
        }
    }
    
    public String format()
    {
        return format(template);
    }
    
    /**
     * test
     * @param args 
     */
    public static void main(String[] args)
    {
        StringFormatter formatter = new StringFormatter();
        //初始格式化工具
        formatter.add("title", "张");
        formatter.add("amount", 111.11);
        formatter.add("date", new Date());
        //前端需要得到有效格式化标签
        Map<String,Map<String,Object>> labels = formatter.getLabels();
        System.out.println(labels);
        //准备格式化值
        formatter.set("title", "张");
        formatter.set("date", new Date());
        formatter.set("amount", 122.234);
        //进行格式化
        String s = formatter.format("您好 {%(title)s} , {%(date)tF} 账单，金额 {%(amount).2f} 。");
        formatter.template("您好 {%(title)s} , {%(date)tF} 账单，金额 {%(amount).2f} 。");
        List fields = formatter.getFields();
        //未用到的label不用设置
        if(fields.contains("title"))
            formatter.set("title", "张三");
        formatter.format();
        System.out.println(s);
    }
}
