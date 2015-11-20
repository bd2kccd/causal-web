# ccd-web
CCD web is a Java web-based application that allows users to run causal modeling algorithms on their dataset.

## How Can I Use It?
### Required Dependencies
#### Algorithm Dependencies
* ccd-algorithm-0.4.3.jar
* colt-1.2.0.jar
* commons-collections-3.1.jar
* commons-math3-3.3.jar
* jama-1.0.2.jar
* lib-tetrad-0.4.1.jar
* mtj-0.9.14.jar
* pal-1.5.1.jar
* xom-1.1.jar

#### Application Dependencies
* ccd-job-queue-0.1.3.jar
* ccd-mail-0.1.2.jar
* ccd-db-0.6.0.jar
* ccd-commons-0.3.0.jar


#### Building the Application
Since this is a maven project all the dependencies will be download from the Maven public repository when compiling.  The following dependencies must be downloaded, built, and installed manually.

* [ccd-algorithm-0.4.3](https://github.com/bd2kccd/ccd-algorithm/releases/tag/v0.4.3) 
* [ccd-job-queue-0.1.3](https://github.com/bd2kccd/ccd-job-queue/releases/tag/v0.1.3) 
* [ccd-mail-0.1.2](https://github.com/bd2kccd/ccd-mail/releases/tag/v0.1.2) 
* [ccd-db-0.6.0](https://github.com/bd2kccd/ccd-db/releases/tag/v0.6.0) 
* [ccd-commons-0.3.0](https://github.com/bd2kccd/ccd-commons/releases/tag/v0.3.0) 

Go to each of these project directory and type **mvn clean install** to build and install the jar libraries.

## Run Web Application
### Configurations
There are three property files you must fill in to tell the application where to run, what database to use, etc.
1. **application-mysql.properties** (MySQL database configurations)
2. **application.properties** (Spring Boot configurations)
3. **ccd.properties** (CCD web application related configurations)

### Setting Up
First, you need to create a workspace for the application to work in.  Create a directory called **workspace**, for an example ***/home/tuser/workspace***.  Inside the workspace directory, create another folder called **lib**, for an example ***/home/tuser/workspace/lib***.  Put all the algorithm dependencies in the **lib** folder.

Make sure you set **ccd.server.workspace=/home/tuser/workspace** and **ccd.folder.lib=lib** in the **ccd.properties** file.

### Compile the Program
Go to the **ccd-web** directory and run the command **mvn clean package**. This will create a jar file called **ccd-web-1.0.0.jar** in the **/target** folder.

### Launch the Program
```java
java -jar ccd-web.jar
```
To give the program 4GB of memory to run on, type the follow, using the jvm options:
```java
java -Xmx4G -jar ccd-web.jar
```

To launch app in the browser
```
http://localhost:[port]/ccd       // default port is 8080, otherwise change to specified port
```

******

## Build a Windows executable application
Add this launch4j configuarion below to the pom.xml in the build/plugins entity. 

```
<plugin>
	<groupId>com.akathist.maven.plugins.launch4j</groupId>
	<artifactId>launch4j-maven-plugin</artifactId>
	<executions>
		<execution>
			<id>l4j-ccd-web-windows</id>
			<phase>package</phase>
			<goals>
				<goal>launch4j</goal>
			</goals>
			<configuration>
				<headerType>console</headerType>
				<outfile>target/ccd-desktop-windows.exe</outfile>
				<jar>target/ccd-web.jar</jar>
				<errTitle>CCD</errTitle>
				<classPath>
					<mainClass>org.springframework.boot.loader.JarLauncher</mainClass>
					<addDependencies>false</addDependencies>
				</classPath>
				<jre>
					<minVersion>1.8.0</minVersion>
					<maxHeapSize>4096</maxHeapSize>
				</jre>
				<versionInfo>
					<fileVersion>1.0.0</fileVersion>
					<txtFileVersion>1.0.0</txtFileVersion>
					<fileDescription>CCD Desktop Windows
						Application</fileDescription>
					<copyright>University of Pittsburgh and Carnegie Mellon University</copyright>
					<productVersion>1.0.0</productVersion>
					<txtProductVersion>1.0.0</txtProductVersion>
					<productName>CCD-Desktop</productName>
					<internalName>ccd-web</internalName>
					<originalFilename>ccd-desktop-windows-0.5.2.exe</originalFilename>
				</versionInfo>
			</configuration>
		</execution>
	</executions>
</plugin>
```
