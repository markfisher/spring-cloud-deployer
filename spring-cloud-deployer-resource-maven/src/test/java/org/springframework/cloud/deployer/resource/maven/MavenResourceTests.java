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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

/**
 * Tests for {@link MavenResource}
 *
 * @author Venil Noronha
 * @author Janne Valkealahti
 * @author Mark Fisher
 */
public class MavenResourceTests {

	private static final String DEFAULT_ARTIFACT_ID = "timestamp-task";

	private static final String DEFAULT_GROUP_ID = "org.springframework.cloud.task.app";

	private static final String DEFAULT_VERSION = "1.0.0.BUILD-SNAPSHOT";

	private MavenResource snapshotResource(String artifactId) {
		MavenProperties properties = new MavenProperties();
		properties.setRemoteRepositories(new String[] {"https://repo.spring.io/libs-snapshot"});
		MavenResource resource = new MavenResource.Builder(properties)
				.artifactId(artifactId)
				.groupId(DEFAULT_GROUP_ID)
				.version(DEFAULT_VERSION)
				.build();
		return resource;
	}

	@Test
	public void mavenResourceFilename() {
		MavenResource resource = snapshotResource(DEFAULT_ARTIFACT_ID);
		assertNotNull("getFilename() returned null", resource.getFilename());
		assertEquals("getFilename() doesn't match the expected filename",
				"timestamp-task-1.0.0.BUILD-SNAPSHOT.jar", resource.getFilename());
	}

	@Test
	public void resourceExists() {
		MavenResource resource = snapshotResource(DEFAULT_ARTIFACT_ID);
		assertEquals(true, resource.exists());
	}

	@Test
	public void resourceDoesNotExist() {
		MavenResource resource = snapshotResource("doesnotexist");
		assertEquals(false, resource.exists());
	}

	@Test
	public void coordinatesParsed() {
		MavenResource resource = MavenResource.parse("example:foo:jar:exec:1.0.1");
		assertEquals("getFilename() doesn't match the expected filename",
				"foo-1.0.1-exec.jar", resource.getFilename());
		resource = MavenResource.parse("example:bar:1.0.2");
		assertEquals("getFilename() doesn't match the expected filename",
				"bar-1.0.2.jar", resource.getFilename());
	}

	@Test
	public void mavenResourceRetrievedFromNonDefaultRemoteRepository() throws Exception {
		String coordinates = "org.springframework.cloud.task.app:timestamp-task:jar:1.0.0.BUILD-SNAPSHOT";
		MavenProperties properties = new MavenProperties();
		String tempLocalRepo = System.getProperty("java.io.tmpdir") + File.separator + ".m2-test1";
		new File(tempLocalRepo).deleteOnExit();
		properties.setLocalRepository(tempLocalRepo);
		properties.setRemoteRepositories(new String[] {"https://repo.spring.io/libs-snapshot-local"});
		MavenResource resource = MavenResource.parse(coordinates, properties);
		assertEquals("getFilename() doesn't match the expected filename",
				"timestamp-task-1.0.0.BUILD-SNAPSHOT.jar", resource.getFilename());
	}

	@Test(expected = IllegalStateException.class)
	public void localResolutionFailsIfNotCached() throws Exception {
		String tempLocalRepo = System.getProperty("java.io.tmpdir") + File.separator + ".m2-test2";
		new File(tempLocalRepo).deleteOnExit();
		MavenProperties properties = new MavenProperties();
		properties.setLocalRepository(tempLocalRepo);
		properties.setRemoteRepositories(new String[0]);
		properties.setOffline(true);
		MavenResource resource = new MavenResource.Builder(properties)
				.artifactId("timestamp-task")
				.groupId("org.springframework.cloud.task.app")
				.version("1.0.0.BUILD-SNAPSHOT")
				.build();
		resource.getFile();
	}

	@Test
	public void localResolutionSucceedsIfCached() throws Exception {
		String coordinates = "org.springframework.cloud.task.app:timestamp-task:jar:1.0.0.BUILD-SNAPSHOT";
		MavenProperties properties1 = new MavenProperties();
		String tempLocalRepo = System.getProperty("java.io.tmpdir") + File.separator + ".m2-test3";
		new File(tempLocalRepo).deleteOnExit();
		properties1.setLocalRepository(tempLocalRepo);
		properties1.setRemoteRepositories(new String[] {"https://repo.spring.io/libs-snapshot-local"});
		MavenResource resource = MavenResource.parse(coordinates, properties1);
		resource.getFile();

		// no remotes; should not fail anymore
		MavenProperties properties2 = new MavenProperties();
		properties2.setLocalRepository(tempLocalRepo);
		properties2.setRemoteRepositories(new String[0]);
		properties2.setOffline(true);
		resource = new MavenResource.Builder(properties2)
				.artifactId("timestamp-task")
				.groupId("org.springframework.cloud.task.app")
				.version("1.0.0.BUILD-SNAPSHOT")
				.build();
		resource.getFile();
	}
	
}
