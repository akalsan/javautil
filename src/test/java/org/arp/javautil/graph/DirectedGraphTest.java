/*
 * #%L
 * JavaUtil
 * %%
 * Copyright (C) 2012 - 2013 Emory University
 * %%
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
 * #L%
 */
package org.arp.javautil.graph;

import org.arp.javautil.collections.Iterators;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Test the methods of DirectedGraph.
 * 
 * @author Nora Sovarel
 * 
 */
public class DirectedGraphTest {

    private ArrayList<InternalEdge> expected = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        expected.clear();
    }

    @Test
    public void testNeighbor() {
        DirectedGraph graph = new DirectedGraph(3);
        graph.add("1");
        graph.add("2");
        graph.add("3");
        addEdge(graph, "1", "2", 2);
        addEdge(graph, "2", "1", 3);
        addEdge(graph, "2", "3", 4);
        assertEquals(Arrays.asList(new String[] { "2" }), Iterators
                .asList(graph.neighbors("1")));
    }

    @Test
    public void testEdges() {
        /*
         * Creates a graph with edges X2X 3X4 56X
         */
        DirectedGraph graph = new DirectedGraph(3);
        graph.add("1");
        graph.add("2");
        graph.add("3");
        addEdge(graph, "1", "2", 2);
        addEdge(graph, "2", "1", 3);
        addEdge(graph, "2", "3", 4);
        addEdge(graph, "3", "1", 5);
        addEdge(graph, "3", "2", 6);

        verifyEdgesResult(graph);
    }

    @Test
    public void testEdges1() {
        /*
         * Creates a graph with XXX 3X4 XXX
         */
        DirectedGraph graph = new DirectedGraph(3);
        graph.add("1");
        graph.add("2");
        graph.add("3");
        addEdge(graph, "2", "1", 3);
        addEdge(graph, "2", "3", 4);

        verifyEdgesResult(graph);
    }

    @Test
    public void testEdges2() {
        // Creates a graph with X2X XXX 56X
        DirectedGraph graph = new DirectedGraph(3);
        graph.add("1");
        graph.add("2");
        graph.add("3");
        addEdge(graph, "1", "2", 2);
        addEdge(graph, "3", "1", 5);
        addEdge(graph, "3", "2", 6);

        verifyEdgesResult(graph);
    }

    @Test
    public void testEdges3() {
        /*
         * Creates a graph with 12XX 3X47 X6X8 XXXX
         */
        DirectedGraph graph = new DirectedGraph(3);
        graph.add("1");
        graph.add("2");
        graph.add("3");
        graph.add("4");
        addEdge(graph, "1", "2", 2);
        addEdge(graph, "2", "1", 3);
        addEdge(graph, "2", "3", 4);
        addEdge(graph, "2", "4", 7);
        addEdge(graph, "3", "2", 6);
        addEdge(graph, "3", "4", 8);

        verifyEdgesResult(graph);
    }

    @Test
    public void testEdgesConcurrentModification() {
        DirectedGraph graph = new DirectedGraph(3);
        graph.add("1");
        graph.add("2");
        graph.add("3");
        graph.add("4");
        addEdge(graph, "1", "2", 2);
        addEdge(graph, "2", "1", 3);
        addEdge(graph, "2", "3", 4);
        addEdge(graph, "2", "4", 7);
        addEdge(graph, "3", "2", 6);

        for (Iterator it = graph.edges(); it.hasNext();) {
            addEdge(graph, "3", "4", 8);
            try {
                it.next();
            } catch (ConcurrentModificationException e) {
                return;
            }
        }
        Assert.fail();
    }

    private void verifyEdgesResult(DirectedGraph graph) {
        int size = 0;
        for (Iterator<Edge> it = graph.edges(); it.hasNext();) {
            Assert.assertTrue(containsEdge(it.next()));
            size++;
        }

        assertEquals(expected.size(), size);
    }

    private void addEdge(DirectedGraph graph, String v1, String v2, int w) {
        graph.setEdge(v1, v2, new Weight(w));
        expected.add(new InternalEdge(v1, v2, w));
    }

    private boolean containsEdge(Edge edge) {
        for (InternalEdge internalEdge : expected) {
            if (internalEdge.v1.equals(edge.getStart())
                    && internalEdge.v2.equals(edge.getFinish())
                    && internalEdge.w == edge.getWeight().value()) {
                return true;
            }
        }
        return false;
    }

    private class InternalEdge {
        private String v1;

        private String v2;

        private int w;

        public InternalEdge(String v1, String v2, int w) {
            this.v1 = v1;
            this.v2 = v2;
            this.w = w;
        }

    }
}
