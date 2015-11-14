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
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.utils.command.Command;

public class FxCopBuilderTest {
	private FxCopBuilder builder ;
	
	@Before
	public void before() {
		builder = new FxCopBuilder();
		
	}
	
	@Test
	public void minimumSpec() {
		File rulesetFile=new File("C:\\ruleset");
		File reportFile=new File("C:\\report");
		List<String> directories = new ArrayList <String>();
		directories.add("dirA");
		directories.add("dirB");
		
		List<String> references = new ArrayList<String>();
		references.add("refA");
		references.add("refB");
		
		builder.setExecutable("C:\\fxCopDir");
		builder.setAssemblies("assemblies");
		builder.setRulesetFile(rulesetFile);
		builder.setReportFile(reportFile);
		builder.setDirectories(directories);
		builder.setReferences(references);
		Command command=builder.build();
		String commandLine=command.toString();
		assertNotNull(command);
		assertEquals("minimal commandline expected",
				"C:\\fxCopDir\\FxCopCmd.exe /file:assemblies /ruleset:=C:\\ruleset /out:C:\\report /outxsl:none /forceoutput /searchgac /directory:dirA /directory:dirB /reference:refA /reference:refB",commandLine);
	}
	
	@Test
	public void aspNetSpecified() {
		File rulesetFile=new File("C:\\ruleset");
		File reportFile=new File("C:\\report");
		List<String> directories = new ArrayList <String>();
		directories.add("dirA");
		directories.add("dirB");
		
		List<String> references = new ArrayList<String>();
		references.add("refA");
		references.add("refB");
		
		builder.setExecutable("C:\\fxCopDir");
		builder.setAssemblies("assemblies");
		builder.setRulesetFile(rulesetFile);
		builder.setReportFile(reportFile);
		builder.setDirectories(directories);
		builder.setReferences(references);
		builder.setAspnet(true);
		Command command=builder.build();
		String commandLine=command.toString();
		assertNotNull(command);
		assertTrue("aspNet set, expect in commandline " + commandLine,commandLine.contains(" /aspnet "));

	}
	
	@Test
	public void dictionaryPathSpecified() {
		File rulesetFile=new File("C:\\ruleset");
		File reportFile=new File("C:\\report");
		List<String> directories = new ArrayList <String>();
		directories.add("dirA");
		directories.add("dirB");
		
		List<String> references = new ArrayList<String>();
		references.add("refA");
		references.add("refB");
		
		builder.setExecutable("C:\\fxCopDir");
		builder.setAssemblies("assemblies");
		builder.setRulesetFile(rulesetFile);
		builder.setReportFile(reportFile);
		builder.setDirectories(directories);
		builder.setReferences(references);
		builder.setDictionary("dictionaryPath");
		Command command=builder.build();
		String commandLine=command.toString();
		assertNotNull(command);
		assertTrue("dictionary set, expect in commandline" + commandLine,commandLine.matches(".* /dictionary:dictionaryPath( |$)"));

	}
	@Test
	public void fxCopFullySpecified() {
		File rulesetFile=new File("C:\\ruleset");
		File reportFile=new File("C:\\report");
		List<String> directories = new ArrayList <String>();
		directories.add("dirA");
		directories.add("dirB");
		
		List<String> references = new ArrayList<String>();
		references.add("refA");
		references.add("refB");
		
		builder.setExecutable("C:\\fxCopDir\\FxCopCmd.exe");
		builder.setAssemblies("assemblies");
		builder.setRulesetFile(rulesetFile);
		builder.setReportFile(reportFile);
		builder.setDirectories(directories);
		builder.setReferences(references);
		Command command=builder.build();
		String commandLine=command.toString();
		assertNotNull(command);
		assertEquals("minimal commandline expected",
				"C:\\fxCopDir\\FxCopCmd.exe /file:assemblies /ruleset:=C:\\ruleset /out:C:\\report /outxsl:none /forceoutput /searchgac /directory:dirA /directory:dirB /reference:refA /reference:refB",commandLine);
	}
}
