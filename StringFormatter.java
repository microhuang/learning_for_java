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
 * 参考：https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html
 * 参考：java.util.Formatter
 * 参考：java.text.MessageFormat
 */
public class StringFormatter
{
    //文本模版
    private String template="";
    
    //实际使用标签
    private List<String> fields;
    
    /**
     * 前端可用标签
     */
    private final Map<String,Map<String,Object>> labels = new HashMap();
    
    /**
     * 添加可用标签
     * @param label，前端标签
     * @param value，前端默认值
     * @param pattern
     * @param comment
     * @return 
     */
    public boolean add(final String label, final Object value, final String pattern, final String comment)
    {
        if(label==null || "".equals(label.trim()))
            return false;
        
        if(!labels.containsKey(label))
            labels.put(label.trim(), new HashMap(){{
                put("index", labels.size()+1);//格式化用：%index$
                put("label", label.trim());//前端用：%(name)$
                put("default", value);//前端用，示例值
                put("pattern", pattern);//前端用，缺省格式
                put("comment", comment);//前端用，label描述
            }});
        
        return true;
    }
    
    /**
     * 给标签设值
     * @param label
     * @param value
     * @return 
     */
    public boolean set(String label, final Object value)
    {
        if(label==null)
            return false;
        
        label = label.trim();
        
        if("".equals(label))
            return false;
        
        if(labels.containsKey(label) && fields.contains(label))
            labels.get(label).put("value", value);
        else
            return false;
        
        return true;
    }
    
    public boolean template(final String temp)
    {
        try
        {
            format(temp,true);
            template = temp;
            return true;
        }
        catch(Exception ex)
        {
            ;
        }
        return false;
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
        if(fields==null)
        {
            fields = new ArrayList();
            Pattern p = Pattern.compile("%\\((.*?)\\)");
            Matcher m = p.matcher(template);
            while(m.find()){
                if(labels.containsKey(m.group(1)))
                    fields.add(m.group(1));
            }
        }
        return fields;
    }
    
    /**
     * 处理模版字符
     * @param temp
     * @param test
     * @return 
     * @throws java.lang.Exception 
     */
    public String format(String temp, boolean test) throws Exception
    {
        fields = new ArrayList();
        
        if(temp==null || "".equals(temp))
            return temp;
        
//        temp = "a {%(title)s} b, c {%(amount)d} d";
        //temp = "a %1s b, c %2$d d";
        Object[] params = new Object[labels.size()];
//        temp = temp.replace("{", "").replace("}", "");//todo:xxx
        for (Map.Entry<String, Map<String, Object>> entry : labels.entrySet()) {
            String tmp = temp;
            temp = temp.replace("%("+entry.getKey()+")", "%"+entry.getValue().get("index")+"$");
            if(!tmp.equals(temp))
                fields.add(entry.getKey());
            if(test)
                params[Integer.parseInt(entry.getValue().get("index").toString())-1] = entry.getValue().get("default");
            else if(entry.getValue().containsKey("value"))
                params[Integer.parseInt(entry.getValue().get("index").toString())-1] = entry.getValue().get("value");
            else
                throw new Exception("Label: "+entry.getKey()+" 没有分配值！");
        }
        //直接使用String格式化
//        try
        {
            //return new Formatter().format(temp, params).toString();
            return String.format(temp, params);
        }
//        catch(Exception ex)
//        {
//            return temp;
//        }
    }
    
    public String format() throws Exception
    {
        return format(template,false);
    }
    
    /**
     * test
     * @param args 
     */
    public static void main(String[] args)
    {
        StringFormatter formatter = new StringFormatter();
        //初始格式化标签和默认值
        formatter.add("title", "张", "%(title)s", "姓名");
        formatter.add("amount", 111.11, "%(amount)d", "金额");
        formatter.add("date", new Date(), "%(date)tF", "日期");
        //前端需要得到有效的格式化标签的信息
        Map<String,Map<String,Object>> labels = formatter.getLabels();
        System.out.println(labels);
        //设置并检查模版格式
        if(formatter.template("您好 {%(title)s} , {%(date)tF} 账单，金额 {%(amount).2f}，{%(amount).2f}。"))
        {
            List fields = formatter.getFields();
            //未用到的label不用设置
            if(fields.contains("title"))
                formatter.set("title", "张三");
            if(fields.contains("amount"))
                formatter.set("amount", -34.123);
            if(fields.contains("date"))
                formatter.set("date", new Date());
            //进行格式化
            try
            {
                String s = formatter.format();
                System.out.println(s);
            }
            catch(Exception ex)
            {
                ;
            }
        }
        else
        {
            System.out.println("模版有误，请更正！");
        }
        //准备格式化值
        formatter.set("title", "张");
        formatter.set("date", new Date());
        formatter.set("amount", 122.234);
        //直接格式化，不建议这样使用，应该先检测模版格式
        try
        {
            String s = formatter.format("您好 {%(title)s} , {%(date)tF} 账单，金额 {%(amount).2f} 。",false);
            System.out.println(s);
        }
        catch(Exception ex)
        {
            ;
        }
        List fields = formatter.getFields();
        System.out.println(fields);
    }
    
