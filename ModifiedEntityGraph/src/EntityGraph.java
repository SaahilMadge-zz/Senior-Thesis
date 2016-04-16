import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Index;
import edu.stanford.nlp.util.IntPair;

import java.io.Serializable;
import java.util.*;


/**
 * Created by ghostof2007 on 11/27/14.
 * Graphs to use a data structures
 */
public class EntityGraph implements Serializable {

    //both nodes and edges are indexed by position in original sentence.
    HashMap<Integer, Entity> nodes = new HashMap<Integer, Entity>();
    HashMap<Integer, HashMap<Integer, EntityEdge>> edges = new HashMap<Integer, HashMap<Integer, EntityEdge>>();

    List<SemanticGraph> clauses = new ArrayList<SemanticGraph>();
    HashMap<IntPair, GrammaticalRelation> clausePair2Relation = new HashMap<IntPair, GrammaticalRelation>();


    EntityGraph(Paragraph paragraph, CoreMap sentence, boolean isSentence) {
        
        //get nouns
        ArrayList<IndexedWord> nouns;

        HashMap<Integer, IndexedWord> noun2EntityHead = new HashMap<Integer, IndexedWord>();
        if(isSentence) {
            nouns = Clause.getNounsOrPronouns(sentence);
            //get corefs for the nouns
            for (IndexedWord noun : nouns) {
                IndexedWord newNoun = Clause.getEntityFromIndexedWord(paragraph, noun);
                if (newNoun != null)
                    noun2EntityHead.put(noun.index(), newNoun);
            }
        }
        else
            nouns = Clause.getNounsOrPronounsOrWh(sentence);

        SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
        for(IndexedWord noun : nouns) {
            int position = noun.index();
            String pos = noun.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            if(noun2EntityHead.containsKey(noun.index()))
                noun = noun2EntityHead.get(noun.index());
            String lemma = noun.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase();
            nodes.put(position, new Entity(noun.get(CoreAnnotations.TextAnnotation.class).toLowerCase(), pos, lemma, position));
        }

        //add in properties to each entity in the graph
        for(Entity en : nodes.values()) {
            en.updateProperties(graph);
        }


        buildEdges(graph);

        //create the clauses
        updateClauses(graph, graph.getFirstRoot());
//        System.out.println(graph.toFormattedString());
//
//        System.out.println("edges: ");

//        SemanticGraphEdge nsubj_edge = null;
//        SemanticGraphEdge cop_edge = null;
//        SemanticGraphEdge dobj_edge = null;
        HashSet<SemanticGraphEdge> nsubj_edges = new HashSet<SemanticGraphEdge>();
        HashSet<SemanticGraphEdge> dobj_edges = new HashSet<SemanticGraphEdge>();
        HashSet<SemanticGraphEdge> cop_edges = new HashSet<SemanticGraphEdge>();

        for (SemanticGraphEdge e : graph.edgeIterable())
        {
//            System.out.println(e.toString());
//            System.out.println(e.getRelation());
//            System.out.println(e.getGovernor());
//            System.out.println(e.getRelation());
//            System.out.println(e.getDependent());

            GrammaticalRelation relation = e.getRelation();
            if(relation.getShortName().equals("nsubj"))
            {
//                System.out.println(e.toString());
//                System.out.println(e.getGovernor().);
//                System.out.println(e.getRelation());
//                System.out.println(e.getDependent());
                nsubj_edges.add(e);
            }
//            System.out.println(relation.getShortName());
            else if (relation.getShortName().equals("dobj"))
            {
                dobj_edges.add(e);
            }
            else if (relation.getShortName().equals("cop"))
            {
                cop_edges.add(e);
            }

        }

        /* NOW WE FIND THE PROPER TRIPLE, ACCORDING TO THE RULES:
            1) If nsubj and dobj, then nsubj dependent is SUBJECT, nsubj governor is VERB, dobj dependent is DOBJ,
                dobj governor is VERB
            2) If nsubj and cop, then nsubj dependent is SUBJECT, nsubj governor is ADJ/NOUN, cop dependent is VERB,
                cop governor is ADJ/NOUN
        */

        // if nsubj + dobj

        for (SemanticGraphEdge nsubj_edge : nsubj_edges)
        {
            IndexedWord nsubj = nsubj_edge.getDependent();
            for (SemanticGraphEdge dobj_edge : dobj_edges)
            {
                if (dobj_edge != null) {
                    IndexedWord verb = nsubj_edge.getGovernor();
                    IndexedWord dobj = dobj_edge.getDependent();
                    assert (verb.equals(dobj_edge.getGovernor()));

                    KnowledgeGraphTriple newTriple = new KnowledgeGraphTriple(nsubj, verb, dobj);
                    System.out.println(newTriple.toString());
                }
            }

            for (SemanticGraphEdge cop_edge : cop_edges)
            {
                if (cop_edge != null) {
                    IndexedWord whatItIs = nsubj_edge.getGovernor();
                    IndexedWord verb = cop_edge.getDependent();
                    assert (whatItIs.equals(cop_edge.getGovernor()));
                    KnowledgeGraphTriple newTriple = new KnowledgeGraphTriple(nsubj, verb, whatItIs);
                    System.out.println(newTriple.toString());
                }
            }
        }


//        System.out.println("roots: ");

//        for (IndexedWord iw : graph.getRoots())
//        {
//            System.out.println(iw.toString());
//            System.out.println("descendants: ");
//            for (IndexedWord desc : graph.getChildList(iw));
//        }

//        System.out.println(graph.);

        //print them (DEBUG only)
//        System.out.println(sentence);
//        for(SemanticGraph clauseGraph : clauses)
//            clauseGraph.prettyPrint();

    }



