package hudson.plugins.setenv;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.StringReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Descriptor;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.plugins.setenv.Messages;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

/**
 * This plugin will set user-configured environment variables. 
 *
 * @author Takeshi Wakasugi
 */
public class SetEnvBuildWrapper extends BuildWrapper {
    private String localVarText;

    @DataBoundConstructor
    public SetEnvBuildWrapper(String localVarText){
        this.localVarText = localVarText;
    }
    
    public String getLocalVarText() {
        return localVarText;
    }
    
    public void setLocalVarText(String localVarText) {
        this.localVarText = localVarText;
    }
    
    public Map<String,String> getLocalVarMap() {
        Map<String,String> map = new HashMap<String,String>(0);
        try {
            for (Entry<Object,Object> entry : load(localVarText).entrySet()) {
                map.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        catch (IOException e) {
            // Not sure what to do here?
        }

        return map;
    }

    private Properties load(String properties) throws IOException {
        Properties p = new Properties();
        try {
            p.load(new StringReader(properties));
        } catch (NoSuchMethodError e) {
            // load(Reader) method is only available on JDK6.
            // this fall back version doesn't work correctly with non-ASCII characters,
            // but there's no other easy ways out it seems.
            p.load(new ByteArrayInputStream(properties.getBytes()));
        }
        return p;
    }

    /*    public class EnvironmentImpl extends BuildWrapper.Environment{
        public void buildEnvVars(Map<String, String> env) {
            // To set local-configured value
            Map<String,String> localVarMap = getLocalVarMap();
            if(localVarMap != null)env.putAll(localVarMap);
        }
        }*/

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher,
                             BuildListener listener) throws IOException, InterruptedException {
        class EnvironmentImpl extends BuildWrapper.Environment {
            public void buildEnvVars(Map<String, String> env) {
                // To set local-configured value
                Map<String,String> localVarMap = getLocalVarMap();
                EnvVars.resolve(localVarMap);
                if (localVarMap != null) {
                    env.putAll(localVarMap);
                }
            }
        }
        
        return new EnvironmentImpl();
    }
    
    /**
     * Descriptor for {@link SetEnvBuildWrapper}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     */
    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.SetEnvBuildWrapper_DisplayName(); 
        }

        @Override
        public boolean isApplicable(AbstractProject item) {
            return true;
        }

    }

}
