/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.github.vete_l.support.gradle.dsl.model.android;

import com.android.tools.idea.gradle.dsl.model.GradleDslBlockModel;
import com.android.tools.idea.gradle.dsl.model.values.GradleNotNullValue;
import com.android.tools.idea.gradle.dsl.model.values.GradleNullableValue;
import com.android.tools.idea.gradle.dsl.parser.android.*;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.android.tools.idea.gradle.dsl.parser.android.AaptOptionsDslElement.AAPT_OPTIONS_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.AdbOptionsDslElement.ADB_OPTIONS_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.BuildTypesDslElement.BUILD_TYPES_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.DataBindingDslElement.DATA_BINDING_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.DexOptionsDslElement.DEX_OPTIONS_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.ExternalNativeBuildDslElement.EXTERNAL_NATIVE_BUILD_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.LintOptionsDslElement.LINT_OPTIONS_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.PackagingOptionsDslElement.PACKAGING_OPTIONS_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.ProductFlavorsDslElement.PRODUCT_FLAVORS_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.SigningConfigsDslElement.SIGNING_CONFIGS_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.SourceSetsDslElement.SOURCE_SETS_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.SplitsDslElement.SPLITS_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.TestOptionsDslElement.TEST_OPTIONS_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.elements.BaseCompileOptionsDslElement.COMPILE_OPTIONS_BLOCK_NAME;

public final class AndroidModel extends GradleDslBlockModel {
  @NonNls private static final String BUILD_TOOLS_VERSION = "buildToolsVersion";
  @NonNls private static final String COMPILE_SDK_VERSION = "compileSdkVersion";
  @NonNls private static final String DEFAULT_CONFIG = "defaultConfig";
  @NonNls private static final String DEFAULT_PUBLISH_CONFIG = "defaultPublishConfig";
  @NonNls private static final String FLAVOR_DIMENSIONS = "flavorDimensions";
  @NonNls private static final String GENERATE_PURE_SPLITS = "generatePureSplits";
  @NonNls private static final String PUBLISH_NON_DEFAULT = "publishNonDefault";
  @NonNls private static final String RESOURCE_PREFIX = "resourcePrefix";

  // TODO: Add support for useLibrary

  public AndroidModel(@NotNull AndroidDslElement dslElement) {
    super(dslElement);
  }

  @NotNull
  public AaptOptionsModel aaptOptions() {
    AaptOptionsDslElement aaptOptionsElement = myDslElement.getPropertyElement(AAPT_OPTIONS_BLOCK_NAME, AaptOptionsDslElement.class);
    if (aaptOptionsElement == null) {
      aaptOptionsElement = new AaptOptionsDslElement(myDslElement);
      myDslElement.setNewElement(AAPT_OPTIONS_BLOCK_NAME, aaptOptionsElement);
    }
    return new AaptOptionsModel(aaptOptionsElement);
  }

  @NotNull
  public AdbOptionsModel adbOptions() {
    AdbOptionsDslElement adbOptionsElement = myDslElement.getPropertyElement(ADB_OPTIONS_BLOCK_NAME, AdbOptionsDslElement.class);
    if (adbOptionsElement == null) {
      adbOptionsElement = new AdbOptionsDslElement(myDslElement);
      myDslElement.setNewElement(ADB_OPTIONS_BLOCK_NAME, adbOptionsElement);
    }
    return new AdbOptionsModel(adbOptionsElement);
  }

  @NotNull
  public GradleNullableValue<String> buildToolsVersion() {
    return getIntOrStringValue(BUILD_TOOLS_VERSION);
  }

  @NotNull
  public AndroidModel setBuildToolsVersion(int buildToolsVersion) {
    myDslElement.setNewLiteral(BUILD_TOOLS_VERSION, buildToolsVersion);
    return this;
  }

  @NotNull
  public AndroidModel setBuildToolsVersion(@NotNull String buildToolsVersion) {
    myDslElement.setNewLiteral(BUILD_TOOLS_VERSION, buildToolsVersion);
    return this;
  }

  @NotNull
  public AndroidModel removeBuildToolsVersion() {
    myDslElement.removeProperty(BUILD_TOOLS_VERSION);
    return this;
  }

  @NotNull
  public List<BuildTypeModel> buildTypes() {
    BuildTypesDslElement buildTypes = myDslElement.getPropertyElement(BUILD_TYPES_BLOCK_NAME, BuildTypesDslElement.class);
    return buildTypes == null ? ImmutableList.of() : buildTypes.get();
  }

