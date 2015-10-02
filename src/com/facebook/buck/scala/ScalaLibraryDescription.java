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

import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.model.BuildTargets;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.BuildRuleType;
import com.facebook.buck.rules.Description;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.TargetGraph;
import com.facebook.infer.annotation.SuppressFieldNotInitialized;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.nio.file.Path;

public class ScalaLibraryDescription implements Description<ScalaLibraryDescription.Arg> {

  private static final BuildRuleType TYPE = BuildRuleType.of("scala_library");

  private final ScalaBuckConfig scalaBuckConfig;

  public ScalaLibraryDescription(ScalaBuckConfig scalaBuckConfig) {
    this.scalaBuckConfig = scalaBuckConfig;
  }

  @Override
  public BuildRuleType getBuildRuleType() {
    return TYPE;
  }

  @Override
  public Arg createUnpopulatedConstructorArg() {
    return new Arg();
  }

  @Override
  public <A extends Arg> BuildRule createBuildRule(
      TargetGraph targetGraph,
      BuildRuleParams params,
      BuildRuleResolver resolver,
      A args) {
    Path output =
      BuildTargets.getGenPath(
          params.getBuildTarget(), "%s/" + params.getBuildTarget().getShortName());
    return new ScalaLibrary(
        params,
        new SourcePathResolver(resolver),
        ImmutableSet.copyOf(args.srcs),
        output,
        scalaBuckConfig.getScalaCompiler().get());
  }

  @SuppressFieldNotInitialized
  public static class Arg {
    public ImmutableSet<SourcePath> srcs;
    public Optional<ImmutableSortedSet<BuildTarget>> deps;
  }
}
