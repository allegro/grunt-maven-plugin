# grunt-maven-plugin

**grunt-maven-plugin** plugin allows you to integrate **Grunt** tasks into the **Maven** build process. [**Grunt**](http://gruntjs.com/) is the JavaScript task runner utility. **grunt-maven-plugin** works on both Windows and \*nix systems.

**grunt-maven-plugin** comes with unique Maven+Grunt Integrated Workflow which removes all impediments present when trying to build project using two different build tools.

*Version 1.5.0 requires grunt-maven NPM plugin 1.2.0 version for Integrated Workflow to work.*

## Prerequisites

The only required dependency is [**nodejs**](http://nodejs.org/) with **npm**.
Globally installed [**grunt-cli**](http://gruntjs.com/getting-started) is optional and preferred, but not necessary, as installing custom node modules can be problematic in some environments (ex. CI servers). Additional configuration is needed when using local **grunt-cli**.

**grunt-maven-plugin** can also run `bower install` from [**bower**](http://bower.io/) to install front-end dependencies.

grunt-maven-plugin is compatible with JDK 6+ and Maven 2.1+.

## Motivation

**grunt-maven-plugin** came to life because I needed a good tool to integrate Maven and Grunt. By good i mean not just firing off a **grunt** process, but a tool that would respect rules from both backend (Maven) and frontend (Grunt) worlds. No *node_modules* in Maven sources, no Maven *src/main/webapp/..* paths in *Gruntfile.js*.

**grunt-maven-plugin** allows you to create a usual NPM/Grunt project that could be built and understood by any Node developer, and put it somewhere inside Maven project. It can be extracted at any time and nothing should break. On the other side backend developers don't need to worry about pesky *node_modules* wandering around src/ - all dependencies, generated sources and deliverables live in dedicated **target-grunt** directory, doing this part of build the Maven way.


## Usage

Add **grunt-maven-plugin** to application build process in your *pom.xml*:

```xml
<plugin>
    <groupId>pl.allegro</groupId>
    <artifactId>grunt-maven-plugin</artifactId>
    <version>1.5.0</version>
    <configuration>
        <!-- relative to src/main/webapp/, default: static -->
        <jsSourceDirectory>path_to_js_project</jsSourceDirectory>

        <!-- example options usage to get verbose output in logs -->
        <gruntOptions>
            <gruntOption>--verbose</gruntOption>
        </gruntOptions>

        <!-- example npm install env variable -->
        <npmEnvironmentVar>
            <PHANTOMJS_CDNURL>http://cnpmjs.org/downloads</PHANTOMJS_CDNURL>
        </npmEnvironmentVar>

        <!-- example options usage to filter variables in given resource -->
        <filteredResources>
            <filteredResource>maven-properties.json</filteredResource>
        </filteredResources>

    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>create-resources</goal>
                <goal>npm</goal>
                <!-- or npm-offline if npm failure is not an option -->
                <goal>bower</goal>
                <goal>grunt</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Usage with local grunt-cli

In order to use local **grunt-cli**, add

```xml
<gruntExecutable>node_modules/grunt-cli/bin/grunt</gruntExecutable>
<runGruntWithNode>true</runGruntWithNode>
```

options to plugin configuration and add **grunt-cli** to JS project **package.json**:

```javascript
{
    "devDependencies": {
        "grunt-cli": "~0.1.6",
        "grunt": "~0.4.2"
        /*...*/
    }
}
```

### Using NPM in offline mode

NPM downtimes can be painful for (some) development and (all) release builds. **grunt-maven-plugin** contains
**npm-offline** goal that uses tar-ed *node_modules* instead of running `npm install` during each build.

**npm-offline** flow:

* extract `node_modules.tar` in `target-grunt`
* run `npm-install --ignore-scripts` in case there are any dependencies to download that were not tar-ed
* run `npm rebuild`

If *node_modules* dir already exists in *target-grunt*, it is not overriden.
Offline flow is based on [this blogpost](http://www.letscodejavascript.com/v3/blog/2014/03/the_npm_debacle).

#### Why only tar, not gz?

* GIT uses compression internally anyway
* TAR is lightweight and easy to extract
* TAR is easier to diff

If there are some compelling arguments for compressing this archive, please post an issue and we might add support in future
releases.

#### Preparing node_modules.tar

In `target-grunt`:

```
rm -rf node_modules/
npm install --ignore-scripts
tar cf node_modules.tar node_modules/
mv node_modules.tar ../src/main/webapp/static/
```

### Using linked node_modules

Removed in 1.3.0 release, use **npm-offline** instead.

### Working example

[Sandbox](https://github.com/kielo/grunt-maven-plugin-sandbox) project contains simple usage example. It is used to PoC/develop/test new features, so it always stays up to date with SNAPSHOT version.


## How it works?

1. JavaScript project sources from

        sourceDirectory/jsSourceDirectory (default: src/main/webapp/static)

    are copied to

        gruntBuildDirectory (default: target-grunt)

    Copied directory has to contain **package.json** and **Gruntfile.js** files

1. `npm install` is called, fetching all dependencies

1. `grunt` is run to complete the build process

Because the plugin creates its own target dir, it should be added to ignored resources in SCM configuration (like .gitignore).

## grunt-maven-plugin options

Plugin options available in `<configuration>...</configuration>` are:

#### misc

* **showColors** : should Grunt and npm use color output; defaults to *false*
* **filteredResources** : list of files (or expressions) that will be filtered using **maven-resources-plugin** when creating resources,
remember to exclude those files from integrated workflow config, as else Grunt will override filtered values
* **excludedResources** : list of files (or expressions) that will be excluded when creating resources,
remember to exclude those files from integrated workflow config, as else Grunt will override filtered values
* **disabled** : skip execution of plugin; defaults to *false*

#### environment

* **gruntBuildDirectory** : path to Grunt build directory (target for Grunt); defaults to *${basedir}/target-grunt*
* **sourceDirectory** : path to directory containing source JavaScript project directory; defaults to *${basedir}/src/main/webapp*
* **jsSourceDirectory** : name of directory relative to *sourceDirectory*, which contents are going to be copied to *gruntBuildDirectory*; defaults to *static*
* **warDirectory**: name of directory relative to WAR root, where artifacts from **grunt** build will be copied; defaults to *jsSourceDirectory*

#### node

* **nodeExecutable** : name of globally available **node** executable; defaults to *node*

#### npm

* **npmExecutable** : name of globally available **npm** executable; defaults to *npm*
* **npmEnvironmentVar** : map of environmental variables passed down to npm install command; might be useful for npm repo customization
* **npmOptions** : list of custom options passed to **npm** when calling `npm install` (defaults to empty)

#### offline

* **npmOfflineModulesFile** : name of tar-ed **node_modules** file; defaults to *node_modules.tar*
* **npmOfflineModulesFilePath** : path to **node_modules** file, relative to project basedir; defaults to *sourceDirectory/jsSourceDirectory*
* **npmRebuildOptions** : list of custom options passed to **npm** when calling `npm rebuild` (defaults to empty)

#### bower

* **bowerExecutable** : name of globally available **bower** executable; defaults to *bower*

#### grunt

* **target** : name of Grunt target to run (defaults to null, default Grunt target is run)
* **gruntOptions** : list of custom options passed to grunt (defaults to empty)
* **ignoreTasksErrors** : ignore failing Grunt tasks errors and finish Maven build with success (ignoring 3 and 6 exit code, more on [Grunt exit codes](http://gruntjs.com/api/exit-codes))
* **ignoreAllErrors** : ignore all Grunt errors and finish Maven build with success (ignoring all exit codes, more on [Grunt exit codes](http://gruntjs.com/api/exit-codes))
* **gruntExecutable** : name of **grunt** executable; defaults to *grunt*
* **runGruntWithNode** : if Grunt executable is a js script, it needs to be run using node, ex: `node path/to/grunt`; defaults to *false*

## Execution goals

* **create-resources** : copies all files and *filteredResources* from *sourceDirectory/jsSourceDirectory* to *gruntBuildDirectory*
* **npm** : executes `npm install` in target directory
* **npm-offline** : reuses packed node modules instead of fetching them from npm
* **bower** : executes `bower install` in target directory
* **grunt** : executes Grunt in target directory
* **clean** : deletes *gruntBuildDirectory*

## Maven+Grunt Integrated workflow

Using grunt-maven-plugin is convenient, but there is still a huge gap between frontend and backend development workflow. Various IDEs (Netbeans, IntelliJ Idea, Eclipse)
try to ease webapp development by synchronizing changes made in *src/webapp/* to exploded WAR directory in *target/*. This naive approach works as long as there is no
need to minify JavaScript sources, compile less files or project does not follow "single WAR" rule and can afford using Maven profiles. Rebuilding project each time a
change was made in \*.js is a horrible thing. Fortunately grunt-maven-plugin is a great tool to solve this problem.

Idea is to ignore IDE mechanisms and run Grunt build each time a change in static files was made. Traditional workflow looks like:

1. user changes *src/webapp/static/hello.js*
1. IDE detects change
1. IDE copies *hello.js* to *target/_war_name_/static/hello.js*

This gives little room to integrate other processes in between. Workflow utilizing **grunt-maven-plugin**:

1. run [Grunt watch process](https://github.com/gruntjs/grunt-contrib-watch)
1. user changes *src/webapp/static/hello.js*
1. Grunt detects changes, copies *hello.js* to *target-grunt/hello.js*
1. run Grunt tasks, produce *target-grunt/dist/hello.min.js* with source map *target-grunt/dist/hello.map.js*
1. Grunt copies results to *target/warname/static*

Now what happens inside *target-grunt* is for us to decide, it can be virtually anything - minification, less compilation, running
JS tests, JS linting. Anything Grunt can do, just like during *heavy* build process.

### Configuring Maven

Since we want grunt-maven-plugin to take control of what ends up in WAR, we need to tell Maven WAR plugin to ignore our statics dir when creating WAR:

```xml
<build>
    <plugins>
	<plugin>
	    <artifactId>maven-war-plugin</artifactId>
	    <version>2.3</version>
	    <configuration>
		<warSourceExcludes>jsSourceDirectory/**</warSourceExcludes>
	    </configuration>
	</plugin>
    </plugins>
</build>
```

### Configuring Grunt

**grunt-maven-plugin** has a dedicated NPM Grunt multitasks that make integrated workflow work.

```js
grunt.initConfig({
  mavenPrepare: {
    options: {
      resources: ['**']
    },
    prepare: {}
  },

  mavenDist: {
    options: {
      warName: '<%= gruntMavenProperties.warName %>',
      deliverables: ['**', '!non-deliverable.js'],
      gruntDistDir: 'dist'
    },
    dist: {}
  },

  gruntMavenProperties: grunt.file.readJSON('grunt-maven.json'),

  watch: {
    maven: {
      files: ['<%= gruntMavenProperties.filesToWatch %>'],
      tasks: 'default'
    }
  }
});


grunt.loadNpmTasks('grunt-maven');

grunt.registerTask('default', ['mavenPrepare', 'jshint', 'karma', 'less', 'uglify', 'mavenDist']);

```

For more information please consult documentation of [grunt-maven-npm project](https://github.com/allegro/grunt-maven-npm).


### Deep customization of workflow

It is possible to override inner workflow configuration during runtime. Inner properties are extracted from **pom.xml**
and used internally inside workflow Grunt tasks. They reside in **target-grunt/grunt-maven.json**.
After reading inner properties JSON file, workflow task seeks for **grunt-maven-custom.json** file in
**target-grunt** and overrides original properties with custom ones.

### Configuring IDE

Depending on IDE, synchronization processes behave differently. Having JSP files synchronized is a nice feature, so it is up to you
to decide what should be turned off. In Netbeans and IntelliJ no configuration is needed, they play nicely with new workflow.

You still have to remember to run Grunt watch process in background so it can monitor changes. It can be run from IDE via grunt-maven-plugin.
Define custom runner that will run:

    mvn grunt:grunt -Dtarget=watch

You should see process output each time static sources change.

#### Configuring Eclipse

Eclipse is a special case. Unfortunately it does not read WAR from Maven target, instead it keeps own file hierarchy.
Eclipse developers on the team should use deep workflow customization to override properties used by others. Proposed
way is to create **grunt-maven-custom.json** in Maven sources and add it to **.gitignore**, so it will not pollute
source repository. Since i have little experience with Eclipse, i would welcome a contribution of sample configuration,
but most probably it is enough to override **targetPath** property.


## Changelog
* **1.5.9** (26.12.2014)
  * added option to change destination of all deliverables within WAR
* **1.4.1** (02.08.2014)
  * added option to exclude custom resources in create-resources phase
* **1.4.0** (07.07.2014)
  * changed default lifecycle bindings
  * support for disabling execution based on flag
* **1.3.2** (19.06.2014)
  * support for defining npm command line parameters
  * fixed bug from 1.3.1 - null pointer when no npm env variables specified
* **1.3.1** (26.05.2014)
  * support for Maven 2.1+ and 3.*
  * option to specify NPM env variables
* **1.3.0** (22.04.2014)
  * **dropped** support for *link-node-modules*
* **1.2.2** (31.03.2014)
  * support for npm offline mode
* **1.2.1** (25.02.2014)
  * executing `bower install`
* **1.2.0** (07.02.2014)
  * new Maven+Grunt NPM multitasks
* **1.1.4** (05.02.2014)
  * added option to ignore Grunt task errors (failing tests etc) (#22)
* **1.1.3** (25.01.2014)
  * fixed issue with using custom Grunt target and CLI options together (#20)
* **1.1.2** (06.01.2014)
  * fixed compatibility issue of 1.1.x branch - once again compatible with JDK 6+
* **1.1.1** (31.12.2013)
  * fixed bug with filtering all resources instead of only chosen
  * fixed bug with Windows paths in workflow properties
* **1.1.0** (30.12.2013)
  * integrated workflow as a separate, auto-configured Grunt task
* **1.0.4** (8.12.2013)
  * explicit declaration of resources filtered on create-resources goal
* **1.0.3** (24.11.2013)
  * passing custom options to grunt executable
  * ability to use external or preinstalled node_modules
* **1.0.2** (15.10.2013)
  * option to disable npm and grunt color output, by default no colors are shown as it looks bad in Maven logs
* **1.0.1** (13.09.2013)
  * compatibility with Maven 3.1.x


## License

**grunt-maven-plugin** is published under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