  @NotNull
  public AndroidModel addBuildType(@NotNull String buildType) {
    BuildTypesDslElement buildTypes = myDslElement.getPropertyElement(BUILD_TYPES_BLOCK_NAME, BuildTypesDslElement.class);
    if (buildTypes == null) {
      buildTypes = new BuildTypesDslElement(myDslElement);
      myDslElement.setNewElement(BUILD_TYPES_BLOCK_NAME, buildTypes);
    }

    BuildTypeDslElement buildTypeElement = buildTypes.getPropertyElement(buildType, BuildTypeDslElement.class);
    if (buildTypeElement == null) {
      buildTypeElement = new BuildTypeDslElement(buildTypes, buildType);
      buildTypes.setNewElement(buildType, buildTypeElement);
    }
    return this;
  }

  @NotNull
  public AndroidModel removeBuildType(@NotNull String buildType) {
    BuildTypesDslElement buildTypes = myDslElement.getPropertyElement(BUILD_TYPES_BLOCK_NAME, BuildTypesDslElement.class);
    if (buildTypes != null) {
      buildTypes.removeProperty(buildType);
    }
    return this;
  }

  @NotNull
  public CompileOptionsModel compileOptions() {
    CompileOptionsDslElement element = myDslElement.getPropertyElement(COMPILE_OPTIONS_BLOCK_NAME, CompileOptionsDslElement.class);
    if (element == null) {
      element = new CompileOptionsDslElement(myDslElement);
      myDslElement.setNewElement(COMPILE_OPTIONS_BLOCK_NAME, element);
    }
    return new CompileOptionsModel(element, false);
  }

  @NotNull
  public GradleNullableValue<String> compileSdkVersion() {
    return getIntOrStringValue(COMPILE_SDK_VERSION);
  }

  @NotNull
  public AndroidModel setCompileSdkVersion(int compileSdkVersion) {
    myDslElement.setNewLiteral(COMPILE_SDK_VERSION, compileSdkVersion);
    return this;
  }

  @NotNull
  public AndroidModel setCompileSdkVersion(@NotNull String compileSdkVersion) {
    myDslElement.setNewLiteral(COMPILE_SDK_VERSION, compileSdkVersion);
    return this;
  }

  @NotNull
  public AndroidModel removeCompileSdkVersion() {
    myDslElement.removeProperty(COMPILE_SDK_VERSION);
    return this;
  }

  @NotNull
  public DataBindingModel dataBinding() {
    DataBindingDslElement dataBindingElement = myDslElement.getPropertyElement(DATA_BINDING_BLOCK_NAME, DataBindingDslElement.class);
    if (dataBindingElement == null) {
      dataBindingElement = new DataBindingDslElement(myDslElement);
      myDslElement.setNewElement(DATA_BINDING_BLOCK_NAME, dataBindingElement);
    }
    return new DataBindingModel(dataBindingElement);
  }

  @NotNull
  public ProductFlavorModel defaultConfig() {
    ProductFlavorDslElement defaultConfigElement = myDslElement.getPropertyElement(DEFAULT_CONFIG, ProductFlavorDslElement.class);
    if (defaultConfigElement == null) {
      defaultConfigElement = new ProductFlavorDslElement(myDslElement, DEFAULT_CONFIG);
      myDslElement.setNewElement(DEFAULT_CONFIG, defaultConfigElement);
    }
    return new ProductFlavorModel(defaultConfigElement);
  }

  @NotNull
  public GradleNullableValue<String> defaultPublishConfig() {
    return myDslElement.getLiteralProperty(DEFAULT_PUBLISH_CONFIG, String.class);
  }

  @NotNull
  public AndroidModel setDefaultPublishConfig(@NotNull String defaultPublishConfig) {
    myDslElement.setNewLiteral(DEFAULT_PUBLISH_CONFIG, defaultPublishConfig);
    return this;
  }

  @NotNull
  public AndroidModel removeDefaultPublishConfig() {
    myDslElement.removeProperty(DEFAULT_PUBLISH_CONFIG);
    return this;
  }

  @NotNull
  public DexOptionsModel dexOptions() {
    DexOptionsDslElement dexOptionsElement = myDslElement.getPropertyElement(DEX_OPTIONS_BLOCK_NAME, DexOptionsDslElement.class);
    if (dexOptionsElement == null) {
      dexOptionsElement = new DexOptionsDslElement(myDslElement);
      myDslElement.setNewElement(DEX_OPTIONS_BLOCK_NAME, dexOptionsElement);
    }
    return new DexOptionsModel(dexOptionsElement);
  }

  @NotNull
  public ExternalNativeBuildModel externalNativeBuild() {
    ExternalNativeBuildDslElement externalNativeBuildDslElement = myDslElement.getPropertyElement(EXTERNAL_NATIVE_BUILD_BLOCK_NAME,
                                                                                                  ExternalNativeBuildDslElement.class);
    if (externalNativeBuildDslElement == null) {
      externalNativeBuildDslElement = new ExternalNativeBuildDslElement(myDslElement);
      myDslElement.setNewElement(EXTERNAL_NATIVE_BUILD_BLOCK_NAME, externalNativeBuildDslElement);
    }
    return new ExternalNativeBuildModel(externalNativeBuildDslElement);
  }

