import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;
import edu.stanford.nlp.util.IntTuple;
import edu.stanford.nlp.util.Pair;

import java.util.*;

/**
 * Created by ghostof2007 on 11/17/14.
 * Class to handle all extraction and processing of clauses in sentences.
 */
public class Clause {
    static HashMap<String, IndexedWord> processSentence(SemanticGraph semanticGraph) {

        HashMap<String, IndexedWord> clauseMap = new HashMap<String, IndexedWord>();

        //get the main verb
        IndexedWord root = semanticGraph.getFirstRoot();

        IndexedWord subj = null, obj = null;
        List<Pair<GrammaticalRelation, IndexedWord>> rootChildren = semanticGraph.childPairs(root);
        for (Pair<GrammaticalRelation, IndexedWord> relAndWord : rootChildren) {
            if (relAndWord.first.getShortName().contains("nsubj"))
                subj = relAndWord.second;
            else if (relAndWord.first.getShortName().contains("dobj"))
                obj = relAndWord.second;
        }

        clauseMap.put("Root", root);
        if (subj != null) {
            clauseMap.put("Subj", subj);
        }

        if (obj != null)
            clauseMap.put("Obj", obj);

        return clauseMap;
    }

    //get list of all agent-action pairs or agent-property pairs
    static List<SemanticGraphEdge> processSentence2(SemanticGraph semanticGraph) {

        HashMap<IndexedWord, IndexedWord> agentMap = new HashMap<IndexedWord, IndexedWord>();

        //get subject edges
        List<SemanticGraphEdge> subjEdges = semanticGraph.edgeListSorted();
        //findAllRelns(GrammaticalRelation.valueOf("nsubj"));

        //TODO coref

        return subjEdges;
    }

    static String getCorefEntity(IndexedWord word, Paragraph paragraph) {
        //use coreference map
        IntPair position = new IntPair(word.sentIndex() + 1, word.index()); //IMP : seems to be mismatch in sentNum between coref and this
        CorefChain.CorefMention head = paragraph.corefMap.get(position);
        if (head != null) {
            IntTuple headPos = head.position;
            String headString = head.mentionSpan;
            return headString;

        }
//        else
//            return Tools.getLemma(word); //TODO : check this - to keep or not to keep
        return "";

    }

    static ArrayList<IndexedWord> getVerbs(SemanticGraph graph) {
        //get all the verbs


        ArrayList<IndexedWord> verbs = new ArrayList<IndexedWord>();
        for (IndexedWord word : graph.topologicalSort()) {
            if (word.get(CoreAnnotations.PartOfSpeechAnnotation.class).charAt(0) == 'V')
                verbs.add(word);
        }

        return verbs;
    }


    static ArrayList<IndexedWord> getNounsOrPronouns(CoreMap coreMap) {
        //get all the nouns

        SemanticGraph graph = coreMap.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

        ArrayList<IndexedWord> nouns = new ArrayList<IndexedWord>();
        for (IndexedWord word : graph.topologicalSort()) {
            String pos = word.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            if (pos.charAt(0) == 'N' || pos.startsWith("PRP"))
                nouns.add(word);
        }

        return nouns;
    }

    static ArrayList<IndexedWord> getNounsOrPronounsOrWh(CoreMap coreMap) {
        //get all the nouns

        SemanticGraph graph = coreMap.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

        ArrayList<IndexedWord> nouns = new ArrayList<IndexedWord>();
        for (IndexedWord word : graph.topologicalSort()) {
            String pos = word.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            if (pos.charAt(0) == 'N' || pos.startsWith("PRP") || pos.startsWith("W"))
                nouns.add(word);
        }

        return nouns;
    }


    static int getNegCnt(CoreMap coreMap) {
        //get number of 'neg' relations

        SemanticGraph graph = coreMap.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

        int negCnt = 0;
        for (SemanticGraphEdge edge : graph.edgeListSorted()) {
            if (edge.getRelation().getShortName().equals("neg"))
                negCnt++;
        }

        return negCnt;
    }

    static IndexedWord getEntityFromIndexedWord(Paragraph paragraph, IndexedWord word) {
        //gets the real entity after resolving coreferences
        if (word == null) return null;
        IntPair position = new IntPair(word.sentIndex() + 1, word.index()); //IMP : seems to be mismatch in sentNum between coref and this

        CorefChain.CorefMention head = paragraph.corefMap.get(position);

        if (head != null) {
            String headString = head.mentionSpan;
            int headSentNum = head.sentNum - 1;

            //process headString - choose the node with most outgoing edges - likely to be the root of the phrase
            int mostOutgoingEdges = -1;
            IndexedWord returnEntity = null;
            CoreMap sentence = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class).get(headSentNum);
            List<CoreLabel> wordsInSent = sentence.get(CoreAnnotations.TokensAnnotation.class);
            SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
//            for (int i = head.startIndex - 1; i < head.endIndex - 1; i++) {
//                CoreLabel w = wordsInSent.get(i);
//                IndexedWord IW = graph.getNodeByIndexSafe(i + 1);
//                assert IW != null;
//                if(IW == null) continue; //hacky - should not be null - somethign wrong with index?
//                if (graph.getOutEdgesSorted(IW).size() > mostOutgoingEdges) {
//                    mostOutgoingEdges = graph.getOutEdgesSorted(IW).size();
//                    returnEntity = IW;
//                }
//            }
            returnEntity = graph.getNodeByIndexSafe(head.headIndex);
//            assert returnEntity != null;
            return returnEntity;
        }

        return null;
    }
}


