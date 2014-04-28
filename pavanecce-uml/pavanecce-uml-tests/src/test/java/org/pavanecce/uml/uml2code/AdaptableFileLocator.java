package org.pavanecce.uml.uml2code;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.internal.adaptor.URLConverterImpl;
import org.pavanecce.uml.common.util.IFileLocator;

@SuppressWarnings("restriction")
public class AdaptableFileLocator implements IFileLocator {
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
					System.out.println("Not found:" + url);
				}
				return url;
			}
			return find;
		}
	}
}
