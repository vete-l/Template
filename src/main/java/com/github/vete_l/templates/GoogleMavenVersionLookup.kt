/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.tools.idea.templates

import com.android.ide.common.repository.GoogleMavenRepository
import com.google.common.io.ByteStreams
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.PathUtil
import com.intellij.util.net.HttpConfigurable
import org.jetbrains.android.AndroidPlugin
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object GoogleMavenVersionLookup : GoogleMavenRepository(getCacheDir()) {
  override fun readUrlData(url: String, timeout: Int): ByteArray? {
    val query = URL(url)
    val connection = HttpConfigurable.getInstance().openConnection(query.toExternalForm())
    if (timeout > 0) {
      connection.connectTimeout = timeout
      connection.readTimeout = timeout
    }
    try {
      val stream = connection.getInputStream() ?: return null
      return ByteStreams.toByteArray(stream)
    }
    finally {
      if (connection is HttpURLConnection) {
        connection.disconnect()
      }
    }
  }

  override fun error(throwable: Throwable, message: String?) {
    Logger.getInstance(GoogleMavenVersionLookup::class.java).warn(message, throwable)
  }
}

private fun getCacheDir(): File? =
    if (ApplicationManager.getApplication().isUnitTestMode || AndroidPlugin.isGuiTestingMode())
      null
    else
      File(PathUtil.getCanonicalPath(PathManager.getSystemPath()), "maven.google")