package helpers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


public class JVMToolHelper {

    public static final String osname = System.getProperty("os.name").toLowerCase();
    public static final String JMX_CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

    private static ClassLoader JVMToolClassloader = null;
    private static final Object lock = new Object();
    private static Class<?> virtualMachine;
    private static Class<?> virtualMachineDescriptor;
    private static Method method_VMList;
    private static Method method_AttachToVM;
    private static Method method_GetAgentProperties;
    private static Method method_VMId;
    private static Method method_GetSystemProperties;
    private static Method method_LoadAgent;

    private static Class<?> hostIdentifierClass;
    private static Class<?> monitoredVmClass;
    private static Class<?> vmIdentifierClass;

    private JVMToolHelper() {

    }

    public static String getJVMVendor() {

        return System.getProperty("java.vm.specification.vendor");
    }

    public static boolean isOracleJVM() {

        return "Sun Microsystems Inc.".equals(getJVMVendor()) || getJVMVendor().startsWith("Oracle");
    }

    public static boolean isVMAlive(String procId) {

        if (procId == null || "".equals(procId)) {
            return false;
        }

        initJVMToolJarClassLoader();

        try {
            Object vm = method_AttachToVM.invoke(null, procId);
            if (vm != null) {
                return true;
            }
        }
        catch (Exception e) {
            // ignore
        }

        return false;
    }

    public static JVMAgentInfo getLocalJvmInfo(String procId, boolean needLocalAttachSupport) {

        if (procId == null || "".equals(procId)) {
            return null;
        }

        initJVMToolJarClassLoader();

        try {
            Object vm = method_AttachToVM.invoke(null, procId);
            if (vm != null) {

                if (needLocalAttachSupport) {
                    startVMAgent(vm);
                }

                Properties jvmProperties = (Properties) method_GetAgentProperties.invoke(vm, (Object[]) null);
                // system properties
                Properties systemProperties = (Properties) method_GetSystemProperties.invoke(vm, (Object[]) null);

                return new JVMAgentInfo(procId, jvmProperties, systemProperties);
            }
        }
        catch (Exception e) {
            // ignore
        }

        return null;
    }

    public static List<JVMAgentInfo> getAllLocalJvmInfo(JVMPropertyFilter filter, boolean needLocalAttachSupport) {

        if (!isOracleJVM()) {
            return Collections.emptyList();
        }

        List<JVMAgentInfo> jvmPropertiesList = new ArrayList<JVMAgentInfo>();

        try {
            initJVMToolJarClassLoader();

            @SuppressWarnings("rawtypes")
            List allVMs = (List) method_VMList.invoke(null, (Object[]) null);

            getAllVMsInfo(filter, needLocalAttachSupport, jvmPropertiesList, allVMs);

        }
        catch (Exception e) {
            // ignore
        }

        return jvmPropertiesList;
    }

    /**
     * @param filter
     * @param needLocalAttachSupport
     * @param jvmPropertiesList
     * @param allVMs
     */
    @SuppressWarnings("rawtypes")
    private static void getAllVMsInfo(JVMPropertyFilter filter, boolean needLocalAttachSupport,
            List<JVMAgentInfo> jvmPropertiesList, List allVMs) {

        for (Object vmInstance : allVMs) {

            /**
             * now we only support 64bit JVM, in case the 32bit JVM is attached with exception
             */
            try {
                String id = (String) method_VMId.invoke(vmInstance, (Object[]) null);

                Object vm = method_AttachToVM.invoke(null, id);

                // agent properties
                Properties jvmProperties = (Properties) method_GetAgentProperties.invoke(vm, (Object[]) null);

                // system properties
                Properties systemProperties = (Properties) method_GetSystemProperties.invoke(vm, (Object[]) null);

                if (jvmProperties != null) {

                    if (filter != null && (!filter.isMatchAgentProperties(jvmProperties)
                            || !filter.isMatchSystemProperties(systemProperties))) {
                        continue;
                    }

                    jvmProperties = fillJVMProperties(needLocalAttachSupport, vm, jvmProperties);

                    jvmPropertiesList.add(new JVMAgentInfo(id, jvmProperties, systemProperties));
                }
            }
            catch (Exception e) {
                // ignore
                continue;
            }
        }
    }

