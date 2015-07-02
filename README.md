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
Since this is a maven project all the dependencies will be download from the Maven public repository when compiling.  There is one dependency that is need to be install manually.  That is the [ccd-db-04](https://github.com/bd2kccd/ccd-db/tree/v0.4-alpha).  Download the ccd-db-0.4 project from GitHub and do a **maven install**.  After ccd-db-0.4 is installed do a **maven package** for this project.  There should be a jar file called ccd-web.jar in the target folder.


## Run As a Desktop Application
### Configurations
Open up the application.properties file in the jar file.  Change the properties to the following:

**spring.profiles.active = desktop
spring.jpa.show-sql = false
app.webapp = false**

### Setup Workspace
Workspace is the directory where the application will be working in.  The application will be working from this directory.

Create a directory called **workspace** in your home directory.  For an example, ***C:\Users\kevin\workspace***.

Create a folder called **lib** in the **workspace** directory.  For an example, ***C:\Users\kevin\workspace\lib***.

Copy all the algorithm dependencies into the lib folder.  This is where the application will call the algorithms.

### Launch the Program
```java
java -jar ccd-web.jar
```
To give the program 4GB of memory to run on, type the follow:
```java
java -Xmx4G -jar ccd-web.jar
```
******
