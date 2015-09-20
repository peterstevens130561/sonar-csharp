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

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.CommandExecutor;
import org.sonar.plugins.fxcop.FxCopConfiguration;
import org.sonar.plugins.fxcop.FxCopIssue;
import org.sonar.plugins.fxcop.FxCopReportParser;
import org.sonar.plugins.fxcop.FxCopRulesetWriter;
import org.sonar.plugins.fxcop.FxCopSensor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;


public class CSharpFxCopSensor implements Sensor {

	  private static final String CUSTOM_RULE_KEY = "CustomRuleTemplate";
	  private static final String CUSTOM_RULE_CHECK_ID_PARAMETER = "CheckId";
	  private static final Logger LOG = LoggerFactory.getLogger(FxCopSensor.class);

	  private final Settings settings;
	  private final RulesProfile profile;
	  private final FileSystem fs;
	  private final ResourcePerspectives perspectives;
	  private final FxCopConfiguration fxCopConf=CSharpFxCopProvider.getConfiguration();

	  public CSharpFxCopSensor( Settings settings, RulesProfile profile, FileSystem fs, ResourcePerspectives perspectives) {
	    this.settings = settings;
	    this.profile = profile;
	    this.fs = fs;
	    this.perspectives = perspectives;
	  }

	  @Override
	  public boolean shouldExecuteOnProject(Project project) {
	    boolean shouldExecute;

	    if (!hasFilesToAnalyze()) {
	      shouldExecute = false;
	    } else if (profile.getActiveRulesByRepository(fxCopConf.repositoryKey()).isEmpty()) {
	      LOG.info("All FxCop rules are disabled, skipping its execution.");
	      shouldExecute = false;
	    } else {
	      shouldExecute = true;
	    }

	    return shouldExecute;
	  }

	  private boolean hasFilesToAnalyze() {
	    return fs.files(fs.predicates().and(fs.predicates().hasLanguage(fxCopConf.languageKey()), fs.predicates().hasType(Type.MAIN))).iterator().hasNext();
	  }

	  @Override
	  public void analyse(Project project, SensorContext context) {
	    analyse(context, new FxCopRulesetWriter(), new FxCopReportParser(), new FxCopBuilder());
	  }

	  @VisibleForTesting
	  void analyse(SensorContext context, FxCopRulesetWriter writer, FxCopReportParser parser, FxCopBuilder fxCopRunner) {
	    fxCopConf.checkProperties(settings);

	    File reportFile;
	    String reportPath = settings.getString(fxCopConf.reportPathPropertyKey());
	    if (reportPath == null) {
	      reportFile = runFxCop(writer, fxCopRunner);
	    } else {
	      LOG.debug("Using the provided FxCop report" + reportPath);
	      reportFile = new File(reportPath);
	    }

	    for (FxCopIssue issue : parser.parse(reportFile)) {
	      if (!hasFileAndLine(issue)) {
	        logSkippedIssue(issue, "which has no associated file.");
	        continue;
	      }

	      File file = new File(new File(issue.path()), issue.file());
	      InputFile inputFile = fs.inputFile(fs.predicates().and(fs.predicates().hasType(Type.MAIN), fs.predicates().hasAbsolutePath(file.getAbsolutePath())));
	      if (inputFile == null) {
	        logSkippedIssueOutsideOfSonarQube(issue, file);
	      } else if (fxCopConf.languageKey().equals(inputFile.language())) {
	        Issuable issuable = perspectives.as(Issuable.class, inputFile);
	        if (issuable == null) {
	          logSkippedIssueOutsideOfSonarQube(issue, file);
	        } else {
	          issuable.addIssue(
	            issuable.newIssueBuilder()
	              .ruleKey(RuleKey.of(fxCopConf.repositoryKey(), ruleKey(issue.ruleConfigKey())))
	              .line(issue.line())
	              .message(issue.message())
	              .build());
	        }
	      }
	    }
	  }

