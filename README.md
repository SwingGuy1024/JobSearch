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

## To Add a Field:

The location of the database may be found in the SqLiteInfo class. For this example, I'll use `~/.sqlite.jobs/jobs.db`

1. Change directory to the the one that has the database, and launch SqLite:

       $> cd ~/.sqlite.jobs/
       $> sqlite3
       sqlite>  

   For help, type .help
   
   To exit, type .exit

1. Open the database file:

       .open jobs.db
1. Show databases: 

       sqlite> .databases
       seq  name             file
       ---  ---------------  ----------------------------------------------------------
       0     main            /Users/miguelmunoz/.sqlite.jobs/jobs.db
 
1. Show the schema for the table "lead":

        sqlite> .schema lead
        CREATE TABLE lead (
          id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          company VARCHAR(512) NOT NULL collate noCase,
          contact_name VARCHAR(512) NOT NULL collate noCase,
          client VARCHAR(512) NOT NULL collate noCase,
          dice_posn VARCHAR(512) NOT NULL collate noCase,
          dice_id VARCHAR(512) NOT NULL collate noCase,
          email VARCHAR(512) NOT NULL collate noCase,
          phone1 VARCHAR(512) NOT NULL collate noCase,
          phone2 VARCHAR(512) NOT NULL collate noCase,
          phone3 VARCHAR(512) NOT NULL collate noCase,
          fax VARCHAR(512) NOT NULL collate noCase,
          website VARCHAR(512) NOT NULL collate noCase,
          skype VARCHAR(512) NOT NULL collate noCase,
          description VARCHAR NOT NULL collate noCase,
          history VARCHAR NOT NULL collate noCase,
          created_on DATETIME NOT NULL DEFAULT (DATETIME('now'))
        );

1. Add a column:

       sqlite> alter table lead add column linked_in varchar(512) not null collate noCase default '';
       sqlite> .schema
       CREATE TABLE lead (
         id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
         company VARCHAR(512) NOT NULL collate noCase,
         contact_name VARCHAR(512) NOT NULL collate noCase,
         client VARCHAR(512) NOT NULL collate noCase,
         dice_posn VARCHAR(512) NOT NULL collate noCase,
         dice_id VARCHAR(512) NOT NULL collate noCase,
         email VARCHAR(512) NOT NULL collate noCase,
         phone1 VARCHAR(512) NOT NULL collate noCase,
         phone2 VARCHAR(512) NOT NULL collate noCase,
         phone3 VARCHAR(512) NOT NULL collate noCase,
         fax VARCHAR(512) NOT NULL collate noCase,
         website VARCHAR(512) NOT NULL collate noCase,
         skype VARCHAR(512) NOT NULL collate noCase,
         description VARCHAR NOT NULL collate noCase,
         history VARCHAR NOT NULL collate noCase,
         created_on DATETIME NOT NULL DEFAULT (DATETIME('now'))
         , linked_in varchar(512) not null collate noCase default '');

1. Exit

       sqlite> .exit
       $>

1. Follow the instructions in class SQLiteRecordDao for jOOQ integration.

1. Modify any java file that references all the existing fields in the database to include the new file.

(See the [SQLite documentation](https://www.sqlite.org/docs.html) for reference)

## Markdown

See the [Markdown Guide](https://www.markdownguide.org/basic-syntax/_) for help editing this file.