  @Nullable
  public List<GradleNotNullValue<String>> flavorDimensions() {
    return myDslElement.getListProperty(FLAVOR_DIMENSIONS, String.class);
  }

  @NotNull
  public AndroidModel addFlavorDimension(@NotNull String flavorDimension) {
    myDslElement.addToNewLiteralList(FLAVOR_DIMENSIONS, flavorDimension);
    return this;
  }

  @NotNull
  public AndroidModel removeFlavorDimension(@NotNull String flavorDimension) {
    myDslElement.removeFromExpressionList(FLAVOR_DIMENSIONS, flavorDimension);
    return this;
  }

  @NotNull
  public AndroidModel removeAllFlavorDimensions() {
    myDslElement.removeProperty(FLAVOR_DIMENSIONS);
    return this;
  }

  @NotNull
  public AndroidModel replaceFlavorDimension(@NotNull String oldFlavorDimension, @NotNull String newFlavorDimension) {
    myDslElement.replaceInExpressionList(FLAVOR_DIMENSIONS, oldFlavorDimension, newFlavorDimension);
    return this;
  }

  @NotNull
  public GradleNullableValue<Boolean> generatePureSplits() {
    return myDslElement.getLiteralProperty(GENERATE_PURE_SPLITS, Boolean.class);
  }

  @NotNull
  public AndroidModel setGeneratePureSplits(boolean generatePureSplits) {
    myDslElement.setNewLiteral(GENERATE_PURE_SPLITS, generatePureSplits);
    return this;
  }

  @NotNull
  public AndroidModel removeGeneratePureSplits() {
    myDslElement.removeProperty(GENERATE_PURE_SPLITS);
    return this;
  }

  @NotNull
  public LintOptionsModel lintOptions() {
    LintOptionsDslElement lintOptionsDslElement = myDslElement.getPropertyElement(LINT_OPTIONS_BLOCK_NAME, LintOptionsDslElement.class);
    if (lintOptionsDslElement == null) {
      lintOptionsDslElement = new LintOptionsDslElement(myDslElement);
      myDslElement.setNewElement(LINT_OPTIONS_BLOCK_NAME, lintOptionsDslElement);
    }
    return new LintOptionsModel(lintOptionsDslElement);
  }

  @NotNull
  public PackagingOptionsModel packagingOptions() {
    PackagingOptionsDslElement packagingOptionsDslElement =
      myDslElement.getPropertyElement(PACKAGING_OPTIONS_BLOCK_NAME, PackagingOptionsDslElement.class);
    if (packagingOptionsDslElement == null) {
      packagingOptionsDslElement = new PackagingOptionsDslElement(myDslElement);
      myDslElement.setNewElement(PACKAGING_OPTIONS_BLOCK_NAME, packagingOptionsDslElement);
    }
    return new PackagingOptionsModel(packagingOptionsDslElement);
  }

  @NotNull
  public List<ProductFlavorModel> productFlavors() {
    ProductFlavorsDslElement productFlavors = myDslElement.getPropertyElement(PRODUCT_FLAVORS_BLOCK_NAME, ProductFlavorsDslElement.class);
    return productFlavors == null ? ImmutableList.of() : productFlavors.get();
  }

  @NotNull
  public AndroidModel addProductFlavor(@NotNull String flavor) {
    ProductFlavorsDslElement productFlavors = myDslElement.getPropertyElement(PRODUCT_FLAVORS_BLOCK_NAME, ProductFlavorsDslElement.class);
    if (productFlavors == null) {
      productFlavors = new ProductFlavorsDslElement(myDslElement);
      myDslElement.setNewElement(PRODUCT_FLAVORS_BLOCK_NAME, productFlavors);
    }

    ProductFlavorDslElement flavorElement = productFlavors.getPropertyElement(flavor, ProductFlavorDslElement.class);
    if (flavorElement == null) {
      flavorElement = new ProductFlavorDslElement(productFlavors, flavor);
      productFlavors.setNewElement(flavor, flavorElement);
    }
    return this;
  }

  @NotNull
  public AndroidModel removeProductFlavor(@NotNull String flavor) {
    ProductFlavorsDslElement productFlavors = myDslElement.getPropertyElement(PRODUCT_FLAVORS_BLOCK_NAME, ProductFlavorsDslElement.class);
    if (productFlavors != null) {
      productFlavors.removeProperty(flavor);
    }
    return this;
  }

  @NotNull
  public List<SigningConfigModel> signingConfigs() {
    SigningConfigsDslElement signingConfigs = myDslElement.getPropertyElement(SIGNING_CONFIGS_BLOCK_NAME, SigningConfigsDslElement.class);
    return signingConfigs == null ? ImmutableList.of() : signingConfigs.get();
  }

