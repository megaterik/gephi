/*
Copyright 2008-2011 Gephi
Authors : Taras Klaskovsky <megaterik@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/
package org.gephi.lsg_generator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = Generator.class)
public class LargeSimpleGraph implements Generator {

    //default values
    private double exponent = 2.5;
    private int nodes = 500;
    private int minDegree = 1;
    private int maxDegree = 50;
    private double shuffleRate = 1;
    private DistributionGenerator random;
    private HashMapForEdges hashtable;
    private Node[] node;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    //values for ui preview
    private previewType typeOfReport = previewType.CUMULATIVE_DEGREE_REPORT;

    public double getExponent() {
        return exponent;
    }

    public void setExponent(double exponent) {
        this.exponent = exponent;
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }

    public int getMinDegree() {
        return minDegree;
    }

    public void setMinDegree(int minDegree) {
        this.minDegree = minDegree;
    }

    public int getNumberOfNodes() {
        return nodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.nodes = numberOfNodes;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void generate(ContainerLoader container) {
        progressTicket.start();
        init();
        int failure = 0;
        do {
            failure++;
            if (failure > 10) {
                throw new IllegalArgumentException(
                        "generation of graph has been failed " + failure
                        + " times, perhaps it is impossible to make it connect with such settings");
            }
            generateDegree();
        } while (!attemptToMakeSimpleGraph());
        addEdgesToHashTable();
        makeGraphConnected();
        shuffleGraph();
        appendResult(container);

        progressTicket.finish();
    }

    private void init() {
        hashtable = new HashMapForEdges();
        random = new DistributionGenerator();
        if (maxDegree > nodes - 1) {
            maxDegree = nodes - 1;
        }
        node = new Node[nodes];
        for (int i = 0; i < nodes; i++) {
            node[i] = new Node();
            node[i].id = i;
            if (cancel) {
                return;
            }
        }
    }

    void generateDegree() {
        int sum = 0;
        for (int i = 0; i < nodes; i++) {
            if (cancel) {
                return;
            }
            do {
                if (cancel) {
                    break;
                }
                node[i].edges = random.nextPowerLaw(minDegree, maxDegree, exponent);//degree of node should be >= 1
            } while (node[i].edges > maxDegree);
            sum += node[i].edges;
        }


        //make number of directed edges even
        for (int i = 0; sum % 2 == 1 && i < nodes; i++) {
            if (node[i].edges < maxDegree) {
                sum++;
                node[i].edges++;
            } else if (node[i].edges > minDegree && node[i].edges >= 2) {
                sum--;
                node[i].edges--;
            }
        }

        if (sum % 2 == 1) {
            sum++;
            node[0].edges++;
        }

        //init adjastment lists
        for (int i = 0; i < nodes; i++) {
            if (cancel) {
                return;
            }
            node[i].edge = new Node[node[i].edges];
        }
    }

    private void addEdgesToHashTable() {
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < node[i].edges; j++) {
                if (cancel) {
                    return;
                }
                hashtable.addEdge(node[i], node[i].edge[j], j);
            }
        }
    }

    /*
     * Visit all components, put edges that can be deleted without disconnecting
     * to nontreeEdge and one edge from every tree to treeEdge, then merge
     * mainEdge is removable edge to connect components with deletable edges
     * between each other with complexity O(M)
     */
    private void makeGraphConnected() {
        Queue<Pair> treeEdge = new LinkedList<Pair>();
        Queue<Pair> nontreeEdge = new LinkedList<Pair>();
        Pair mainEdge = new Pair(-1, -1);
        boolean[] visited = new boolean[nodes];
        int[] distanceFromRoot = new int[nodes];
        int[] parent = new int[nodes];
        boolean isConnectedToMainComponent;
        for (int i = 0; i < nodes; i++) {
            if (cancel) {
                return;
            }
            if (!visited[i]) {
                isConnectedToMainComponent = false;
                Queue<Node> queue = new LinkedList<Node>();
                distanceFromRoot[i] = 0;
                visited[i] = true;
                queue.add(node[i]);
                while (queue.size() > 0) {
                    for (int j = 0; j < queue.peek().edges; j++) {
                        if (!visited[queue.peek().edge[j].id]) {
                            visited[queue.peek().edge[j].id] = true;
                            parent[queue.peek().edge[j].id] = queue.peek().id;
                            distanceFromRoot[queue.peek().edge[j].id] = distanceFromRoot[queue.peek().id] + 1;
                            queue.add(queue.peek().edge[j]);
                        } else if (parent[queue.peek().id] != queue.peek().edge[j].id && (queue.peek().id > queue.peek().edge[j].id)) {//avoid parent edges and avoid two directed edges instead of one undirected
                            if (!isConnectedToMainComponent) {
                                if (mainEdge.equals(new Pair(-1, -1))) { //if it is first component in graph
                                    isConnectedToMainComponent = true;
                                    mainEdge = new Pair(queue.peek().id, queue.peek().edge[j].id);
                                } else { //connect to main component
                                    isConnectedToMainComponent = true;
                                    hashtable.swapEdge(queue.peek(), queue.peek().edge[j], node[mainEdge.first], node[mainEdge.second]);
                                    mainEdge.first = queue.peek().id;//change source after swap
                                }
                            } else { //we can use that edge to connect some tree
                                nontreeEdge.add(new Pair(queue.peek().id, queue.peek().edge[j].id));
                            }
                        }
                    }
                    queue.poll();
                }
                if (!isConnectedToMainComponent) {
                    treeEdge.add(new Pair(i, node[i].edge[0].id));
                }
            }
        }
        if (mainEdge.first != -1) { //if at least one component with removable edge
            nontreeEdge.add(new Pair(mainEdge.first, mainEdge.second));
        }

        if (treeEdge.size() == 1 && nontreeEdge.size() == 0)//just one tree
        {
            return;
        }

        while (treeEdge.size() > 0) { //connect tree to main component
            if (cancel) {
                return;
            }
            hashtable.swapEdge(node[treeEdge.peek().first], node[treeEdge.peek().second], node[nontreeEdge.peek().first], node[nontreeEdge.peek().second]);
            treeEdge.poll();
            nontreeEdge.poll();
        }

    }

    private boolean attemptToMakeSimpleGraph() {

        //O(N + M) havel-hakimi alg. Creates undirected simple not connected graph with prescribed degree sequence

        int sum = 0;
        for (int i = 0; i < nodes; i++) {
            sum += node[i].edges;
        }

        if (sum / 2 < nodes - 1) {
            return false;
        }

        int max = 1;
        for (int i = 0; i < nodes; i++) {
            max = Math.max(max, node[i].edges);
        }

        ArrayList<Queue<Integer>> k = new ArrayList<Queue<Integer>>(max + 1);
        for (int i = 0; i <= maxDegree; i++) {
            k.add(new LinkedList<Integer>());
        }
        for (int i = 0; i < nodes; i++) {
            if (cancel) {
                return false;
            }
            k.get(node[i].edges).add(new Integer(i));
        }

        int topDegree = max;
        while (topDegree > 0) {
            if (cancel) {
                return false;
            }
            //take node with higher degree
            Node first = node[k.get(topDegree).poll()];
            int degree = topDegree;
            int nextLevelUsed = 0;//number of nodes that were already linked with first node and moved on level lower, to end of queue. We should not touch them again
            int thisLeveLUsed = 0;
            while (first.edges > 0) {
                thisLeveLUsed = nextLevelUsed;
                nextLevelUsed = 0;
                while (degree > 0 && k.get(degree).size() == 0) {
                    degree--;
                }
                if (degree == 0) {
                    return false;
                }

                while (first.edges > 0 && k.get(degree).size() > thisLeveLUsed) {
                    if (cancel) {
                        return false;
                    }
                    Node second = node[k.get(degree).poll()];
                    first.edge[first.edges - 1] = second;
                    second.edge[second.edges - 1] = first;
                    first.edges -= 1;
                    second.edges -= 1;
                    nextLevelUsed += 1;
                    k.get(degree - 1).add(new Integer(second.id));
                }
                degree--;
            }

            while (topDegree > 0 && k.get(topDegree).size() == 0) {
                topDegree--;
            }
        }

        for (int i = 0; i < nodes; i++) {
            node[i].edges = node[i].edge.length;
        }

        return true;
    }

    //shuffle edges at least edges times, to be truly random graph
    private void shuffleGraph() {
        double gplus = 0.1311;
        double gminus = 0.0763;
        //values generated so sqrt(gplus * gminus) ~ 0.1 and gplus/gminus ~ e - 1, that magic numbers from http://www-rp.lip6.fr/~latapy/FV/generation.html
        int edges = 0;
        for (int i = 0; i < nodes; i++) {
            edges += node[i].edges;
        }
        edges /= 2;

        double numberOfSteps = 1;//number of iterations before connectivity check, increases\decreases with time

        ArrayList<EdgeSwapStorage> operations = new ArrayList<EdgeSwapStorage>();
        for (int successes = 0; successes < edges * shuffleRate;) {
            if (cancel) {
                return;
            }
            for (int i = 0; i < numberOfSteps; i++) {
                if (cancel) {
                    return;
                }
                Node source1 = node[random.nextInt(nodes)];
                Node source2 = node[random.nextInt(nodes)];

                Node target1 = node[source1.id].edge[random.nextInt(node[source1.id].edges)];
                Node target2 = node[source2.id].edge[random.nextInt(node[source2.id].edges)];

                if (!hashtable.existEdge(source1, target2) && !hashtable.existEdge(source2, target1) && source1 != source2 && target1 != target2
                        && source1 != target2 && source2 != target1) {
                    operations.add(new EdgeSwapStorage(source1, target1, source2, target2));
                    hashtable.swapEdge(source1, target1, source2, target2);
                }
            }
            if (bfs(node, node[0]) == nodes) {
                //if after numberOfSteps graph still connected than increase numberOfSteps and move ahead
                successes += numberOfSteps;
                numberOfSteps *= (1 + gplus);
                operations.clear();
            } else {
                //else revert changes
                revertChanges(operations);
                numberOfSteps *= (1 - gminus);
                if (numberOfSteps < 1) {
                    numberOfSteps = 1;
                }
            }
        }
    }

    void revertChanges(ArrayList<EdgeSwapStorage> operations) {
        for (int i = operations.size() - 1; i >= 0; i--) {
            if (cancel) {
                return;
            }
            hashtable.swapEdge(operations.get(i).source1, operations.get(i).target2, operations.get(i).source2, operations.get(i).target1);
        }
        operations.clear();
    }

    int bfs(Node[] node, Node source) {
        int result = 1;
        boolean[] visited = new boolean[node.length];
        visited[source.id] = true;
        Queue<Node> q = new LinkedList<Node>();
        q.add(source);

        while (q.size() > 0) {
            if (cancel) {
                return result;
            }
            for (int i = 0; i < q.peek().edges; i++) {
                if (!visited[q.peek().edge[i].id]) {
                    q.add(node[q.peek().edge[i].id]);
                    visited[q.peek().edge[i].id] = true;
                    result++;
                }
            }
            q.poll();
        }

        return result;
    }

    void appendResult(ContainerLoader container) {
        NodeDraft[] nodeDraft = new NodeDraft[nodes];
        for (int i = 0; i < nodes; i++) {
            if (cancel) {
                return;
            }
            nodeDraft[i] = container.factory().newNodeDraft();
            container.addNode(nodeDraft[i]);
        }
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < node[i].edges; j++) {
                if (cancel) {
                    return;
                }
                EdgeDraft edgeDraft = container.factory().newEdgeDraft();
                edgeDraft.setSource(nodeDraft[i]);
                edgeDraft.setTarget(nodeDraft[node[i].edge[j].id]);
                container.addEdge(edgeDraft);
            }
        }
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(LargeSimpleGraph.class, "LargeSimpleGraph.name");
    }

    @Override
    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(LargeSimpleGraphUI.class);
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public previewType getTypeOfReport() {
        return typeOfReport;
    }

    public void setTypeOfReport(previewType typeOfReport) {
        this.typeOfReport = typeOfReport;
    }

    /* void testGenerator(int nodes, int minDegree, int maxDegree, double exponent) {
        this.nodes = nodes;
        this.minDegree = minDegree;
        this.maxDegree = maxDegree;
        this.exponent = exponent;
        System.err.println("testing " + nodes + " " + minDegree + " " + maxDegree + " " + exponent);
        init();
        int failure = 0;
        do {
            failure++;
            if (failure > 10) {
                throw new IllegalArgumentException("generation of graph has been failed " + failure + " times, perhaps it is impossible");
            }
            generateDegree();
            System.err.println("fail");
        } while (!attemptToMakeSimpleGraph());
        addEdgesToHashTable();

        System.err.println(node.length + " length");
        for (int i = 0; i < nodes; i++) {
            System.err.print(node[i].id + ":");
            for (int j = 0; j < node[i].edges; j++) {
                System.err.print(node[i].edge[j].id + " ");
            }
            System.err.println();
        }
        System.err.println("-----connecting-----");
        makeGraphConnected();

        for (int i = 0; i < nodes; i++) {
            System.err.print(node[i].id + ":");
            for (int j = 0; j < node[i].edges; j++) {
                System.err.print(node[i].edge[j].id + " ");
            }
            System.err.println();
        }
        System.err.println("-----shuffle-----");

        shuffleGraph();

        for (int i = 0; i < nodes; i++) {
            System.err.print(node[i].id + ":");
            for (int j = 0; j < node[i].edges; j++) {
                System.err.print(node[i].edge[j].id + " ");
            }
            System.err.println();
        }
        System.err.println("-----end-----");
        System.err.println("-----end-----");
        System.err.println("-----end-----");
    } */

    public double getShuffleRate() {
        return shuffleRate;
    }

    public void setShuffleRate(double shuffleRate) {
        this.shuffleRate = shuffleRate;
    }
}

class Pair {

    int first;
    int second;

    public Pair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair t = (Pair) o;
        return (first == t.first && second == t.second);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.first;
        hash = 83 * hash + this.second;
        return hash;
    }
}

class EdgeSwapStorage {

    Node source1, source2, target1, target2;

    public EdgeSwapStorage(Node source1, Node target1, Node source2, Node target2) {
        this.source1 = source1;
        this.source2 = source2;
        this.target1 = target1;
        this.target2 = target2;
    }
}