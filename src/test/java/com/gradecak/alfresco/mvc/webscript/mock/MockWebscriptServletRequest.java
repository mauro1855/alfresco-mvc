/**
 * Copyright gradecak.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradecak.alfresco.mvc.webscript.mock;

import static org.mockito.Mockito.mock;

import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Runtime;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

public class MockWebscriptServletRequest extends WebScriptServletRequest {

  // private Content content;

  private MockWebscriptServletRequest(Runtime mockedRuntime, MockHttpServletRequest mockHttpServletRequest, Match match) {
    super(mockedRuntime, mockHttpServletRequest, match, null);
  }

  static public MockWebscriptServletRequest createMockWebscriptServletRequest(AbstractWebScript webScript, String method, String webscriptUrl, String controllerMapping,
      final Map<String, String> parameters, final Map<String, String> body) {
    Match match = new Match(null, ImmutableMap.of("", ""), webscriptUrl, webScript);
    MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(method, "http://localhost/alfresco" + webscriptUrl + controllerMapping);
    mockHttpServletRequest.setServletPath("alfresco");
    mockHttpServletRequest.setContextPath("http://localhost/");
    mockHttpServletRequest.setContentType("application/json");
    if (parameters != null) {
      mockHttpServletRequest.setParameters(parameters);
    }
    try {
      if (HttpMethod.POST.name().equals(method) && body != null) {
        mockHttpServletRequest.setContent(new ObjectMapper().writeValueAsString(body).getBytes());
      }
    } catch (JsonProcessingException e) {
      Throwables.propagate(e);
    }

    MockWebscriptServletRequest webscriptServletRequest = new MockWebscriptServletRequest(mock(Runtime.class), mockHttpServletRequest, match);
    // if (content != null && !content.isEmpty()) {
    // Content mockedContent = mock(Content.class);
    // webscriptServletRequest.content = mockedContent;
    // }

    return webscriptServletRequest;
  }
}
