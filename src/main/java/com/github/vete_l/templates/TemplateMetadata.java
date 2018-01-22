/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.github.vete_l.templates;

import com.android.SdkConstants;
import com.android.annotations.VisibleForTesting;
import com.android.sdklib.AndroidTargetHash;
import com.android.sdklib.AndroidVersion;
import com.android.tools.idea.npw.assetstudio.icon.AndroidIconType;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.github.vete_l.templates.Template.*;

/**
 * An ADT template along with metadata
 */
public class TemplateMetadata {
  public static final String ATTR_PARENT_ACTIVITY_CLASS = "parentActivityClass";
  public static final String ATTR_ACTIVITY_TITLE = "activityTitle";
  public static final String ATTR_IS_LAUNCHER = "isLauncher";
  public static final String ATTR_IS_LIBRARY_MODULE = "isLibraryProject";
  public static final String ATTR_CREATE_ICONS = "createIcons";
  public static final String ATTR_COPY_ICONS = "copyIcons";
  public static final String ATTR_TARGET_API = "targetApi";
  public static final String ATTR_TARGET_API_STRING = "targetApiString";
  public static final String ATTR_MIN_API = "minApi";
  public static final String ATTR_MIN_BUILD_API = "minBuildApi";
  public static final String ATTR_BUILD_API = "buildApi";
  public static final String ATTR_BUILD_API_STRING = "buildApiString";
  public static final String ATTR_BUILD_API_REVISION = "buildApiRevision";
  public static final String ATTR_HAS_APPLICATION_THEME = "hasApplicationTheme";
  public static final String ATTR_REVISION = "revision";
  public static final String ATTR_MIN_API_LEVEL = "minApiLevel";
  public static final String ATTR_PACKAGE_NAME = "packageName";
  public static final String ATTR_PACKAGE_ROOT = "packageRoot";
  public static final String ATTR_APP_TITLE = "appTitle";
  public static final String ATTR_IS_NEW_PROJECT = "isNewProject";
  public static final String ATTR_THEME_EXISTS = "themeExists";
  public static final String ATTR_IS_GRADLE = "isGradle";
  public static final String ATTR_TOP_OUT = "topOut";
  public static final String ATTR_PROJECT_OUT = "projectOut";
  public static final String ATTR_SRC_OUT = "srcOut";
  public static final String ATTR_RES_OUT = "resOut";
  public static final String ATTR_MANIFEST_OUT = "manifestOut";
  public static final String ATTR_TEST_OUT = "testOut";
  public static final String ATTR_SRC_DIR = "srcDir";
  public static final String ATTR_RES_DIR = "resDir";
  public static final String ATTR_MANIFEST_DIR = "manifestDir";
  public static final String ATTR_TEST_DIR = "testDir";
  public static final String ATTR_AIDL_DIR = "aidlDir";
  public static final String ATTR_AIDL_OUT = "aidlOut";
  public static final String ATTR_DEBUG_KEYSTORE_SHA1 = "debugKeystoreSha1";
  public static final String ATTR_BUILD_TOOLS_VERSION = "buildToolsVersion";
  public static final String ATTR_GRADLE_PLUGIN_VERSION = "gradlePluginVersion";
  public static final String ATTR_GRADLE_VERSION = "gradleVersion";
  public static final String ATTR_JAVA_VERSION = "javaVersion";
  public static final String ATTR_SDK_DIR = "sdkDir";
  public static final String ATTR_APPLICATION_PACKAGE = "applicationPackage";
  public static final String ATTR_SOURCE_PROVIDER_NAME = "sourceProviderName";
  public static final String ATTR_MODULE_NAME = "projectName";
  public static final String ATTR_CREATE_ACTIVITY = "createActivity";
  public static final String ATTR_INCLUDE_FORM_FACTOR = "included";
  public static final String ATTR_IS_LOW_MEMORY = "isLowMemory";
  public static final String ATTR_ESPRESSO_VERSION = "espressoVersion";
  public static final String ATTR_NUM_ENABLED_FORM_FACTORS = "NumberOfEnabledFormFactors";

  public static final String ATTR_CPP_FLAGS = "cppFlags";
  public static final String ATTR_CPP_SUPPORT = "includeCppSupport";
  public static final String ATTR_DEPENDENCIES_MULTIMAP = "dependenciesMultimap";
  public static final String ATTR_FRAGMENTS_EXTRA = "usesFragments";
  public static final String ATTR_ACTION_BAR_EXTRA = "usesActionBar";
  public static final String ATTR_GRID_LAYOUT_EXTRA = "usesGridLayout";
  public static final String ATTR_NAVIGATION_DRAWER_EXTRA = "usesNavigationDrawer";

