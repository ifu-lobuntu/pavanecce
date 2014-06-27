package org.pavanecce.uml.test.uml2code.test;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.internal.adaptor.URLConverterImpl;
import org.pavanecce.uml.common.util.IFileLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class AdaptableFileLocator implements IFileLocator {
	Logger logger = LoggerFactory.getLogger(getClass());
	URLConverterImpl uci = new URLConverterImpl();

	@Override
	public URL resolve(URL url) {
		if (url.getProtocol().startsWith("file") || url.getProtocol().startsWith("jar")) {
			return url;
		} else {
			URL find = FileLocator.find(url);
			if (find == null) {
				try {
					find = uci.toFileURL(url);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (find == null) {
					logger.info("Not found:" + url);
				}
				return url;
			}
			return find;
		}
	}
}
