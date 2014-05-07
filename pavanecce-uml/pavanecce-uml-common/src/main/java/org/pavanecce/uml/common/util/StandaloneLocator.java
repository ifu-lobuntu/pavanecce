package org.pavanecce.uml.common.util;

import java.net.URL;

public class StandaloneLocator implements IFileLocator {

	@Override
	public URL resolve(URL url) {
		if (url.getProtocol().startsWith("file") || url.getProtocol().startsWith("jar")) {
			return url;
		} else {
			throw new IllegalArgumentException("Not a file URL:" + url);
		}
	}

}
