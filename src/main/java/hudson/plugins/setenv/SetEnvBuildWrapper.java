package hudson.plugins.setenv;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;



import org.kohsuke.stapler.StaplerRequest;


import hudson.Launcher;
import hudson.model.Build;
import hudson.model.Descriptor;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.plugins.setenv.Messages;
import hudson.tasks.BuildWrapper;


/**
 * This plugin will set user-configured environment variables. 
 *
 * @author Takeshi Wakasugi
 */
public class SetEnvBuildWrapper extends BuildWrapper {
    /**
     * Descriptor should be singleton.
     */
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	
	public DescriptorImpl getDescriptor() {
		return DESCRIPTOR;
	}

	private String localVarText;
	
	public SetEnvBuildWrapper(String localVarText){
		this.localVarText = localVarText;
	}

	public String getLocalVarText() {
		return localVarText;
	}

	public void setLocalVarText(String localVarText) {
		this.localVarText = localVarText;
	}

	public Map<String,String> getGlobalVarMap(){
		return getVarMap(getDescriptor().getGlobalVarText());
	}

	public Map<String,String> getLocalVarMap(){
		return getVarMap(localVarText);
	}

	private Map<String,String> getVarMap(String varText){
		Map<String,String> map = new HashMap<String,String>(0);
		if(varText == null)return null;
		String[] lines = varText.split("\\r?\\n");
		for(String line : lines){
			String[] words = line.split("=");
			if(words.length >= 2){
				// NOTE: words[2..n] will be ignored.
				if(words[0] != null && words[1] != null)
					map.put(words[0].trim(), words[1].trim());
			}
		}
		return map;
	}

	private void expandVarMap(Map<String,String> varMap){
		Iterator<Entry<String,String>> iter = varMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String,String>entry = iter.next(); 
			entry.setValue(expandVariable(entry.getValue(), varMap));
		}
	}
	
	private String expandVariable(String value, Map<String,String> varMap){
		int beginPoint = value.indexOf("${");
		if(beginPoint == -1)return value;
		int endPoint = value.indexOf("}", beginPoint);
		if(endPoint == -1)return value;
		String expandVarName = value.substring(beginPoint+2, endPoint);
		String expandVarValue = varMap.get(expandVarName);
		if(expandVarValue==null)expandVarValue="";
		value = value.substring(0, beginPoint) + expandVarValue + value.substring(endPoint +1);
		return expandVariable(value, varMap);
	}
	
	

	public class Environment extends BuildWrapper.Environment{
		public void buildEnvVars(Map<String, String> env) {
			// To set global-configured value
			Map<String,String> globalVarMap = getGlobalVarMap();
			if(globalVarMap != null)env.putAll(globalVarMap);
			// To set local-configured value
			Map<String,String> localVarMap = getLocalVarMap();
			if(localVarMap != null)env.putAll(localVarMap);
			expandVarMap(env);
		}
	}

	public Environment setUp(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {
		Environment env = new Environment();
		return env;
	}

	public Environment setUp(Build build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {
		return setUp((AbstractBuild)build,launcher,listener);
	}
	
	/**
     * Descriptor for {@link SetEnvBuildWrapper}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     */
    public static final class DescriptorImpl extends Descriptor<BuildWrapper> {
    	private String globalVarText;

        DescriptorImpl() {
            super(SetEnvBuildWrapper.class);
            load();
        }

        public String getDisplayName() {
            return Messages.SetEnvBuildWrapper_DisplayName(); 
        }

        public boolean configure(StaplerRequest req) throws FormException {
        	globalVarText = req.getParameter("setenv.global_var_text"); 
        	save();
            return super.configure(req);
        }

        public SetEnvBuildWrapper newInstance(StaplerRequest req) throws FormException {
            return new SetEnvBuildWrapper(req.getParameter("setenv.local_var_text"));
        }

		public String getGlobalVarText() {
			return globalVarText;
		}

		public void setGlobalVarText(String globalVarText) {
			this.globalVarText = globalVarText;
		}
    }

}