  public static final String ATTR_IS_INSTANT_APP = "isInstantApp";
  public static final String ATTR_HAS_INSTANT_APP_WRAPPER = "hasInstantAppWrapper";
  public static final String ATTR_HAS_MONOLITHIC_APP_WRAPPER = "hasMonolithicAppWrapper";
  public static final String ATTR_MONOLITHIC_MODULE_NAME = "monolithicModuleName";
  public static final String ATTR_INSTANT_APP_PACKAGE_NAME = "instantAppPackageName";
  public static final String ATTR_INSTANT_APP_API_MIN_VERSION = "instantAppApiMinVersion";
  public static final String ATTR_COMPANY_DOMAIN = "companyDomain";
  public static final String ATTR_IS_BASE_FEATURE = "isBaseFeature";
  public static final String ATTR_BASE_FEATURE_NAME = "baseFeatureName";
  public static final String ATTR_BASE_FEATURE_DIR = "baseFeatureDir";
  public static final String ATTR_BASE_FEATURE_RES_DIR = "baseFeatureResDir";
  public static final String ATTR_CLASS_NAME = "className";
  public static final String ATTR_MAKE_IGNORE = "makeIgnore";

  public static final String ATTR_KOTLIN_SUPPORT = "includeKotlinSupport";
  public static final String ATTR_LANGUAGE = "language"; // Java vs Kotlin
  public static final String ATTR_KOTLIN_VERSION = "kotlinVersion";

  public static final String TAG_CATEGORY = "category";
  public static final String TAG_FORMFACTOR = "formfactor";

  private final Document myDocument;
  private final Map<String, Parameter> myParameterMap;

  private final AndroidIconType myIconType;
  private final String myIconName;
  private String myFormFactor;
  private String myCategory;
  private final Multimap<Parameter, Parameter> myRelatedParameters;

  @VisibleForTesting
  public TemplateMetadata(@NotNull Document document) {
    myDocument = document;

    NodeList parameters = myDocument.getElementsByTagName(TAG_PARAMETER);
    myParameterMap = new LinkedHashMap<>(parameters.getLength());
    for (int index = 0, max = parameters.getLength(); index < max; index++) {
      Element element = (Element)parameters.item(index);
      Parameter parameter = new Parameter(this, element);
      if (parameter.id != null) {
        myParameterMap.put(parameter.id, parameter);
      }
    }

    NodeList icons = myDocument.getElementsByTagName(TAG_ICONS);
    if (icons.getLength() > 0) {
      Element element = (Element)icons.item(0);
      if (element.hasAttribute(ATTR_TYPE)) {
        String iconTypeName = element.getAttribute(ATTR_TYPE).toUpperCase(Locale.US);
        myIconType = AndroidIconType.valueOf(iconTypeName);
      }
      else {
        myIconType = null;
      }
      myIconName = element.getAttribute(ATTR_NAME);
    }
    else {
      myIconType = null;
      myIconName = null;
    }

    NodeList categories = myDocument.getElementsByTagName(TAG_CATEGORY);
    if (categories.getLength() > 0) {
      Element element = (Element)categories.item(0);
      if (element.hasAttribute(ATTR_VALUE)) {
        myCategory = element.getAttribute(ATTR_VALUE);
      }
    }

    NodeList formFactors = myDocument.getElementsByTagName(TAG_FORMFACTOR);
    if (formFactors.getLength() > 0) {
      Element element = (Element)formFactors.item(0);
      if (element.hasAttribute(ATTR_VALUE)) {
        myFormFactor = element.getAttribute(ATTR_VALUE);
      }
    }
    myRelatedParameters = computeRelatedParameters();
  }

  private Multimap<Parameter, Parameter> computeRelatedParameters() {
    ImmutableMultimap.Builder<Parameter, Parameter> builder = ImmutableMultimap.builder();
    for (Parameter p : myParameterMap.values()) {
      for (Parameter p2 : myParameterMap.values()) {
        if (p == p2) {
          continue;
        }
        if (p.isRelated(p2)) {
          builder.put(p, p2);
        }
      }
    }
    return builder.build();
  }

  @Nullable
  public String getTitle() {
    return getAttrNonEmpty(ATTR_NAME);
  }

  @Nullable
  public String getDescription() {
    return getAttrNonEmpty(ATTR_DESCRIPTION);
  }

  public int getMinSdk() {
    return getInteger(ATTR_MIN_API, 1);
  }

  public int getMinBuildApi() {
    return getInteger(ATTR_MIN_BUILD_API, 1);
  }

  public int getRevision() {
    return getInteger(ATTR_REVISION, 1);
  }

  @Nullable
  public String getCategory() {
    return myCategory;
  }

  @Nullable
  public String getFormFactor() {
    return myFormFactor;
  }

  @Nullable
  public AndroidIconType getIconType() {
    return myIconType;
  }

