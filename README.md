# JobSearch
Experiment of a simple embedded database application 

This application is an experiment and a learning exercise. 

## Building
More details on building and running may be found in the [project wiki](https://github.com/SwingGuy1024/Sk-JobHunt/wiki/Skeleton-Key-Application), but here's a quick overview.

Maven must run under Java 8. Running under Java 9 or later doesn't work. (I have read that a later version of JOOQ, 3.11, will fix this problem. I have not yet confirmed this.)

The project is written to be built in JDK 1.8. It has not been tested with any later versions.

After ensuring the maven runner is using Java 8, run the code generator like this:

    mvn jooq-codegen:generate clean install

(`mvn clean` does not remove the generated code.)

Generated code goes into the `com.neptunedreams.jobs.gen` package, in the `src/gen/java` folder.

Once the code has been generated, it need not be generated again unless the schema changes. So at this point, you can build the Mac application by just typing

`mvn clean install`

This builds the OSX executable. 

For other platforms, you may now assemble an executable jar file from the compiled classes:

    mvn clean assembly:assembly
    
The location of the database is determined by the `SQLiteInfo` class.

### Troubleshooting Building

To build, type `mvn clean install`
This uses the checkerframework, which can be finicky. If maven will not build be sure of three things:

1. Maven should be 3.6 or later

1. The maven runner should use java 1.8, but not later versions.

1. JAVA_HOME should be defined, and point to JDK 1.8. 

If you want to use JDK 1.11 or later, you will need to make changes to the pom.xml file. See the checker framework for instructions. It's not clear if JDK 1.9 or 1.10 are supported, and I haven't tried them.

### Assembly
The pom.xml file will build an application bundle for the Mac. As it is currently configured, it does not bundle the JDK with the app. This is done for size reasons. To include the JDK, uncomment the line that specifies a value for `<jrePath>`.

## Why?

For more information on the purpose of this project, see the [project wiki](https://github.com/SwingGuy1024/Skeleton/wiki/Skeleton-Key-Application).
