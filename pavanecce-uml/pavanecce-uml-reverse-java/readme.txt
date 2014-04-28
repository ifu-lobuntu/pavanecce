This project contains the abstract class common to the different Java code formats that we can potentially reverse into UML
Package structure:

1. org.pavanecce.uml.reverse.java.sourcemodel 
The interface representing the Java source model

2. org.pavanecce.uml.reverse.java 
The classes that can generate UML models and profiles from these classes

3. org.pavanecce.uml.reverse.java.sourcemodel.reflect
An implementation of the Java source model that delegates to the Java reflection API