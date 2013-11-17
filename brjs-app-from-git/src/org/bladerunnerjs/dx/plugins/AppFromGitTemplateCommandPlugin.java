package org.bladerunnerjs.dx.plugins;

import java.io.File;
import java.io.IOException;

import javax.naming.InvalidNameException;

import org.apache.commons.io.FileUtils;
import org.bladerunnerjs.core.plugin.command.ArgsParsingCommandPlugin;
import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.Aspect;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.exception.command.CommandArgumentsException;
import org.bladerunnerjs.model.exception.command.CommandOperationException;
import org.bladerunnerjs.model.exception.modelupdate.ModelUpdateException;
import org.bladerunnerjs.model.exception.template.TemplateInstallationException;
import org.bladerunnerjs.model.utility.NameValidator;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.UnflaggedOption;

public class AppFromGitTemplateCommandPlugin extends ArgsParsingCommandPlugin {

	private BRJS brjs = null;
	private App app = null;
	
	private static final String NEW_APP_ACTION = "new";
	private static final String CLONE_APP_ACTION = "clone";
	
	private static final String ACTION_HELP_TEXT =
			"Either '" + CLONE_APP_ACTION + "' the application from git preserving the remote " +
			"reference. Or create a '" + NEW_APP_ACTION + "' application based on the contents " +
			"of the git repository";

	@Override
	public String getCommandDescription() {
		return "Create a new BRJS application based on BRJS application code in Github";
	}

	@Override
	public String getCommandName() {
		return "app-from-git";
	}

	@Override
	public void setBRJS(BRJS brjs) {
		this.brjs = brjs;
	}

	@Override
	protected void configureArgsParser(JSAP argsParser) throws JSAPException {
		// app-from-git <url> <appName>
		// Could make appName optional and use a name based on the git URL
		// When global install is available a --directory flag will be required
		
		Parameter actionArg = new UnflaggedOption("action")
			.setRequired(true).setHelp(ACTION_HELP_TEXT);
		UnflaggedOption gitUrlArg = new UnflaggedOption("git-url").setGreedy(false)
				.setRequired(true);
		UnflaggedOption appNameArg = new UnflaggedOption("app-name").setGreedy(
				false).setRequired(true);

		argsParser.registerParameter(actionArg);
		argsParser.registerParameter(gitUrlArg);
		argsParser.registerParameter(appNameArg);
	}

	@Override
	protected void doCommand(JSAPResult result)
			throws CommandArgumentsException, CommandOperationException {
		log("doCommand for \"" + this.getCommandName() + "\" command.");
		
		String action = result.getString("action");
		String appName = result.getString("app-name");
		String gitUrl = result.getString("git-url");

		switch(action) {
			case CLONE_APP_ACTION:
				throw new CommandArgumentsException("clone action not yet implemented", this);
//				break;
			case NEW_APP_ACTION:
				break;
			default:
				throw new CommandArgumentsException(action + " is an unsupported action", this);			
		}
		
		try {
			app = createApp(appName);
		} catch (InvalidNameException e) {
			String errorMessage = "The app name \"" + appName
					+ "\" is not valid";
			throw new CommandArgumentsException(errorMessage, e, this);
		} catch (ModelUpdateException e) {
			String errorMessage = "Something went wrong creating the app:\n" +
					  "Error:\n" + e.getMessage();
			throw new CommandOperationException(errorMessage, e);
		} catch (TemplateInstallationException e) {
			String errorMessage = "Something went wrong creating the app:\n" +
					  "Error:\n" + e.getMessage();
			throw new CommandOperationException(errorMessage, e);
		}

		try {
			getAppFilesFromGit(gitUrl);
		} catch (IOException e) {
			String errorMessage = "Something went wrong saving the Git remote config:\n" +
								  "Error:\n" + e.getMessage();
			throw new CommandOperationException(errorMessage, e);
		} catch (GitAPIException e) {
			String errorMessage = "Something went wrong pulling from the Git remote:\n" +
								  "Error:\n" + e.getMessage();
			throw new CommandOperationException(errorMessage, e);
		}
		
		try {
			createGitRepo();
		} catch (GitAPIException e) {
			String errorMessage = "Something went wrong initializing the Git repo:\n"
					+ "Error:\n" + e.getMessage();
			throw new CommandOperationException(errorMessage, e);
		}
		
		log("App \"" + appName + "\" created in \"" + app.dir().getPath() + "\"");
	}

	private void getAppFilesFromGit(String url) throws IOException, GitAPIException {
		
		File tmpDir = new File(app.dir().getPath() + File.separator + ".tmp");
		
		log("Cloning " + url + " into .tmp folder: " + tmpDir.getPath());
		CloneCommand clone = Git.cloneRepository();
		clone.setURI(url);
		clone.setDirectory(tmpDir);
		clone.call();
		
		// Delete .git folder
		File gitDir = new File(tmpDir.getPath() + File.separator + ".git");
		log("Deleting .git folder: " + gitDir.getPath());
		gitDir.delete();
		
		// Move all files out of .tmp
		log("Moving files out of .tmp into " + app.dir().getPath() + "...");
		File[] content = tmpDir.listFiles();
	    for(int i = 0; i < content.length; i++) {
	    	log(content[i].getName());
	    	FileUtils.moveToDirectory(content[i], app.dir(), false);
	    }
		
		// Delete .tmp
	    log("Removing .tmp folder");
	    tmpDir.delete();
	}

	private App createApp(String appName) throws InvalidNameException,
			ModelUpdateException, TemplateInstallationException {
		log("Creating app \"" + appName + "\"");

		App app = this.brjs.app(appName);		
		
		NameValidator.assertValidDirectoryName(app);
		String appNamespace = NameValidator.generateAppNamespaceFromApp(app);

		app.populate(appNamespace);
		
		app.deploy();
		
		// Remove any default files as they'll be replaced when the app
		// is pulled from git
		log("Removing auto-generated aspects");
		for(Aspect aspect : app.aspects()) {
			aspect.delete();
		}
		
		log("Deleting old app.conf");
		FileUtils.deleteQuietly(new File(app.dir().getPath() + File.separator + "app.conf"));
		
		return app;
	}

	private Git createGitRepo() throws GitAPIException {
		log("Initializing Git repo in \"" + app.dir().getPath() + "\"");
		InitCommand initCommand = Git.init();
		initCommand.setDirectory(app.dir());
		return initCommand.call();
	}

	private void log(String msg) {
		if (this.brjs != null) {
			this.brjs.getConsoleWriter().println(msg);
		} else {
			System.out.println(msg);
		}
	}

}
