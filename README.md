# ccd-web
CCD web is a Java web-based application that allows users to run causal modeling algorithms on their dataset.  The Center for Causal Discovery is hosting this application at the Pittsburgh Supercomputing Center and you can access it at https://dxcvm26.psc.edu/ccd

Documentation for using the web application can be found at http://www.ccd.pitt.edu/wiki/index.php?title=CCD_Web_Application_Quick_Start_and_User_Guide 

If you want to host your own hardware, follow the instructions below for configuring, building and installing the application.

## Building the software

### Prerequisites - You must have the following installed to build/install CCD Web
* Oracle Java 1.8 - (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* Maven 3.x -(https://maven.apache.org/download.cgi)
* For production use install MySQL 5.x - (http://dev.mysql.com/downloads/)
* You will need a GMail account.  GMail will be used to send out account activation link and feedback.

### Download the source code
Since this is a multimodule project all the submodules will need to be download and installed.  The following modules must be downloaded, built, and installed.

* [lib-tetrad-0.4.1](https://github.com/bd2kccd/lib-tetrad/releases/tag/v0.4.1)
* [ccd-algorithm-0.4.3](https://github.com/bd2kccd/ccd-algorithm/releases/tag/v0.4.3) 
* [ccd-job-queue-0.1.3](https://github.com/bd2kccd/ccd-job-queue/releases/tag/v0.1.3) 
* [ccd-mail-0.1.2](https://github.com/bd2kccd/ccd-mail/releases/tag/v0.1.2) 
* [ccd-db-0.6.0](https://github.com/bd2kccd/ccd-db/releases/tag/v0.6.0) 
* [ccd-commons-0.3.0](https://github.com/bd2kccd/ccd-commons/releases/tag/v0.3.0) 
* [ccd-web-1.0.0](https://github.com/bd2kccd/ccd-web/releases/tag/v1.0.0)

Download each module, uncompress the download, cd to each project directory and type **mvn clean install** to build and install the jar libraries.

## Configure the software

### Setup the directory structure and copy libraries
First, you need to create a workspace for the application to work in.  Create a directory called **workspace**, for an example ***/home/tuser/workspace***.  
Inside the workspace directory, create another folder called **lib**, for example ***/home/tuser/workspace/lib***.  

Copy the following libraries to the  **workspace/lib** folder
* ccd-algorithm-0.4.3.jar (found in ccd-algorithm-0.4.3/target)
* lib-tetrad-0.4.1.jar (found in ccd-tetrad-0.4.1/target)
* colt-1.2.0.jar (found in lib-tetrad-0.4.1/lib)
* commons-collections-3.1.jar (found in lib-tetrad-0.4.1/lib)
* commons-math3-3.3.jar (found in lib-tetrad-0.4.1/lib)
* jama-1.0.2.jar (found in lib-tetrad-0.4.1/lib)
* mtj-0.9.14.jar (found in lib-tetrad-0.4.1/lib)
* pal-1.5.1.jar (found in lib-tetrad-0.4.1/lib)
* xom-1.1.jar (found in lib-tetrad-0.4.1/lib)

### Configure
There are 4 configuration files to configure located in ccd-web-1.0.0/src/main/resources:
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
Set the following properties in the **ccd.properties** file:
```java
// add gmail account
ccd.mail.feedback.to=<gmail username>
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


###  Dependencies


#### ccd-web application dependencies (build/install before building the ccd-web application)
* ccd-job-queue-0.1.3.jar
* ccd-mail-0.1.2.jar
* ccd-db-0.6.0.jar
* ccd-commons-0.3.0.jar
