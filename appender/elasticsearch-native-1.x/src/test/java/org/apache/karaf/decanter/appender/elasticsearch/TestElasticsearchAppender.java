/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.decanter.appender.elasticsearch;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;

import org.elasticsearch.client.Requests;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.event.Event;

import static org.elasticsearch.node.NodeBuilder.*;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.apache.karaf.decanter.api.marshaller.Marshaller;
import org.apache.karaf.decanter.marshaller.json.JsonMarshaller;

public class TestElasticsearchAppender {
    private static final String HOST = "127.0.0.1";
    private static final String CLUSTER_NAME = "elasticsearch-test";
    private static final int PORT = 9300;
    private static final int MAX_TRIES = 10;

   @Test
   public void testAppender() throws Exception {
       
       Settings settings = settingsBuilder()
               .put("cluster.name", CLUSTER_NAME)
               .put("http.enabled", "true")
               .put("node.data", true)
               .put("path.data", "target/data")
               .put("network.host", HOST)
               .put("port", PORT)
               .put("index.store.type", "memory")
               .put("index.store.fs.memory.enabled", "true")
               .put("path.plugins", "target/plugins")
               .build();
       
       Node node = nodeBuilder().settings(settings).node();
       Marshaller marshaller = new JsonMarshaller();
       ElasticsearchAppender appender = new ElasticsearchAppender();
       appender.setMarshaller(marshaller);
       Dictionary<String, Object> config = new Hashtable<>();
       config.put("clusterName", CLUSTER_NAME);
       config.put("port", "" + PORT);
       appender.open(config);
       appender.handleEvent(new Event("testTopic", dummyMap()));
       appender.handleEvent(new Event("testTopic", dummyMap()));
       appender.handleEvent(new Event("testTopic", dummyMap()));
       appender.close();

       long currentCount = 0;
       int c = 0; 
       while (c < MAX_TRIES && currentCount != 3) {
           currentCount = node.client().count(Requests.countRequest()).actionGet().getCount();
           Thread.sleep(500);
           c++;
       }
       
       Assert.assertEquals(3L, currentCount);
       node.close();
   }

   private Map<String, String> dummyMap() {
       return MapBuilder.<String, String>newMapBuilder().put("a", "b").put("c", "d").map();
   }

}
