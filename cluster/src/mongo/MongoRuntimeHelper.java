package mongo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import helpers.IOHelper;
import helpers.JSONHelper;
import helpers.RuntimeHelper;



public class MongoRuntimeHelper {

    private static String jspath = IOHelper.getCurrentPath() + "/js/";
    private static String mongopath = "D:/MongoDB/bin/";
    private static int port = 27017;
    private static List<File> origin = IOHelper.getFiles(jspath);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static String runMongoJs(String jsonStr) throws Exception {

        Map map = JSONHelper.toObject(jsonStr, Map.class);
        String res = "ERROR";
        List<Object> args = new ArrayList();
        String dbname = null;
        String jsname = null;

        for (Object key : map.keySet()) {
        	
            if (key.toString().equals("dbname")) {
                dbname = (String) map.get(key);
            }
            else if (key.toString().equals("cmd")) {
                jsname = (String) map.get(key);
            }
            else if (key.toString().equals("args")) {
                Object value = map.get("args");
                if (value instanceof List) {
                    args.addAll((List<Object>) value);
                }
                else {
                    return new String("args error, check args : " + value);
                }
            }
            else {
                return new String("input error, check input : " + jsonStr);
            }
        }

        String JS = jspath + jsname + ".js";
        if (!origin.contains(new File(JS))) {
            return new String("cmd error, check cmd: " + jsname);
        }
        String jsContent = new String(IOHelper.readFile(JS));

        for (int i = 0; i < args.size(); i++) {
            jsContent = jsContent.replace("#args" + i + "#", args.get(i).toString());
        }
        String tempJS = jspath + +System.currentTimeMillis() + "_temp.js";

        IOHelper.writeFile(tempJS, jsContent.getBytes(), false);

        String cmd = mongopath + "mongo --port " + port + " " + dbname + " " + tempJS;

        res = RuntimeHelper.exeShell(cmd, jspath);

        IOHelper.deleteFile(tempJS);
        return res;

    }

    public static void main(String[] args) throws Exception {

        String cmd = new String(IOHelper.readFile(jspath + "cmd.json"));
        System.out.println(runMongoJs(cmd));

    }
}
