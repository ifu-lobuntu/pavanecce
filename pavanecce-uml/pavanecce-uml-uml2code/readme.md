# 1. Overview
This project brings logic from a couple of other projects together to allow Java, Javascript and Python code to be generated from a UML model.

Some of the interesting features of ths project are:

# 2. OCL Support
Generate semantically equivalent Java and Javascript (Backbone.js) code based on OCL expression in the UML model.

# 3. JPA
Generate the necessary JPA annotations to persist your Java objects in a relational database

# 4. Jackrabbit ObjectContentManager
Generate the necssary OCM annotations to persist your Java objects in a JCR document database.

# 5. Two way associations.
One of the most common problems with domain models implemented in code is that developers find it too difficult to maintain associations from
both directions. The code that gets generated here can ensure that changes to the one side of an association are reflected automatically in
the object representing the other end of the association.

# 6. Highly extensible.
The generation of code in this project entails an intermediate, programming language independent metamodel. Implement your own logic by changing
the state of this model, and those changes will automatically reflect in the generated code. Also provide your own code decorators to add a little
bit of extra logic in your targeted programming language. 