    private void buildEdges(SemanticGraph graph) {

        //init edges from the graph
        for(SemanticGraphEdge edge : graph.edgeListSorted()) {
            IndexedWord v1 = edge.getGovernor();
            IndexedWord v2 = edge.getDependent();
            if(!edges.containsKey(v1.index()))
                edges.put(v1.index(), new HashMap<Integer, EntityEdge>());
            if(!edges.containsKey(v2.index()))
                edges.put(v2.index(), new HashMap<Integer, EntityEdge>());

            edges.get(v1.index()).put(v2.index(), new EntityEdge(v1, v2));
            edges.get(v2.index()).put(v1.index(), new EntityEdge(v2, v1));
        }

        //collapse non-entities
        Set<Integer> keySet1 = new HashSet<Integer>(edges.keySet());

        for(int i : keySet1) {
            if(nodes.containsKey(i)) continue; //it is an entity, we dont want to collapse this

//            boolean edgesAdded = false;
            Set<Integer> keySet = new HashSet<Integer>(edges.get(i).keySet());
            for(int j : keySet) {
                for(int k : keySet) {
                    if (j == k) continue;
                    if(edges.get(j).containsKey(k)) ;
//                        edges.get(j).get(k).addInfo(graph.getNodeByIndex(i), edges.get(i).get(j), edges.get(i).get(k));
                    else
                        edges.get(j).put(k, new EntityEdge(graph.getNodeByIndex(j), graph.getNodeByIndex(k), graph.getNodeByIndex(i),
                            edges.get(i).get(j), edges.get(i).get(k)));
//                    edgesAdded = true;
                }
            }

            if(true) {//edgesAdded) {
                //remove all old links
                for (int j : keySet) {
                    edges.get(j).remove(i);
                }
                edges.remove(i);
            }
        }

    }

    //Function to split the sentence into clauses and collect them
    int updateClauses(SemanticGraph graph, IndexedWord root) {
        //from root, do DFS and at any edge that connects, cut off
        Queue<IndexedWord> queue = new LinkedList<IndexedWord>();
        queue.add(root);

        SemanticGraph clauseGraph =  new SemanticGraph();
        clauseGraph.addRoot(root);

        HashMap<Integer, GrammaticalRelation> clauseLinkNumbers
                = new HashMap<Integer, GrammaticalRelation>(); //to keep track of edges between clauses and this one

        //BFS
        while(!queue.isEmpty()) {
            IndexedWord node = queue.poll();
            for(SemanticGraphEdge edge : graph.getOutEdgesSorted(node)) {
                //TODO: clean up the if condition if possible
                if(Global.clauseConnectors.contains(edge.getRelation()) || edge.getRelation().getShortName().equals("prep")
                        || edge.getRelation().getShortName().equals("partmod")) {
                    int tmpClauseNum = updateClauses(graph, edge.getDependent());
                    clauseLinkNumbers.put(tmpClauseNum, edge.getRelation());
                }
                else {
                    clauseGraph.addEdge(edge.getGovernor(), edge.getDependent(), edge.getRelation(), edge.getWeight(), edge.isExtra());
                    clauseGraph.addVertex(edge.getDependent());
                    queue.add(edge.getDependent());
                }
            }
        }

        int clauseNum = clauses.size(); //get clause Num for this clause

        //add into list of clauses and link this clause to all its neighbors
        clauses.add(clauseGraph);

        for(Map.Entry<Integer, GrammaticalRelation> numAndRel : clauseLinkNumbers.entrySet()) {
            clausePair2Relation.put(new IntPair(clauseNum, numAndRel.getKey()), numAndRel.getValue()); //TODO: how about the reverse link?
        }

        return clauseNum;

    }

    class KnowledgeGraphTriple
    {
        IndexedWord subject;
        IndexedWord verb;
        IndexedWord object;

        public KnowledgeGraphTriple(IndexedWord s, IndexedWord v, IndexedWord o)
        {
            subject = s;
            verb = v;
            object = o;
        }

        public String toString()
        {
            return "< " + subject.toString() + ", " + verb.toString() + ", " + object.toString() + " >";
        }
    }


}