  @NotNull
  public AndroidModel addSigningConfig(@NotNull String config) {
    SigningConfigsDslElement signingConfigs = myDslElement.getPropertyElement(SIGNING_CONFIGS_BLOCK_NAME, SigningConfigsDslElement.class);
    if (signingConfigs == null) {
      signingConfigs = new SigningConfigsDslElement(myDslElement);
      myDslElement.setNewElement(SIGNING_CONFIGS_BLOCK_NAME, signingConfigs);
    }

    SigningConfigDslElement configElement = signingConfigs.getPropertyElement(config, SigningConfigDslElement.class);
    if (configElement == null) {
      configElement = new SigningConfigDslElement(signingConfigs, config);
      signingConfigs.setNewElement(config, configElement);
    }
    return this;
  }

  @NotNull
  public AndroidModel removeSigningConfig(@NotNull String configName) {
    SigningConfigsDslElement signingConfig = myDslElement.getPropertyElement(SIGNING_CONFIGS_BLOCK_NAME, SigningConfigsDslElement.class);
    if (signingConfig != null) {
      signingConfig.removeProperty(configName);
    }
    return this;
  }

  @NotNull
  public List<SourceSetModel> sourceSets() {
    SourceSetsDslElement sourceSets = myDslElement.getPropertyElement(SOURCE_SETS_BLOCK_NAME, SourceSetsDslElement.class);
    return sourceSets == null ? ImmutableList.of() : sourceSets.get();
  }

  @NotNull
  public AndroidModel addSourceSet(@NotNull String sourceSet) {
    SourceSetsDslElement sourceSets = myDslElement.getPropertyElement(SOURCE_SETS_BLOCK_NAME, SourceSetsDslElement.class);
    if (sourceSets == null) {
      sourceSets = new SourceSetsDslElement(myDslElement);
      myDslElement.setNewElement(SOURCE_SETS_BLOCK_NAME, sourceSets);
    }

    SourceSetDslElement sourceSetElement = sourceSets.getPropertyElement(sourceSet, SourceSetDslElement.class);
    if (sourceSetElement == null) {
      sourceSetElement = new SourceSetDslElement(sourceSets, sourceSet);
      sourceSets.setNewElement(sourceSet, sourceSetElement);
    }
    return this;
  }

  @NotNull
  public AndroidModel removeSourceSet(@NotNull String sourceSet) {
    SourceSetsDslElement sourceSets = myDslElement.getPropertyElement(SOURCE_SETS_BLOCK_NAME, SourceSetsDslElement.class);
    if (sourceSets != null) {
      sourceSets.removeProperty(sourceSet);
    }
    return this;
  }

  @NotNull
  public SplitsModel splits() {
    SplitsDslElement splitsDslElement = myDslElement.getPropertyElement(SPLITS_BLOCK_NAME, SplitsDslElement.class);
    if (splitsDslElement == null) {
      splitsDslElement = new SplitsDslElement(myDslElement);
      myDslElement.setNewElement(SPLITS_BLOCK_NAME, splitsDslElement);
    }
    return new SplitsModel(splitsDslElement);
  }

  @NotNull
  public TestOptionsModel testOptions() {
    TestOptionsDslElement testOptionsDslElement = myDslElement.getPropertyElement(TEST_OPTIONS_BLOCK_NAME, TestOptionsDslElement.class);
    if (testOptionsDslElement == null) {
      testOptionsDslElement = new TestOptionsDslElement(myDslElement);
      myDslElement.setNewElement(TEST_OPTIONS_BLOCK_NAME, testOptionsDslElement);
    }
    return new TestOptionsModel(testOptionsDslElement);
  }

  @NotNull
  public GradleNullableValue<Boolean> publishNonDefault() {
    return myDslElement.getLiteralProperty(PUBLISH_NON_DEFAULT, Boolean.class);
  }

  @NotNull
  public AndroidModel setPublishNonDefault(boolean publishNonDefault) {
    myDslElement.setNewLiteral(PUBLISH_NON_DEFAULT, publishNonDefault);
    return this;
  }

  @NotNull
  public AndroidModel removePublishNonDefault() {
    myDslElement.removeProperty(PUBLISH_NON_DEFAULT);
    return this;
  }

  @NotNull
  public GradleNullableValue<String> resourcePrefix() {
    return myDslElement.getLiteralProperty(RESOURCE_PREFIX, String.class);
  }

  @NotNull
  public AndroidModel setResourcePrefix(@NotNull String resourcePrefix) {
    myDslElement.setNewLiteral(RESOURCE_PREFIX, resourcePrefix);
    return this;
  }

  @NotNull
  public AndroidModel removeResourcePrefix() {
    myDslElement.removeProperty(RESOURCE_PREFIX);
    return this;
  }
}
