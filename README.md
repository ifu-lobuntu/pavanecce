[![Build Status](https://travis-ci.org/ifu-lobuntu/pavanecce.png)](https://travis-ci.org/ifu-lobuntu/pavanecce)
#PlAtform for VAlue NEtwork Emergence and Continued Capability Evolution

This project aims to establish a web based platform that allows participants to collaborate across traditional organizational boundaries, producing, exchanging and transforming value of different kinds.
Read more about the project at [Ifu Lobuntu](http://www.ifu-lobuntu.org/home/projects/pavanecce.html)

A this point, the project consists of the following sub-projects: 

1. [Pavanecce CMMN](pavanecce-cmmn/readme.md) which provides runtime semantics of CMMN.
2. [Pavanecce UML](pavanecce-uml/readme.md) which provides integrated domain models generated from UML 
3. [Pavanecce Common](pavanecce-common/readme.md) provides some common infrastructure
4. [Pavanecce Environments](pavanecce-environments/readme.md) provides projects bringing in dependencies for each supported environment

A related Github repository, pavanecce-ide, provides some optional utilities we use during development

#Build instructions and project configurations
##Background:
Most Pavanecce projects need to build and execute consistently in various execution environments. Currently the OSGi environments of Jahia CMS and Eclipse are supported. Support for the CDI based environment of jBPM's KIE Workbench is being implemented. A different Maven profile representing each execution environment is available in the [![root pom file](pom.xml). Currently, the default is "jahia". The active profile will
select a subproject from the [pavanecce-environments](pavanecce-environments/readme.md) project that brings in the dependencies of the chosen environment. 

#Development environment
##Basic development
It is recommended that an IDE that supports Maven and OSGi is used, such as Eclipse, but Maven support alone should suffice (untested). Before importing the projects into your, execute
mvn clean install
on the command line. This will build some supporting Maven projects for the Eclipse dependencies (UML, OCL, etc.). In Eclipse, install all the M2E connectors that have been discovered. Import the projects into Eclipse using 
File->Import->Existing Maven Projects.

##OSGi development
For a more accurate reflection of how the dependencies are managed in an OSGi environment, the use of Eclipse with the UML and OCL features is recommended. Follow the instructions above, however, when importing
 the projects, de-select the project /pavanecce/pavencce-uml/pavanecce-uml-dependencies, as we are expecting Eclipse to bring in those dependencies. Once the rest of the projects have been imported, they will use standard Maven for dependency resolution. To enable OSGi for dependency resolution, right-click on an OSGi project, e.g.  /pavanecce/pavencce-uml/pavencce-uml-uml2code and select 
Plugin Tools->Update Classpath
A Dialog pops up showing all the OSGi projects. Select all projects except those ending with "-test", and click "Finish". OSGi/Maven development in Eclipse is a fairly recent feature, so expect the project configurations to get out of synch every now and then. To fix it, first select any of the projects and select
Maven->Update Project
This brings up a dialog with all the Maven projects. Select all the projects and click "OK". Next select an OSGi project and select 
Plugin Tools->Update Classpath
As before, select all the projects except those ending with "-test" and click "Finish"

##UML and Code Generation
Some of the projects in Pavanecce use code generated from UML models. To view the source UML models, install the [![Papyrus feature](http://www.eclipse.org/papyrus/updates/index.php)]. To generate code from the UML models, install the Pavanecce IDE feature from ... (coming soon). It is also possible to generate the code with the normal Pavanecce UML2Code functionality.
 

