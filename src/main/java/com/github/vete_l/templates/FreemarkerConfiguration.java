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
package com.github.vete_l.templates;

import com.github.vete_l.templates.propertyAdapters.PropertyObjectWrapper;
import com.google.common.base.Charsets;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

/**
 * A Freemarker {@link Configuration} initialized with sensible built-in values for instantiating
 * Android project templates.
 */
public final class FreemarkerConfiguration extends Configuration {
  public FreemarkerConfiguration() {
    setDefaultEncoding(Charsets.UTF_8.name());
    setLocalizedLookup(false);
    setIncompatibleImprovements(Configuration.getVersion());
    setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    setObjectWrapper(new PropertyObjectWrapper());
  }
}
