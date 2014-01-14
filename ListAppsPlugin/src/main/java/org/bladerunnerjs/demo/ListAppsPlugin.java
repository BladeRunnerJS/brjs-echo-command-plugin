package org.bladerunnerjs.demo;

import java.util.List;

import org.bladerunnerjs.logging.Logger;
import org.bladerunnerjs.logging.LoggerType;
import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.exception.command.CommandArgumentsException;
import org.bladerunnerjs.model.exception.command.CommandOperationException;
import org.bladerunnerjs.plugin.utility.command.ArgsParsingCommandPlugin;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

public class ListAppsPlugin extends ArgsParsingCommandPlugin {

	private BRJS brjs;
	private Logger logger;

	@Override
	public String getCommandName() {
		return "list-apps";
	}

	@Override
	public String getCommandDescription() {
		return "List all applications associated with a BRJS install";
	}

	@Override
	public void setBRJS(BRJS brjs) {
		this.brjs = brjs;
		this.logger = brjs.logger(LoggerType.COMMAND, ListAppsPlugin.class);
	}

	@Override
	protected void configureArgsParser(JSAP argsParser) throws JSAPException {
		argsParser.registerParameter(
				new Switch("system-apps").setShortFlag('s')
				);
	}

	@Override
	protected void doCommand(JSAPResult parsedArgs)
			throws CommandArgumentsException, CommandOperationException {
		listApps("Apps", brjs.apps());
		
		if(parsedArgs.getBoolean("system-apps")){
			listApps("\nSystem Apps", brjs.systemApps());
		}
	}

	private void listApps(String title, List<App> apps) {
		logger.info(title + ":\n");
		for(App app : apps) {
			logger.info("* %s", app.getName());
		}
	}

}
