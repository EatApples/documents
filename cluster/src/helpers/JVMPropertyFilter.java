package helpers;

import java.util.Properties;

public interface JVMPropertyFilter {

    public boolean isMatchAgentProperties(Properties jvmProperties);

    public boolean isMatchSystemProperties(Properties systemProperties);
}
