# BRJS Create App from Git Repo Plugin

## Usage

    ./brjs app-from-git <action> <git_url> <app_name>

Where:

* `action` - `new` to create a new application using the git contents as a template.
* `git_url` - The URL to the application template e.g. `https://github.com/BladeRunnerJS/brjs-todo.git`
* `app_name` - This **must** be the namespace of the application in github. See **TODO section** below.

### Example

    ./brjs app-from-git new https://github.com/BladeRunnerJS/brjs-todo.git brjs-todo

## Dependencies

Some dependencies need to be on the classpath. The simplest way to do this is to put them into:

    brjs_install_location/conf/java

The dependencies are:

* [jgit](http://eclipse.org/jgit/) - you can download the JAR from [here](http://eclipse.org/jgit/download/)
* [JScraft jsch](http://www.jcraft.com/jsch/) - download via the main page

## TODO

* Support `./brjs app-from-github clone ...` command