//    //TODO: consider more than just nouns?
//    //IMP: Uses coreference and other collapsing methods to extract the entities from the sentence.
//    static Set<String> getEntities(Paragraph paragraph, int sentNum, boolean isQuestion, int ansNum) {
//        ArrayList<IndexedWord> nouns;
//        HashSet<String> entities = new HashSet<String>();
//
//        if(isQuestion) {
//            if(ansNum == -1) {
//                //question
//                nouns = getNounsOrPronouns(paragraph.questions.get(sentNum).question.get(CoreAnnotations.SentencesAnnotation.class).get(0));
//            }
//            else {
//                //answer
//                nouns = getNounsOrPronouns(paragraph.questions.get(sentNum).choices.get(ansNum).get(CoreAnnotations.SentencesAnnotation.class).get(0));
//            }
//
//            for(IndexedWord noun : nouns) {
//                entities.add(Tools.getLemma(noun));
//            }
//            return entities;
//        }
//
//
//        CoreMap sent = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class).get(sentNum);
//
//
//        nouns = getNounsOrPronouns(sent);
//
//        //use coreference map
//        for(IndexedWord noun : nouns) {
//            IntPair position = new IntPair(noun.sentIndex()+1, noun.index()); //IMP : seems to be mismatch in sentNum between coref and this
//
//            CorefChain.CorefMention head = paragraph.corefMap.get(position);
//
//            if(head != null) {
//                String headString = head.mentionSpan;
//                int headSentNum  = head.sentNum-1;
//
//                //process headString (split phrases, etc.)
//                List<CoreLabel> wordsInSent = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class).get(headSentNum).get(CoreAnnotations.TokensAnnotation.class);
//                for(int i=head.startIndex-1; i < head.endIndex-1; i++) {
//                    CoreLabel w = wordsInSent.get(i);
//                    if(w.get(CoreAnnotations.PartOfSpeechAnnotation.class).charAt(0) == 'N')
//                        entities.add(w.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase());  //lower case
//                }
//
//
////                entities.add(headString); //OLD: direct addition of entity
//            }
//            else
//                entities.add(Tools.getLemma(noun)); //TODO : check this - to keep or not to keep
//        }
//
//        return entities;
//    }
//
//    //gets features tailored to use clauses
//    static HashMap<Integer, Double> getClauseFeatures(Paragraph paragraph, int sentNum1, int sentNum2, boolean isQuestion, int answerNum) {
//        //IMP : if isQuestion == true, then take Question instead.
//        //IMP : isQuestion has to be true and answerNum !=-1 for Answer case.
//        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
//        String featurePrefix;
//        if(isQuestion) {
//            if(answerNum!=-1)
//                featurePrefix = "A_";
//            else
//                featurePrefix = "Q_";
//        }
//        else
//            featurePrefix = "Z_";
//
//        //Take care of various cases this function can be used in
//        SemanticGraph sent1 = paragraph.sentDepGraphs.get(sentNum1);
//        SemanticGraph sent2;
//        if(isQuestion) {
//            if (answerNum!=-1)
//                sent2 = paragraph.questions.get(sentNum2).choices.get(answerNum).get(CoreAnnotations.SentencesAnnotation.class)
//                        .get(0).get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
//            else
//                sent2 = paragraph.questions.get(sentNum2).question.get(CoreAnnotations.SentencesAnnotation.class)
//                        .get(0).get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
//        }
//        else
//            sent2 = paragraph.sentDepGraphs.get(sentNum2);
//
//        HashMap<String, IndexedWord> clauseMap1 = Clause.processSentence(sent1);
//        HashMap<String, IndexedWord> clauseMap2 = Clause.processSentence(sent2);
//
//        //add a feature for each match
//        for(String key : clauseMap1.keySet()) {
//            IndexedWord e1, e2;
//
//            if(!isQuestion) {
//                e1 = getEntityFromIndexedWord(paragraph, clauseMap1.get(key));
//                e2 = getEntityFromIndexedWord(paragraph, clauseMap2.get(key));
//            }
//            else {
//                e1 = clauseMap1.get(key);
//                e2 = clauseMap2.get(key);
//            }
//
//            if(e1!=null && e2!=null && Tools.getLemma(e1).equals(Tools.getLemma(e2)))
//                Tools.addFeature(features, featurePrefix+key, 1.0);
////            else if(e1!=null && e2!=null && e2.get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("W") && key.equals("Subj")) //hacky check
////                Tools.addFeature(features, featurePrefix+key, 1.0);
//        }
//
//        //based on edges
//        List<SemanticGraphEdge> edges1 = processSentence2(sent1);
//        List<SemanticGraphEdge> edges2 = processSentence2(sent2);
//
//        int unlabeledEdgeMatch = 0, depHeadCnt = 0, labeledEdgeMatch= 0;
////        int fuzzyUnlabeledMatch = 0; //matches "Wh-word" to entity
//        HashMap<String, Double> labeledMatchCnt = new HashMap<String, Double>();
//
//        boolean depHeadUsed1 [] = new boolean[edges1.size()];
//        boolean depHeadUsed2 [] = new boolean[edges2.size()];
//        boolean edgeUsed2 [] = new boolean[edges2.size()];
//
//        int e1 =0, e2;
//        for(SemanticGraphEdge edge1 : edges1) {
//            e2 = 0;
//            for (SemanticGraphEdge edge2 : edges2) {
//                if (edge1.getGovernor().get(CoreAnnotations.LemmaAnnotation.class).equals(edge2.getGovernor().get(CoreAnnotations.LemmaAnnotation.class))) {
//
//                    IndexedWord w1 = edge1.getDependent();
//                    IndexedWord w2 = edge2.getDependent();
//
//                    //get the agent through coref
//                    String entity1 = getCorefEntity(w1, paragraph);
//                    String entity2 = getCorefEntity(w2, paragraph);
//
//                    boolean breakFlag = false;
//                    //TODO : check more than direct equality?
//                    if (entity1.equals(entity2)) {
////                        if (edgeUsed2[e2]) continue;
//                        unlabeledEdgeMatch++;
//                        String label1 = edge1.getRelation().getShortName();
//                        if (label1.equals(edge2.getRelation().getShortName())) {
//                            labeledEdgeMatch++;
//                            Tools.incrementMap(labeledMatchCnt, label1);
//                        }
//                        edgeUsed2[e2] = true;
//                        breakFlag = true;
//                    }
////                    else if(w1.get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("W") || w2.get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("W") )
////                        fuzzyUnlabeledMatch++;
//
//                    if(!depHeadUsed1[e1] && !depHeadUsed2[e2]) {
//                        depHeadCnt++;
//                        depHeadUsed2[e2] = true;
//                        depHeadUsed1[e1] = true;
//                    }
////                    if(breakFlag) break;
//                }
//                e2++;
//            }
//            e1++;
//        }
//
//        if(unlabeledEdgeMatch>0)
//            Tools.addFeature(features, featurePrefix+"unlabeledEdgeMatch", unlabeledEdgeMatch);
////        Tools.addFeature(features, featurePrefix+"unlabeledEdgeMatch_"+ unlabeledEdgeMatch, 1.0);
//
////        if(fuzzyUnlabeledMatch> 0 ) //WH-word match
////            Tools.addFeature(features, featurePrefix+"fuzzyUnlabeledMatch", 1.);
//        if(labeledEdgeMatch>0) {
//            Tools.addFeature(features, featurePrefix+"labeledEdgeMatch", labeledEdgeMatch);
//
//            for(Map.Entry<String, Double> label : labeledMatchCnt.entrySet()) {
//                Tools.addFeature(features, featurePrefix+"labeledMatch_"+label.getKey(), label.getValue());
//            }
//        }
////        Tools.addFeature(features, featurePrefix+"labeledEdgeMatch_"+labeledEdgeMatch,1.);
//
//        if(depHeadCnt>0)
//            Tools.addFeature(features, featurePrefix+"depHead", depHeadCnt);
////        Tools.addFeature(features, featurePrefix+"depHead_"+ depHeadCnt, 1.);
//
//        List<IndexedWord> verbs1 = getVerbs(sent1);
//        List<IndexedWord> verbs2 = getVerbs(sent2);
//        int verbCnt  = 0;
//        boolean[] verbUsed = new boolean[verbs2.size()];
//
//        for(IndexedWord w1 : verbs1) {
//            int i = 0;
//            for (IndexedWord w2 : verbs2) {
//                if (verbUsed[i]) continue;
//                if (w1.get(CoreAnnotations.LemmaAnnotation.class).equals(w2.get(CoreAnnotations.LemmaAnnotation.class))) {
//                    verbCnt++;
//                    verbUsed[i] = true;
//                }
//                i++;
//            }
//        }
//
//        if(verbCnt>0)
//            Tools.addFeature(features, featurePrefix+"VerbMatch", verbCnt);
////        Tools.addFeature(features, featurePrefix+"VerbMatch_"+verbCnt, 1.);
//
//        //get Entities
//        Set<String> entities1 = getEntities(paragraph, sentNum1, false, answerNum);
//        Set<String> entities2;
//
//        entities2 = getEntities(paragraph, sentNum2, isQuestion, answerNum);
//        HashSet<String> entities1Copy = new HashSet<String>(entities1);
//
//        entities1.retainAll(entities2); //intersection
//        entities1Copy.addAll(entities2); //union
//        if(entities1.size() > 0) {
//            //if answer, tie it up with the number of entities covered by question in sentence
////            Set<String> quesEntities = getEntities(paragraph, sentNum2, isQuestion, -1);
////            entities1Copy.retainAll(quesEntities);
//
////            if(featurePrefix.equals("A_"))
////                Tools.addFeature(features, featurePrefix + "EntityMatch_Q"+entities1Copy.size(), ((float) entities1.size()));
////            else
//                Tools.addFeature(features, featurePrefix + "EntityMatch", entities1.size());
//        }
////        Tools.addFeature(features, featurePrefix + "EntityMatch_"+entities1.size(), 1.);
//
////        if(isQuestion) {
////            if(answerNum==-1)
////                Tools.updateMap(features, getFeaturesQZ(paragraph, new int [] {sentNum1, sentNum1}, sentNum2));
////            else
////                Tools.updateMap(features, getFeaturesAQZ(paragraph, new int [] {sentNum1, sentNum1}, sentNum2, answerNum));
////        }
//
//
//        return features;
//    }
//
//
//
//    static HashMap<Integer, Double> getClauseFeatures(Paragraph paragraph, int sentNum1, int sentNum2) {
//        return getClauseFeatures(paragraph, sentNum1, sentNum2, false, -1);
//    }
//
//
//    //gets features tailored to use clauses
//    static HashMap<Integer, Double> getClauseFeaturesMulti(Paragraph paragraph, int [] sentNums, int sentNum2, boolean isQuestion, int answerNum) {
//        //IMP : if isQuestion == true, then take Question instead.
//        //IMP : isQuestion has to be true and answerNum !=-1 for Answer case.
//        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
//        String featurePrefix;
//        if(isQuestion) {
//            if(answerNum!=-1)
//                featurePrefix = "A_";
//            else
//                featurePrefix = "Q_";
//        }
//        else
//            featurePrefix = "Z_";
//
//
//
//
//        int unlabeledEdgeMatch = 0, depHeadCnt = 0, labeledEdgeMatch = 0;
//        int verbCnt  = 0;
//        HashMap<String, Double> labeledMatchCnt = new HashMap<String, Double>();
//
//        for(int sentNum : sentNums) {
//            //Take care of various cases this function can be used in
//            SemanticGraph sent1 = paragraph.sentDepGraphs.get(sentNum);
//            SemanticGraph sent2;
//            if(isQuestion) {
//                if (answerNum!=-1)
//                    sent2 = paragraph.questions.get(sentNum2).choices.get(answerNum).get(CoreAnnotations.SentencesAnnotation.class)
//                            .get(0).get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
//                else
//                    sent2 = paragraph.questions.get(sentNum2).question.get(CoreAnnotations.SentencesAnnotation.class)
//                            .get(0).get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
//            }
//            else
//                sent2 = paragraph.sentDepGraphs.get(sentNum2);
//
//            HashMap<String, IndexedWord> clauseMap1 = Clause.processSentence(sent1);
//            HashMap<String, IndexedWord> clauseMap2 = Clause.processSentence(sent2);
//
//            //add a feature for each match
//            for(String key : clauseMap1.keySet()) {
//                IndexedWord e1, e2;
//
//                if(!isQuestion) {
//                    e1 = getEntityFromIndexedWord(paragraph, clauseMap1.get(key));
//                    e2 = getEntityFromIndexedWord(paragraph, clauseMap2.get(key));
//                }
//                else {
//                    e1 = clauseMap1.get(key);
//                    e2 = clauseMap2.get(key);
//                }
//
//                if(e1!=null && e2!=null && Tools.getLemma(e1).equals(Tools.getLemma(e2)))
//                    Tools.addFeature(features, featurePrefix+key, 1.0);
//            }
//
//            //based on edges
//            List<SemanticGraphEdge> edges1 = processSentence2(sent1);
//            List<SemanticGraphEdge> edges2 = processSentence2(sent2);
//
//
//
//            boolean depHeadUsed1[] = new boolean[edges1.size()];
//            boolean depHeadUsed2[] = new boolean[edges2.size()];
//            boolean edgeUsed2[] = new boolean[edges2.size()];
//
//            int e1 = 0, e2;
//            for (SemanticGraphEdge edge1 : edges1) {
//                e2 = 0;
//                for (SemanticGraphEdge edge2 : edges2) {
//                    if (edge1.getGovernor().get(CoreAnnotations.LemmaAnnotation.class).equals(edge2.getGovernor().get(CoreAnnotations.LemmaAnnotation.class))) {
//
//                        IndexedWord w1 = edge1.getDependent();
//                        IndexedWord w2 = edge2.getDependent();
//
//                        //get the agent through coref
//                        String entity1 = getCorefEntity(w1, paragraph);
//                        String entity2 = getCorefEntity(w2, paragraph);
//
//                        boolean breakFlag = false;
//                        //TODO : check more than direct equality?
//                        if (entity1.equals(entity2)) {
////                        if (edgeUsed2[e2]) continue;
//                            unlabeledEdgeMatch++;
//                            String label1 = edge1.getRelation().getShortName();
//                            if (label1.equals(edge2.getRelation().getShortName())) {
//                                labeledEdgeMatch++;
//                                Tools.incrementMap(labeledMatchCnt, label1);
//                            }
//                            edgeUsed2[e2] = true;
//                            breakFlag = true;
//                        }
////                    else if(w1.get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("W") || w2.get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("W") )
////                        fuzzyUnlabeledMatch++;
//
//                        if (!depHeadUsed1[e1] && !depHeadUsed2[e2]) {
//                            depHeadCnt++;
//                            depHeadUsed2[e2] = true;
//                            depHeadUsed1[e1] = true;
//                        }
////                    if(breakFlag) break;
//                    }
//                    e2++;
//                }
//                e1++;
//            }
//
//            List<IndexedWord> verbs1 = getVerbs(sent1);
//            List<IndexedWord> verbs2 = getVerbs(sent2);
//
//            boolean[] verbUsed = new boolean[verbs2.size()];
//
//            for(IndexedWord w1 : verbs1) {
//                int i = 0;
//                for (IndexedWord w2 : verbs2) {
//                    if (verbUsed[i]) continue;
//                    if (w1.get(CoreAnnotations.LemmaAnnotation.class).equals(w2.get(CoreAnnotations.LemmaAnnotation.class))) {
//                        verbCnt++;
//                        verbUsed[i] = true;
//                    }
//                    i++;
//                }
//            }
//        }
//
//        if(unlabeledEdgeMatch>0)
//            Tools.addFeature(features, featurePrefix+"unlabeledEdgeMatch", unlabeledEdgeMatch);
////        Tools.addFeature(features, featurePrefix+"unlabeledEdgeMatch_"+ unlabeledEdgeMatch, 1.0);
//
////        if(fuzzyUnlabeledMatch> 0 ) //WH-word match
////            Tools.addFeature(features, featurePrefix+"fuzzyUnlabeledMatch", 1.);
//        if(labeledEdgeMatch>0) {
//            Tools.addFeature(features, featurePrefix+"labeledEdgeMatch", labeledEdgeMatch);
//
//            for(Map.Entry<String, Double> label : labeledMatchCnt.entrySet()) {
//                Tools.addFeature(features, featurePrefix+"labeledMatch_"+label.getKey(), label.getValue());
//            }
//        }
////        Tools.addFeature(features, featurePrefix+"labeledEdgeMatch_"+labeledEdgeMatch,1.);
//
//        if(depHeadCnt>0)
//            Tools.addFeature(features, featurePrefix+"depHead", depHeadCnt);
////        Tools.addFeature(features, featurePrefix+"depHead_"+ depHeadCnt, 1.);
//
//
//
//        if(verbCnt>0)
//            Tools.addFeature(features, featurePrefix+"VerbMatch", verbCnt);
////        Tools.addFeature(features, featurePrefix+"VerbMatch_"+verbCnt, 1.);
//
//        //get Entities
//        Set<String> entitiesZ1 = getEntities(paragraph, sentNums[0], false, answerNum);
//        Set<String> entitiesZ2 = getEntities(paragraph, sentNums[1], false, answerNum);
//        entitiesZ1.retainAll(entitiesZ2);
//        Set<String> entitiesZ = new HashSet<String>(entitiesZ1);
//
//        Set<String> entities2;
//        entities2 = getEntities(paragraph, sentNum2, isQuestion, answerNum);
//        HashSet<String> entities1Copy = new HashSet<String>(entitiesZ);
//
//        entitiesZ.retainAll(entities2); //intersection
//        entities1Copy.addAll(entities2); //union
//        if(entitiesZ.size() > 0) {
//            //if answer, tie it up with the number of entities covered by question in sentence
////            Set<String> quesEntities = getEntities(paragraph, sentNum2, isQuestion, -1);
////            entities1Copy.retainAll(quesEntities);
//
////            if(featurePrefix.equals("A_"))
////                Tools.addFeature(features, featurePrefix + "EntityMatch_Q"+entities1Copy.size(), ((float) entitiesZ.size()));
////            else
////            if(! isQuestion)
//                Tools.addFeature(features, featurePrefix + "entityMatch", entitiesZ.size());
//        }
////        Tools.addFeature(features, featurePrefix + "EntityMatch_"+entitiesZ.size(), 1.);
//
////
////        if(isQuestion) {
////            if(answerNum==-1)
////                Tools.updateMap(features, getFeaturesQZ(paragraph, sentNums, sentNum2));
////            else
////                Tools.updateMap(features, getFeaturesAQZ(paragraph, sentNums, sentNum2, answerNum));
////        }
//
//        return features;
//    }
//
//    //--------------------------------------- Features relying on Entity Graph -------------------------------------------------------
//
//
//    static Set<String> getEntitiesFromGraph(EntityGraph graph, boolean lemmatize) {
//        Set<String> entities =  new HashSet<String>();
//        for(Entity e : graph.nodes.values())
//            if(lemmatize)
//                entities.add(e.lemma);
//            else
//                entities.add(e.str);
//        return entities;
//    }
//
//    static Set<String> getActionsFromCoreMap(CoreMap coreMap) {
//        Set<String> actions = new HashSet<String>();
//
//        for(CoreLabel token : coreMap.get(CoreAnnotations.TokensAnnotation.class))
//            if(token.get(CoreAnnotations.PartOfSpeechAnnotation.class).charAt(0) == 'V')
//                actions.add(token.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase());
//
//        return actions;
//    }
//
//
//    static HashMap<Integer, Double> getFeaturesQZ(Paragraph paragraph, int [] sentNums, int qNum) {
//        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
//
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//
//        //Entity matches
//        Set<String> entityMatches1 = getEntityMatches(paragraph.sentEntityGraphs.get(sentNums[0]), paragraph.questions.get(qNum).entityGraph);
//        Set<String> entityMatches2 = getEntityMatches(paragraph.sentEntityGraphs.get(sentNums[1]), paragraph.questions.get(qNum).entityGraph);
//
//        entityMatches1.addAll(entityMatches2);
//        int numEntityMatch = entityMatches1.size();
////        int numEntityMatch = Math.max(entityMatches1.size(), entityMatches2.size());
//        if(numEntityMatch > 0)
//            Tools.addFeatureIncrement(features, "Q_EntityMatch", numEntityMatch);
//
//
//        //features that capture absence of entities/actions from Q in Z
//        Set<String> quesEntities = getEntitiesFromGraph(paragraph.questions.get(qNum).entityGraph, true);
//        quesEntities.removeAll(entityMatches1);
//        int numMissedEntities = quesEntities.size();
//        if(numMissedEntities > 0)
//            Tools.addFeatureIncrement(features, "Q_MissedEntities", numMissedEntities);
//
//        //features that capture absence of entities/actions Z in Q (to remove Z's that have too many wrong entities)
//        Set<String> quesEntities2 = getEntitiesFromGraph(paragraph.questions.get(qNum).entityGraph, true);
//        Set<String> sentEntities2 = getEntitiesFromGraph(paragraph.sentEntityGraphs.get(sentNums[0]), true);
//        sentEntities2.addAll(getEntitiesFromGraph(paragraph.sentEntityGraphs.get(sentNums[1]), true));
//        sentEntities2.removeAll(quesEntities2); //IMP: make sure entityMatches1 is not used after this point
//        int numMissedEntities2 = quesEntities.size();
////        if(numMissedEntities2 > 0)
////            Tools.addFeatureIncrement(features, "Q_MissedEntities2", numMissedEntities2);
//
//        //Action matches
//        //TODO : reduce redundant matches between sentences?
//        CoreMap question = paragraph.questions.get(qNum).question.get(CoreAnnotations.SentencesAnnotation.class).get(0);
//        int numActionMatch1 = getActionMatches(sentences.get(sentNums[0]), question);
//        int numActionMatch2 = getActionMatches(sentences.get(sentNums[1]), question);
//
//        if(numActionMatch1 > 0)
//            Tools.addFeatureIncrement(features, "Q_ActionMatch", numActionMatch1);
//
//        if(numActionMatch2 > 0)
//            Tools.addFeatureIncrement(features, "Q_ActionMatch", numActionMatch2);
//
//        //missed actions
//        Set<String> quesActions = getActionsFromCoreMap(question);
//        Set<String> sentActions1 = getActionsFromCoreMap(sentences.get(sentNums[0]));
//        Set<String> sentActions2 = getActionsFromCoreMap(sentences.get(sentNums[1]));
//        quesActions.remove(sentActions1);
//        quesActions.remove(sentActions2);
//        if(quesActions.size() > 0)
//            Tools.addFeatureIncrement(features, "Q_MissedActions", quesActions.size());
//
//
//
//        //Entity Edge matches
//        int unlabeledEntityEdgeMatch1 = getUnlabeledEntityEdgeMatch(paragraph.sentEntityGraphs.get(sentNums[0]), paragraph.questions.get(qNum).entityGraph);
//        int unlabeledEntityEdgeMatch2 = getUnlabeledEntityEdgeMatch(paragraph.sentEntityGraphs.get(sentNums[1]), paragraph.questions.get(qNum).entityGraph);
//
//        if(unlabeledEntityEdgeMatch1 > 0)
//            Tools.addFeatureIncrement(features, "Q_UnlabeledEntityEdge", unlabeledEntityEdgeMatch1);
//
//        if(unlabeledEntityEdgeMatch2 > 0)
//            Tools.addFeatureIncrement(features, "Q_UnlabeledEntityEdge", unlabeledEntityEdgeMatch2);
//
//        //Labeled edge matches
//        ArrayList<Integer> labeledEntityEdgeMatch1 = getLabeledEntityEdgeMatch(paragraph.sentEntityGraphs.get(sentNums[0]), paragraph.questions.get(qNum).entityGraph);
//        ArrayList<Integer> labeledEntityEdgeMatch2 = getLabeledEntityEdgeMatch(paragraph.sentEntityGraphs.get(sentNums[1]), paragraph.questions.get(qNum).entityGraph);
//        if(labeledEntityEdgeMatch1.size() > 0)
//            Tools.addFeatureIncrement(features, "Q_LabeledEntityEdgeCnt", labeledEntityEdgeMatch1.size());
//
//        if(labeledEntityEdgeMatch2.size() > 0)
//            Tools.addFeatureIncrement(features, "Q_LabeledEntityEdgeCnt", labeledEntityEdgeMatch2.size());
//
//
//        for(int i : labeledEntityEdgeMatch1)
//            Tools.addFeatureIncrement(features, "Q_LabeledEntityEdgeMatch", i);
//        for(int i : labeledEntityEdgeMatch2)
//            Tools.addFeatureIncrement(features, "Q_LabeledEntityEdgeMatch", i);
//
//        return features;
//    }
//
////    //single sentence version
////    static HashMap<Integer, Double> getFeaturesQZ(Paragraph paragraph, int sentNum, int qNum) {
////        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
////
////        String qType = "";
////        Question Q = paragraph.questions.get(qNum);
////        if(Main.MIXED_SINGLE_MULTI) qType = "_"+Integer.toString(Q.type);
////
////        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
////
////        //Entity matches
////        Set<String> entityMatches1 = getEntityMatches(paragraph.sentEntityGraphs.get(sentNum), paragraph.questions.get(qNum).entityGraph);
////
////        int numEntityMatch = entityMatches1.size();
//////        if(numEntityMatch > 0)
////            Tools.addFeatureSmart(features, "Q_EntityMatch"+qType, numEntityMatch);
////
////
////        //IMP: NEW: features that capture absence of entities/actions from Q in Z
////        Set<String> quesEntities = getEntitiesFromGraph(paragraph.questions.get(qNum).entityGraph, true);
////        quesEntities.removeAll(entityMatches1);
////        int numMissedEntities = quesEntities.size();
//////        if(numMissedEntities > 0)
////            Tools.addFeatureSmart(features, "Q_MissedEntities"+qType, numMissedEntities);
////
////        //Action matches
////        //TODO : reduce redundant matches between sentences?
////        CoreMap question = paragraph.questions.get(qNum).question.get(CoreAnnotations.SentencesAnnotation.class).get(0);
////        int numActionMatch1 = getActionMatches(sentences.get(sentNum), question);
////
//////        if(numActionMatch1 > 0)
////            Tools.addFeatureSmart(features, "Q_ActionMatch"+qType, numActionMatch1);
////
////        //missed actions
////        Set<String> quesActions = getActionsFromCoreMap(question);
////        Set<String> sentActions1 = getActionsFromCoreMap(sentences.get(sentNum));
////        quesActions.remove(sentActions1);
//////        if(quesActions.size() > 0)
////            Tools.addFeatureSmart(features, "Q_MissedActions"+qType, quesActions.size());
////
////
////
////        //Entity Edge matches
////        int unlabeledEntityEdgeMatch1 = getUnlabeledEntityEdgeMatch(paragraph.sentEntityGraphs.get(sentNum), paragraph.questions.get(qNum).entityGraph);
////
////
////
////        //Labeled edge matches
////        ArrayList<Integer> labeledEntityEdgeMatch1 = getLabeledEntityEdgeMatch(paragraph.sentEntityGraphs.get(sentNum), paragraph.questions.get(qNum).entityGraph);
//////        if(labeledEntityEdgeMatch1.size() > 0)
////        Tools.addFeatureSmart(features, "Q_LabeledEntityEdgeCnt"+qType, labeledEntityEdgeMatch1.size());
////
//////        for(int i : labeledEntityEdgeMatch1)
//////            Tools.addFeatureIncrement(features, "Q_LabeledEntityEdgeMatch", i);
////
////        return features;
////    }
//
////    //features using EntityGraph for the triple (A,Q,Z)
////    //TODO : separately handle single word answers?
////    static HashMap<Integer, Double> getFeaturesAQZ(Paragraph paragraph, int [] sentNums, int qNum, int ansNum) {
////        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
////
////        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
////
////        //Entity matches
////        //TODO : reduce redundant matches between sentences?
////        Set<String> entityMatches1 = getEntityMatches(paragraph.sentEntityGraphs.get(sentNums[0]), paragraph.questions.get(qNum).choiceGraphs.get(ansNum));
////        Set<String> entityMatches2 = getEntityMatches(paragraph.sentEntityGraphs.get(sentNums[1]), paragraph.questions.get(qNum).choiceGraphs.get(ansNum));
////
////        entityMatches1.retainAll(entityMatches2);
////        int numEntityMatch = entityMatches1.size();
//////        int numEntityMatch = Math.max(entityMatches1.size(), entityMatches2.size());
////
//////        if(numEntityMatch > 0)
////            Tools.addFeatureIncrement(features, "A_EntityMatch", numEntityMatch);
//////        else
//////            Tools.addFeatureIncrement(features, "A_EntityMatch_0", 1.0);
////
////        //Action matches
////        CoreMap answer = paragraph.questions.get(qNum).choices.get(ansNum).get(CoreAnnotations.SentencesAnnotation.class).get(0);
////        int numActionMatch1 = getActionMatches(sentences.get(sentNums[0]), answer);
////        int numActionMatch2 = getActionMatches(sentences.get(sentNums[1]), answer);
////
////        Tools.addFeatureIncrement(features, "A_ActionMatch", numActionMatch1);
////        Tools.addFeatureIncrement(features, "A_ActionMatch", numActionMatch2);
////
//////        int numActionMatch = Math.max(numActionMatch1, numActionMatch2);
//////        if(numActionMatch > 0)
//////            Tools.addFeatureIncrement(features, "A_ActionMatch", numActionMatch);
//////        else
//////            Tools.addFeatureIncrement(features, "A_ActionMatch_0", 1.0);
////
////        //Entity Edge matches - TODO not necessary probably
////        int unlabeledEntityEdgeMatch1 = getUnlabeledEntityEdgeMatch(paragraph.sentEntityGraphs.get(sentNums[0]), paragraph.questions.get(qNum).choiceGraphs.get(ansNum));
////        int unlabeledEntityEdgeMatch2 = getUnlabeledEntityEdgeMatch(paragraph.sentEntityGraphs.get(sentNums[1]), paragraph.questions.get(qNum).choiceGraphs.get(ansNum));
////
////        Tools.addFeatureIncrement(features, "A_UnlabeledEntityEdge", unlabeledEntityEdgeMatch1);
////        Tools.addFeatureIncrement(features, "A_UnlabeledEntityEdge", unlabeledEntityEdgeMatch2);
////
//////        int unlabeledEntityEdgeMatch = Math.max(unlabeledEntityEdgeMatch1, unlabeledEntityEdgeMatch2);
//////        if(unlabeledEntityEdgeMatch > 0)
//////            Tools.addFeatureIncrement(features, "A_UnlabeledEntityEdge", unlabeledEntityEdgeMatch);
//////        else
//////            Tools.addFeatureIncrement(features, "A_UnlabeledEntityEdge_0", 1);
////
////        //Labeled edge matches TODO not necessary
////        ArrayList<Integer> labeledEntityEdgeMatch1 = getLabeledEntityEdgeMatch(paragraph.sentEntityGraphs.get(sentNums[0]), paragraph.questions.get(qNum).choiceGraphs.get(ansNum));
////        ArrayList<Integer> labeledEntityEdgeMatch2 = getLabeledEntityEdgeMatch(paragraph.sentEntityGraphs.get(sentNums[1]), paragraph.questions.get(qNum).choiceGraphs.get(ansNum));
////
////        Tools.addFeatureIncrement(features, "A_LabeledEntityEdgeCnt", labeledEntityEdgeMatch1.size());
////        Tools.addFeatureIncrement(features, "A_LabeledEntityEdgeCnt", labeledEntityEdgeMatch2.size());
////
//////        int labeledEntityEdgeMatch = Math.max(labeledEntityEdgeMatch1.size(), labeledEntityEdgeMatch2.size());
//////        if(labeledEntityEdgeMatch > 0)
//////            Tools.addFeatureIncrement(features, "A_LabeledEntityEdgeCnt", labeledEntityEdgeMatch);
//////        else
//////            Tools.addFeatureIncrement(features, "A_LabeledEntityEdgeCnt_0", 1.);
////
////        for(int i : labeledEntityEdgeMatch1)
////            Tools.addFeatureIncrement(features, "A_LabeledEntityEdgeMatch", i);
////        for(int i : labeledEntityEdgeMatch2)
////            Tools.addFeatureIncrement(features, "A_LabeledEntityEdgeMatch", i);
////
////        //using the Wh words in question
////        int WHwordMatch1 = getWhWordMatch(paragraph.sentEntityGraphs.get(sentNums[0]),  paragraph.questions.get(qNum).entityGraph, paragraph.questions.get(qNum).choices.get(ansNum));
////        int WHwordMatch2 = getWhWordMatch(paragraph.sentEntityGraphs.get(sentNums[1]),  paragraph.questions.get(qNum).entityGraph, paragraph.questions.get(qNum).choices.get(ansNum));
////
////        Tools.addFeatureIncrement(features, "A_WhMatch", WHwordMatch1);
////        Tools.addFeatureIncrement(features, "A_WhMatch", WHwordMatch2);
////
//////        int WHwordMatch = Math.max(WHwordMatch1, WHwordMatch2);
//////        if(WHwordMatch > 0)
//////            Tools.addFeatureIncrement(features, "A_WhMatch", WHwordMatch);
//////        else
//////            Tools.addFeatureIncrement(features, "A_WhMatch_0", 1.);
////
////
////        //using all words connected to words in answer, and taking intersection of word in Q with words in the connecting edge
////        int intersectMatch1 = getIntersectMatch(paragraph.sentEntityGraphs.get(sentNums[0]), paragraph.questions.get(qNum).question, paragraph.questions.get(qNum).choices.get(ansNum));
////        int intersectMatch2 = getIntersectMatch(paragraph.sentEntityGraphs.get(sentNums[1]), paragraph.questions.get(qNum).question, paragraph.questions.get(qNum).choices.get(ansNum));
////
////        Tools.addFeatureIncrement(features, "A_IntersectMatch", intersectMatch1);
////        Tools.addFeatureIncrement(features, "A_IntersectMatch", intersectMatch2);
////
//////        int intersectMatch = Math.max(intersectMatch1, intersectMatch2);
//////        if(intersectMatch > 0)
//////            Tools.addFeatureIncrement(features, "A_IntersectMatch", intersectMatch);
//////        else
//////            Tools.addFeatureIncrement(features, "A_IntersectMatch_0", 1.);
////
////
////        //NEW: Clause features - intersection between split clauses and answer and question
////        Tools.updateMap(features, splitClauseFeatures(paragraph, sentNums, qNum, ansNum));
////
////
////        return features;
////    }
//
//    static HashMap<Integer, Double> getFeaturesAQZ(Paragraph paragraph, int sentNum, int qNum, int ansNum) {
//        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
//
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//
//        //Entity matches
//        //TODO : reduce redundant matches between sentences in multi case?
//        Set<String> entityMatches1 = getEntityMatches(paragraph.sentEntityGraphs.get(sentNum), paragraph.questions.get(qNum).choiceGraphs.get(ansNum));
//
//        int numEntityMatch = entityMatches1.size();
//
//        Tools.addFeatureIncrement(features, "A_EntityMatch", numEntityMatch);
//
//        //Action matches
//        CoreMap answer = paragraph.questions.get(qNum).choices.get(ansNum).get(CoreAnnotations.SentencesAnnotation.class).get(0);
//        int numActionMatch1 = getActionMatches(sentences.get(sentNum), answer);
//
//        Tools.addFeatureIncrement(features, "A_ActionMatch", numActionMatch1);
//
//
//        //Entity Edge matches - TODO not necessary probably
//        int unlabeledEntityEdgeMatch1 = getUnlabeledEntityEdgeMatch(paragraph.sentEntityGraphs.get(sentNum), paragraph.questions.get(qNum).choiceGraphs.get(ansNum));
//        Tools.addFeatureIncrement(features, "A_UnlabeledEntityEdge", unlabeledEntityEdgeMatch1);
//
//
//        //Labeled edge matches TODO not necessary
//        ArrayList<Integer> labeledEntityEdgeMatch1 = getLabeledEntityEdgeMatch(paragraph.sentEntityGraphs.get(sentNum), paragraph.questions.get(qNum).choiceGraphs.get(ansNum));
//        Tools.addFeatureIncrement(features, "A_LabeledEntityEdgeCnt", labeledEntityEdgeMatch1.size());
//
//        for(int i : labeledEntityEdgeMatch1)
//            Tools.addFeatureIncrement(features, "A_LabeledEntityEdgeMatch", i);
//
//        //using the Wh words in question
////        int WHwordMatch1 = getWhWordMatch(paragraph.sentEntityGraphs.get(sentNum),  paragraph.questions.get(qNum).entityGraph, paragraph.questions.get(qNum).choices.get(ansNum));
////        Tools.addFeatureIncrement(features, "A_WhMatch", WHwordMatch1);
//
//
//
//        //using all words connected to words in answer, and taking intersection of word in Q with words in the connecting edge
////        int intersectMatch1 = getIntersectMatch(paragraph.sentEntityGraphs.get(sentNum), paragraph.questions.get(qNum).question, paragraph.questions.get(qNum).choices.get(ansNum));
////        Tools.addFeatureIncrement(features, "A_IntersectMatch", intersectMatch1);
//
//        return features;
//    }
//
////    //NEW: Very specific matching inside clause with words from question and answer
////    public static HashMap<Integer, Double>  splitClauseFeatures(Paragraph paragraph, int[] sentNums, int qNum, int ansNum) {
////        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
////
////        Set<String> qWords = new HashSet<String>();
////        for(IndexedWord w : paragraph.questions.get(qNum).depGraph.vertexSet())
////            qWords.add(Tools.getLemma(w));
////
////        Set<String> aWords = new HashSet<String>();
////        for(IndexedWord w : paragraph.questions.get(qNum).choices.get(ansNum).get(CoreAnnotations.SentencesAnnotation.class).get(0)
////                .get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class).vertexSet())
////            aWords.add(Tools.getLemma(w));
////
////
////        String qType = "";
////        Question Q = paragraph.questions.get(qNum);
////        if(Main.MIXED_SINGLE_MULTI) qType = "_"+Integer.toString(Q.type);
////
////        for(int sentNum : sentNums) {
////            ArrayList<SemanticGraph> graphList = new ArrayList<SemanticGraph>(paragraph.sentEntityGraphs.get(sentNum).clauses);
////
////            for(SemanticGraph clause : graphList) {
////                boolean flagQ = false, flagA = false;
////                double cntQ = 0, cntA = 0;
////                for (IndexedWord w : clause.vertexSet()) {
////                    if(!Global.POStags.contains(w.get(CoreAnnotations.PartOfSpeechAnnotation.class).substring(0,1))) continue;
////                    String lemma = Tools.getLemma(w);
////                    if (qWords.contains(lemma)) {
////                        flagQ = true;
////                        cntQ++;
//////                        if(SentRel.TEST)
//////                            System.out.println("QMatch: "+w.get(CoreAnnotations.LemmaAnnotation.class));
////                    }
////                    else if (aWords.contains(lemma)) {
////                        flagA = true;
////                        cntA++;
//////                        if(SentRel.TEST)
//////                            System.out.println("AMatch: "+w.get(CoreAnnotations.LemmaAnnotation.class));
////                    }
////                }
////                if(flagA && flagQ) {
//////                    Tools.addFeatureIncrement(features, "CLAUSE_A", cntA); //TODO : be careful with multi-sentences - cant just increment
//////                    Tools.addFeatureIncrement(features, "CLAUSE_Q", cntQ);
////                    Tools.addFeatureIncrement(features, "CLAUSE"+qType, cntA+cntQ);
////                }
////                else {
////                    Tools.addFeatureIncrement(features, "CLAUSE_0"+qType, 1.);
////                }
////
////            }
////
////            //take the entire sentence now
////            SemanticGraph graph = paragraph.sentDepGraphs.get(sentNum);
////            boolean flagQ = false, flagA = false;
////            int cntQ = 0, cntA = 0;
////            for (IndexedWord w : graph.vertexSet()) {
////                if(!Global.POStags.contains(w.get(CoreAnnotations.PartOfSpeechAnnotation.class).substring(0,1))) continue;
////                String lemma = Tools.getLemma(w);
////                if (qWords.contains(lemma)) {
////                    flagQ = true;
////                    cntQ++;
////                }
////                else if (aWords.contains(lemma)) {
////                    flagA = true;
////                    cntA++;
////                }
////            }
////            if(flagA && flagQ) {
////                Tools.addFeatureIncrement(features, "CLAUSE_FULL"+qType, cntA+cntQ);
//////                Tools.addFeatureIncrement(features, "CLAUSE_FULL_A_"+cntA, cntQ);
//////                Tools.addFeatureIncrement(features, "CLAUSE_FULL_Q_"+cntQ, cntA);
////            }
////            else
////                Tools.addFeatureIncrement(features, "CLAUSE_FULL_0"+qType, 1.);
////
////
////        }
////
////        return features;
////    }
//
//    static Set<String> getWordsOnEdge(EntityEdge edge) {
//        Set<String> wordsOnEdge = new HashSet<String>();
//        for(org.apache.commons.lang3.tuple.Pair<String, String> lemmaAndPOS : edge.word2LemmaAndPOS.values())
//            wordsOnEdge.add(lemmaAndPOS.getKey());
//        return wordsOnEdge;
//    }
//
//    //intersect words with wordsOnEdge between answer word and an entity in Z
//    private static int getIntersectMatch(EntityGraph sentGraph, Annotation question, Annotation answer) {
//        HashSet<String> answerWords = new HashSet<String>();
//        HashSet<String> quesWords = new HashSet<String>();
//        for(CoreLabel token : question.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(CoreAnnotations.TokensAnnotation.class))
//            quesWords.add(token.get(CoreAnnotations.LemmaAnnotation.class));
//        for(CoreLabel token : answer.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(CoreAnnotations.TokensAnnotation.class))
//            answerWords.add(token.get(CoreAnnotations.LemmaAnnotation.class));
//
//        int cnt = 0;
//        Map<Integer, Entity> nodes = sentGraph.nodes;
//        for(int i : sentGraph.edges.keySet())
//            if(answerWords.contains(nodes.get(i).lemma))
//                for(int j : sentGraph.edges.get(i).keySet()) {
//                    Set<String> wordsOnEdge = getWordsOnEdge(sentGraph.edges.get(i).get(j));
//                    wordsOnEdge.retainAll(quesWords);
//                    cnt += wordsOnEdge.size();
//                }
//        return cnt;
//    }
//
//    private static int getWhWordMatch(EntityGraph sentGraph, EntityGraph quesGraph, Annotation ansAnnotation) {
//        int cnt = 0;// each integer matches the number of words in the edge that appear in common
//        Map<Integer, Entity> nodes1 = sentGraph.nodes;
//        Map<Integer, Entity> nodes2 = quesGraph.nodes;
//        for(int i : sentGraph.edges.keySet())
//            for(int j : sentGraph.edges.get(i).keySet()) {
//                if (i >= j) continue;
//                for (int i2 : quesGraph.edges.keySet())
//                    for (int j2 : quesGraph.edges.get(i2).keySet()) {
//                        if ((nodes1.get(i).lemma.equals(nodes2.get(i2).lemma) && (nodes1.get(j).lemma.equals(nodes2.get(j2).lemma) || nodes2.get(j2).pos.charAt(0) == 'W')) ||
//                                ((nodes1.get(i).lemma.equals(nodes2.get(i2).lemma) || nodes2.get(i2).pos.charAt(0) == 'W') && (nodes1.get(j).lemma.equals(nodes2.get(j2).lemma) )) )  {
//
//                            //normal match
////                            Set<String> wordsOnEdge = new HashSet<String>(sentGraph.edges.get(i).get(j).word2LemmaAndPOS.keySet());
//                            //lemma match
//                            Set<String> wordsOnEdge = getWordsOnEdge(sentGraph.edges.get(i).get(j));
//                            for(CoreLabel token : ansAnnotation.get(CoreAnnotations.TokensAnnotation.class)) {
//                                if(wordsOnEdge.contains(token.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase()))
//                                    cnt++;
//                            }
//                        }
//                    }
//            }
//
//        return cnt;
//    }
//
//    private static int getUnlabeledEntityEdgeMatch(EntityGraph entityGraph1, EntityGraph entityGraph2) {
//        int cnt = 0;
//        Map<Integer, Entity> nodes1 = entityGraph1.nodes;
//        Map<Integer, Entity> nodes2 = entityGraph2.nodes;
//        for(int i : entityGraph1.edges.keySet())
//            for(int j : entityGraph1.edges.get(i).keySet()) {
//                if (i >= j) continue;  //to avoid counting twice - only for outer loop
//                for (int i2 : entityGraph2.edges.keySet())
//                    for (int j2 : entityGraph2.edges.get(i2).keySet()) {
//                        if (nodes1.get(i).lemma.equals(nodes2.get(i2).lemma) && nodes1.get(j).lemma.equals(nodes2.get(j2).lemma))
//                            cnt++;
//                    }
//            }
//
//        return cnt;
//    }
//
//    private static ArrayList<Integer> getLabeledEntityEdgeMatch(EntityGraph entityGraph1, EntityGraph entityGraph2) {
//        ArrayList<Integer> labeledMatchCnt = new ArrayList<Integer>(); // each integer matches the number of words in the edge that appear in common
//        Map<Integer, Entity> nodes1 = entityGraph1.nodes;
//        Map<Integer, Entity> nodes2 = entityGraph2.nodes;
//        for(int i : entityGraph1.edges.keySet())
//            for(int j : entityGraph1.edges.get(i).keySet()) {
//                if (i >= j) continue;
//                for (int i2 : entityGraph2.edges.keySet())
//                    for (int j2 : entityGraph2.edges.get(i2).keySet()) {
//                        if (nodes1.get(i).lemma.equals(nodes2.get(i2).lemma) && nodes1.get(j).lemma.equals(nodes2.get(j2).lemma)) {
//                            //normal match
////                            Set<String> wordsOnEdge = new HashSet<String>(sentGraph.edges.get(i).get(j).word2LemmaAndPOS.keySet());
//                            //lemma match
//                            Set<String> wordsOnEdge1 = getWordsOnEdge(entityGraph1.edges.get(i).get(j));
//                            Set<String> wordsOnEdge2 = getWordsOnEdge(entityGraph2.edges.get(i2).get(j2));
//
//                            wordsOnEdge2.retainAll(wordsOnEdge2);
//                            if (wordsOnEdge1.size() > 0)
//                                labeledMatchCnt.add(wordsOnEdge1.size());
//                        }
//                    }
//            }
//
//        return labeledMatchCnt;
//    }
//
//    static int getActionMatches(CoreMap coreMap1, CoreMap coreMap2) {
//        Set<String> actions1 = new HashSet<String>();
//        Set<String> actions2 = new HashSet<String>();
//
//        for(CoreLabel token : coreMap1.get(CoreAnnotations.TokensAnnotation.class))
//            if(token.get(CoreAnnotations.PartOfSpeechAnnotation.class).charAt(0) == 'V')
//                if(!token.get(CoreAnnotations.TextAnnotation.class).toLowerCase().equals("is"))
//                    actions1.add(token.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase());
//
//        for(CoreLabel token : coreMap2.get(CoreAnnotations.TokensAnnotation.class))
//            if(token.get(CoreAnnotations.PartOfSpeechAnnotation.class).charAt(0) == 'V')
//                actions2.add(token.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase());
//
//        actions1.retainAll(actions2);
//
//        return actions1.size();
//    }
//
//    static Set<String> getEntityMatches(EntityGraph entityGraph1, EntityGraph entityGraph2) {
//        Collection<Entity> nodes1 = entityGraph1.nodes.values();
//        Collection<Entity> nodes2 = entityGraph2.nodes.values();
//
//        Set<String> e1 = new HashSet<String>();
//        Set<String> e2 = new HashSet<String>();
//
//        for(Entity x : nodes1)
//            e1.add(x.lemma.toLowerCase());
//
//        for(Entity x : nodes2)
//            e2.add(x.lemma.toLowerCase());
//
//        e1.retainAll(e2);
//
//        return e1;
//    }
//
//
//}
