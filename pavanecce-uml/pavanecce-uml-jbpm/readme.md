This project provides jBPM ProcessDialect that enables expressions written in Object Constraint Language in jBPM processes. At this point,
it is specifically intended for CMMN Cases.

It assumes that the structure and functionality of the objects that are stored in the Case instances are reflected an equivalent UML model. Currently,
you can use the [Uml2Code](../pavanecce-uml-uml2code/readme.md) facility to generate these classes, but we are looking at ways to reverse existing
Java classes into an in-memory UML model to allow you to specify OCL expressions without the need for a UML model.