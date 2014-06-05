package org.pavanecce.uml.common.util.emulated;

import java.io.File;

import org.eclipse.emf.common.util.URI;

public interface UriToFileConverter {
	File resolveUri(URI uri);
}
