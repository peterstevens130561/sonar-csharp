/*
 * SonarQube C# Plugin
 * Copyright (C) 2014 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.csharp;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.plugins.fxcop.FxCopReportParser;
import org.sonar.plugins.fxcop.FxCopRulesetWriter;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FxCopSensorTest {
	@Mock FxCopBuilder fxCopBuilder ;
	@Mock private Settings settings;
	@Mock private RulesProfile profile;
	private UnitTestFileSystem fs;
	@Mock private ResourcePerspectives perspectives;
	@Mock private Iterable<File> files;
	@Mock private Iterator<File> fileIterator;
	private CSharpFxCopSensor sensor ;
	@Mock private Project project;
	@Mock private List<ActiveRule> rules;
	@Mock private SensorContext context;
	@Mock private FxCopRulesetWriter writer;
	@Mock private FxCopReportParser parser;
	@Mock private FxCopBuilder fxCopRunner;
	
	@Before
	public void before() {
		org.mockito.MockitoAnnotations.initMocks(this);
		when(files.iterator()).thenReturn(fileIterator);
		
		fs  = new UnitTestFileSystem();
		fs.setIterable(files);
		sensor = new CSharpFxCopSensor(settings,profile,fs,perspectives);
		when(profile.getActiveRulesByRepository("fxcop")).thenReturn(rules);
	}
	
	@Test
	public void NoRulesEnabledShouldNotExecute() {
		when(fileIterator.hasNext()).thenReturn(true);
		when(rules.isEmpty()).thenReturn(true);
	
		boolean result=sensor.shouldExecuteOnProject(project);
		
		assertFalse("no rules defined, should not execute",result);
		verify(profile,times(1)).getActiveRulesByRepository("fxcop");
	}
	
	@Test
	public void NoFilesShouldNotExecute() {
		when(fileIterator.hasNext()).thenReturn(false);	
		when(rules.isEmpty()).thenReturn(false);
		
		boolean result=sensor.shouldExecuteOnProject(project);
		
		assertFalse("no files, should not execute",result);
		verify(profile,times(0)).getActiveRulesByRepository("fxcop");
	}
	
	@Test
	public void RulesEnabledAndFilesShouldExecute() {
		when(fileIterator.hasNext()).thenReturn(true);		
		when(rules.isEmpty()).thenReturn(false);
		
		boolean result=sensor.shouldExecuteOnProject(project);
		
		assertTrue("files found & rules enables: should execute",result);
		verify(profile,times(1)).getActiveRulesByRepository("fxcop");
	}
	
	@Test
	public void analyseTest() {
		when(settings.hasKey("sonar.cs.fxcop.assembly")).thenReturn(true);
		when(settings.getString("sonar.cs.fxcop.assembly")).thenReturn("assembly.dll");
		sensor.analyse(context, writer, parser, fxCopRunner);
		
	}
	private class UnitTestFileSystem extends DefaultFileSystem {

		private Iterable<File> iterable;
		void setIterable(Iterable<File> iterable) {
			this.iterable=iterable;
		}
		@Override 
		public Iterable<File> files(FilePredicate predicate) {
			return iterable;
			
		}
	}
}
