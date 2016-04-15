//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.ling.IndexedWord;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.util.CoreMap;
//import edu.stanford.nlp.util.IntPair;
//import nilgiri.math.DoubleReal;
//import nilgiri.math.autodiff.Constant;
//import nilgiri.math.autodiff.DifferentialFunction;
//import org.apache.commons.lang3.tuple.MutablePair;
//import org.apache.commons.lang3.tuple.Pair;
//
//import java.util.*;
//import java.util.List;
//
///**
// * Created by ghostof2007 on 5/6/14.
// *
// * Tools
// */
//public class Tools {
//
//    public static double max(double[] values) {
//        double max = - Double.MAX_VALUE;
//        for(double value : values) {
//            if(value > max)
//                max = value;
//        }
//        return max;
//    }
//
//    public static double maxAD(ArrayList<DifferentialFunction<DoubleReal>> values) {
//        double max = - Double.MAX_VALUE;
//        for(DifferentialFunction<DoubleReal> value : values) {
//            if(value.getValue().doubleValue() > max)
//                max = value.getValue().doubleValue();
//        }
//        return max;
//    }
//
//
//    public static int maxIndex(double[] values) {
//        double max = - Double.MAX_VALUE;
//        int maxIndex = 0;
//        for(int i=0;i < values.length; i++) {
//            if(values[i] > max) {
//                max = values[i];
//                maxIndex = i;
//            }
//        }
//        return maxIndex;
//    }
//
//
//    public static double logSumOfExponentials(double [] xs) {
//        if (xs.length == 1) return xs[0];
//        double max = max(xs);
//        double sum = 0.0;
//        for (double x : xs)
//            if (x != Double.NEGATIVE_INFINITY)
//                sum += Math.exp(x - max);
//        return max + java.lang.Math.log(sum);
//    }
//
//    public static double logSumOfExponentials(ArrayList<Double> x) {
//        double [] xs = new double[x.size()];
//        for(int i=0;i<x.size(); i++)
//            xs[i] = x.get(i);
//        return logSumOfExponentials(xs);
//    }
//
//    public static double logSumOfExponentials(Map<?, Double> x) {
//        double [] xs = new double[x.size()];
//        int i=0;
//        for(double val : x.values())
//            xs[i++] = val;
//        return logSumOfExponentials(xs);
//    }
//
//    public static DifferentialFunction<DoubleReal> logSumOfExponentialsAD(ArrayList<DifferentialFunction<DoubleReal>> xs) {
//        if (xs.size() == 1) return xs.get(0);
//        double max = maxAD(xs);
//        DifferentialFunction<DoubleReal> sum = AutoDiff.DFFactory.val(new DoubleReal(0.));
//        Constant<DoubleReal> maxC = AutoDiff.DFFactory.val(new DoubleReal(max));
//        for (DifferentialFunction<DoubleReal> x : xs)
//            if (x.getValue().doubleValue() != Double.NEGATIVE_INFINITY)
//                sum = sum.plus(AutoDiff.DFFactory.exp(x.minus(maxC)));
//        return maxC.plus(AutoDiff.DFFactory.log(sum));
//    }
//
//    static double dot(String a, String b) {
//        double sum = 0.;
//        ArrayList<Double> vec1 = SentRel.wordVec.get(a);
//        ArrayList<Double> vec2 = SentRel.wordVec.get(b);
//        if(vec1==null || vec2==null) return -0.5; //FIXME - make sure not using just plain DOT
//        for(int i=0; i < vec1.size(); i++)
//            sum += vec1.get(i) * vec2.get(i);
//        return sum;
//    }
//
//    //    dot product of feature and weights(global)
//    static double featureWeightProduct(HashMap<Integer, Double> features) {
//        double sum = 0.;
//        if(features==null || features.size()==0) return 0.;
//        for(int i : features.keySet())
//            if( i < SentRel.weights.size())  //check if weight exists for the feature
//                sum += features.get(i) * SentRel.weights.get(i);
//        return sum;
//    }
//
//    //add values from one map(b) to another(a) (weighted by factor)
//    static void updateMap(HashMap<Integer, Double> a, HashMap<Integer, Double> b, double factor) {
//        for(int key : b.keySet()) {
//            if (a.containsKey(key))
//                a.put(key, a.get(key) + b.get(key) * factor);
//            else
//                a.put(key, b.get(key) * factor);
//        }
//    }
//
//    static void updateMap(HashMap<Integer, Double> a, HashMap<Integer, Double> b) {
//        updateMap(a, b, 1.);
//    }
//
//    static double sum(double [] array){
//        double sum = 0.;
//        for (double anArray : array) sum += anArray;
//        return sum;
//    }
//
//    static double squareSum(double [] array){
//        double sum = 0.;
//        for (double anArray : array) sum += anArray * anArray;
//        return sum;
//    }
//
//    static int getFeatureType(String feature) {
//        if(feature.charAt(0) == 'Q')
//            return 1;
//        else
//            return 2;
//    }
//
//    static int getFeatureType(int featureNum) {
//        String feature = SentRel.index2Feature.get(featureNum);
//        if(feature.charAt(0) == 'Q')
//            return 1;
//        else
//            return 2;
//    }
//
//    static int getFeatureIndex(String feature) {
//        //create new features
//        if(!SentRel.feature2Index.containsKey(feature)) {
//            if(SentRel.TEST) return -1; //if in testing phase, and feature does not exist already, do not create new
//
//            int index =  SentRel.feature2Index.size();
//            SentRel.feature2Index.put(feature, index);
//            SentRel.index2Feature.add(feature);
//            SentRel.weights.add(0.); //init weights with 1
//
//            //update the list of type specific features
//            if(getFeatureType(feature) == 1)
//                SentRel.type1Features.add(index);
//            else
//                SentRel.type2Features.add(index);
//
//
//            return  index;
//        }
//        return SentRel.feature2Index.get(feature);
//    }
//
//    //descending sort
//    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
//    {
//        List<Map.Entry<K, V>> list =
//                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
//        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
//        {
//            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
//            {
//                return -(o1.getValue()).compareTo( o2.getValue() ); //change sign to make ascending
//            }
//        } );
//
//        Map<K, V> result = new LinkedHashMap<K, V>();
//        for (Map.Entry<K, V> entry : list)
//        {
//            result.put( entry.getKey(), entry.getValue() );
//        }
//        return result;
//    }
//
//    //sort map
//    public static
//    <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
//        List<T> list = new ArrayList<T>(c);
//        java.util.Collections.sort(list);
//        return list;
//    }
//
//    static void addFeature(HashMap<Integer, Double> features, String newFeature, double value) {
//        //dont do anything if value is 0
//        if(value==0) return;
//        int featureIndex = getFeatureIndex(newFeature);
//        if(featureIndex!=-1)
//            features.put(featureIndex,value);
//    }
//
//    static void addFeatureIncrement(HashMap<Integer, Double> features, String newFeature, double value) {
//        //dont do anything if value is 0
//        if(value==0) return;
//        int featureIndex = getFeatureIndex(newFeature);
//
//        if(featureIndex!=-1) {
//            Double oldValue = features.get(featureIndex);
//            if (oldValue!= null && oldValue > 0) value += oldValue;
//            features.put(featureIndex, value);
//        }
//    }
//
//    static void addFeatureSmart(HashMap<Integer, Double> features, String newFeature, double value) {
//        //dont do anything if value is 0
//        if(value==0) {
//            newFeature = "###_" + newFeature + "_0";
//            value = 1.;
//        }
//        int featureIndex = getFeatureIndex(newFeature);
//
//        if(featureIndex!=-1) {
//            Double oldValue = features.get(featureIndex);
//            if (oldValue!= null && oldValue > 0) value += oldValue;
//            features.put(featureIndex, value);
//        }
//    }
//
//    static void incrementMap(Map<Integer, Integer> map, int key) {
//        Integer value = map.get(key);
//        if(value==null)
//            map.put(key,1);
//        else
//            map.put(key, value+1);
//    }
//
//
//    static void incrementMap(Map<String, Double> map, String key) {
//        Double value = map.get(key);
//        if(value==null)
//            map.put(key,1.);
//        else
//            map.put(key, value+1);
//    }
//
//
//    public static double[] convertHashMap(HashMap<?, Double> map) {
//        double [] values = new double[map.size()];
//        int i =0;
//        for(double val : map.values() )  {
//            values[i] = val;
//            i++;
//        }
//        return values;
//    }
//
//    public static double[] convertHashMapInt(HashMap<Integer, Double> map) {
//        double [] values = new double[map.size()];
//        int i =0;
//        for(double val : map.values() )  {
//            values[i] = val;
//            i++;
//        }
//        return values;
//    }
//
//    public static  <T extends Comparable<? super T>> HashMap<T, Double> normalizeHashMap(HashMap<T, Double> map) {
//        HashMap<T, Double> newMap = new HashMap<T, Double>();
//        double [] values = convertHashMap(map);
//        double Z = sum(values);
//
//        for(T key : map.keySet()) {
//            assert !Double.isInfinite(map.get(key)) && !Double.isNaN(map.get(key));
//            newMap.put(key, map.get(key)/Z);
//        }
//        return newMap;
//    }
//
//    public static HashMap<Integer, Double> normalizeHashMapInt(HashMap<Integer, Double> map) {
//        HashMap<Integer, Double> newMap = new HashMap<Integer, Double>();
//        double [] values = convertHashMapInt(map);
//        double Z = sum(values);
//
//        for(Integer key : map.keySet()) {
//            assert !Double.isInfinite(map.get(key)) && !Double.isNaN(map.get(key));
//            newMap.put(key, map.get(key)/Z);
//        }
//        return newMap;
//    }
//
//    static Map<String, Double> getFeatureNames(Paragraph paragraph, int sentNum, int quesNum, int ansNum, boolean useAnswer) {
//        HashMap<String, Double> features = new HashMap<String, Double>();
//        for(Map.Entry<Integer, Double> entry : SentRel.getFeatures(paragraph, sentNum, quesNum, ansNum).entrySet())
//            features.put(SentRel.index2Feature.get(entry.getKey())+"#"+entry.getValue(), SentRel.weights.get(entry.getKey()));
//
//        return Tools.sortByValue(features);
//    }
//    static Map<String, Double> getFeatureNames( Paragraph paragraph, int sentNum, int quesNum, int ansNum) {
//        return getFeatureNames(paragraph, sentNum, quesNum, ansNum, true);
//    }
//
//    static Map<String, Double> getFeatureNamesMulti(Paragraph paragraph, int [] sentNums, int quesNum, int ansNum, boolean useAnswer) {
//        HashMap<String, Double> features = new HashMap<String, Double>();
//        for(Map.Entry<Integer, Double> entry : SentRel.getFeaturesUniversal(paragraph, sentNums, quesNum, ansNum, useAnswer).entrySet())
////        for(Map.Entry<Integer, Double> entry : Clause.getFeaturesAQZ(paragraph, sentNums, quesNum, ansNum).entrySet())
//            features.put(SentRel.index2Feature.get(entry.getKey())+"#"+entry.getValue(), SentRel.weights.get(entry.getKey()));
//
//        return Tools.sortByValue(features);
//    }
//
//    static void setFeatureWeight(String feature, double weight) {
//        SentRel.weights.set(SentRel.feature2Index.get(feature), weight);
//    }
//
//    static HashSet<String> clone(HashSet<String> map) {
//        HashSet<String> newMap = new HashSet<String>();
//        for(String key : map)
//            newMap.add(key);
//        return newMap;
//    }
//
//
//    static HashMap<String, Double> getFeatureNamesSentences(int paraNum, int quesNum, int ansNum, int sentNum) {
//        Paragraph paragraph = SentRel.trainParagraphs.get(paraNum);
//        HashMap<String, Double> features = new HashMap<String, Double>();
//        for(Map.Entry<Integer, Double> entry : SentRel.getFeatures( paragraph, sentNum, quesNum, ansNum).entrySet())
//            features.put(SentRel.index2Feature.get(entry.getKey()), SentRel.weights.get(entry.getKey()));
//
//        return features;
//    }
//
//    //score the answer giving partial credit where applicable
//    public static double scoreAnswer(double[] ansScores, int correct) {
//        HashMap<Integer, Double> scores = new HashMap<Integer, Double>();
//        for(int i=0; i<ansScores.length;i++)
//            scores.put(i, ansScores[i]);
//        Map<Integer, Double> sortedScores = sortByValue(scores);
//
//        double bestScore = max(ansScores);
//        double i=0;
//        boolean flag = false;
//        for(Map.Entry<Integer, Double >indexAndScore : sortedScores.entrySet()) {
//            if(indexAndScore.getValue() < bestScore) break;
//            if(indexAndScore.getKey() == correct)
//                flag = true;
//
//            i++;
//        }
//        if(flag) return 1./i;
//        return 0;
//    }
//
//    public static void printBestSentencePairs(Paragraph paragraph) {
//        int numPairsToPrint = 10;
//        HashMap<IntPair, Double> pair2Score = new HashMap<IntPair, Double>();
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//        for(int i=0;i<sentences.size(); i++)
//            for(int j = i+1;j<sentences.size(); j++)
//                pair2Score.put(new IntPair(i, j), Tools.featureWeightProduct(MultiSentence.getSentFeatures(paragraph, i, j)));
//
//
//
//        System.out.println("############################################################");
//        for(Map.Entry<IntPair, Double> entry : sortByValue(pair2Score).entrySet()) {
//            if(numPairsToPrint-- == 0) break;
//            System.out.println("**************   " + entry.getValue());
//            System.out.println(sentences.get(entry.getKey().getSource()).toString());
//            System.out.println(sentences.get(entry.getKey().getTarget()).toString());
//        }
//
//    }
//
//    public static void printClosestSent(Paragraph paragraph, int sentNum) {
//        int numPairsToPrint = 1;
//        HashMap<Integer, Double> pair2Score = new HashMap<Integer, Double>();
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//        for(int i=0;i<sentences.size(); i++)
//            if(i != sentNum)
//                pair2Score.put(i, Tools.featureWeightProduct(MultiSentence.getSentFeatures(paragraph, sentNum, i)));
//
//
//
//        System.out.println("############################################################");
//        for(Map.Entry<Integer, Double> entry : sortByValue(pair2Score).entrySet()) {
//            if(numPairsToPrint-- == 0) break;
//            System.out.println("**************   " + entry.getValue());
//            System.out.println(sentences.get(sentNum).toString());
//            System.out.println(sentences.get(entry.getKey()).toString());
//            System.out.println(Tools.featureWeightProduct(MultiSentence.getSentFeatures(paragraph, sentNum, entry.getKey())));
//        }
//
//    }
//
//    public static int getClosestSent(Paragraph paragraph, int sentNum) {
//        HashMap<Integer, Double> pair2Score = new HashMap<Integer, Double>();
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//        for(int i=Math.max(0, sentNum - SentRel.maxSentRange+1);i< Math.min(sentences.size(), sentNum + SentRel.maxSentRange); i++)
//            if(i != sentNum)
//                pair2Score.put(i, Tools.featureWeightProduct(MultiSentence.getSentFeatures(paragraph, sentNum, i)));
//
//        int bestIndex = 0;
//        for(int index : sortByValue(pair2Score).keySet()) {
//            bestIndex = index;
//            break;
//        }
//
//        return bestIndex;
//    }
//
//    public static String getLemma(IndexedWord word) {
//        return word.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase();  //hacky
//    }
//
//    public static String getText(CoreLabel word) {
//        return word.get(CoreAnnotations.TextAnnotation.class).toLowerCase();
//    }
//
//
//    //Matching functions
//    static boolean lemmaMatch(IndexedWord w1, IndexedWord w2) {
//        return w1.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase().equals(w2.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase());
//    }
//
//    public static Set<String> getWordSet(Annotation sentence) {
//        HashSet<String> wordSet = new HashSet<>();
//
//        for(CoreLabel label : sentence.get(CoreAnnotations.TokensAnnotation.class))
//            wordSet.add(getText(label));
//
//        return wordSet;
//    }
//
//    public static ArrayList<String> getWordSeq(Paragraph paragraph) {
//        ArrayList<String> wordSeq = new ArrayList<>();
//        for(CoreMap sentence : paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class))
//            for(CoreLabel label : sentence.get(CoreAnnotations.TokensAnnotation.class))
//                wordSeq.add(getText(label));
//
//        return wordSeq;
//    }
//
//    public static Set<String> getWordSet(Paragraph paragraph) {
//        Set<String> wordSeq = new HashSet<>();
//        for(CoreMap sentence : paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class))
//            for(CoreLabel label : sentence.get(CoreAnnotations.TokensAnnotation.class))
//                wordSeq.add(getText(label));
//
//        return wordSeq;
//    }
//
//    static Set<String> getWordsInSent(Annotation sent) {
//        HashSet<String> words = new HashSet<String>();
//        for(CoreLabel word : sent.get(CoreAnnotations.TokensAnnotation.class))
//            words.add(word.get(CoreAnnotations.TextAnnotation.class).toLowerCase());
//
//        return words;
//    }
//
//    static ArrayList<String> getWordSeq(CoreMap sent) {
//        ArrayList<String> words = new ArrayList<>();
//        for(CoreLabel word : sent.get(CoreAnnotations.TokensAnnotation.class))
//            words.add(word.get(CoreAnnotations.TextAnnotation.class).toLowerCase());
//
//        return words;
//    }
//
//    public static int countOccurences(CoreMap sent, String word) {
//        int cnt = 0;
//        for(CoreLabel token : sent.get(CoreAnnotations.TokensAnnotation.class))
//            if(token.get(CoreAnnotations.TextAnnotation.class).toLowerCase().equals(word))
//                cnt++;
//        return cnt;
//    }
//
//    public static int countOccurences(String text, Set<String> phrases) {
//        int cnt = 0;
//        text = " "+text+" ";
//        for (String phrase : phrases)
//            if (text.contains(" "+phrase+" ") || text.contains(" "+phrase+".") || text.contains(" "+phrase+","))
//                cnt++;
//        return cnt;
//    }
//
//    public static int countOccurences(String text, String word) {
//        int cnt = 0;
//        text = " "+text+" ";
//        if (text.contains(" "+word+" ") || text.contains(" "+word+".") || text.contains(" "+word+","))
//            cnt++;
//        return cnt;
//    }
//
//
//    //mostly for matching
//    public static String binFeature(String base, int value) {
//        if(value > 5) value = 3;
//        return base + value;
//    }
//
//    //zero feature if 0 else, just the cnt itself
//    public static Pair<String, Double> smartFeature(String base, int value) {
//        if(value == 0)
//            return new MutablePair<>(base+"_0", 1.);
//        return new MutablePair<>(base, (double)value);
//
//    }
//
//
////    public static double entropy(ArrayList<Sample.MultinomialObject> multinomial) {
////        double entropy = 0;
////        for(Sample.MultinomialObject obj : multinomial) {
////            entropy -= obj.score * Math.log(obj.score);
////        }
////        return entropy;
////    }
////
////    public static double maxValue(ArrayList<Sample.MultinomialObject> multinomial) {
////        double maxVal = -Double.MAX_VALUE;
////        for(Sample.MultinomialObject obj : multinomial) {
////            if(obj.score > maxVal)
////                maxVal = obj.score;
////        }
////        return maxVal;
////    }
//}
