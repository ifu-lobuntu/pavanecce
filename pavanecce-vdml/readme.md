This project contains all the projects that implement the Pavanecce CMMN (Case Management Model and Notation) runtime. CMMN is a new modeling language that allows the development of highly adaptive, event driven, human-centric applications. If your are new to CMMN, please read this short [introduction](cmmn-intro.md).

It consists of the following sub-projects:

1. The [jBPM CMMN extension](pavanecce-cmmn-jbpm/readme.md) that implements the core CMMN runtime semantics on top of jBPM.
2. Some [extensions to CMMN](pavanecce-cmmn-cfa/readme.md) to support costing and advanced role selection.
3. [Jahia integrations](pavanecce-cmmn-jahia/readme.md) that integrates the jBPM CMMN runtime into the Jahia CMS.

For developers interested in knowing how this projects leverages jBPM's existing code base, these [implementation notes](pavanecce-cmmn-jbpm/implementation_notes.md) may be interesting.