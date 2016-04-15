import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;

import java.util.*;

/**
 * Created by ghostof2007 on 11/14/14.
 * To keep track of all global params
 */
public class Global {
    static Set<String> causalPhrases = new HashSet<String>(Arrays.asList("because", "why", "due to", "so that"));
    static Set<String> howPhrases = new HashSet<String>(Arrays.asList("how", "by", "using"));

    static Set<String> temporalPhrases = new HashSet<String>(Arrays.asList("when", "begin", "end", "between", "tomorrow", "yesterday", "time", "soon", "before", "after",
            "during", "then", "finally", "past","present","future", "now", "nowadays","back then", "at present", "first"));
    static Set<String> locationPhrases = new HashSet<String>(Arrays.asList("above", "across", "against", "among", "at", "away", "before", "behind", "below", "beneath", "beside",
            "between", "beyond", "by", "from", "in", "in front of", "inside", "near", "next to", "off", "on", "opposite", "out", "outside", "over", "past", "through",
            "under", "upon", "with", "within", "without", "where"));

//    static Set<String> howPhrases = new HashSet<String>(Arrays.asList("how", "by", "using"));

    static Set<String> personPhrases = new HashSet<String>(Arrays.asList("he", "she", "him", "her"));

    static String POStags = "CJMNRV";  //first letters of important POS tags

    static List<GrammaticalRelation> clauseConnectors = new ArrayList<GrammaticalRelation>(
            Arrays.asList(EnglishGrammaticalRelations.ADV_CLAUSE_MODIFIER,
                    EnglishGrammaticalRelations.APPOSITIONAL_MODIFIER,
                    EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT,
                    EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER,
//                    EnglishGrammaticalRelations.CLAUSAL_SUBJECT,
//                    EnglishGrammaticalRelations.CLAUSAL_PASSIVE_SUBJECT,
                    EnglishGrammaticalRelations.PARATAXIS,
                    EnglishGrammaticalRelations.PREPOSITIONAL_COMPLEMENT,
                    EnglishGrammaticalRelations.PREPOSITIONAL_OBJECT,
                    EnglishGrammaticalRelations.TEMPORAL_MODIFIER,
                    EnglishGrammaticalRelations.VERBAL_MODIFIER,
                    EnglishGrammaticalRelations.XCLAUSAL_COMPLEMENT
                    ));

    //TODO: use specific WH-words in question with some features (location, person, etc.)

}
