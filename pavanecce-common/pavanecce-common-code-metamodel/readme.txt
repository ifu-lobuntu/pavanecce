This project contains a programming language independent metamodel for source code. Currently it seems to work for Java, Python and Javascript

Package structure:

org.pavanecce.common.code.metamodel
Contains the core constructs such as classes, methods, etc.

org.pavanecce.common.code.metamodel.expressions
Defines the various types of expressions one would encounter in a programming language. The assumption is that an expression always returns a value.

org.pavanecce.common.code.metamodel.relationaldb
Some classes that can be useful to map code constructs to relational databases

org.pavanecce.common.code.metamodel.statements
Defines the various types of statements one would encounter in a programming language. The assumption is that a statement never returns a value.
