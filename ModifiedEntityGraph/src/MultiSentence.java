//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.util.IntPair;
//
//import javax.xml.soap.Text;
//import java.util.*;
//
///**
// * Created by ghostof2007 on 11/25/14.
// * Class containing multi sentence handling functions
// */
//public class MultiSentence {
//
//
//    static HashMap<Integer, Double> getSentFeatures(Paragraph paragraph, int sentNum1, int sentNum2, String quesType) {
//        //strictly for sentences in paragraph
//        //TODO: extend to more than 2? or maybe stick to pairwise
//        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
//
//        //Entity matching and related stuff between Z1 and Z2
////        Tools.updateMap(features, Clause.getClauseFeatures(paragraph, sentNum1, sentNum2));
//
//        //Temporal features
//        //distance between them in para
//        //TODO: direction is important?
//        Tools.addFeatureSmart(features, "SentDist", Math.abs(sentNum1 - sentNum2));
//
//
////        Set<String> words1 = getWordsInSent(paragraph, sentNum1);
////        Set<String> words2 = getWordsInSent(paragraph, sentNum2);
//
////        if (quesType.equals("WHEN")) {
////            words1.retainAll(Global.temporalPhrases);
////            words2.retainAll(Global.temporalPhrases);
////            if (words1.size() + words2.size() > 0)
////                Tools.addFeature(features, "QuestionType", 1.);
////
////        } else if (quesType.equals("WHAT")) {
////        } else if (quesType.equals("WHY")) {
////            words1.retainAll(Global.causalPhrases);
////            words2.retainAll(Global.causalPhrases);
////            if (words1.size() + words2.size() > 0)
////                Tools.addFeature(features, "QuestionType", 1.);
////        } else if (quesType.equals("WHO")) {
////        } else if (quesType.equals("WHERE")) {
////        } else {
////        }
//
//
//        return features;
//    }
//
//    static HashMap<Integer, Double> getSentFeatures(Paragraph paragraph, int sentNum1, int sentNum2) {
//
//        return getSentFeatures(paragraph, sentNum1, sentNum2, "");
//    }
//
//    static String getQuestion(Paragraph paragraph, int sentNum) {
//        String question = "";
//        List<CoreLabel> words = paragraph.questions.get(sentNum).question.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(CoreAnnotations.TokensAnnotation.class);
//        for (CoreLabel word : words) {
//            if (word.get(CoreAnnotations.PartOfSpeechAnnotation.class).charAt(0) == 'W') {
//                question = word.get(CoreAnnotations.TextAnnotation.class).toUpperCase();
//                break;
//            }
//        }
//        return question;
//    }
//
//    //get features for multiple sentences specifically
//    //TODO: if using quesNum, change the cache
//    static HashMap<Integer, Double> getFeaturesMulti(Paragraph paragraph, int[] sentNums, int quesNum) {
//
//        if (!SentRel.INIT && paragraph.checkCacheMultiSent(sentNums[0], sentNums[1]))
//            return paragraph.multiSentFeatures.get(new IntPair(sentNums[0], sentNums[1]));
//
//        HashMap<Integer, Double> features = new HashMap<Integer, Double>();
//
////        if(Main.MIXED_SINGLE_MULTI && sentNums[0] == sentNums[1]) return features; //IMP: comment this if using multi even for same sentence pairs!
//
//        String question = getQuestion(paragraph, quesNum);
//        Tools.updateMap(features, getSentFeatures(paragraph, sentNums[0], sentNums[1], question));
//
//        Question Q = paragraph.questions.get(quesNum);
//
//        //IMP : sentNums[0] already has this in featuresQZSingle
////        Tools.updateMap(features, SentRel.getQuestionTypeFeatures(Q.question, paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class).get(sentNums[1])));
//
//
//        //RST features
////        if(Main.RST_ON) {
////            if (paragraph.sent2RSTNodePointers.size() > 0 && sentNums[0] != sentNums[1]) {
////                for (Tree x : paragraph.sent2RSTNodePointers.get(sentNums[0]))
////                    for (Tree y : paragraph.sent2RSTNodePointers.get(sentNums[1])) {
////                        Tree z = RST.getCommonAncestor(x, y);
////                        String s = z.text.split("\\[")[0];
////                        if (!s.contains("Elaboration"))
////                            Tools.addFeatureIncrement(features, question + "_" + s, 1.);
////                    }
////            }
////        }
//
//
//        paragraph.cacheFeaturesMultiSent(sentNums[0], sentNums[1], features);
//
//        return features;
//    }
//
//}
