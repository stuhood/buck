/*
 * Copyright 2015-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.scala;

import com.facebook.buck.java.Classpaths;
import com.facebook.buck.java.JavaLibrary;
import com.facebook.buck.rules.AbstractBuildRule;
import com.facebook.buck.rules.AddToRuleKey;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.BuildableProperties;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.Tool;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MakeCleanDirectoryStep;
import com.facebook.buck.util.HumanReadableException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.nio.file.Path;
import java.util.LinkedHashSet;

public class ScalaLibrary extends AbstractBuildRule {
  @AddToRuleKey
  private final Tool compiler;
  @AddToRuleKey
  private final ImmutableSet<SourcePath> srcs;
  private final Path output;

  ScalaLibrary(
      BuildRuleParams params,
      SourcePathResolver resolver,
      ImmutableSet<SourcePath> srcs,
      Path output,
      Tool compiler) {
    super(params, resolver);
    this.srcs = srcs;
    this.output = output;
    this.compiler = compiler;
  }

  /**
   * TODO: should implement src/com/facebook/buck/java/HasClasspathEntries.java, except that
   * that interface currently assumes JavaLibrary all over the place. Need to split JVMLibrary
   * and JavaLibrary.
   */
  @VisibleForTesting
  LinkedHashSet<Path> getClasspath() {
    LinkedHashSet<Path> classpath = new LinkedHashSet<Path>();
    for (BuildRule buildRule : getDeps()) {
      if (buildRule instanceof ScalaLibrary) {
        classpath.add(Preconditions.checkNotNull(buildRule.getPathToOutput()));
      } else if (buildRule instanceof JavaLibrary) {
        classpath.addAll(Classpaths.getClasspathEntries(ImmutableSet.of(buildRule)).values());
      } else {
        throw new HumanReadableException(
            "%s (dep of %s) is not known to have a classpath!",
            buildRule.getBuildTarget().getFullyQualifiedName(),
            getBuildTarget().getFullyQualifiedName());
      }
    }
    return classpath;
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context,
      BuildableContext buildableContext) {
    return ImmutableList.of(
        new MakeCleanDirectoryStep(getProjectFilesystem(), output.getParent()),
        new ScalaCompileStep(
            getProjectFilesystem().getRootPath(),
            compiler.getCommandPrefix(getResolver()),
            output,
            getClasspath(),
            srcs));
  }

  @Override
  public Path getPathToOutput() {
    return output;
  }

  @Override
  public BuildableProperties getProperties() {
    return new BuildableProperties(BuildableProperties.Kind.LIBRARY);
  }
}
