This project defines the various environments that Pavanecce may have to be deployed to. These environments are differentiated
from each other w.r.t. support for OSGi, Java EE, Spring, etc. A single Pavanecce build will typically target a single
environment. Subprojects in this project all need to make a certain set of dependencies/packages available, although there are
no limits on how the dependencies are fulfilled.

This approach was chosen to "isolate" the common dependencies that are required for Pavanecce. It does create the impression
of one monolithic dependency. However, Pavanecce is by its nature a project that builds on quite a collection of other projects,
and there is very little functionality in Pavaenecce that can be used without these projects. 

Pavanecce relies heavily on
1. Hibernate
2. jBPM
3. Jackrabbit/JCR   