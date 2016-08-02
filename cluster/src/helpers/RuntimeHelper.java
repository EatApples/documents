package helpers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class RuntimeHelper {

    private static class PSWorker extends Thread {

        private final Process process;
        public Integer exit;

        private PSWorker(Process process) {
            this.process = process;
        }

        public void run() {

            try {
                exit = process.waitFor();
            }
            catch (InterruptedException ignore) {
                return;
            }
        }
    }

    /**
     * 自动生成并执行脚本，bat或shell
     * 
     * @param cmd
     * @param shellParentPath
     * @return
     * @throws Exception
     */
    public static String exeShell(String cmd, String shellParentPath) throws Exception {

        String fileExt = ".bat";

        boolean isWindows = JVMToolHelper.isWindows();

        String exePrefix = "";

        if (!isWindows) {
            fileExt = ".sh";
            exePrefix = "sh ";
        }

        String fileName = "s" + cmd.hashCode() + ".uav" + fileExt;

        String filePath = shellParentPath + "/" + fileName;

        if (!IOHelper.exists(filePath)) {

            if (!IOHelper.exists(shellParentPath)) {
                IOHelper.createFolder(shellParentPath);
            }

            StringBuilder sb = new StringBuilder();

            if (!isWindows) {
                sb.append("#!/bin/bash" + System.lineSeparator());
            }

            sb.append(cmd);

            String data = sb.toString();

            IOHelper.writeTxtFile(filePath, data, "utf-8", false);

            if (!isWindows) {
                // we need get the execute auth
                RuntimeHelper.exec("sh -c \"chmod u+x " + filePath + "\"");
            }
        }

        String output = exec(exePrefix + filePath);

        return output;
    }

    /**
     * execute cmd / shell command
     * 
     * @param cmd
     * @return
     * @throws Exception
     */
    public static String exec(String cmd) throws Exception {

        return exec(5000, cmd);
    }

    public static String exec(long timeout, String... cmd) throws Exception {

        if (null == cmd) {
            return "";
        }

        StringBuilder output = new StringBuilder();
        BufferedReader br = null;
        BufferedReader ebr = null;
        InputStream in = null;
        InputStream ein = null;
        try {
            Process ps = null;
            if (cmd.length == 1) {
                ps = Runtime.getRuntime().exec(cmd[0]);
            }
            else {
                ps = Runtime.getRuntime().exec(cmd);
            }
            in = ps.getInputStream();
            ein = ps.getErrorStream();
            br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line + "\n");
            }

            ebr = new BufferedReader(new InputStreamReader(ein));

            while ((line = ebr.readLine()) != null) {
                output.append(line + "\n");
            }

            PSWorker worker = new PSWorker(ps);
            worker.start();
            worker.join(timeout);

            if (worker.exit == null) {
                throw new TimeoutException();
            }

        }
        finally {
            if (br != null) {
                br.close();
            }
            if (ebr != null) {
                ebr.close();
            }
        }
        return output.toString();
    }
}