	private File runFxCop(@Nonnull FxCopRulesetWriter writer, @Nonnull FxCopBuilder fxCopBuilder) {
		File rulesetFile = new File(fs.workDir(), "fxcop-sonarqube.ruleset");
	      writer.write(enabledRuleConfigKeys(), rulesetFile);

	      String executable=settings.getString(fxCopConf.fxCopCmdPropertyKey());
	      fxCopBuilder.setExecutable(executable);
	     
	      String assembly=settings.getString(fxCopConf.assemblyPropertyKey());
	      fxCopBuilder.setAssemblies(assembly);
	      
	      fxCopBuilder.setRulesetFile(rulesetFile);
	      
	      File reportFile = new File(fs.workDir(), "fxcop-report.xml");
	      fxCopBuilder.setReportFile(reportFile);
	      
	      int timeout=settings.getInt(fxCopConf.timeoutPropertyKey());
	      fxCopBuilder.setTimeout(timeout);
	      
	      boolean isAspnet=settings.getBoolean(fxCopConf.aspnetPropertyKey());
	      fxCopBuilder.setAspnet(isAspnet);
	      
	      List<String> directories=splitOnCommas(settings.getString(fxCopConf.directoriesPropertyKey()));
	      fxCopBuilder.setDirectories(directories);
	      
	      
	      List<String> references=splitOnCommas(settings.getString(fxCopConf.referencesPropertyKey()));
	      fxCopBuilder.setReferences(references);
	      
	      String dictionaryPath=settings.getString(CSharpFxCopProvider.FXCOP_DICTIONARY_PROPERTY_KEY);
	      fxCopBuilder.setDictionary(dictionaryPath);
	      
	      Command command=fxCopBuilder.build();
		    int exitCode = CommandExecutor.create().execute(
		      command,
		      TimeUnit.MINUTES.toMillis(timeout));
		    Preconditions.checkState((exitCode & 1) == 0,
		      "The execution of \"" + executable + "\" failed and returned " + exitCode
		        + " as exit code. See http://msdn.microsoft.com/en-us/library/bb429400(v=vs.80).aspx for details.");
		return reportFile;
	}

	  private static List<String> splitOnCommas(@Nullable String property) {
	    if (property == null) {
	      return ImmutableList.of();
	    } else {
	      return ImmutableList.copyOf(Splitter.on(",").trimResults().omitEmptyStrings().split(property));
	    }
	  }

	  private static boolean hasFileAndLine(FxCopIssue issue) {
	    return issue.path() != null && issue.file() != null && issue.line() != null;
	  }

	  private static void logSkippedIssueOutsideOfSonarQube(FxCopIssue issue, File file) {
	    logSkippedIssue(issue, "whose file \"" + file.getAbsolutePath() + "\" is not in SonarQube.");
	  }

	  private static void logSkippedIssue(FxCopIssue issue, String reason) {
	    LOG.debug("Skipping the FxCop issue at line " + issue.reportLine() + " " + reason);
	  }

	  private List<String> enabledRuleConfigKeys() {
	    ImmutableList.Builder<String> builder = ImmutableList.builder();
	    for (ActiveRule activeRule : profile.getActiveRulesByRepository(fxCopConf.repositoryKey())) {
	      if (!CUSTOM_RULE_KEY.equals(activeRule.getRuleKey())) {
	        String effectiveConfigKey = activeRule.getConfigKey();
	        if (effectiveConfigKey == null) {
	          effectiveConfigKey = activeRule.getParameter(CUSTOM_RULE_CHECK_ID_PARAMETER);
	        }

	        builder.add(effectiveConfigKey);
	      }
	    }
	    return builder.build();
	  }

	  private String ruleKey(String ruleConfigKey) {
	    for (ActiveRule activeRule : profile.getActiveRulesByRepository(fxCopConf.repositoryKey())) {
	      if (ruleConfigKey.equals(activeRule.getConfigKey()) || ruleConfigKey.equals(activeRule.getParameter(CUSTOM_RULE_CHECK_ID_PARAMETER))) {
	        return activeRule.getRuleKey();
	      }
	    }

	    throw new IllegalStateException(
	      "Unable to find the rule key corresponding to the rule config key \"" + ruleConfigKey + "\" in repository \"" + fxCopConf.repositoryKey() + "\".");
	  }

}
