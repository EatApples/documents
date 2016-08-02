package mongo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import helpers.IOHelper;
import helpers.JSONHelper;



public class MongoClusterGeneratorSecurity {

    private static String demoPath = IOHelper.getCurrentPath() + "/clusterDemo/";
    private static String shPath = demoPath + "security/";
    private static String global = demoPath + "global.json";

    private static String shardJson = demoPath + "shard.json";
    private static String configServerJson = demoPath + "configServer.json";
    private static String mongosJson = demoPath + "mongos.json";

    private static String shardConf = demoPath + "shard.conf";
    private static String configServerConf = demoPath + "configServer.conf";
    private static String mongosConf = demoPath + "mongos.conf";

    static List<String> shardPorts = new ArrayList<String>();
    static List<String> rsnames = new ArrayList<String>();
    static List<String> shardDB = new ArrayList<String>();
    static List<String> shardLog = new ArrayList<String>();

    static List<String> ips = new ArrayList<String>();
    static List<String> mongosPorts = new ArrayList<String>();
    static List<String> mongosLog = new ArrayList<String>();

    static List<String> configPorts = new ArrayList<String>();
    static List<String> configDB = new ArrayList<String>();
    static List<String> configLog = new ArrayList<String>();

    static String filePath = "/home/work/mongo/mongoFilePath/";
    static String mongoPath = "/home/work/mongo/bin/";
    static String keyString = " --keyFile ../key/clusterKey";
    static {

        File fp = new File(shPath);
        if (!fp.exists()) {
            fp.mkdir();
        }

        getGlobal(global);

        getDBPath(shardJson, shardDB);
        getLogPath(shardJson, shardLog);
        getRs(shardJson, rsnames);
        getPorts(shardJson, shardPorts);

        getIps(mongosJson, ips);
        getLogPath(mongosJson, mongosLog);
        getPorts(mongosJson, mongosPorts);

        getDBPath(configServerJson, configDB);
        getLogPath(configServerJson, configLog);
        getPorts(configServerJson, configPorts);

    }

    public static void main(String[] args) {

        mkdir();
        key();
        shard();
        config();
        mongos();
        addShard();
        startShard();
        close();
    }

