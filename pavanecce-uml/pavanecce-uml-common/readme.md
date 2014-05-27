This project contains common utilities that can be used when dealing with EMF based UML and OCL models

Package structure:

org.pavanecce.uml.common.ocl
This package defines the extensions to the core OCL-UML infrastructure in EMF. It allows for additional context
variables to be defined and has support for the various built-in types that are  used in Pavanecce

org.pavanecce.uml.common.util
This package contains utility classes that extract significant information from UML models

org.pavanecce.uml.common.util.emulated 
During code generation, classes from this package are used to mimic UML Property related behavior in
scenarios such as AssociationClasses where different Properties are emulated to setup complex relationships 