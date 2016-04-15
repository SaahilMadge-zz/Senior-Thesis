import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;
//import nilgiri.math.DoubleReal;
import org.fun4j.compiler.expressions.Mul;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by ghostof2007 on 10/14/14.
 * Class for sentence relevance model.
 */
public class SentRel {

    static boolean TEST = false; //IMP: turn on while testing to avoid creating new features - don't change manually
    static boolean INIT = false; //IMP: turn on while initializing - don't change manually
    static boolean LEMMA = false;
    static boolean POSTAGS = true;
    static boolean LEXICAL = false; //lexical features
    static boolean UNIGRAM_MATCH = true; //lexical features
    static boolean NER = false;
    static boolean BIGRAM_MATCH = true;
    static boolean IGNORE_STOPWORDS = false;
    static boolean HANDLE_NEGATION = false;

    static boolean MULTI_SENTENCE; //set in params.properties
    static boolean RTE = false;


    //PARAMS
    static double LAMBDA_OPT;  //set in params.properties
    static int VECTOR_SIZE = 200;
    static int maxSentRange; //set in params.properties


    //features and auxiliary variables
    static HashMap<String, ArrayList<Double>> wordVec = new HashMap<String, ArrayList<Double>>();
    static HashSet<String> stopWords = new HashSet<String>();
    static HashMap<String, Integer> word2Cnt = new HashMap<String, Integer>(); //word counts from a big corpus like Wikipedia
    static HashMap<String, Integer> feature2Index = new HashMap<String, Integer>();
    static ArrayList<String> index2Feature = new ArrayList<String>();
    static Set<Integer> type1Features = new HashSet<Integer>(); //QZ
    static Set<Integer> type2Features = new HashSet<Integer>(); //AQZ
    static ArrayList<Double> weights = new ArrayList<Double>();  //automatically grows while initializing features

    //Data variables
    static ArrayList<Paragraph> trainParagraphs;
    static ArrayList<String[]> trainAnswers;
    static ArrayList<Paragraph> devParagraphs;
    static ArrayList<String[]> devAnswers;

    //stanford NLP
    static Properties props = new Properties();
    static StanfordCoreNLP pipeline;

    //for debugging
    static Map<String, Double> feature2Weight = new HashMap<String, Double>();