    public static void key() {

        String path = shPath + "key/";
        File fp = new File(path);
        if (!fp.exists()) {
            fp.mkdir();
        }
        StringBuffer writer = new StringBuffer();
        String sh = path + "key.sh";
        writer.append("#!/bin/sh\n");

        writer.append("echo \"=======================keyFile==================================\"\n");
        writer.append("echo 'This is key' >clusterKey\n");
        writer.append("sudo chmod 600 clusterKey\n");

        System.out.println(writer.toString());
        try {
            fileWrite(writer, sh);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void shard() {

        String path = shPath + "shard/";
        File fp = new File(path);
        if (!fp.exists()) {
            fp.mkdir();
        }
        StringBuffer writer = new StringBuffer();
        for (int i = 0; i < rsnames.size(); i++) {
            String file = path + rsnames.get(i) + ".conf";
            writer = new StringBuffer();
            String conf = getData(shardConf);
            conf = conf.replace("#logpath#", filePath + shardLog.get(i));
            conf = conf.replace("#dbpath#", filePath + shardDB.get(i));
            conf = conf.replace("#port#", shardPorts.get(i));
            conf = conf.replace("#rsname#", rsnames.get(i));
            writer.append(conf);

            System.out.println(writer.toString());
            try {
                fileWrite(writer, file);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String sh = path + "shard.sh";
        writer = new StringBuffer();
        writer.append("#!/bin/sh\n");
        for (int i = 0; i < rsnames.size(); i++) {
            writer.append("echo \"=======================" + rsnames.get(i) + "==================================\"\n");

            writer.append(mongoPath + "mongod --config " + "./" + rsnames.get(i) + ".conf" + keyString + "\n");
            writer.append("sleep 2 \n");
        }
        System.out.println(writer.toString());
        try {
            fileWrite(writer, sh);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void config() {

        String path = shPath + "config/";
        File fp = new File(path);
        if (!fp.exists()) {
            fp.mkdir();
        }
        StringBuffer writer = new StringBuffer();
        for (int i = 0; i < configPorts.size(); i++) {
            String file = path + "configServer" + i + ".conf";
            writer = new StringBuffer();
            String conf = getData(configServerConf);
            conf = conf.replace("#logpath#", filePath + configLog.get(i));
            conf = conf.replace("#dbpath#", filePath + configDB.get(i));
            conf = conf.replace("#port#", configPorts.get(i));
            writer.append(conf);

            System.out.println(writer.toString());
            try {
                fileWrite(writer, file);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String sh = path + "configServer.sh";
        writer = new StringBuffer();
        writer.append("#!/bin/sh\n");
        for (int i = 0; i < configPorts.size(); i++) {
            writer.append("echo \"=======================configServer" + i + "==================================\"\n");

            writer.append(mongoPath + "mongod --config " + "./" + "configServer" + i + ".conf" + keyString + "\n");
            writer.append("sleep 2 \n");
        }
        System.out.println(writer.toString());
        try {
            fileWrite(writer, sh);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void mongos() {

        String path = shPath + "mongos/";
        File fp = new File(path);
        if (!fp.exists()) {
            fp.mkdir();
        }
        StringBuffer configDB = new StringBuffer();
        for (int i = 0; i < ips.size(); i++)
            for (int j = 0; j < configPorts.size(); j++) {
                configDB.append(ips.get(i) + ":" + configPorts.get(j));
                if (((i + 1) != ips.size()) || ((j + 1) != configPorts.size()))
                    configDB.append(",");
            }
        StringBuffer writer = new StringBuffer();
        for (int i = 0; i < mongosPorts.size(); i++) {
            String file = path + "mongos" + i + ".conf";
            writer = new StringBuffer();
            String conf = getData(mongosConf);
            conf = conf.replace("#logpath#", filePath + mongosLog.get(i));
            conf = conf.replace("#port#", mongosPorts.get(i));
            conf = conf.replace("#configDB#", configDB);
            writer.append(conf);

            System.out.println(writer.toString());
            try {
                fileWrite(writer, file);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String sh = path + "mongos.sh";
        writer = new StringBuffer();
        writer.append("#!/bin/sh\n");
        for (int i = 0; i < mongosPorts.size(); i++) {
            writer.append("echo \"=======================mongos" + i + "==================================\"\n");

            writer.append(mongoPath + "mongos --fork --config " + "./" + "mongos" + i + ".conf" + keyString + "\n");
            writer.append("sleep 2 \n");
        }
        System.out.println(writer.toString());
        try {
            fileWrite(writer, sh);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void startShard() {

        String path = shPath + "startShard/";
        File fp = new File(path);
        if (!fp.exists()) {
            fp.mkdir();
        }
        String file = path + "startShard.js";
        StringBuffer writer = new StringBuffer();
        for (int i = 0; i < rsnames.size(); i++) {
            writer.append("var " + rsnames.get(i) + "=db.runCommand({ addshard:\"" + rsnames.get(i) + "/");
            for (int j = 0; j < ips.size(); j++) {
                writer.append(ips.get(j) + ":" + shardPorts.get(i));
                if (j != (ips.size() - 1))
                    writer.append(",");
            }
            writer.append("\"});\n");

            writer.append("printjson(" + rsnames.get(i) + ");\n");
        }
        System.out.println(writer.toString());
        try {
            fileWrite(writer, file);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String sh = path + "startShard.sh";
        writer = new StringBuffer();
        writer.append("#!/bin/sh\n");
        writer.append("echo \"=======================startShard==================================\"\n");

        writer.append(mongoPath + "mongo --port " + mongosPorts.get(0) + " admin " + "./" + "startShard.js");
        writer.append("\n");

        System.out.println(writer.toString());
        try {
            fileWrite(writer, sh);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unchecked")
    public static void addShard() {

        for (int i = 0; i < rsnames.size(); i++) {
            String path = shPath + "node_" + rsnames.get(i) + "/";
            File fp = new File(path);
            if (!fp.exists()) {
                fp.mkdir();
            }
            String file = path + rsnames.get(i) + ".js";
            StringBuffer writer = new StringBuffer();
            writer.append("config = {_id: \"" + rsnames.get(i) + "\", members: [\n");

            for (int j = 0; j < ips.size(); j++) {

                writer.append("{_id: " + j + ", host: \"" + ips.get(j) + ":" + shardPorts.get(i) + "\"}");
                if (j != (ips.size() - 1))
                    writer.append(",");
                writer.append("\n");
            }
            writer.append("]};\n");
            writer.append("rs.initiate(config);\n");
            writer.append("var x=rs.status();\n");
            writer.append("printjson(x);\n");
            writer.append("print(\"" + rsnames.get(i) + " OK\");\n");
            System.out.println(writer.toString());
            try {
                fileWrite(writer, file);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String sh = path + rsnames.get(i) + ".sh";
            writer = new StringBuffer();
            writer.append("#!/bin/sh\n");
            writer.append("echo \"=======================" + rsnames.get(i) + "==================================\"\n");

            writer.append(mongoPath + "mongo --port " + shardPorts.get(i) + " admin " + "./" + rsnames.get(i) + ".js");
            writer.append("\n");
            System.out.println(writer.toString());
            try {
                fileWrite(writer, sh);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public static void close() {

        String path = shPath + "stop/";
        File fp = new File(path);
        if (!fp.exists()) {
            fp.mkdir();
        }
        String file = path + "close.sh";
        StringBuffer writer = new StringBuffer();
        writer.append("#!/bin/sh\n");
        writer.append("echo \"================closeCluster==================\"\n");
        List<String> ports = new ArrayList<String>();

        ports.addAll(shardPorts);
        ports.addAll(configPorts);
        ports.addAll(mongosPorts);

        for (int i = 0; i < ports.size(); i++) {
            writer.append(mongoPath + "mongo admin --port " + ports.get(i)
                    + " --eval \"db.shutdownServer({force:true})\";\n");
        }
        writer.append("ps -ef | grep mongo\n");

        try {
            fileWrite(writer, file);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(writer.toString());
    }

    public static void mkdir() {

        String path = shPath + "start/";
        File fp = new File(path);
        if (!fp.exists()) {
            fp.mkdir();
        }
        String file = path + "mkdir.sh";
        StringBuffer writer = new StringBuffer();
        writer.append("#!/bin/sh\n");
        writer.append("echo \"================mkdir==================\"\n");
        List<String> filePaths = new ArrayList<String>();

        filePaths.addAll(shardDB);
        filePaths.addAll(shardLog);
        filePaths.addAll(configDB);
        filePaths.addAll(configLog);
        filePaths.addAll(mongosLog);

        for (int i = 0; i < filePaths.size(); i++) {
            writer.append("mkdir -p " + filePath + filePaths.get(i) + "\n");
        }
        try {
            fileWrite(writer, file);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(writer.toString());
        file = path + "README.txt";
        writer = new StringBuffer();
        writer.append(getData(demoPath + "README.txt"));
        try {
            fileWrite(writer, file);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(writer.toString());

    }

    @SuppressWarnings("rawtypes")
    private static void getGlobal(String jsonfile) {

        String str = getData(jsonfile);
        List<Map> map = JSONHelper.toObjectArray(str, Map.class);

        for (Map temp : map) {
            mongoPath = (String) temp.get("mongoPath");
            filePath = (String) temp.get("filePath");
        }
    }

    @SuppressWarnings("rawtypes")
    private static void getPorts(String jsonfile, List<String> ports) {

        String str = getData(jsonfile);
        List<Map> shard = JSONHelper.toObjectArray(str, Map.class);
        String port = null;
        for (Map temp : shard) {
            port = (String) temp.get("port");
            if (port != null)
                ports.add(port);
        }
    }

    @SuppressWarnings("rawtypes")
    private static void getRs(String jsonfile, List<String> rsname) {

        String str = getData(jsonfile);
        List<Map> map = JSONHelper.toObjectArray(str, Map.class);
        String rs = null;
        for (Map temp : map) {
            rs = (String) temp.get("rsname");
            if (rs != null)
                rsname.add(rs);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void getIps(String jsonfile, List<String> ips) {

        String ipstr = getData(jsonfile);
        List<Map> map = JSONHelper.toObjectArray(ipstr, Map.class);
        for (Map temp : map) {
            Object ip = temp.get("ip");
            if (ip != null) {
                if (ip instanceof List) {
                    List<String> iparray = (List<String>) ip;
                    for (String x : iparray) {
                        if (!ips.contains(x))
                            ips.add(x);
                    }
                }
            }

        }
        System.out.println(ips.toString());

    }

    @SuppressWarnings("rawtypes")
    private static void getDBPath(String jsonfile, List<String> filePaths) {

        String str = getData(jsonfile);
        List<Map> map = JSONHelper.toObjectArray(str, Map.class);
        String path = null;
        for (Map temp : map) {
            path = (String) temp.get("dbpath");
            if (path != null)
                filePaths.add(path);
        }
    }

    @SuppressWarnings("rawtypes")
    private static void getLogPath(String jsonfile, List<String> filePaths) {

        String str = getData(jsonfile);
        List<Map> map = JSONHelper.toObjectArray(str, Map.class);
        String path = null;
        for (Map temp : map) {
            path = (String) temp.get("logpath");
            if (path != null)
                filePaths.add(path);
        }
    }

    private static String getData(String fireDir) {

        StringBuffer buffer = new StringBuffer();

        try {

            File file = new File(fireDir);
            BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(file));

            String tempString = null;
            while ((tempString = reader.readLine()) != null) {

                buffer.append(tempString + "\n");
            }
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String rawData = buffer.toString();

        return rawData;
    }

    private static void fileWrite(StringBuffer sb, String fileName) throws IOException {

        File file = new File(fileName);
        if (!file.exists())
            file.createNewFile();
        FileOutputStream out = new FileOutputStream(file, false);
        sb.append("\n");
        out.write(sb.toString().getBytes("utf-8"));

        out.close();
    }
}