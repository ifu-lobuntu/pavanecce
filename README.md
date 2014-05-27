pavanecce
=========

PlAtform for VAlue NEtwork Emergence and Continued Capability Evolution



Build instructions and project configurations
Background:
Most Pavanecce projects need to build and execute consistently in both OSGi environments as well as non-OSGi (standalone) environments 
The deployment architecture of Pavanecce is heavily dependent on the target execution environment which can be OSGi, OSGi in JEE, JEE alone or standalone.
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
	mvn clean install -P eclipse 
In order to test if the code works properly in a non-OSGi environment, run
	mvn clean install -P standalone

