package helpers;

import java.util.Properties;

public class JVMAgentInfo {

    private Properties agentProperties;
    private Properties systemProperties;
    private String id;

    public JVMAgentInfo(String id, Properties agentProperties, Properties systemProperties) {
        this.id = id;
        this.agentProperties = agentProperties;
        this.systemProperties = systemProperties;
    }

    public Properties getAgentProperties() {

        return agentProperties;
    }

    public void setAgentProperties(Properties agentProperties) {

        this.agentProperties = agentProperties;
    }

    public Properties getSystemProperties() {

        return systemProperties;
    }

    public void setSystemProperties(Properties systemProperties) {

        this.systemProperties = systemProperties;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

}
