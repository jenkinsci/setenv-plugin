package hudson.plugins.setenv;

import hudson.Plugin;
import hudson.tasks.BuildWrappers;

/**
 * Entry point of a plugin.
 * 
 * @author Takeshi Wakasugi
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
        // plugins normally extend Hudson by providing custom implementations
        // of 'extension points'. In this example, we'll add one builder.
    	BuildWrappers.WRAPPERS.add(SetEnvBuildWrapper.DESCRIPTOR);
        
    }
}
