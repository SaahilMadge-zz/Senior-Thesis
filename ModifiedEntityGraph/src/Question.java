import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghostof2007 on 10/14/14.
 */
public class Question implements Serializable{
    Annotation question;
    ArrayList<Annotation> choices = new ArrayList<Annotation>();

    int correct;
    int type; //1 - one, 2 - multiple
    int number;
    EntityGraph entityGraph;
    ArrayList<EntityGraph> choiceGraphs = new ArrayList<EntityGraph>();

    //dep graph
    SemanticGraph depGraph;

    //Turn Q+A into a statement
    ArrayList<Annotation> choicesReformatted = new ArrayList<Annotation>();

    //debug vars
    HashMap<String, Double> sent2ExpScores = new HashMap<String, Double>();
    ArrayList<HashMap<String, Double>> ans2sent2ExpScores = new ArrayList<HashMap<String, Double>>();


    Question() {
        correct = -1;
    }
}