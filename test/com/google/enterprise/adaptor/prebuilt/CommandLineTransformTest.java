// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.adaptor.prebuilt;

import static org.junit.Assert.*;

import com.google.enterprise.adaptor.TestHelper;
import com.google.enterprise.adaptor.TransformException;
import com.google.enterprise.adaptor.TransformPipeline;
import com.google.enterprise.adaptor.prebuilt.CommandLineTransform;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Tests for {@link CommandLineTransform}.
 */
public class CommandLineTransformTest {
  @Test
  public void testSed() throws IOException, TransformException {
    TestHelper.assumeOsIsNotWindows();

    ByteArrayOutputStream contentOut = new ByteArrayOutputStream();
    // The newline causes the test to work with both BSD and GNU sed.
    String testStr = "testing\n";
    Map<String, String> metadata = new HashMap<String, String>();
    metadata.put("metaKey1", "metaValue1");
    Map<String, String> params = new HashMap<String, String>();
    params.put("key1", "value1");

    CommandLineTransform cmd = new CommandLineTransform();
    cmd.setTransformCommand(Arrays.asList(new String[] {"sed", "s/i/1/"}));
    cmd.setCommandAcceptsParameters(false);
    TransformPipeline pipeline = new TransformPipeline(Arrays.asList(cmd));
    pipeline.transform(testStr.getBytes(), contentOut, new HashMap<String, String>(), params);

    assertEquals(testStr.replace("i", "1"), contentOut.toString());
    assertEquals("metaValue1", metadata.get("metaKey1"));
    assertEquals(1, metadata.size());
    assertEquals("value1", params.get("key1"));
    assertEquals(1, params.keySet().size());
  }

  @Test
  public void testSedWithMetadata() throws IOException, TransformException {
    TestHelper.assumeOsIsNotWindows();

    ByteArrayOutputStream contentOut = new ByteArrayOutputStream();
    // The newline causes the test to work with both BSD and GNU sed.
    String testStr = "testing\n";
    Map<String, String> metadata = new HashMap<String, String>();
    metadata.put("metaKey1", "metaValue1");
    Map<String, String> params = new HashMap<String, String>();
    params.put("key1", "value1");

    CommandLineTransform cmd = new CommandLineTransform();
    cmd.setTransformCommand(Arrays.asList(new String[] {"/bin/sh", "-c",
      // Process content.
      "sed s/i/1/; META=\"$0\"; PARAM=\"$1\"; TMPFILE=$(mktemp /tmp/adaptor.test.XXXXXXXX);"
      // Process metadata.
      + "(sed s/1/2/g < \"$META\" > \"$TMPFILE\"; cp \"$TMPFILE\" \"$META\") >&2;"
      // Process params.
      + "(sed s/1/3/g < \"$PARAM\" > \"$TMPFILE\"; cp \"$TMPFILE\" \"$PARAM\") >&2;"
      // Cleanup.
      + "rm \"$TMPFILE\" >&2;"
    }));
    cmd.setCommandAcceptsParameters(true);
    TransformPipeline pipeline = new TransformPipeline(Arrays.asList(cmd));
    pipeline.transform(testStr.getBytes(), contentOut, metadata, params);

    assertEquals(testStr.replace("i", "1"), contentOut.toString());
    assertEquals("metaValue2", metadata.get("metaKey2"));
    assertEquals(1, metadata.size());
    assertEquals("value3", params.get("key3"));
    assertEquals(1, params.size());
  }
}