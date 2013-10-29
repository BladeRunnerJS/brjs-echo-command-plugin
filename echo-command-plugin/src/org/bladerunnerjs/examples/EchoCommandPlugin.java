package org.bladerunnerjs.examples;

import org.bladerunnerjs.core.plugin.command.ArgsParsingCommandPlugin;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.exception.command.CommandArgumentsException;
import org.bladerunnerjs.model.exception.command.CommandOperationException;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.UnflaggedOption;

public class EchoCommandPlugin extends ArgsParsingCommandPlugin {

	private BRJS brjs;

	@Override
	public String getCommandDescription() {
		return "echos out the input arguments that you pass to it";
	}

	@Override
	public String getCommandName() {
		return "echo";
	}

	@Override
	public void setBRJS(BRJS brjs) {
		this.brjs = brjs;
	}

	@Override
	protected void configureArgsParser(JSAP argsParser) throws JSAPException {
		UnflaggedOption messageOption = new UnflaggedOption("message")
												.setGreedy(true);
		argsParser.registerParameter(messageOption);
	}

	@Override
	protected void doCommand(JSAPResult result) throws CommandArgumentsException,
			CommandOperationException {
		String[] messageArgs = result.getStringArray("message");
		String echo = java.util.Arrays.toString(messageArgs);
		this.brjs.getConsoleWriter().println(echo);
	}

}
