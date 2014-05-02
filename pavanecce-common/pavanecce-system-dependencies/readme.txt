This project was required as a workaround for conflicts encounntered when OSGi bundles explicitly require packages
that are already present in the underlying JRE.
It is implemented as a Fragment to the org.eclipsep.osgi bundle, which is an eclipse-specific artifact, but this fragment should never be directly accessed.
Simply use import-package statements