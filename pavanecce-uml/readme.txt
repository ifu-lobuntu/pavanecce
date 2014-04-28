This POM project contains all the projects that deal with UML, potentially in a standalone, non-Eclipse environment
Although these modules are written to run outside of Eclipse, they all still have a strong dependency on
the Eclipse EMF, UML and OCL bundles. 
The pavanecce-uml-dependencies project aggregates all these Eclipse dependencies
and makes them available as a normal jar project. 
All these projects can run in either a standalone environment or Eclipse, and the tests should be executed in both 
environments using the 'normal' profile as well as the 'eclipse' profile