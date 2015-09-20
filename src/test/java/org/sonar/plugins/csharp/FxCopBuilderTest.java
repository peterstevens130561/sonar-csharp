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
