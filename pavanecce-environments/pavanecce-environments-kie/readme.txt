The sole purpose of this project is to make all those libraries that are not available as OSGi bundles
available in an OSGi world.
It retrieves the source code and jar files of all the standard Maven dependencies and copies them
into a lib directory.
In order to populate these libraries, run the "init" profile. This only needs to be done if any of the normal
Maven jar dependencies are added, removed or upgraded.
OSGi projects in Pavanecce should not reference this project at all, but rather use "importedPackage" statements, as
it is very likely that these classes will already be available in the OSGi runtime (e.g. Jahia)