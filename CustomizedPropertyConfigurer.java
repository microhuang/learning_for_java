/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xxx.yyy.zzz;

import java.util.HashMap;  
import java.util.Map;  
import java.util.Properties;  
  
import org.springframework.beans.BeansException;  
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;  
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;  
  
/**
 * 在spring项目中获得、使用配置文件中的值
 */
public class CustomizedPropertyConfigurer extends PropertyPlaceholderConfigurer {  
  
    private static Map<String, Object> ctxPropertiesMap;  
    private static Properties props;  
    
    private PropertyPlaceholderConfigurer ref; 
    
    @Override  
    protected void processProperties(ConfigurableListableBeanFactory beanFactory,  
            Properties props)throws BeansException {  

        super.processProperties(beanFactory, props);  
        this.props=props;  
        //load properties to ctxPropertiesMap  
        ctxPropertiesMap = new HashMap<String, Object>();  
        for (Object key : props.keySet()) {  
            String keyStr = key.toString();  
            String value = props.getProperty(keyStr);  
            ctxPropertiesMap.put(keyStr, value);  
        }  
    }  
  
    //static method for accessing context properties  
    public static Object getContextProperty(String name) {  
//        return props.getProperty(name);
        return ctxPropertiesMap.get(name);  
    }  
    
    public static <T> T getContextProperty(String name, T def) {  
        Object ret = ctxPropertiesMap.get(name);
        if(ret!=null)
            return (T)ret;
        else
            return def;
    }
    
    public static Map<String, Object> getProperties() {
		return ctxPropertiesMap;
	}

    /**
     * @return the ref
     */
    public PropertyPlaceholderConfigurer getRef() {
        return ref;
    }

    /**
     * @param ref the ref to set
     */
    public void setRef(PropertyPlaceholderConfigurer ref) {
        this.ref = ref;
    }
}  
