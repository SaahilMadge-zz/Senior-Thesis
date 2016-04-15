//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.semgraph.SemanticGraph;
//import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
//import edu.stanford.nlp.util.CoreMap;
//import edu.stanford.nlp.util.IntPair;
//import nilgiri.math.DoubleReal;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * Created by ghostof2007 on 2/10/15.
// */
//public class Relation {
//
//
//
//    //main relation types
//    static final int NONE = 0; //just symbolic
//    static final int CAUSAL = NONE+1;
//    static final int TEMPORAL = CAUSAL + 1;
//    static final int HOW = TEMPORAL + 1;
//    static final int ENTITY_MATCH = HOW + 1;
//
//    static final int NUM_RELATIONS = Main.NOREL? 1 : HOW+1; //ignoring entity match!
////    static final int NUM_RELATIONS = 1; //IMP: comment this if using relations!
//
//    //types for question focus
//    static final int LOCATION = NUM_RELATIONS; //where
//    static final int PERSON = NUM_RELATIONS+1; //who
//    static final int ACTION = NUM_RELATIONS+2; // what did someone do?
//    static final int OBJECT = NUM_RELATIONS+3; //what was the name..., properties of objects, etc.
//
//    static HashMap<Integer, Double> getRelationFeatures(Paragraph paragraph, int [] sentNums, int relation) {
//        if(!SentRel.INIT && paragraph.checkCacheRelation(sentNums[0], sentNums[1], relation))
//            return paragraph.sentPair2RelationFeatures.get(new IntPair(sentNums[0], sentNums[1])).get(relation);
//
//        HashMap<Integer, Double> features = new HashMap<>();
//
//
//
//        if(Main.NOREL) return features; //IMP: comment this if using relations!
//
//        if(Main.MIXED_SINGLE_MULTI && sentNums[0] == sentNums[1]) return features; //IMP: comment this if using multi even for same sentence pairs!
//
//        if(sentNums[0] == sentNums[1]) {
//            Tools.addFeatureIncrement(features, "R_SameZ", 1.);
//        }
//        else {
//            //order the sentence acc. to number
//            if(sentNums[0] > sentNums[1]) {
//                int tmp = sentNums[0];
//                sentNums[0] = sentNums[1];
//                sentNums[1] = tmp;
//            }
//
//            CoreMap sent1 = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class).get(sentNums[0]);
//            CoreMap sent2 = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class).get(sentNums[1]);
//            String sentText = sent1.get(CoreAnnotations.TextAnnotation.class).toLowerCase() + " " + sent2.get(CoreAnnotations.TextAnnotation.class).toLowerCase();
//
//            int actionMatch = Clause.getActionMatches(sent1, sent2);
//            int entityMatch = Clause.getEntityMatches(paragraph.sentEntityGraphs.get(sentNums[0]), paragraph.sentEntityGraphs.get(sentNums[1])).size();
//
//            //RST
//            String rstRelations = "";
//            if(Main.RST_ON) {
//                if (paragraph.sent2RSTNodePointers.size() > 0 && sentNums[0] != sentNums[1]) {
//                    for (Tree x : paragraph.sent2RSTNodePointers.get(sentNums[0]))
//                        for (Tree y : paragraph.sent2RSTNodePointers.get(sentNums[1])) {
//                            Tree z = RST.getCommonAncestor(x, y);
//                            String s = z.text.split("\\[")[0];
//                            rstRelations += (s.toLowerCase()+" ");
//                        }
//                }
//            }
//
//            if (relation == CAUSAL) {
//                int matchCnt = Tools.countOccurences(sentText, Global.causalPhrases);
//                Tools.addFeatureSmart(features, "R_CAUSAL_"+CAUSAL, matchCnt);
//                if(matchCnt>0) {
//                    Tools.addFeatureSmart(features, "R_CAUSAL_Act_", actionMatch);
//                    Tools.addFeatureSmart(features, "R_CAUSAL_Ent_", entityMatch);
//                }
//                if(Main.RST_ON)
//                    Tools.addFeatureSmart(features, "RST_Causal" , Tools.countOccurences(rstRelations, "cause"));
//            } else if (relation == TEMPORAL) {
//                int matchCnt = Tools.countOccurences(sentText, Global.temporalPhrases);
//                Tools.addFeatureSmart(features, "R_TEMPORAL_"+TEMPORAL, matchCnt);
//                if(matchCnt>0) {
//                    Tools.addFeatureSmart(features, "R_TEMPORAL_Act_", actionMatch);
//                    Tools.addFeatureSmart(features, "R_TEMPORAL_Ent_", entityMatch);
//                }
//                if(Main.RST_ON)
//                    Tools.addFeatureSmart(features, "RST_Temporal" , Tools.countOccurences(rstRelations, "temporal"));
//            } else if (relation == HOW) {
//                int matchCnt = Tools.countOccurences(sentText, Global.howPhrases);
//                Tools.addFeatureSmart(features, "R_HOW_"+HOW, matchCnt);
//                if(matchCnt>0) {
//                    Tools.addFeatureSmart(features, "R_HOW_Act_", actionMatch);
//                    Tools.addFeatureSmart(features, "R_HOW_Ent_", entityMatch);
//                }
//                if(Main.RST_ON)
//                    Tools.addFeatureSmart(features, "RST_How" , Tools.countOccurences(rstRelations, "explanation"));
//            }
//            else if(relation == ENTITY_MATCH){
//                Set<String> entity1 = Clause.getEntities(paragraph, sentNums[0], false, -1);
//                Set<String> entity2 = Clause.getEntities(paragraph, sentNums[1], false, -1);
//                entity1.retainAll(entity2);
//                int matchCnt = entity1.size();
//
//                Tools.addFeatureSmart(features, "R_ENTITY_"+ENTITY_MATCH, matchCnt);
//            }
//            else {
//                Tools.addFeature(features, "R_NONE_" + NONE, 1.);
//            }
//        }
//
//        //cache
//        paragraph.cacheFeaturesRelation(sentNums[0], sentNums[1], relation, features);
//
//        return features;
//    }
//
//    static HashMap<Integer, Double> getFeaturesQR(Paragraph paragraph, int quesNum, int relation) {
//        if(!SentRel.INIT && paragraph.checkCacheQR(quesNum, relation))
//            return paragraph.QRFeatures.get(quesNum).get(relation);
//
//        HashMap<Integer, Double> features = new HashMap<>();
//
//        if(Main.NOREL || Main.NO_QR_FEATURES) return features;        //IMP: comment this if using relations!
//
//
//        int [] qTypes = getQuestionType(paragraph.questions.get(quesNum).question);
//
//        if(qTypes[0] == relation)
//            Tools.addFeature(features, "RQ_Match", 1.0);
//        else
//            Tools.addFeature(features, "RQ_NoMatch", 1.0);
//
//
//        //cache
//        paragraph.cacheFeaturesQR(quesNum, relation, features);
//
//        return features;
//    }
//
//
//    //uses the words in the question to get types corresponding to relations above
//    static int[] getQuestionType(Annotation question) {
//        SemanticGraph depGraph = question.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
//
//        //first element for Wh word; second for words in question
//        int[] qTypes = new int[]{NONE, NONE};
//
//        for(String word : Tools.getWordsInSent(question)) {
//            if (word.contains("why"))
//                qTypes[0] = CAUSAL;
//            else if (word.contains("when"))
//                qTypes[0] = TEMPORAL;
//            else if(word.contains("how"))
//                qTypes[0] = HOW;
////            else if (word.contains("where"))
////                qTypes[0] = LOCATION;
////            else if (word.contains("who"))
////                qTypes[0] = PERSON;
//            //TODO: OBJECT and ACTION
//
//            if(qTypes[0] != NONE) break; //probably for questions like what, etc.
//        }
//
//        //now count the occurences of diff types of phrases and take the max one
//        //TODO: check if good?
//        if(qTypes[0] == NONE) {
//            String quesText = question.get(CoreAnnotations.TextAnnotation.class).toLowerCase();
//            int matchCnt1 = Tools.countOccurences(quesText, Global.causalPhrases);
//            int matchCnt2 = Tools.countOccurences(quesText, Global.temporalPhrases);
//
//            if(matchCnt1 > matchCnt2 && matchCnt1 > 0)
//                qTypes[0] = CAUSAL;
//            else if(matchCnt2 > matchCnt1 && matchCnt2 > 0)
//                qTypes[0] = TEMPORAL;
//        }
//
//        return qTypes;
//    }
//
//
//
//
//    //TODO: may not be correct
//    static HashMap<Integer, Double> relDistZ(Paragraph paragraph, int[] sentNums) {
//        HashMap<Integer, Double> relDist = new HashMap<>();
//        for (int rel = 0; rel < NUM_RELATIONS; rel++)
//            relDist.put(rel, Math.exp(Tools.featureWeightProduct(getRelationFeatures(paragraph, sentNums, rel))));
//        relDist = (HashMap<Integer, Double>) Tools.sortByValue(Tools.normalizeHashMapInt(relDist));
//
//        return relDist;
//    }
//
//    static HashMap<Integer, Double> relDistQ(Paragraph paragraph, int quesNum) {
//        HashMap<Integer, Double> relDist = new HashMap<>();
//        for (int rel = 0; rel < NUM_RELATIONS; rel++)
//            relDist.put(rel, Math.exp(Tools.featureWeightProduct(getFeaturesQR(paragraph, quesNum, rel))));
//        relDist = (HashMap<Integer, Double>) Tools.sortByValue(Tools.normalizeHashMapInt(relDist));
//
//        return relDist;
//    }
//
//    static HashMap<Integer, Double> relDistQZ(Paragraph paragraph, int quesNum, int [] sentNums) {
//        HashMap<Integer, Double> relDist = new HashMap<>();
//        for (int rel = 0; rel < NUM_RELATIONS; rel++)
//            relDist.put(rel, (Tools.featureWeightProduct(getFeaturesQR(paragraph, quesNum, rel))
//                    + Tools.featureWeightProduct(getRelationFeatures(paragraph, sentNums, rel))
//                    + Tools.featureWeightProduct(SentRel.getFeaturesQZSingle(paragraph, sentNums[0], quesNum))));
////        relDist = (HashMap<Integer, Double>) Tools.sortByValue(Tools.normalizeHashMapInt(relDist));
//        relDist = (HashMap<Integer, Double>) Tools.sortByValue(relDist);
//
//        return relDist;
//    }
//
//}
