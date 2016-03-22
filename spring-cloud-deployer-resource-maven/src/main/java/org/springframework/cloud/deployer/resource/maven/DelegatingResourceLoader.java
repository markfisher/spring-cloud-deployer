/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.deployer.resource.maven;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author Mark Fisher
 */
public class DelegatingResourceLoader implements ResourceLoader {

	private final ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

	private final Map<String, ResourceLoader> loaders;

	public DelegatingResourceLoader(Map<String, ResourceLoader> loaders) {
		Assert.notEmpty(loaders, "at least one ResourceLoader is required");
		this.loaders = Collections.unmodifiableMap(loaders);
	}

	@Override
	public Resource getResource(String location) {
		try {
			URI uri = new URI(location);
			String scheme = uri.getScheme();
			Assert.notNull(scheme, "a prefix is required");
			ResourceLoader loader = loaders.get(scheme);
			Assert.notNull(loader, String.format("no loader for prefix: %s", scheme));
			return loader.getResource(location);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	public static void main(String[] args) throws IOException {
		Map<String, ResourceLoader> delegates = new HashMap<>();
		delegates.put("maven", new MavenResourceLoader(new MavenProperties()));
		delegates.put("file", new FileSystemResourceLoader());
		DelegatingResourceLoader loader = new DelegatingResourceLoader(delegates);
		Resource resource1 = loader.getResource(
				"maven://org.springframework.cloud.stream.module:time-source:jar:exec:1.0.0.BUILD-SNAPSHOT");
		System.out.println("MavenResource:      " + resource1.getFile());
		String localMavenRepo = System.getProperty("user.home") +
				File.separator + ".m2" + File.separator + "repository";
		Resource resource2 = loader.getResource("file://" + localMavenRepo
				+ "/org/springframework/cloud/stream/module/time-source/"
				+ "1.0.0.BUILD-SNAPSHOT/time-source-1.0.0.BUILD-SNAPSHOT-exec.jar");
		System.out.println("FileSystemResource: " + resource2.getFile());
	}
}
