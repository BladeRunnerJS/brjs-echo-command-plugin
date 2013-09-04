package org.bladerunnerjs.examples;

import java.util.Arrays;

import com.caplin.brjs.core.plugin.command.CommandPlugin;
import com.caplin.brjs.model.BRJS;
import com.caplin.brjs.model.exception.command.CommandArgumentsException;
import com.caplin.brjs.model.exception.command.CommandOperationException;

public class EchoCommandPlugin implements CommandPlugin {

	private BRJS brjs;

	@Override
	public void setBRJS(BRJS brjs) {
		this.brjs = brjs;
	}

	@Override
	public void doCommand(String[] args) throws CommandArgumentsException,
			CommandOperationException {
		String echo = Arrays.toString(args);
		this.brjs.getConsoleWriter().println(echo);
	}

	@Override
	public String getCommandDescription() {
		return "echos out the input arguments that you pass to it";
	}

	@Override
	public String getCommandName() {
		return "echo";
	}

	@Override
	public String getCommandUsage() {
		return "brjs echo <argument1> <argument2> <argumentx>";
	}

}
