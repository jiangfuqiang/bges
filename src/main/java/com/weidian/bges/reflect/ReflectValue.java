package com.weidian.bges.reflect;

import com.weidian.bges.annotation.ColName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang on 17/11/21.
 */
public class ReflectValue {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectValue.class);
    private static final int length = 8;
    private static final int int_length = 4;
    private static final int short_length = 2;
    private static final int offset = 0;

    private static Map<String, Method> methodMap = new HashMap<String, Method>();
    private static Map<String, Field[]> fieldMap = new HashMap<String, Field[]>();

    protected Object newInstanceObject(Class clazz) {
        Object object = null;
        try {
            object = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return object;
    }

    protected Object reflectT(Object object, Class clazz, Object value, String cn) throws Exception{


        try {
            //把列名转为方法驼峰名
            String methodName = transferMethod(cn);


            Field[] fields = clazz.getDeclaredFields();

            for(Field field : fields) {
                Annotation[] annotations = field.getDeclaredAnnotations();
                if(annotations == null || annotations.length == 0) {
                    continue;
                }
                boolean isSet = false;
                for(Annotation annotation : annotations) {
                    if(annotation instanceof ColName) {
                        //如果有注解，则用注解中指定的名字匹配hbase的列名
                        if(cn.equals(((ColName) annotation).name())) {
                            String fieldName = field.getName();
                            methodName = transferFieldName(fieldName);
                            isSet = true;
                            break;
                        }
                    }
                }
                if(isSet) {
                    break;
                }
            }

            String key = clazz.getName() + "_" + methodName;
            Method method = null;

            if(methodMap.containsKey(key)) {
                method = methodMap.get(key);
            } else {
                method = getMethod(clazz, methodName);
                if(method != null) {
                    methodMap.put(key, method);
                }
            }
            if(method == null) {
//                LOGGER.warn("not found this method : " + methodName);
                return object;
            }

            Class[] parameterTypes = method.getParameterTypes();

            Object[] values = new Object[parameterTypes.length];
            if(parameterTypes.length > 0) {
                int index = 0;
                for(Class ctClass : parameterTypes) {
                    try {
                        values[index] = changeMethodValue(ctClass.getSimpleName(),value);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("change value error: " + cn + " " + clazz.getName(),e);
                    }
                    index += 1;
                }
            }

            try {
                method.invoke(object, values);
            } catch (InvocationTargetException e) {
                LOGGER.error("colName=" + cn + " " + key + " " + values.toString(),e);
                throw new IllegalArgumentException(e.getMessage(),e);
            }

        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage(),e);
        }

        return object;
    }

    /**
     * 获取类或者父类的method
     * @param clazz
     * @param methodName
     * @return
     */
    private Method getMethod(Class clazz,String methodName) {
        Method[] methods = clazz.getDeclaredMethods();
        Method method = null;
        for(Method m : methods) {
            String mn = m.getName();
            if(mn.equals(methodName)) {
                method = m;
                break;
            }
        }
        if(method == null) {
            Class superClass = clazz.getSuperclass();
            if(superClass != null) {
                method = getMethod(superClass,methodName);
            }
        }
        return method;
    }

    /**
     * 转换方法参数类型
     * @param clazzName
     * @return
     */
    private Object changeMethodValue(String clazzName,Object value) throws Exception{
        String cclazzName = clazzName.toLowerCase();
        switch (cclazzName) {
            case "string":
                if(value != null) {
                    return value.toString();
                }
                return "";
            case "char":
                if(value == null || "".equals(value)) {
                    throw new IllegalArgumentException("value is null for char");
                }
                return Character.valueOf(value.toString().charAt(0));
            case "int":
                if(value == null) {
                    throw new IllegalArgumentException("value is null for int");
                }
            case "integer":
                if(value == null) {
                    return null;
                }
                return Integer.valueOf(value.toString());
            case "double":
                if(value == null) {
                    if("double".equals(clazzName)) {
                        throw new IllegalArgumentException("value is null for double");
                    }
                    return null;
                }
               return Double.valueOf(value.toString());
            case "float":
                if(value == null) {
                    if("float".equals(clazzName)) {
                        throw new IllegalArgumentException("value is null for float");
                    }
                    return null;
                }
               return Float.valueOf(value.toString());
            case "long":
                if(value == null) {
                    if("long".equals(clazzName)) {
                        throw new IllegalArgumentException("value is null for long");
                    }
                    return null;
                }
              return Long.valueOf(value.toString());

            case "short":
                if(value == null) {
                    if("short".equals(clazzName)) {
                        throw new IllegalArgumentException("value is null for short");
                    }
                    return null;
                }
               return Short.valueOf(value.toString());
            case "byte":
                if(value == null) {
                    if("byte".equals(clazzName)) {
                        throw new IllegalArgumentException("value is null for byte");
                    }
                    return null;
                }
                return Byte.valueOf(value.toString());
            case "bigdecimal":
                if(null == value || "".equals(value.toString())) {
                    return new BigDecimal(0);
                }
                return BigDecimal.valueOf(Double.parseDouble(value.toString()));

            case "biginteger":
                if(null == value || "".equals(value.toString())) {
                    return new BigInteger("0");
                }
                return BigInteger.valueOf(Long.parseLong(value.toString()));

            case "date":
                if(value == null || "".equals(value.toString())) {
                    return null;
                }
                Date ndate = null;
                String svalue = value.toString();

                if(svalue.indexOf("-") > 0) {
                    String format = dateFormatValue(svalue);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                    try {
                        ndate = simpleDateFormat.parse(svalue);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    ndate = new Date(Long.parseLong(value.toString()));
                }
                return ndate;
            case "timestamp":
                if(value == null || "".equals(value.toString())) {
                    return null;
                }
                return new Timestamp(Long.parseLong(value.toString()));
            default:return value;
        }
    }

    private String dateFormatValue(String value) {
        String[] dateTime = value.split(" ");
        if(dateTime.length == 1) {
            String dt = dateTime[0];
            if(dt.indexOf("-") > 0) {
                String[] dtSp = dt.split("-");
                if(dtSp.length == 2) {
                    return "yyyy-MM";
                }else {
                    return "yyyy-MM-dd";
                }
            } else {
                String[] tsp = dt.split(":");
                if(tsp.length == 2) {
                    return "HH:mm";
                } else {
                    return "HH:mm:ss";
                }
            }
        } else {
            String tsp = dateTime[1];
            String[] tsps = tsp.split(":");
            if(tsps.length == 2) {
                return "yyyy-MM-dd HH:mm";
            } else {
                return "yyyy-MM-dd HH:mm:ss";
            }
        }
    }

    /**
     * 获取方法参数的类型
     * @param clazzName
     * @return
     */
    private Class getMethodParameterClazz(String clazzName) {
        clazzName = clazzName.toLowerCase();
        if(clazzName.equals("string")) {
            return String.class;
        } else if(clazzName.equals("long")) {
            return long.class;
        } else if(clazzName.equals("int")) {
            return int.class;
        } else if(clazzName.equals("double")) {
            return double.class;
        } else if(clazzName.equals("float")) {
            return float.class;
        } else if(clazzName.equals("short")) {
            return short.class;
        } else if(clazzName.equals("byte")) {
            return byte.class;
        } else if(clazzName.equals("bigdecimal")) {
            return BigDecimal.class;
        } else if(clazzName.equals("biginteger")) {
            return BigInteger.class;
        } else if(clazzName.equals("date")) {
            return Date.class;
        } else if(clazzName.equals("timestamp")) {
            return Timestamp.class;
        } else if(clazzName.equals("char")) {
            return char.class;
        }
        return null;
    }

    /**
     * 列名转为方法驼峰名
     * @param colName
     * @return
     */
    private String transferMethod(String colName) {
        String[] cns = colName.split("_");
        if(cns.length == 1) {
            return "set" + colName.substring(0,1).toUpperCase()+colName.substring(1);
        }
        StringBuilder methodName = new StringBuilder();
        for(String cn : cns) {
            String cname = cn.substring(0,1).toUpperCase()+cn.substring(1);
            methodName.append(cname);
        }
        return "set"+methodName.toString();
    }

    /**
     * 列名转为方法驼峰名
     * @param colName
     * @return
     */
    private String transferFieldName(String colName) {

        return "set" + colName.substring(0,1).toUpperCase()+colName.substring(1);


    }

    public Object convertToEntity(Class clazz,Map<String, Object> getFieldMap) throws IllegalArgumentException{


        Object object = newInstanceObject(clazz);
        if(object == null) {
            LOGGER.error("can't new instance object: " + clazz);
            return object;
        }

        for(Map.Entry<String,Object> entry : getFieldMap.entrySet()) {
            try {
                reflectT(object, clazz, entry.getValue(), entry.getKey());
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + " " + entry.getKey() + " = " + entry.getValue(), e);
            }
        }
        return object;
    }
}
