//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.util.CoreMap;
//
//import java.util.*;
//
///**
// * Created by ghostof2007 on 2/3/15.
// */
//public class Baseline {
//
//
//    static Map<String, Integer> getCountMap(Paragraph paragraph) {
//        HashMap<String, Integer> countMap = new HashMap<String, Integer>();
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//
//        for(CoreMap sentence : sentences) {
//            List<CoreLabel> words = sentence.get(CoreAnnotations.TokensAnnotation.class);
//            for(CoreLabel wordLabel : words) {
//                String word = Tools.getText(wordLabel);
//                if (countMap.containsKey(word))
//                    countMap.put(word, countMap.get(word) + 1);
//                else
//                    countMap.put(word, 1);
//            }
//        }
//
//        return countMap;
//    }
//
//    static double IC(Map<String, Integer> countMap, String word) {
//        return Math.log(1 + 1./countMap.get(word));
//    }
//
//
//    static double ITF(String word) {
//        Integer cnt = SentRel.word2Cnt.get(word);
//        if(cnt==null)
//            return 0;
//        return Math.log(1 + 1./cnt);
//    }
//
//
//    static double slidingWindow(Paragraph paragraph, int quesNum, int ansNum) {
//        Map<String, Integer> countMap = getCountMap(paragraph);
//
//        Set<String> qWords = Tools.getWordSet(paragraph.questions.get(quesNum).question);
//
//
//        Set<String> aWords = Tools.getWordSet(paragraph.questions.get(quesNum).choices.get(ansNum));
//        aWords.addAll(qWords);
//
//        int slideSize = aWords.size();
//        double maxValue = - Double.MAX_VALUE;
//        ArrayList<String> wordSeq = Tools.getWordSeq(paragraph);
//
//        for(int i=0;i<wordSeq.size(); i++) {
//            double sum = 0;
//            for(int j=0;j<Math.min(slideSize, wordSeq.size()-i);j++) {
//                String w = wordSeq.get(i+j);
//                if(aWords.contains(w))
////                    sum += IC(countMap, w) + ITF(w);
//                    sum += IC(countMap, w);
//            }
//            if(sum > maxValue)
//                maxValue = sum;
//        }
//
//        return maxValue;
//    }
//
//    static double distanceBased(Paragraph paragraph, int quesNum, int ansNum) {
//        Set<String> pWords = Tools.getWordSet(paragraph);
//        Set<String> qWords = Tools.getWordSet(paragraph.questions.get(quesNum).question);
//        Set<String> aWords = Tools.getWordSet(paragraph.questions.get(quesNum).choices.get(ansNum));
//
//        qWords.retainAll(pWords);
//        qWords.removeAll(SentRel.stopWords);
//
//        aWords.retainAll(pWords);
//        aWords.removeAll(SentRel.stopWords);
//
//        if(qWords.size()==0 || aWords.size()==0)
//            return 1.;
//
//        //else compute the distance based metric
//        ArrayList<String> wordSeq = Tools.getWordSeq(paragraph);
//        double avg = 0;
//        HashMap<String, Double> minD = new HashMap<>();
//
//        for(int i=0;i<wordSeq.size(); i++)
//            if(qWords.contains(wordSeq.get(i))) {
//                String qWord = wordSeq.get(i);
//                for(int j = 0; j < wordSeq.size() ; j++) {
//                    if((i-j >=0 && aWords.contains(wordSeq.get(i-j)))
//                            || (i+j < wordSeq.size() && aWords.contains(wordSeq.get(i + j)))) {
//                        if(!minD.containsKey(qWord) || j < minD.get(qWord))
//                            minD.put(qWord, (double)j);
//                        break;
//                    }
//                }
//            }
//
//        assert minD.size() == qWords.size();
//
//        for(double val : minD.values())
//            avg += val;
//
//        avg /= minD.size();
//
//
//        return avg / (wordSeq.size()-1);
//    }
//
//    static HashMap<Integer, Double> swdFeatures(Paragraph paragraph, int sentNum, int quesNum, int ansNum) {
//
//        HashMap<Integer, Double> features = new HashMap<>();
//
//        double sw = slidingWindow(paragraph, quesNum, ansNum);
//        double D = distanceBased(paragraph, quesNum, ansNum);
//
//        Tools.addFeatureIncrement(features, "SW", sw);
////        Tools.addFeatureIncrement(features, "Dist", D);
////        Tools.addFeatureIncrement(features, "SW+D", sw-D);
//
//        return features;
//    }
//
//}
