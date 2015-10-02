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

import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.shell.ShellStep;
import com.facebook.buck.step.ExecutionContext;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.nio.file.Path;
import java.util.LinkedHashSet;

public class ScalaCompileStep extends ShellStep {

  private final static Joiner CP_JOINER = Joiner.on(":");

  private final ImmutableList<String> compilerCommandPrefix;
  private final Path output;
  private final LinkedHashSet<Path> classpath;
  private final ImmutableSet<SourcePath> srcs;

  /** TODO: pass upstream analysis to zinc */
  public ScalaCompileStep(
      Path workingDirectory,
      ImmutableList<String> compilerCommandPrefix,
      Path output,
      // TODO: use src/com/facebook/buck/java/Classpaths ?
      LinkedHashSet<Path> classpath,
      ImmutableSet<SourcePath> srcs) {
    super(workingDirectory);
    this.compilerCommandPrefix = compilerCommandPrefix;
    this.output = output;
    this.classpath = classpath;
    this.srcs = srcs;
  }

  @Override
  protected ImmutableList<String> getShellCommandInternal(ExecutionContext context) {
    ImmutableList.Builder<String> commandBuilder = ImmutableList.<String>builder()
        .addAll(compilerCommandPrefix)
        .add("-d", output.toString())
        .add("-cp", CP_JOINER.join(classpath))
        .addAll(FluentIterable.from(srcs).transform(Functions.toStringFunction()));

    return commandBuilder.build();
  }

  @Override
  public String getShortName() {
    return "scala compile";
  }
}
