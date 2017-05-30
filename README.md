# causal-web
Causal web is a Java web-based application that allows users to run causal modeling algorithms on their dataset.  The Center for Causal Discovery is hosting this application at the Pittsburgh Supercomputing Center and you can access it at https://ccd2.vm.bridges.psc.edu/ccd

Documentation for using the web application can be found at [https://bd2kccd.github.io/docs/causal-web/](https://bd2kccd.github.io/docs/causal-web/) 

If you want to host the application with your own hardware, follow the instructions below for configuring, building and installing the application.

## Building the software

### Prerequisites - You must have the following installed to build/install causal-web
* Oracle Java 1.8 - (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* Maven 3.x -(https://maven.apache.org/download.cgi)
* For production install MySQL 5.x - (http://dev.mysql.com/downloads/)
* You will need a GMail account.  GMail will be used to send out account activation links and feedbacks.

### Download the source code
#### Dependencies
Download and uncompress the source code for each of following dependencies:

* [ccd-job-queue-0.1.8](https://github.com/bd2kccd/ccd-job-queue/releases/tag/v0.1.8)
* [ccd-mail-0.2.4](https://github.com/bd2kccd/ccd-mail/releases/tag/v0.2.4)
* [ccd-db-0.6.5](https://github.com/bd2kccd/ccd-db/releases/tag/v0.6.5)
* [ccd-commons-0.3.1](https://github.com/bd2kccd/ccd-commons/releases/tag/v0.3.1)
To install the dependencies, go to the top directory of each project and do a maven install by typing **mvn install**.

#### Application
Download and uncompress the application source code  [causal-web-0.2.0](https://github.com/bd2kccd/causal-web/releases/tag/v1.3.4).  To compile and build the application, go to the directory **causal-web-0.2.0** and type **mvn package**.

#### External Dependencies
* [causal-cmd-0.2.0-jar-with-dependencies.jar](https://github.com/bd2kccd/causal-cmd/releases/tag/v0.2.0)

## Configure the software

### Setup the directory structure and copy libraries
First, you need to create a workspace for the application to work in.  Create a directory called **workspace**, for an example ***/home/tuser/workspace***.  
Inside the workspace directory, create another folder called **lib**, for example ***/home/tuser/workspace/lib***.  

Copy the **causal-cmd-0.2.0-jar-with-dependencies.jar** to the  **workspace/lib** folder.

### Configure
There are 4 configuration files to configure located in causal-web-0.2.0/src/main/resources folder:
1. **application-hsqldb.properties**: HSQLDB database configurations (for testing only).
2. **application-mysql.properties**: MySQL database configurations
3. **application.properties**: Spring Boot configurations
4. **ccd.properties**: CCD web application related configurations

#### Workspace Configuration
Set the property **ccd.server.workspace** in the  **ccd.properties** file to the workspace location.
```java
// based on the above example
ccd.server.workspace=/home/tuser/workspace
```

#### Mail Configuration
Set the following properties in the **application.properties** file:
```java
// add gmail account
spring.mail.username=<gmail username>
spring.mail.password=<gmail username>
```

#### Testing Configuration
Set the following properties in the **application.properties** file:
```java
spring.profiles.active=scheduler,hsqldb
```

#### Production Configuration
Set the following properties in the **application.properties** file:
```java
spring.profiles.active=scheduler,mysql
```

Make sure you set **ccd.server.workspace=/home/tuser/workspace** and **ccd.folder.lib=lib** in the **ccd.properties** file.

## Compile the Program
Go to the **causal-web** directory and run the command **mvn clean package**. This will create a jar file called **causal-web-0.2.0.jar** in the **/target** folder.

### Launch the Program
```java
java -jar causal-web-0.2.0.jar
```
To give the program 4GB of memory to run on, type the follow, using the jvm options:
```java
java -Xmx4G -jar causal-web-0.2.0.jar
```

To launch app in the browser
```
http://localhost:[port]/ccd       // default port is 8080, otherwise change to specified port
```
