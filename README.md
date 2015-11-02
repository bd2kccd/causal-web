# ccd-web
CCD web is a Java web-based application that allows users to run causal modeling algorithms on their dataset.  The application comes in two flavors, desktop and web version.

The desktop version allows a single user to run the algorithms on the the dataset locally on his/her desktop and run the algorithms remotely over at the Pittsburgh Supercomputing Center (PSC) for more computing power.

The web version allows multiple users to run the algorithms on their dataset on the server.  Like the desktop version, users can also run remotely over at the PSC.

## How Can I Use It?
### Required Dependencies
#### Algorithm Dependencies
* colt-1.2.0.jar
* commons-collections-3.1.jar
* commons-math3-3.3.jar
* jama-1.0.2.jar
* lib-tetrad-0.3.jar
* mtj-0.9.14.jar
* xom-1.1.jar
* ccd-algorithm-0.3.jar

#### Application Dependencies
* ccd-db-0.4.jar

#### Building the Application
Since this is a maven project all the dependencies will be download from the Maven public repository when compiling. There is one dependency that must be built and installed manually, the database component.  
* Download the [ccd-db-04](https://github.com/bd2kccd/ccd-db/tree/v0.4-alpha) project from GitHub.
* In the ccd-db-04 directory run **mvn install** to build and install the ccd-db-0.4.jar library   
* After ccd-db-0.4 has been successfully built and installed, go to the **ccd-web** directory and run **mvn package**. This will create a jar file called **ccd-web.jar** in the **/target** folder.


## Run As a Desktop Application
### Configurations
In order to run as a desktop application you must edit the **application.properties** file in the jar file and change the following properties:

* spring.profiles.active = desktop

* spring.jpa.show-sql = false

* app.webapp = false

* server.port=[port]     *// add this property if you have a conflict with the default port 8080, (e.g,. server.port=8181) *

> *Note: this file follows the standard Spring boot configurations properties. see [here](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#common-application-properties) for more information

### Setup Workspace
Workspace is the directory where the application will be working in.  The application will be working from this directory.

1. Create a directory called **workspace** in your home directory.  For an example, ***C:\Users\kevin\workspace***.

2. Create a folder called **lib** in the **workspace** directory.  For an example, ***C:\Users\kevin\workspace\lib***.


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
								<fileVersion>0.5.2.0</fileVersion>
								<txtFileVersion>0.5.2.0</txtFileVersion>
								<fileDescription>CCD Desktop Windows
									Application</fileDescription>
								<copyright>University of Pittsburgh and Carnegie Mellon University</copyright>
								<productVersion>0.5.2.0</productVersion>
								<txtProductVersion>0.5.2.0</txtProductVersion>
								<productName>CCD-Desktop</productName>
								<internalName>ccd-web</internalName>
								<originalFilename>ccd-desktop-windows-0.5.2.exe</originalFilename>
							</versionInfo>
						</configuration>
					</execution>
				</executions>
			</plugin>

```