    /**
     * @param needLocalAttachSupport
     * @param vm
     * @param jvmProperties
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private static Properties fillJVMProperties(boolean needLocalAttachSupport, Object vm, Properties jvmProperties)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        if (needLocalAttachSupport) {
            startVMAgent(vm);
            jvmProperties = (Properties) method_GetAgentProperties.invoke(vm, (Object[]) null);
        }
        return jvmProperties;
    }

    private static void startVMAgent(Object vm) {

        try {
            Properties systemProperties = (Properties) method_GetSystemProperties.invoke(vm, (Object[]) null);

            String agent = systemProperties.getProperty("java.home") + File.separator + "lib" + File.separator
                    + "management-agent.jar";
            method_LoadAgent.invoke(vm, new Object[] { agent });
        }
        catch (Exception e) {
            // ignore
        }
    }

    private static void initJVMToolJarClassLoader() {

        String javaHome = System.getProperty("java.home");

        String tools = javaHome + File.separator + ".." + File.separator + "lib" + File.separator + "tools.jar";

        if (JVMToolClassloader == null) {
            synchronized (lock) {
                if (JVMToolClassloader == null) {
                    try {
                        JVMToolClassloader = new URLClassLoader(new URL[] { new File(tools).toURI().toURL() });

                        // virtual machine
                        virtualMachine = JVMToolClassloader.loadClass("com.sun.tools.attach.VirtualMachine");
                        virtualMachineDescriptor = JVMToolClassloader
                                .loadClass("com.sun.tools.attach.VirtualMachineDescriptor");

                        method_VMList = virtualMachine.getMethod("list", (Class[]) null);
                        method_AttachToVM = virtualMachine.getMethod("attach", String.class);
                        method_GetAgentProperties = virtualMachine.getMethod("getAgentProperties", (Class[]) null);
                        method_VMId = virtualMachineDescriptor.getMethod("id", (Class[]) null);
                        method_GetSystemProperties = virtualMachine.getMethod("getSystemProperties", (Class[]) null);
                        method_LoadAgent = virtualMachine.getMethod("loadAgent", new Class[] { String.class });

                        // java process
                        hostIdentifierClass = JVMToolClassloader.loadClass("sun.jvmstat.monitor.HostIdentifier");
                        monitoredVmClass = JVMToolClassloader.loadClass("sun.jvmstat.monitor.MonitoredVm");
                        vmIdentifierClass = JVMToolClassloader.loadClass("sun.jvmstat.monitor.VmIdentifier");

                    }
                    catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
    }

    public static List<Map<String, String>> getAllJVMProcesses(String host) {

        initJVMToolJarClassLoader();

        // HostIdentifier localHostIdentifier = new HostIdentifier(host);
        Object localHostIdentifier = ReflectHelper.newInstance("sun.jvmstat.monitor.HostIdentifier",
                new Class[] { String.class }, new Object[] { host }, JVMToolClassloader);

        // localObject1 = MonitoredHost.getMonitoredHost(localHostIdentifier);
        Object localObject1 = ReflectHelper.invokeStatic("sun.jvmstat.monitor.MonitoredHost", "getMonitoredHost",
                new Class<?>[] { hostIdentifierClass }, new Object[] { localHostIdentifier }, JVMToolClassloader);

        // Set<Integer> localSet = ((MonitoredHost)localObject1).activeVms();
        @SuppressWarnings("unchecked")
        Set<Integer> localSet = (Set<Integer>) ReflectHelper.invoke("sun.jvmstat.monitor.MonitoredHost", localObject1,
                "activeVms", new Class[] {}, new Object[] {}, JVMToolClassloader);

        List<Map<String, String>> vms = new ArrayList<Map<String, String>>();

        for (Integer localIterator : localSet) {

            int pid = localIterator.intValue();

            String str1 = "//" + pid + "?mode=r";

            try {

                // VmIdentifier localVmIdentifier = new VmIdentifier(str1);
                Object localVmIdentifier = ReflectHelper.newInstance("sun.jvmstat.monitor.VmIdentifier",
                        new Class[] { String.class }, new Object[] { str1 }, JVMToolClassloader);
                // Object localMonitoredVm =
                // ((MonitoredHost)localObject1).getMonitoredVm(localVmIdentifier,
                // 0);
                Object localMonitoredVm = ReflectHelper.invoke("sun.jvmstat.monitor.MonitoredHost", localObject1,
                        "getMonitoredVm", new Class<?>[] { vmIdentifierClass }, new Object[] { localVmIdentifier },
                        JVMToolClassloader);

                // String mainClass=
                // MonitoredVmUtil.mainClass(localMonitoredVm,true);
                String mainClass = (String) ReflectHelper.invokeStatic("sun.jvmstat.monitor.MonitoredVmUtil",
                        "mainClass", new Class<?>[] { monitoredVmClass, boolean.class },
                        new Object[] { localMonitoredVm, true }, JVMToolClassloader);
                // String mainArgs= MonitoredVmUtil.mainArgs(localMonitoredVm);
                String mainArgs = (String) ReflectHelper.invokeStatic("sun.jvmstat.monitor.MonitoredVmUtil", "mainArgs",
                        new Class<?>[] { monitoredVmClass }, new Object[] { localMonitoredVm }, JVMToolClassloader);
                // str3 = MonitoredVmUtil.jvmArgs(localMonitoredVm);
                String jvmArgs = (String) ReflectHelper.invokeStatic("sun.jvmstat.monitor.MonitoredVmUtil", "jvmArgs",
                        new Class<?>[] { monitoredVmClass }, new Object[] { localMonitoredVm }, JVMToolClassloader);
                // str3 = MonitoredVmUtil.jvmFlags(localMonitoredVm);
                String jvmFlags = (String) ReflectHelper.invokeStatic("sun.jvmstat.monitor.MonitoredVmUtil", "jvmFlags",
                        new Class<?>[] { monitoredVmClass }, new Object[] { localMonitoredVm }, JVMToolClassloader);

                Map<String, String> map = new LinkedHashMap<String, String>();

                map.put("pid", localIterator.toString());
                map.put("main", mainClass);
                map.put("margs", mainArgs);
                map.put("jargs", jvmArgs);
                map.put("jflags", jvmFlags);

                vms.add(map);

            }
            catch (Exception e) {
                // ignore
            }
        }

        return vms;
    }

    /**
     * isWindows
     * 
     * @return
     */
    public static boolean isWindows() {

        return (osname.indexOf("win") > -1) ? true : false;
    }

    /**
     * getLineSeperator
     * 
     * @return
     */
    public static String getLineSeperator() {

        String marker = "\r\n";
        if (!isWindows()) {
            marker = "\n";
        }

        return marker;
    }

}