    static void initStanfordPipeline() {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref"); //, lemma, ner, parse, dcoref");
        pipeline = new StanfordCoreNLP(props);
    }
}
//
////    static void initialize() throws IOException, InterruptedException {
////        INIT = true;
////        readWordVectors();
////        readWordList();
////        readStopWords();
////
////        //calculate all possible features and update the weights vector
////        int i = 0;
////        System.err.println("Initializing features....");
////        for(Paragraph paragraph : SentRel.trainParagraphs) {
////            paragraph.initCaches();
////            int quesNum = 0;
////            for(Question Q : paragraph.questions) {
////                for (int ansNum=0; ansNum < Q.choices.size(); ansNum++) {
////                    int N = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class).size();
////                    for (int sentNum = 0; sentNum < N; sentNum++) {
////                        if (MULTI_SENTENCE) {
////                            for (int sentNum2 : getNeighborSentences(sentNum, N)) {
////                                for (int rel = 0; rel < Relation.NUM_RELATIONS; rel++)
////                                    getFeatures(paragraph, new int[]{sentNum, sentNum2}, quesNum, ansNum, rel); //just for init
////                            }
////                            if(Main.MIXED_SINGLE_MULTI)
////                                getFeatures(paragraph, sentNum, quesNum, ansNum);
////                        }
////                        else {
////                            HashMap<Integer, Double> features = getFeatures(paragraph, sentNum, quesNum, ansNum);
////                        }
////                    }
////                }
////                quesNum++;
////            }
////
////            System.err.print("\r" + (i++) + "/" + SentRel.trainParagraphs.size());
////        }
////        System.err.println();
////
////        INIT = false;
////
////    }
//
////    static void readWordVectors() throws IOException, InterruptedException {
////        BufferedReader br = new BufferedReader(new FileReader(Main.wordVectorFile));
////        try {
////            String line = br.readLine();
////            while (line != null) {
////                String[] parts = line.split(" ");
////                ArrayList<Double> vector = new ArrayList<Double>();
////                String word = parts[0];
////                for(int i=1;i<Math.min(VECTOR_SIZE + 1, parts.length);i++) {
////                    vector.add(Double.parseDouble(parts[i]));
////                }
////                wordVec.put(word, vector);
////                line = br.readLine();
////            }
////        } finally {
////            br.close();
////        }
////        System.err.println("Read in " + Integer.toString(wordVec.size()) + " vectors");
////    }
////
////    static void readWordList() throws IOException, InterruptedException {
////        BufferedReader br = new BufferedReader(new FileReader(Main.wordListFile));
////        try {
////            StringBuilder sb;
////            String line = br.readLine();
////
////            while (line != null) {
////                sb = new StringBuilder();
////                sb.append(line);
////                String[] parts = sb.toString().split(" ");
////                String word = parts[0];
////                word2Cnt.put(word, Integer.parseInt(parts[1]));
////                line = br.readLine();
////            }
////        } finally {
////            br.close();
////        }
////        System.err.println("Read in "+Integer.toString(word2Cnt.size())+" words");
////    }
////
////    static void readStopWords() throws IOException, InterruptedException {
////        BufferedReader br = new BufferedReader(new FileReader(Main.stopWordsFile));
////        try {
////
////            String line = br.readLine();
////            while (line != null) {
////                stopWords.add(line.trim());
////                line = br.readLine();
////            }
////        } finally {
////            br.close();
////        }
////        System.err.println("Read in "+Integer.toString(stopWords.size())+" stopwords");
////    }
////
////
////    //IMP: Using prefixes to denote the type of feature (QZ or AQZ)
////    //this is an umbrella function to get both types of features.
////    static HashMap<Integer, Double> getFeatures(Paragraph paragraph, int sentNum, int quesNum, int ansNum) {
////        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
////
////        Tools.updateMap(features, getFeaturesQZSingle(paragraph, sentNum, quesNum));
////        Tools.updateMap(features, getFeaturesAQZSingle(paragraph, sentNum, quesNum, ansNum));
////
////        return features;
////    }
////
////    static HashMap<Integer, Double> getFeatures(Paragraph paragraph, int [] sentNums, int quesNum, int ansNum, int relation) {
////        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
////
////        Tools.updateMap(features, getFeaturesQZSingle(paragraph, sentNums[0], quesNum));
////        Tools.updateMap(features, Relation.getFeaturesQR(paragraph, quesNum, relation));
////        Tools.updateMap(features, MultiSentence.getFeaturesMulti(paragraph, sentNums, quesNum));
////        Tools.updateMap(features, Relation.getRelationFeatures(paragraph, sentNums, relation));
////        Tools.updateMap(features, getFeaturesAQZSingle(paragraph, sentNums[1], quesNum, ansNum));
//////        Tools.updateMap(features, getFeaturesAQZSingle(paragraph, sentNums[0], quesNum, ansNum));  //IMP : using z1 also
////
////        return features;
////    }
////
////    static HashMap<Integer, Double> getFeaturesQZSingle(Paragraph paragraph, int sentNum, int quesNum) {
////
////        if(!INIT && paragraph.checkCacheExists(quesNum, sentNum))
////            return paragraph.q2Z2featuresType1.get(quesNum).get(sentNum);
////
////
////
////
////        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
////
////        if(Main.SWD_ONLY) return features; //IMP: use if running with only SWD features
////
////
////        //sentZ - sentence under consideration
////        Question Q = paragraph.questions.get(quesNum);
////
////        //TODO: add  question type
////        String qType = getQuestionType(paragraph, quesNum);
////
////        //match only words in question with the words in the sentence - make set first
////        Set<String> zWords = new HashSet<String>();  //used in matching
////        Set<String> qWords = new HashSet<String>(); //used in matching
////        Set<String> qContentWords = new HashSet<String>();
////        float matchCnt = 0, uniqueMatchCnt = 0, verbMatchCnt = 0;
////        float bigramMatchCnt = 0;
////        boolean prevMatch  = false;
////        for(IndexedWord w1 : paragraph.sentDepGraphs.get(sentNum).vertexSet()) {
////            if(!Global.POStags.contains(w1.get(CoreAnnotations.PartOfSpeechAnnotation.class).substring(0,1))) continue;
////
////            for(IndexedWord w2 : Q.depGraph.vertexSet()) {
////
////                if(Global.POStags.contains(w2.get(CoreAnnotations.PartOfSpeechAnnotation.class).substring(0,1))) {
////                    qContentWords.add(Tools.getLemma(w2));
////                }
////
////                if(Tools.lemmaMatch(w1, w2)) {
////                    matchCnt++;
////
////                    if(w1.get(CoreAnnotations.PartOfSpeechAnnotation.class).charAt(0) =='V' ||
////                            w2.get(CoreAnnotations.PartOfSpeechAnnotation.class).charAt(0) =='V')
////                        verbMatchCnt++;
////
////                    if(!zWords.contains(Tools.getLemma(w1)) && !qWords.contains(Tools.getLemma(w2)))
////                        uniqueMatchCnt++;
////                    zWords.add(Tools.getLemma(w1));
////                    qWords.add(Tools.getLemma(w2));
////                }
////
////            }
////        }
////
////        for(IndexedWord w1 : paragraph.sentDepGraphs.get(sentNum).vertexSet()) {
////            for(IndexedWord w2 : Q.depGraph.vertexSet()) {
////                if(Tools.lemmaMatch(w1, w2)) {
////                    if(prevMatch) bigramMatchCnt++;
////                    prevMatch = true;
////                }
////                else
////                    prevMatch = false;
////            }
////        }
////
////        qType = "";
////
////        if(Main.MIXED_SINGLE_MULTI) qType = Integer.toString(Q.type);
////
////        //nothing but the root
////        Set<String> zImpWords = getImpWords(paragraph.sentDepGraphs.get(sentNum));
////        Set<String> qImpWords = getImpWords(paragraph.questions.get(quesNum).depGraph);
////        zImpWords.retainAll(qImpWords);
////        Tools.addFeatureSmart(features, "Q_Imp_"+qType, zImpWords.size());
////
////
////        Tools.addFeatureSmart(features, "Q_MatchCnt_"+qType, matchCnt);
//////        Tools.addFeatureSmart(features, "Q_uniqueMatchCnt_"+qType, uniqueMatchCnt);
////        if(BIGRAM_MATCH)
////            Tools.addFeatureSmart(features, "Q_bigramMatchCnt_"+qType, bigramMatchCnt);
//////        Tools.addFeatureSmart(features, "Q_verbMatchCnt_", verbMatchCnt);
////
////        //feature to check coverage of words in question
////        if(qContentWords.size() > 0)
////            Tools.addFeatureSmart(features, "Q_coverage_"+qType, ((float)qWords.size())/qContentWords.size());
////
//////        Tools.updateMap(features, getQuestionTypeFeatures(Q.question, paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class).get(sentNum)));
////
////        Tools.updateMap(features, Clause.getFeaturesQZ(paragraph, sentNum, quesNum));
////
////        Tools.addFeature(features, "BIAS_Q_"+qType, 1.);
////
////
////
////        paragraph.cacheFeatures(quesNum, sentNum, features);
////
////        return features;
////    }
//
//
//    static HashMap<Integer, Double> getFeaturesQZMulti(Paragraph paragraph, int [] sentNums, int quesNum) {
//
//        if(!INIT && paragraph.checkCacheExistsMulti(quesNum, sentNums[0], sentNums[1]))
//            return paragraph.q2Z2Multi2featuresType1.get(quesNum).get(sentNums[0]).get(sentNums[1]);
//
//        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
//
//        //sentZ - sentence under consideration
//        Question Q = paragraph.questions.get(quesNum);
//
//
//        //TODO: add  question type
//        String qType = getQuestionType(paragraph, quesNum);
//
//        //match only words in question with the words in the sentence - make set first
//        for(int sentNum : sentNums) {
//            Set<String> zWords = new HashSet<String>();  //used in matching
//            Set<String> qWords = new HashSet<String>(); //used in matching
//            Set<String> qContentWords = new HashSet<String>();
//            float matchCnt = 0, uniqueMatchCnt = 0, verbMatchCnt = 0;
//            float bigramMatchCnt = 0;
//            boolean prevMatch = false;
//
//            for (IndexedWord w1 : paragraph.sentDepGraphs.get(sentNum).vertexSet()) {
//                if (!Global.POStags.contains(w1.get(CoreAnnotations.PartOfSpeechAnnotation.class).substring(0, 1)))
//                    continue;
//
//                for (IndexedWord w2 : Q.depGraph.vertexSet()) {
//
//                    if (Global.POStags.contains(w2.get(CoreAnnotations.PartOfSpeechAnnotation.class).substring(0, 1))) {
//                        qContentWords.add(Tools.getLemma(w2));
//                    }
//
//
//                    if (Tools.lemmaMatch(w1, w2)) {
//                        matchCnt++;
//
//                        if (w1.get(CoreAnnotations.PartOfSpeechAnnotation.class).charAt(0) == 'V' &&
//                                w2.get(CoreAnnotations.PartOfSpeechAnnotation.class).charAt(0) == 'V')
//                            verbMatchCnt++;
//
//                        if (!zWords.contains(Tools.getLemma(w1)) && !qWords.contains(Tools.getLemma(w2)))
//                            uniqueMatchCnt++;
//                        zWords.add(Tools.getLemma(w1));
//                        qWords.add(Tools.getLemma(w2));
//
//                        if (prevMatch) bigramMatchCnt++;
//
//                        prevMatch = true;
//                    } else
//                        prevMatch = false;
//                }
//            }
//
//
//            Tools.addFeatureIncrement(features, "Q_MatchCnt_" + qType, matchCnt);
//            Tools.addFeatureIncrement(features, "Q_uniqueMatchCnt_" + qType, uniqueMatchCnt);
//            if (BIGRAM_MATCH)
//                Tools.addFeatureIncrement(features, "Q_bigramMatchCnt_" + qType, bigramMatchCnt);
////        Tools.addFeatureIncrement(features, "Q_verbMatchCnt_"+qType, verbMatchCnt);
//
//            //feature to check coverage of words in question
//
//            if (qContentWords.size() > 0)
//                Tools.addFeatureIncrement(features, "Q_coverage_" + qType, ((float) qWords.size()) / qContentWords.size());
//        }
//
//        Tools.updateMap(features, Clause.getFeaturesQZ(paragraph, sentNums, quesNum));
//
////        Tools.updateMap(features, MultiSentence.getFeaturesMulti(paragraph, sentNums, quesNum));
//
//        Tools.addFeature(features, "BIAS_Q", 1.);
//
//        paragraph.cacheFeaturesMulti(quesNum, sentNums[0], sentNums[1], features);
//
//        return features;
//    }
//
//
//    static HashMap<Integer, Double> getFeaturesAQZSingle(Paragraph paragraph, int sentNum, int quesNum, int ansNum) {
//
//        if(!INIT && paragraph.checkCacheExists(quesNum, sentNum, ansNum))
//            return paragraph.q2A2Z2featuresType2.get(quesNum).get(ansNum).get(sentNum);
//
//        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
//
//        //IMP: SWD features
//        Tools.updateMap(features, Baseline.swdFeatures(paragraph, sentNum, quesNum, ansNum));
//
//        if(Main.SWD_ONLY) return features; //IMP: use if running with only SWD features
//
//        //sentZ - sentence under consideration
//        Question Q = paragraph.questions.get(quesNum);
//
//
//        String qType = getQuestionType(paragraph, quesNum);
//
//        //match only words in question with the words in the sentence - make set first
//
//        double matchCnt = 0, uniqueMatchCnt = 0, verbMatchCnt = 0, bigramMatchCnt = 0;
//
//        //match only words in question with the words in the sentence - make set first
//        Set<String> zWords = new HashSet<String>();  //used in matching
//        Set<String> aWords = new HashSet<String>(); //used in matching
//        Set<String> aContentWords = new HashSet<String>(); //used in matching
//        matchCnt = 0; uniqueMatchCnt = 0;
//
//        //TODO: get only imp words, try tf-idf/count based metrics
//        Set<String> sentImpWords = new HashSet<String>();
//        Set<String> ansImpWords = new HashSet<String>();
//
//        ArrayList<Annotation> ansChoices;
//
//        if(RTE)
//            ansChoices = Q.choicesReformatted;
//        else
//            ansChoices = Q.choices;
//
//
//        for(IndexedWord w1 : paragraph.sentDepGraphs.get(sentNum).vertexSet()) {
//            if(!Global.POStags.contains(w1.get(CoreAnnotations.PartOfSpeechAnnotation.class).substring(0,1))
//                    || stopWords.contains(Tools.getLemma(w1))) continue;
//            sentImpWords.add(Tools.getLemma(w1));
//
//            for(IndexedWord w2 : ansChoices.get(ansNum).get(CoreAnnotations.SentencesAnnotation.class).get(0)
//                    .get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class).vertexSet()) {
//
//                if(Global.POStags.contains(w2.get(CoreAnnotations.PartOfSpeechAnnotation.class).substring(0,1))) {
//                    aContentWords.add(Tools.getLemma(w2));
//                }
//
//                if(Tools.lemmaMatch(w1, w2)) {
//                    matchCnt++;
//
//                    if(!zWords.contains(Tools.getLemma(w1)) && !aWords.contains(Tools.getLemma(w2)))
//                        uniqueMatchCnt++;
//                    zWords.add(Tools.getLemma(w1));
//                    aWords.add(Tools.getLemma(w2));
//                }
//            }
//        }
//
//        boolean prevMatch= false;
//        for(IndexedWord w1 : paragraph.sentDepGraphs.get(sentNum).vertexSet()) {
//            for(IndexedWord w2 : Q.choices.get(ansNum).get(CoreAnnotations.SentencesAnnotation.class).get(0)
//                    .get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class).vertexSet()) {
//                if(Tools.lemmaMatch(w1, w2)) {
//                    if(prevMatch) bigramMatchCnt++;
//                    prevMatch = true;
//                }
//                else
//                    prevMatch = false;
//            }
//        }
//
//        for(IndexedWord w2 : Q.choices.get(ansNum).get(CoreAnnotations.SentencesAnnotation.class).get(0)
//                .get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class).vertexSet()) {
//            ansImpWords.add(Tools.getLemma(w2));
//        }
//
//        qType = "";
//        if(Main.MIXED_SINGLE_MULTI) qType = Integer.toString(Q.type);
//
//        CoreMap sentence = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class).get(sentNum);
//        Annotation answer = Q.choices.get(ansNum);
//        Set<String> ansNeighborWords = getNeighborWords(sentence, answer);
//        Set<String> qWords = Tools.getWordSet(Q.question);
//        ansNeighborWords.retainAll(qWords);
//
//        Tools.addFeatureSmart(features, "A_neighborMatch_" + qType, ansNeighborWords.size());
//
//        Set<String> zImpWords = getImpWords(paragraph.sentDepGraphs.get(sentNum));
//        Set<String> qImpWords = getImpWords(paragraph.questions.get(quesNum).depGraph);
//        zImpWords.retainAll(qImpWords);
//        String qRootMatch;
//        if(zImpWords.size() > 0)
//            qRootMatch = "yes";
//        else
//            qRootMatch = "no";
//
//
//        Set<String> qNegWords = SentRel.getNegWords(paragraph.questions.get(quesNum).question
//                .get(CoreAnnotations.SentencesAnnotation.class).get(0)
//                .get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class));
//
//
////        Tools.addFeatureSmart(features, "A_MatchCnt_"+qType, matchCnt);
//        Tools.addFeatureSmart(features, "A_uniqueMatchCnt_"+qType, uniqueMatchCnt);
//        if(BIGRAM_MATCH)
//            Tools.addFeatureSmart(features, "A_bigramMatchCnt_"+qType, bigramMatchCnt);
//
////        Tools.updateMap(features, Clause.getFeaturesAQZ(paragraph, sentNum, quesNum, ansNum));
//
//        //feature to check coverage of words in question
//        if(aContentWords.size() > 0)
//            Tools.addFeatureSmart(features, "A_coverage_"+qType, ((float)aWords.size())/aContentWords.size());
//
//
//        //missed words features
//        ansImpWords.removeAll(sentImpWords);
//        Tools.addFeatureSmart(features, "A_missedWords_" + qType, ansImpWords.size());
//
//
//
//        //split clause features
//        Tools.updateMap(features, Clause.splitClauseFeatures(paragraph, new int[]{sentNum}, quesNum, ansNum));
//
//        Tools.addFeature(features, "BIAS_A", 1.);
//
//        paragraph.cacheFeatures(quesNum, ansNum, sentNum, features);
//
//        return features;
//    }
//
//
//    static HashMap<Integer, Double> getFeaturesAQZMulti(Paragraph paragraph, int[] sentNums, int quesNum, int ansNum) {
//
//        if(!INIT && paragraph.checkCacheExistsMulti(quesNum, ansNum, sentNums[0], sentNums[1]))
//            return paragraph.q2A2Z2Multi2featuresType2.get(quesNum).get(ansNum).get(sentNums[0]).get(sentNums[1]);
//
//        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
//
//        //sentZ - sentence under consideration
//        Question Q = paragraph.questions.get(quesNum);
//
//
//        String qType = getQuestionType(paragraph, quesNum);
//
//        //match only words in question with the words in the sentence - make set first
//
//        float matchCnt = 0, uniqueMatchCnt = 0, verbMatchCnt = 0;
//
//        //match only words in question with the words in the sentence - make set first
//        Set<String> zWords = new HashSet<String>();  //used in matching
//        Set<String> aWords = new HashSet<String>(); //used in matching
//        matchCnt = 0; uniqueMatchCnt = 0;
//
//        for(int sentNum : sentNums) {
//            for (IndexedWord w1 : paragraph.sentDepGraphs.get(sentNum).vertexSet()) {
//                if (!Global.POStags.contains(w1.get(CoreAnnotations.PartOfSpeechAnnotation.class).substring(0, 1)) || stopWords.contains(Tools.getLemma(w1)))
//                    continue;
//
//                for (IndexedWord w2 : Q.choices.get(ansNum).get(CoreAnnotations.SentencesAnnotation.class).get(0)
//                        .get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class).vertexSet()) {
//                    if (Tools.lemmaMatch(w1, w2)) {
//                        matchCnt++;
//
//                        if (!zWords.contains(Tools.getLemma(w1)) && !aWords.contains(Tools.getLemma(w2)))
//                            uniqueMatchCnt++;
//                        zWords.add(Tools.getLemma(w1));
//                        aWords.add(Tools.getLemma(w2));
//                    }
//                }
//            }
//        }
//
//        Tools.addFeatureIncrement(features, "A_MatchCnt_"+qType, matchCnt);
//        Tools.addFeatureIncrement(features, "A_uniqueMatchCnt_"+qType, uniqueMatchCnt);
//
//        Tools.updateMap(features, Clause.splitClauseFeatures(paragraph, sentNums, quesNum, ansNum));
//
//
//        Tools.addFeature(features, "BIAS_A", 1.);
//
//
//        paragraph.cacheFeaturesMulti(quesNum, ansNum, sentNums[0], sentNums[1], features);
//
//        return features;
//    }
//
//
//    //function to find the type of question being asked
//    private static String getQuestionType(Paragraph paragraph, int quesNum) {
//        String type = "";
//        Question question = paragraph.questions.get(quesNum);
//        SemanticGraph graph = question.depGraph;
//        IndexedWord root = graph.getFirstRoot();
//        if(root.get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("W") || root.get(CoreAnnotations.LemmaAnnotation.class).equals("is"))
//            type = "COP"; //copula
//        else
//            type = "ACT"; //TODO: need to further sub-categorize this (hacky now)
//
//        return type;
//    }
//
//    //uses the words in the question to get types corresponding to relations above
//    static HashMap<Integer, Double> getQuestionTypeFeatures(Annotation question, CoreMap sentence) {
//        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
//
//        int qType = Relation.NONE;
//        for (String word : Tools.getWordsInSent(question)) {
//            if (word.contains("why"))
//                qType = Relation.CAUSAL;
//            else if (word.contains("when"))
//                qType = Relation.TEMPORAL;
//            else if (word.contains("how"))
//                qType = Relation.HOW;
//            else if (word.contains("where"))
//                qType = Relation.LOCATION;
//            else if (word.contains("who"))
//                qType = Relation.PERSON;
//            //TODO: OBJECT and ACTION
//
//            if (qType != Relation.NONE) break; //probably for questions like what, etc.
//        }
//
//
//        String sentText = sentence.get(CoreAnnotations.TextAnnotation.class).toLowerCase();
//
//        if (qType == Relation.CAUSAL) {
//            int matchCnt = Tools.countOccurences(sentText, Global.causalPhrases);
//            Tools.addFeatureIncrement(features, "QZ_CAUSAL", matchCnt);
//        } else if (qType == Relation.TEMPORAL) {
//            int matchCnt = Tools.countOccurences(sentText, Global.temporalPhrases);
//            Tools.addFeatureIncrement(features, "QZ_TEMPORAL", matchCnt);
//        } else if (qType == Relation.HOW) {
//            int matchCnt = Tools.countOccurences(sentText, Global.howPhrases);
//            Tools.addFeatureIncrement(features, "QZ_HOW", matchCnt);
//        } else if (qType == Relation.LOCATION) {
//            int matchCnt = Tools.countOccurences(sentText, Global.locationPhrases);
//            Tools.addFeatureIncrement(features, "QZ_LOCATION", matchCnt);
//        }
//
//
//        return features;
//
//    }
//
//    static HashMap<Integer, Double> getFeaturesUniversal(Paragraph paragraph, int [] sentNums, int quesNum, int ansNum, boolean useAnswer) {
//        if(sentNums.length == 1)
//            return getFeatures(paragraph, sentNums[0], quesNum, ansNum);
//        else
//            return MultiSentence.getFeaturesMulti(paragraph, sentNums, quesNum);
//    }
//
//    static Set<Integer> getNeighborSentences(int sentNum, int N) {
//        HashSet<Integer> neighborSents = new HashSet<Integer>();
//        for(int i = sentNum; i < Math.min(sentNum + maxSentRange + 1, N); i++) //IMP: only forward looking
////        for(int i = Math.max(0, sentNum - maxSentRange); i < Math.min(sentNum + maxSentRange + 1, N); i++) //both ways
//            neighborSents.add(i);
//
//        return neighborSents;
//    }
//
//    static Set<String> getNeighborWords(CoreMap sentence, Annotation answer) {
//        int maxRange = 2;
//        Set<String> aWords = Tools.getWordSet(answer);
//        ArrayList<String> zWords = Tools.getWordSeq(sentence);
//        int N = zWords.size();
//        Set<String> neighborWords = new HashSet<String>();
//        for(int i=0; i < zWords.size(); i++) {
//            if(aWords.contains(zWords.get(i))) {
//                for(int j = Math.max(0, i-maxRange); j < Math.min(N, i+maxRange+1); j++)
//                    neighborWords.add(zWords.get(j));
//            }
//        }
//        return neighborWords;
//    }
//
//    static Set<String> getImpWords(SemanticGraph graph) {
//        //get only the word that is a direct child of the ROOT
//        Set<String> wordSet = new HashSet<String>();
//
//        wordSet.add(Tools.getLemma(graph.getFirstRoot()));
//
//
////        for (IndexedWord word : graph.getChildren(graph.getFirstRoot())) {
////            wordSet.add(Tools.getLemma(word)); //TODO: try toText instead?
////        }
//        return wordSet;
//    }
//
//    static Set<String> getNegWords(SemanticGraph graph) {
//        //get only the word that is a direct child of the ROOT
//        Set<String> wordSet = new HashSet<String>();
//
//        for(SemanticGraphEdge edge : graph.edgeIterable()) {
//            if(edge.getRelation() == EnglishGrammaticalRelations.NEGATION_MODIFIER)
//                wordSet.add(Tools.getLemma(edge.getDependent()));
//        }
//
//        return wordSet;
//    }
//
//    static HashMap<IntPair, Double> sentDist(Paragraph paragraph, int quesNum) {
//
//        HashMap<IntPair, Double> dist = new HashMap<IntPair, Double>();
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//
//        for(int z1=0;z1 < sentences.size(); z1++)
//            for(int z2 : getNeighborSentences(z1, sentences.size())) {
//
//                double score = 0;
//                double [] scores = new double[Relation.NUM_RELATIONS];
//                for(int rel =0; rel < Relation.NUM_RELATIONS; rel++) {
//                    scores[rel] = Tools.featureWeightProduct(Relation.getRelationFeatures(paragraph, new int[]{z1,z2}, rel))
//                            + Tools.featureWeightProduct(Relation.getFeaturesQR(paragraph, quesNum, rel))
//                            + Tools.featureWeightProduct(MultiSentence.getFeaturesMulti(paragraph, new int[]{z1,z2}, quesNum));  //keeping in line with training and testing use of these features
//                }
//                score = Tools.logSumOfExponentials(scores);
//
//                dist.put(new IntPair(z1, z2), Math.exp(Tools.featureWeightProduct(SentRel.getFeaturesQZSingle(paragraph, z1, quesNum))
//                        + score));
//            }
//
//        dist = (HashMap<IntPair, Double>) Tools.sortByValue(Tools.normalizeHashMap(dist));
//
//        return dist;
//    }
//
//
////
////    static HashMap<Integer, Double> getFeaturesUniversal(Paragraph paragraph, int [] sentNums, int quesNum, int ansNum) {
////        //caching only for useAnswers case because that's the one used in training
////        if(paragraph.checkCacheExistsMulti(quesNum, ansNum, sentNums[0], sentNums[1]))
////            return paragraph.q2A2Z2Multi2features.get(quesNum).get(ansNum).get(sentNums[0]).get(sentNums[1]);
////
////        //else compute again
////        return getFeaturesUniversal(paragraph, sentNums, quesNum, ansNum, true);
////    }
//
//
//
//}
