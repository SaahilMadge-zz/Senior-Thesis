import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/**
 * Created by ghostof2007 on 12/3/14.
 * Process a parse tree in different ways
 */
public class ParseProcess {

    //edge labels to break sentences at. TODO: look at specific labels in this that require special consideration
    static Set<String> clauseBreakersDirect = new HashSet<String>(Arrays.asList("advcl", "appos", "ccomp", "csubj", "csubjpass", "dep",
            "npadvmod", "parataxis", "pcomp", "prep", "prepc", "tmod", "vmod", "xcomp"));

    //labels that signal the need for duplication of subject/object (conjunctions, etc.)
    static Set<String> clauseDuplicators = new HashSet<String>(Arrays.asList("cc", "conj", "iobj", "preconj"));


    static SemanticGraph splitClauses(CoreMap sentence) {
        SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
        List<SemanticGraphEdge> edges = new ArrayList<SemanticGraphEdge>(graph.edgeListSorted());
//        System.out.println("before breaking....");
//        System.out.println(graph.toString());
        for(SemanticGraphEdge edge : edges) {
            if(clauseBreakersDirect.contains(edge.getRelation().toString())) {

                if (edge.getRelation().toString().equals("xcomp")) {
                    String s = "{word:"+edge.getGovernor().originalText()+"} >nsubj {}=subject";
                    SemgrexPattern p = SemgrexPattern.compile(s);
                    SemgrexMatcher matcher = p.matcher(graph);
                    while(matcher.find()) {
                        IndexedWord subj = matcher.getNode("subject");
                        graph.addEdge(edge.getDependent(), subj, EnglishGrammaticalRelations.SUBJECT, 1, false);
//                        System.err.println("Added edge! "+subj.toString());
                    }
                }

                graph.removeEdge(edge);
            }

        }
//        System.out.println("after breaking....");
//        System.out.println(graph.toString());
//        System.out.println("Done.");

        return graph;
    }

}
