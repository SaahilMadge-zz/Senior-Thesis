import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.IntPair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ghostof2007 on 11/27/14.
 */
public class EntityEdge implements Serializable{

    IntPair nodes;
    HashMap<String, Pair<String, String>> word2LemmaAndPOS = new HashMap<String, Pair<String, String>>();
    int distance; //number of hops in dependency tree

    EntityEdge(IndexedWord w1, IndexedWord w2, IndexedWord collapsedWord, EntityEdge left, EntityEdge right) {
        nodes = new IntPair(w1.index(), w2.index());
        distance = left.distance +right.distance;
        word2LemmaAndPOS.putAll(left.word2LemmaAndPOS);
        word2LemmaAndPOS.putAll(right.word2LemmaAndPOS);
//        if(!SentRel.stopWords.contains(collapsedWord.get(CoreAnnotations.TextAnnotation.class)))
        word2LemmaAndPOS.put(collapsedWord.get(CoreAnnotations.TextAnnotation.class).toLowerCase(),
                new MutablePair<String, String>(collapsedWord.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase(), collapsedWord.get(CoreAnnotations.PartOfSpeechAnnotation.class)));
    }

    EntityEdge(IndexedWord w1, IndexedWord w2, IndexedWord collapsedWord) {
        nodes = new IntPair(w1.index(), w2.index());
        distance = 2;
        word2LemmaAndPOS.put(collapsedWord.get(CoreAnnotations.TextAnnotation.class).toLowerCase(),
                new MutablePair<String, String>(collapsedWord.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase(), collapsedWord.get(CoreAnnotations.PartOfSpeechAnnotation.class)));
    }

    EntityEdge(IndexedWord w1, IndexedWord w2) {
        nodes = new IntPair(w1.index(), w2.index());
        distance = 1;
    }

    void addInfo( IndexedWord collapsedWord, EntityEdge left, EntityEdge right) {
        distance = Math.min(distance, left.distance +right.distance);
//        if(left.wordAndPOS != null)
            word2LemmaAndPOS.putAll(left.word2LemmaAndPOS);
//        if(right.word2LemmaAndPOS != null)
            word2LemmaAndPOS.putAll(right.word2LemmaAndPOS);
        word2LemmaAndPOS.put(collapsedWord.get(CoreAnnotations.TextAnnotation.class).toLowerCase(),
                new MutablePair<String, String>(collapsedWord.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase(), collapsedWord.get(CoreAnnotations.PartOfSpeechAnnotation.class)));
    }
}
