/**
 * Copyright 2012 Liferay Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.phidias.compile.internal;

import java.net.URL;
import java.util.Collection;

import org.osgi.framework.wiring.BundleWiring;
import org.phidias.compile.ResourceResolver;

/**
 * @author Raymond Augé
 */
public class BasicResourceResolver implements ResourceResolver {

	@Override
	public URL getResource(BundleWiring bundleWiring, String name) {
		return bundleWiring.getBundle().getResource(name);
	}

	@Override
	public Collection<String> resolveResources(BundleWiring bundleWiring, String path, String filePattern, int options) {

		return bundleWiring.listResources(path, filePattern, options);
	}

}