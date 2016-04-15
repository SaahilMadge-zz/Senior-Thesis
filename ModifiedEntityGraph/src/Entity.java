import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ghostof2007 on 11/27/14.
 */
public class Entity implements Serializable {
    String str;
    String pos;
    String lemma;
    int position; //position in sentence - zero indexed

    Entity(String a, String b, String lem, int c) {
        str = a;
        pos = b;
        lemma = lem;
        position = c;
    }

    Multimap<String, String> properties = new HashMultimap<String, String>();

    void updateProperties(SemanticGraph graph) {
        IndexedWord node = graph.getNodeByIndex(position);

        Set<IndexedWord> descendants = graph.descendants(node);
        for(IndexedWord w : descendants) {
            SemanticGraphEdge edge = graph.getEdge(node, w);
            if(edge!= null && edge.getRelation().equals(EnglishGrammaticalRelations.ADJECTIVAL_MODIFIER))
                properties.put(edge.getRelation().toString(), w.get(CoreAnnotations.TextAnnotation.class));
            else if(edge != null && edge.getRelation().equals(EnglishGrammaticalRelations.POSSESSION_MODIFIER))
                properties.put(edge.getRelation().toString(), w.get(CoreAnnotations.TextAnnotation.class));
            //TODO other relations?
        }

        //need to add possessive stuff here
        Set<IndexedWord> parents = graph.getParents(node);
        for(IndexedWord w : parents) {
            SemanticGraphEdge edge = graph.getEdge(w, node);

        }
    }



}
