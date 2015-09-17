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

import com.google.common.base.Preconditions;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.CommandExecutor;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FxCopRunner {

  private static final Logger LOG = LoggerFactory.getLogger(FxCopRunner.class);
  private static final String EXECUTABLE = "FxCopCmd.exe";
  private String executable;
private String assemblies;
private File rulesetFile;
private File reportFile;
private int timeout;
private boolean aspnet;
private String dictionaryPath;
private List<String> directories;
private List<String> references;

  public void setExecutable(String executable) {
	  this.executable=executable;
  }
  
  public void setAssemblies(String assemblies) {
	  this.assemblies = assemblies;
  }
  
  public void setRulesetFile(File rulesetFile) {
	  this.rulesetFile = rulesetFile;
  }
  
  public void setReportFile(File reportFile) {
	  this.reportFile = reportFile;
  }
  
  public void setTimeout(int timeout) {
	  this.timeout = timeout;
  }
  
  public void setAspnet(boolean aspnet) {
	  this.aspnet = aspnet ;
  }
  
  public void setDictionary(String dictionaryPath) {
	  this.dictionaryPath = dictionaryPath;
  }
  
  public void setDirectories(List<String> directories) {
	  this.directories = directories;
  }
  
  public void setReferences(List<String> references) {
	  this.references = references;
  }
  
  public void execute() {
	  Command command=build();
    int exitCode = CommandExecutor.create().execute(
      command,
      TimeUnit.MINUTES.toMillis(timeout));

    LOG.info("FxCopCmd.exe ended with the exit code: " + exitCode);

    Preconditions.checkState((exitCode & 1) == 0,
      "The execution of \"" + executable + "\" failed and returned " + exitCode
        + " as exit code. See http://msdn.microsoft.com/en-us/library/bb429400(v=vs.80).aspx for details.");
  }
  
	public Command build() {
		Command command = Command.create(getExecutable(executable))
				.addArgument("/file:" + assemblies)
				.addArgument("/ruleset:=" + rulesetFile.getAbsolutePath())
				.addArgument("/out:" + reportFile.getAbsolutePath())
				.addArgument("/outxsl:none").addArgument("/forceoutput")
				.addArgument("/searchgac");

		if (aspnet) {
			command.addArgument("/aspnet");
		}

		for (String directory : directories) {
			command.addArgument("/directory:" + directory);
		}
		for (String reference : references) {
			command.addArgument("/reference:" + reference);
		}

		if (!StringUtils.isEmpty(dictionaryPath)) {
			command.addArgument("/dictionary:" + dictionaryPath);
		}
		return command;
	}

  /**
   * Handles deprecated property: "installDirectory", which gives the path to the directory only.
   */
  private static String getExecutable(String path) {
    return path.endsWith(EXECUTABLE) ? path : new File(path, EXECUTABLE).getAbsolutePath();
  }

}
