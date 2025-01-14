/*
 * Copyright 2012-present Facebook, Inc.
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

package com.facebook.buck.rules;

import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.model.Flavor;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Standard set of parameters that is passed to all build rules.
 */
@Beta
public class BuildRuleParams {

  private final BuildTarget buildTarget;
  private final Supplier<ImmutableSortedSet<BuildRule>> declaredDeps;
  private final Supplier<ImmutableSortedSet<BuildRule>> extraDeps;
  private final Supplier<ImmutableSortedSet<BuildRule>> totalDeps;
  private final ProjectFilesystem projectFilesystem;
  private final RuleKeyBuilderFactory ruleKeyBuilderFactory;

  public BuildRuleParams(
      BuildTarget buildTarget,
      final Supplier<ImmutableSortedSet<BuildRule>> declaredDeps,
      final Supplier<ImmutableSortedSet<BuildRule>> extraDeps,
      ProjectFilesystem projectFilesystem,
      RuleKeyBuilderFactory ruleKeyBuilderFactory) {
    this.buildTarget = buildTarget;
    this.declaredDeps = Suppliers.memoize(declaredDeps);
    this.extraDeps = Suppliers.memoize(extraDeps);
    this.projectFilesystem = projectFilesystem;
    this.ruleKeyBuilderFactory = ruleKeyBuilderFactory;

    this.totalDeps = Suppliers.memoize(
        new Supplier<ImmutableSortedSet<BuildRule>>() {

          @Override
          public ImmutableSortedSet<BuildRule> get() {
            return ImmutableSortedSet.<BuildRule>naturalOrder()
                .addAll(declaredDeps.get())
                .addAll(extraDeps.get())
                .build();
          }
        });
  }

  public BuildRuleParams copyWithExtraDeps(Supplier<ImmutableSortedSet<BuildRule>> extraDeps) {
    return copyWithDeps(declaredDeps, extraDeps);
  }

  public BuildRuleParams appendExtraDeps(
      final Supplier<? extends Iterable<? extends BuildRule>> additional) {
    return copyWithDeps(
        declaredDeps,
        new Supplier<ImmutableSortedSet<BuildRule>>() {

          @Override
          public ImmutableSortedSet<BuildRule> get() {
            return ImmutableSortedSet.<BuildRule>naturalOrder()
                .addAll(extraDeps.get())
                .addAll(additional.get())
                .build();
          }
        });
  }

  public BuildRuleParams appendExtraDeps(Iterable<? extends BuildRule> additional) {
    return appendExtraDeps(Suppliers.ofInstance(additional));
  }

  public BuildRuleParams copyWithDeps(
      Supplier<ImmutableSortedSet<BuildRule>> declaredDeps,
      Supplier<ImmutableSortedSet<BuildRule>> extraDeps) {
    return copyWithChanges(buildTarget, declaredDeps, extraDeps);
  }

  public BuildRuleParams copyWithBuildTarget(BuildTarget target) {
    return copyWithChanges(target, declaredDeps, extraDeps);
  }

  public BuildRuleParams copyWithChanges(
      BuildTarget buildTarget,
      Supplier<ImmutableSortedSet<BuildRule>> declaredDeps,
      Supplier<ImmutableSortedSet<BuildRule>> extraDeps) {
    return new BuildRuleParams(
        buildTarget,
        declaredDeps,
        extraDeps,
        projectFilesystem,
        ruleKeyBuilderFactory);
  }

  public BuildRuleParams withoutFlavor(Flavor flavor) {
    Set<Flavor> flavors = Sets.newHashSet(getBuildTarget().getFlavors());
    Preconditions.checkArgument(flavors.contains(flavor));
    flavors.remove(flavor);
    BuildTarget target = BuildTarget
        .builder(getBuildTarget().getUnflavoredBuildTarget())
        .addAllFlavors(flavors)
        .build();

    return copyWithChanges(
        target,
        declaredDeps,
        extraDeps);
  }

  public BuildRuleParams withFlavor(Flavor flavor) {
    Set<Flavor> flavors = Sets.newHashSet(getBuildTarget().getFlavors());
    Preconditions.checkArgument(!flavors.contains(flavor));
    flavors.add(flavor);
    BuildTarget target = BuildTarget
        .builder(getBuildTarget().getUnflavoredBuildTarget())
        .addAllFlavors(flavors)
        .build();

    return copyWithChanges(
        target,
        declaredDeps,
        extraDeps);
  }

  public BuildTarget getBuildTarget() {
    return buildTarget;
  }

  public ImmutableSortedSet<BuildRule> getDeps() {
    return totalDeps.get();
  }

  public Supplier<ImmutableSortedSet<BuildRule>> getTotalDeps() {
    return totalDeps;
  }

  public Supplier<ImmutableSortedSet<BuildRule>> getDeclaredDeps() {
    return declaredDeps;
  }

  public Supplier<ImmutableSortedSet<BuildRule>> getExtraDeps() {
    return extraDeps;
  }

  public ProjectFilesystem getProjectFilesystem() {
    return projectFilesystem;
  }

  public RuleKeyBuilderFactory getRuleKeyBuilderFactory() {
    return ruleKeyBuilderFactory;
  }

}