  @Nullable
  public String getIconName() {
    return myIconName;
  }

  /**
   * Get the default thumbnail path (the first choice, essentially).
   */
  @Nullable
  public String getThumbnailPath() {
    return getThumbnailPath(null);
  }

  /**
   * Get the active thumbnail path, which requires a callback that takes a parameter ID and returns
   * its value. Using that information, we can select the associated thumbnail path and return it.
   */
  @Nullable
  public String getThumbnailPath(Function<String, Object> produceParameterValue) {
    // Apply selector logic. Pick the thumb first thumb that satisfies the largest number
    // of conditions.
    NodeList thumbs = myDocument.getElementsByTagName(TAG_THUMB);
    if (thumbs.getLength() == 0) {
      return null;
    }

    int bestMatchCount = 0;
    Element bestMatch = null;

    for (int i = 0, n = thumbs.getLength(); i < n; i++) {
      Element thumb = (Element)thumbs.item(i);

      NamedNodeMap attributes = thumb.getAttributes();
      if (bestMatch == null && attributes.getLength() == 0) {
        bestMatch = thumb;
      }
      else if (attributes.getLength() > bestMatchCount) {
        boolean match = true;
        for (int j = 0, max = attributes.getLength(); j < max; j++) {
          Attr attribute = (Attr)attributes.item(j);
          String variableName = attribute.getName();
          String thumbNailValue = attribute.getValue();

          if (produceParameterValue == null || !thumbNailValue.equals(produceParameterValue.apply(variableName))) {
            match = false;
            break;
          }
        }
        if (match) {
          bestMatch = thumb;
          bestMatchCount = attributes.getLength();
        }
      }
    }

    if (bestMatch != null) {
      NodeList children = bestMatch.getChildNodes();
      for (int i = 0, n = children.getLength(); i < n; i++) {
        Node child = children.item(i);
        if (child.getNodeType() == Node.TEXT_NODE) {
          return child.getNodeValue().trim();
        }
      }
    }

    return null;
  }

  public boolean isSupported() {
    String versionString = myDocument.getDocumentElement().getAttribute(ATTR_FORMAT);
    if (versionString != null && !versionString.isEmpty()) {
      try {
        int version = Integer.parseInt(versionString);
        return version <= CURRENT_FORMAT;
      }
      catch (NumberFormatException nfe) {
        return false;
      }
    }

    // Older templates without version specified: supported
    return true;
  }

  public boolean useImplicitRootFolder() {
    String format = myDocument.getDocumentElement().getAttribute(ATTR_FORMAT);
    if (format == null || format.isEmpty()) {
      // If no format is specified, assume this is an old format:
      return true;
    }
    try {
      int version = Integer.parseInt(format);
      return version < RELATIVE_FILES_FORMAT;
    }
    catch (NumberFormatException ignore) {
      // If we cannot parse the format string assume this is an old format:
      return true;
    }
  }

  /**
   * Returns the list of available parameters
   */
  @NotNull
  public Collection<Parameter> getParameters() {
    return myParameterMap.values();
  }

  /**
   * Returns the parameter of the given id, or null if not found
   *
   * @param id the id of the target parameter
   * @return the corresponding parameter, or null if not found
   */
  @Nullable
  public Parameter getParameter(@NotNull String id) {
    return myParameterMap.get(id);
  }

  @Nullable
  private String getAttrNonEmpty(@NotNull String attrName) {
    String attr = myDocument.getDocumentElement().getAttribute(attrName);
    return (attr == null || attr.isEmpty()) ? null : attr;
  }

  private int getInteger(@NotNull String attrName, int defaultValue) {
    try {
      return Integer.parseInt(myDocument.getDocumentElement().getAttribute(attrName));
    }
    catch (NumberFormatException nfe) {
      // Templates aren't allowed to contain codenames, should always be an integer
      //LOG.warn(nfe);
      return defaultValue;
    }
    catch (RuntimeException e) {
      return defaultValue;
    }
  }

  private boolean getBoolean(@NotNull String attrName, boolean defaultValue) {
    String value = myDocument.getDocumentElement().getAttribute(attrName);
    return (value == null) ? defaultValue : SdkConstants.VALUE_TRUE.equals(value);
  }

  /**
   * Computes a suitable build api string, e.g. for API level 18 the build
   * API string is "18".
   */
  @NotNull
  public static String getBuildApiString(@NotNull AndroidVersion version) {
    return version.isPreview() ? AndroidTargetHash.getPlatformHashString(version) : version.getApiString();
  }

  /**
   * Gets all the params that share a type constraint with the given param,
   */
  @NotNull
  public Collection<Parameter> getRelatedParams(Parameter param) {
    return myRelatedParameters.get(param);
  }
}
