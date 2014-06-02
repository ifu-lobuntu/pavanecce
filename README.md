#PlAtform for VAlue NEtwork Emergence and Continued Capability Evolution

This project aims to establish a web based platform that allows participants to collaborate across traditional organizational boundaries, producing, exchanging and transforming value of different kinds.
Read more about the project at [Ifu Lobuntu](http://www.ifu-lobuntu.org/home/projects/pavanecce.html)

A this point, the project consists of the following subprojectssupport for the following 

1. [Pavanecce CMMN](pavanecce-cmm/readme.md) which provides runtime semantics of CMMN.
2. [Pavanecce UML](pavanecce-uml/readme.md) which provides integrated domain models generated from UML 
3. pavanecce-common provides some common infrastructure
4. pavanecce-eclipse provides some optional utilities we use during development

#Build instructions and project configurations
##Background:
Most Pavanecce projects need to build and execute consistently in both OSGi environments as well as non-OSGi (standalone) environments. 
The structure of Pavanecce's deployment artifacts is dependent on the target execution environment which can be OSGi, OSGi in JEE, JEE alone or standalone.
There are therefore two different build profiles

##Standalone:

mvn clean install -P standalone
The standalone profile is active by default, so strictly speaking there is no need to invoke this profile. The generated artifacts would not work in an OSGi environment.

##'Eclipse'
We use Tycho inside Maven to build OSGi- compliant bundles. This profile can be activated thus:

mvn clean install -P eclipse 

#Project structures
Only Eclipse can be used as an IDE for the various Pavanecce projects. In addition to being Maven projects, each project is also an Eclipse plugin project which gives us a bit more control over dependencies amongst projeccts.
Please import all projects into Eclipse using File->Import->Existing Projects into Workspace.

One of the challenges of having a dual built process is that, under certain circumstances we need to embed non-OSGi dependencies in a bundle. We do this using the maven dependency plugin, but the .gitignore files are 
setup to ignore these embedded dependencies.  

At this point in time, the transition from OSGi to non-OSGi build artefacts in Maven is not quite seemless yet. 
This resulted in a slightly more complex build design: 
	some projects have dependencies on OSGi bundles, but need to executed in a non-OSGi environment
	some projects have dependencies on non-OSGi jar files, but need to be executed in an OSGi environment.
As a result some purely "transitional" projects were introduced to
	package OSGi dependencies in a normal Maven jar
	package non-OSGi dependencies in a normal OSGi bundle.
The projects were setup in Eclipse. Most projects have both a Maven and Plugin(OSGi) nature, but generally we use the Plugin nature to define dependencies.
However, we do not check any jar files into Git so on cloning the original git repository, the jar files required for some of the projects may not be present
In order to initialise all projects to by running 
	mvn clean install -P init from the root. 
Then import the projects as "Exisiting Eclipse Projects" into Eclipse, and not "Existing Maven Projects"
When working in an Eclipse environment, an OSGi architecture manages classloading and dependency management.
In order to test if the code works properly in an OSGi/Eclipse environment, run
In order to test if the code works properly in a non-OSGi environment, run

