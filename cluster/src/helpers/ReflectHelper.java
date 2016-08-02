package helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReflectHelper {

    private ReflectHelper() {

    }

    /**
     * constructor by static method
     * 
     * @param className
     * @param paramTypes
     * @param params
     * @param clsLoaders
     * @return
     */
    public static Object newInstance(String className, String MethodName, Class<?>[] paramTypes, Object[] params,
            ClassLoader... clsLoaders) {

        Object objInst = null;
        try {
            Class<?> capClass = null;

            if (null != clsLoaders && clsLoaders.length > 0) {
                capClass = tryLoadClass(className, clsLoaders);
            }

            if (null == capClass) {
                capClass = tryLoadClassFromThreadClassLoader(className);
            }

            if (capClass == null)
                return null;

            objInst = capClass.getMethod(MethodName, paramTypes).invoke(null, params);
        }
        catch (ClassCastException e) {
            // ignore
        }
        catch (IllegalAccessException e) {
            // ignore
        }
        catch (IllegalArgumentException e) {
            // ignore
        }
        catch (InvocationTargetException e) {
            // ignore
        }
        catch (NoSuchMethodException e) {
            // ignore
        }
        catch (SecurityException e) {
            // ignore
        }
        return objInst;
    }

    /**
     * newInstance which has Construct params
     * 
     * @param className
     * @param clsLoaders
     * @param params
     * @return
     */
    public static Object newInstance(String className, Class<?>[] paramtype, Object[] params,
            ClassLoader... clsLoaders) {

        Object objInst = null;
        try {
            Class<?> capClass = null;

            if (null != clsLoaders && clsLoaders.length > 0) {
                capClass = tryLoadClass(className, clsLoaders);
            }

            if (null == capClass) {
                capClass = tryLoadClassFromThreadClassLoader(className);
            }

            if (capClass == null)
                return null;

            @SuppressWarnings("rawtypes")
            Constructor constructor = capClass.getConstructor(paramtype);

            objInst = constructor.newInstance(params);
        }
        catch (ClassCastException e) {
            // ignore
        }
        catch (InstantiationException e) {
            // ignore
        }
        catch (IllegalAccessException e) {
            // ignore
        }
        catch (NoSuchMethodException e) {
            // ignore
        }
        catch (InvocationTargetException e) {
            // ignore
        }
        return objInst;
    }

    /**
     * newInstance
     * 
     * @param className
     * @param clsLoaders
     * @return
     */
    public static Object newInstance(String className, ClassLoader... clsLoaders) {

        Object objInst = null;
        try {
            Class<?> capClass = null;

            if (null != clsLoaders && clsLoaders.length > 0) {
                capClass = tryLoadClass(className, clsLoaders);
            }

            if (null == capClass) {
                capClass = tryLoadClassFromThreadClassLoader(className);
            }

            if (capClass == null)
                return null;

            objInst = capClass.newInstance();
        }
        catch (ClassCastException e) {
            // ignore
        }
        catch (InstantiationException e) {
            // ignore
        }
        catch (IllegalAccessException e) {
            // ignore
        }
        return objInst;
    }

    /**
     * @param className
     * @param capClass
     * @return
     */
    private static Class<?> tryLoadClassFromThreadClassLoader(String className) {

        Class<?> capClass = null;
        try {
            capClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException e) {
            capClass = reflectLoadClass(className);
        }
        return capClass;
    }

    /**
     * @param className
     * @param capClass
     * @return
     */
    private static Class<?> reflectLoadClass(String className) {

        Class<?> capClass = null;
        try {
            capClass = ReflectHelper.class.getClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException e1) {
            // ignore
        }
        return capClass;
    }

    /**
     * @param className
     * @param capClass
     * @param clsLoaders
     * @return
     */
    private static Class<?> tryLoadClass(String className, ClassLoader... clsLoaders) {

        Class<?> capClass = null;

        if (null == clsLoaders) {
            return null;
        }

        for (ClassLoader cl : clsLoaders) {
            try {
                capClass = cl.loadClass(className);

                if (null != capClass) {
                    break;
                }

            }
            catch (ClassNotFoundException e) {
                // ignore
                continue;
            }
        }
        return capClass;
    }

    /**
     * loops
     * 
     * @param beInvokeObjectRoot
     * @param methodNames
     * @return
     */
    public static Object invokes(Object beInvokeObjectRoot, LinkedList<String> methodNames,
            LinkedList<Class<?>[]> paramTypes, LinkedList<Object[]> params) {

        if (null == beInvokeObjectRoot || null == methodNames) {
            return null;
        }
        if (methodNames.isEmpty()) {
            return beInvokeObjectRoot;
        }

        Class<?>[] firstParamTypes = null;

        Object[] firstParams = null;
        if (null != paramTypes && null != params) {
            firstParamTypes = paramTypes.getFirst();
            firstParams = params.getFirst();
            paramTypes.removeFirst();
            params.removeFirst();
        }
        Object beInvokeObject = invoke(beInvokeObjectRoot.getClass().getName(), beInvokeObjectRoot,
                methodNames.getFirst(), firstParamTypes, firstParams);
        methodNames.removeFirst();

        return invokes(beInvokeObject, methodNames, paramTypes, params);
    }

    public static Object invokeStatic(String clsName, String methodName, Class<?>[] paramTypes, Object[] params,
            ClassLoader... cl) {

        return invoke(clsName, null, methodName, paramTypes, params, cl);
    }

    public static Object invoke(String clsName, Object clsObject, String methodName, Class<?>[] paramTypes,
            Object[] params, ClassLoader... clsLoaders) {

        if (null == clsName || null == methodName) {
            return null;
        }

        Class<?> cls = tryLoadClass(clsName, clsLoaders);

        if (cls == null) {
            try {
                cls = ReflectHelper.class.getClassLoader().loadClass(clsName);
            }
            catch (ClassNotFoundException e) {
                return null;
            }
        }

        Method method = null;
        try {
            method = (null == paramTypes) ? cls.getMethod(methodName) : cls.getMethod(methodName, paramTypes);
        }
        catch (NoSuchMethodException e) {
            // ignore
            return null;
        }
        catch (SecurityException e) {
            // ignore
            return null;
        }

        try {
            if (null != clsObject) {
                return method.invoke(clsObject, params);
            }
            else {
                return method.invoke(cls, params);
            }
        }
        catch (IllegalAccessException e) {
            // ignore
            return null;
        }
        catch (IllegalArgumentException e) {
            // ignore
            return null;
        }
        catch (InvocationTargetException e) {
            // ignore
            return null;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object getAnnotationValue(Class<?> c, Class annoCls, String annoField) {

        Object value = null;
        try {
            if (c.isAnnotationPresent(annoCls)) {
                Annotation anno = c.getAnnotation(annoCls);
                Method mtd = anno.getClass().getDeclaredMethod(annoField);
                value = mtd.invoke(anno);
            }
        }
        catch (Exception e) {
            // ignore
        }

        return value;
    }

    /**
     * @param method
     * @param annocls
     * @param annoField
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object getAnnotationValue(Method method, Class annocls, String annoField) {

        Object value = null;
        Annotation anno = method.getAnnotation(annocls);
        if (anno == null)
            return value;
        try {
            Method mtd = anno.getClass().getDeclaredMethod(annoField);
            value = mtd.invoke(anno);
        }
        catch (Exception e) {
            // ignore
        }

        // if value is ARRAY
        if (value.getClass().isArray()) {

            Object[] vArray = (Object[]) value;

            List<Object> vList = new ArrayList<Object>();

            for (Object vObj : vArray) {

                if (Annotation.class.isAssignableFrom(vObj.getClass())) {

                    Map<String, Object> SubAnnofieldValues = new LinkedHashMap<String, Object>();

                    SubAnnofieldValues = getAllFieldValuesOfAnnotation(SubAnnofieldValues, (Annotation) vObj);

                    vList.add(SubAnnofieldValues);
                }
                else {
                    vList.add(vObj);
                }
            }

            return vList;
        }
        // if value is not ARRAY
        else {

            return value;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<String, Object> getAnnotationAllFieldValues(Class c, Class annoCls) {

        Map<String, Object> fieldValues = new LinkedHashMap<String, Object>();

        Annotation anno = c.getAnnotation(annoCls);
        if (anno == null) {
            return null;
        }

        return getAllFieldValuesOfAnnotation(fieldValues, anno);
    }

    /**
     * 
     * @param method
     * @param annoCls
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<String, Object> getAnnotationAllFieldValues(Method method, Class annoCls) {

        Map<String, Object> fieldValues = new LinkedHashMap<String, Object>();

        Annotation anno = method.getAnnotation(annoCls);
        if (anno == null) {
            return null;
        }

        return getAllFieldValuesOfAnnotation(fieldValues, anno);
    }

    private static Map<String, Object> getAllFieldValuesOfAnnotation(Map<String, Object> fieldValues, Annotation anno) {

        Method[] mtds = anno.getClass().getDeclaredMethods();

        for (Method mtd : mtds) {

            String name = mtd.getName();

            if ("annotationType".equalsIgnoreCase(name) || "hashCode".equalsIgnoreCase(name)
                    || "toString".equalsIgnoreCase(name)) {
                continue;
            }

            Object value = null;
            try {
                value = mtd.invoke(anno);
            }
            catch (Exception e) {
                // ignore
                continue;
            }

            // should consider Class array
            if (value.getClass().isArray()) {

                Object[] vArray = (Object[]) value;

                List<Object> vList = new ArrayList<Object>();

                for (Object vObj : vArray) {

                    if (Annotation.class.isAssignableFrom(vObj.getClass())) {

                        Map<String, Object> SubAnnofieldValues = new LinkedHashMap<String, Object>();

                        SubAnnofieldValues = getAllFieldValuesOfAnnotation(SubAnnofieldValues, (Annotation) vObj);

                        if (null == SubAnnofieldValues || SubAnnofieldValues.size() == 0) {
                            continue;
                        }

                        vList.add(SubAnnofieldValues);
                    }
                    else {
                        vList.add(vObj);
                    }
                }

                if (vList.size() == 0) {
                    continue;
                }

                fieldValues.put(name, vList);
            }
            else {

                fieldValues.put(name, value);
            }
        }

        return fieldValues;
    }
}
