/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.github.vete_l.support.gradle.structure.model.repositories.search;

import com.android.ide.common.repository.GradleVersion;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.util.io.HttpRequests;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.util.List;

import static com.android.SdkConstants.GRADLE_PATH_SEPARATOR;
import static com.intellij.openapi.util.text.StringUtil.isNotEmpty;

public class JCenterRepository extends ArtifactRepository {
  @Override
  @NotNull
  public String getName() {
    return "JCenter";
  }

  @Override
  public boolean isRemote() {
    return true;
  }

  @Override
  @NotNull
  protected SearchResult doSearch(@NotNull SearchRequest request) throws Exception {
    String url = createRequestUrl(request);
    return HttpRequests.request(url).accept("application/json").connect(request1 -> parse(request1.getReader()));
  }

  @VisibleForTesting
  @NotNull
  static String createRequestUrl(SearchRequest request) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("https://api.bintray.com/search/packages/maven?");
    String groupId = request.getGroupId();
    if (isNotEmpty(groupId)) {
      buffer.append("g=").append(groupId).append("&");
    }
    buffer.append("a=").append(request.getArtifactName()).append("&subject=bintray&repo=jcenter");
    return buffer.toString();
  }

  SearchResult parse(@NotNull Reader response) {
    /*
      Sample response:
      [
        {
          "name": "com.atlassian.guava:guava",
          "repo": "jcenter",
          "owner": "bintray",
          "desc": null,
          "system_ids": [
            "com.atlassian.guava:guava"
          ],
          "versions": [
            "15.0"
          ],
          "latest_version": "15.0"
        },
        {
          "name": "com.atlassian.bundles:guava",
          "repo": "jcenter",
          "owner": "bintray",
          "desc": null,
          "system_ids": [
            "com.atlassian.bundles:guava"
          ],
          "versions": [
            "8.1",
            "8.0",
            "1.0-actually-8.1"
          ],
          "latest_version": "8.1"
        },
        {
          "name": "io.janusproject.guava:guava",
          "repo": "jcenter",
          "owner": "bintray",
          "desc": null,
          "system_ids": [
            "io.janusproject.guava:guava"
          ],
          "versions": [
            "19.0.0",
            "17.0.2",
            "17.0"
          ],
          "latest_version": "19.0.0"
        },
        {
          "name": "com.google.guava:guava",
          "repo": "jcenter",
          "owner": "bintray",
          "desc": "Guava is a suite of core and expanded libraries that include\n    utility classes, google's collections, io classes, and much\n    much more.\n\n    Guava has two code dependencies - javax.annotation\n    per the JSR-305 spec and javax.inject per the JSR-330 spec.",
          "system_ids": [
            "com.google.guava:guava"
          ],
          "versions": [
            "19.0",
            "19.0-rc3",
            "19.0-rc2",
            "19.0-rc1",
            "18.0",
            "18.0-rc2",
            "18.0-rc1",
            "11.0.2-atlassian-02",
            "17.0",
            "17.0-rc2",
            "17.0-rc1",
            "16.0.1",
            "16.0",
            "16.0-rc1",
            "15.0",
            "15.0-rc1",
            "14.0.1",
            "14.0",
            "14.0-rc3",
            "14.0-rc2",
            "14.0-rc1",
            "13.0.1",
            "13.0",
            "13.0-final",
            "13.0-rc2",
            "13.0-rc1",
            "12.0.1",
            "12.0",
            "12.0-rc2",
            "12.0-rc1",
            "11.0.2-atlassian-01",
            "11.0.2",
            "11.0.1",
            "11.0",
            "11.0-rc1",
            "10.0.1",
            "10.0",
            "10.0-rc3",
            "10.0-rc2",
            "10.0-rc1",
            "r09",
            "r08",
            "r07",
            "r06",
            "r05",
            "r03"
          ],
          "latest_version": "19.0"
        }
      ]
     */

    JsonParser parser = new JsonParser();
    JsonArray array = parser.parse(response).getAsJsonArray();

    int totalFound = array.size();
    List<FoundArtifact> artifacts = Lists.newArrayListWithExpectedSize(totalFound);

    for (int i = 0; i < totalFound; i++) {
      JsonObject root = array.get(i).getAsJsonObject();
      String name = root.getAsJsonPrimitive("name").getAsString();

      List<GradleVersion> availableVersions = Lists.newArrayList();
      JsonArray versions = root.getAsJsonArray("versions");
      versions.forEach(element -> {
        String version = element.getAsString();
        availableVersions.add(GradleVersion.parse(version));
      });

      List<String> coordinate = Splitter.on(GRADLE_PATH_SEPARATOR).splitToList(name);
      assert coordinate.size() == 2;

      artifacts.add(new FoundArtifact(getName(), coordinate.get(0), coordinate.get(1), availableVersions));
    }

    return new SearchResult(getName(), artifacts, totalFound);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return 1;
  }
}
