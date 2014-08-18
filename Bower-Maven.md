
## Motivation

**grunt-maven-plugin bower-maven** task came to life because I needed to package both my backend and frontend on a private shared repository. I had already the private maven repository, and started to play with AngularJS, as i added a generic module to handle Users i wanted to share it with all my new Java web project. I used grunt-maven-plugin to build the final frontend, but the user module dependency was just a simple jar. So i added a task than can extract JS module from Jar and add it to my bower.json. This way i was able to create each AngularJS module independant with their backend counterpart. 



## Usage

Let say we have a store application that require a user module

WebStore-1.0.0-SNAPSHOT

 |- UserModule-1.0.0-SNAPSHOT

On my user module i've added this :
```xml
<build>
<resources>
	<resource>
		<targetPath>META-INF/bower/</targetPath>
		<directory>src/main/javascript</directory>
	</resource>
</resources>
</build>
```
So now inside the UserModule-1.0.0-SNAPSHOT.jar i have all the javascript source code
( i will integrate by default the handler for webjars )

In the pom.xml of WebStore i've added this :
```xml
<build>
	<plugins>
		<plugin>
			<groupId>pl.allegro</groupId>
			<artifactId>grunt-maven-plugin</artifactId>
			<version>1.4.2-LZ-SNAPSHOT</version>
			<configuration>
				<jsSourceDirectory>../javascript</jsSourceDirectory>				<gruntOptions>
					<gruntOption>--verbose</gruntOption>
				</gruntOptions>
				<gruntBuildDirectory>target/grunt/</gruntBuildDirectory>
				<filteredResources>
					<filteredResource>maven-properties.json</filteredResource>
				</filteredResources>
			</configuration>
			<executions>
				<execution>
					<goals>
						<goal>create-resources</goal>
						<goal>bower-maven</goal>
						<goal>npm</goal>
						<goal>bower</goal>
						<goal>grunt</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```
So during the build process with bower-maven, my bower.json is transformed from :
```json
{
  "dependencies": {
    "angular": "1.2.6",
    "json3": "~3.2.6",
    "es5-shim": "~2.1.0",
    "jquery": "~1.10.2",
    "sass-bootstrap": "~3.0.2",
    "angular-resource": "1.2.6",
    "angular-cookies": "1.2.6",
    "angular-sanitize": "1.2.6",
    "angular-route": "1.2.6"
  },
  "name": "static",
  "devDependencies": {
    "angular-mocks": "1.2.6",
    "angular-scenario": "1.2.6"
  },
  "version": "0.0.0"
}
```
to
```json
{
  "dependencies": {
    "angular": "1.2.6",
    "json3": "~3.2.6",
    "es5-shim": "~2.1.0",
    "jquery": "~1.10.2",
    "sass-bootstrap": "~3.0.2",
    "angular-resource": "1.2.6",
    "angular-cookies": "1.2.6",
    "angular-sanitize": "1.2.6",
    "angular-route": "1.2.6",
    "UserModule-1.0.0-SNAPSHOT.jar": "/Users/loopingz/git/webstore/target/grunt/bower-maven-repo/UserModule-1.0.0-SNAPSHOT.jar.zip"
  },
  "name": "static",
  "devDependencies": {
    "angular-mocks": "1.2.6",
    "angular-scenario": "1.2.6"
  },
  "version": "0.0.0"
}
```

And the UserModule-1.0.0-SNAPSHOT.jar.zip contains all the javascript files from my module.

I can then use all the others task from **grunt-maven-plugin** to package my final war with the compiled version of the application
