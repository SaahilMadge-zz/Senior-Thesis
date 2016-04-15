//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
//import edu.stanford.nlp.util.CoreMap;
//import edu.stanford.nlp.util.IntPair;
//import org.apache.commons.lang3.tuple.MutablePair;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.*;
//
///**
// * Created by ghostof2007 on 10/17/14.
// */
//public class Evaluate {
//
//    static int topSentences = 1;
//    static int topSentencesForRanking = 10;
//
//    static boolean MULTI_SENTENCE_EVAL;  //set in params.properties
//    static boolean BASIC_SCORING= false;
//    static boolean SCORE_PAIRS_DIRECT = false;
//
//    static HashMap<Integer, Integer> qType2IncorrectCnt = new HashMap<Integer, Integer>();
//
//
//    static ArrayList<ArrayList<Annotation>> readAnnotatedSentences(String inFile) throws IOException {
//        BufferedReader br = new BufferedReader(new FileReader(inFile));
//        ArrayList<ArrayList<Annotation>> paragraph2SentenceAnnotated = new ArrayList<ArrayList<Annotation>>();
//        try {
//            String line = br.readLine();
//            int i=0;
//            ArrayList<Annotation> sentences = new ArrayList<Annotation>();
//            while (line != null) {
//
//                if(line.trim().length() == 0) {
//                    assert sentences.size() > 0;
//                    paragraph2SentenceAnnotated.add(sentences);
//                    sentences = new ArrayList<Annotation>();
//                    line = br.readLine();
//                    continue;
//                }
//
//                //process
//                Annotation tmpAnno = new Annotation(line);
//                SentRel.pipeline.annotate(tmpAnno);
//                sentences.add(tmpAnno);
//
//                //read next line
//                line = br.readLine();
//                i++;
//            }
//
//            if(sentences.size()>0)
//                paragraph2SentenceAnnotated.add(sentences);
//        } finally {
//            br.close();
//        }
//        System.err.println("Read in answers from " + inFile);
//        return paragraph2SentenceAnnotated;
//    }
//
//
//    //rank sentences given the correct answer (sanity check)
//    static List<Integer> rankSentences(Paragraph paragraph) {
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//        ArrayList<Integer> rankedSentences = new ArrayList<Integer>();
//        for(Question Q : paragraph.questions) {
//            System.out.println("Question: "+Q.question);
//            Annotation correctAns = Q.choices.get(Q.correct);
//            System.out.println("Correct Answer: "+correctAns);
//            System.out.println("Choices: "+Q.choices);
//            HashMap<Integer, Double> sent2Scores = new HashMap<Integer, Double>();
//            for(int i=0;i<sentences.size(); i++)
//                sent2Scores.put(i, Tools.featureWeightProduct(SentRel.getFeatures(paragraph, i, Q.number, Q.correct))); //use correct answer
//            Map<Integer, Double> sortedScores = Tools.sortByValue(sent2Scores);
//            int i=0;
//            for(Map.Entry<Integer, Double> sent : sortedScores.entrySet()) {
//                System.out.println(sentences.get(sent.getKey())+" "+sent.getValue());
//                System.out.println(Tools.getFeatureNames(paragraph, sent.getKey(), Q.number,  Q.correct, false));
//                rankedSentences.add(sent.getKey());
//                i++;
//                if(i>=topSentencesForRanking) break;
//            }
//            System.out.println("************************************");
//        }
//
//        return rankedSentences;
//    }
//
//    //rank sentence pairs given the correct answer (sanity check)
//    static List<IntPair> rankSentencePairs(Paragraph paragraph) {
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//        ArrayList<IntPair> rankedSentencePairs = new ArrayList<IntPair>();
//        for(Question Q : paragraph.questions) {
//            System.out.println("Question: "+Q.question);
//            Annotation correctAns = Q.choices.get(Q.correct);
//            System.out.println("Correct Answer: "+correctAns);
//            System.out.println("Choices: "+Q.choices);
//            HashMap<IntPair, Double> sent2Scores = new HashMap<IntPair, Double>();
//            for(int i=0;i<sentences.size(); i++)
//                for(int j=i+1; j< sentences.size();j++)
//                    sent2Scores.put(new IntPair(i, j), Tools.featureWeightProduct(SentRel.getFeaturesUniversal(paragraph, new int[] {i, j}, Q.number, Q.correct, false))); //use correct answer
//            Map<IntPair, Double> sortedScores = Tools.sortByValue(sent2Scores);
//            int i=0;
//            for(Map.Entry<IntPair, Double> sent : sortedScores.entrySet()) {
//                System.out.println("##############\n"+sentences.get(sent.getKey().getSource())+"\n"+sentences.get(sent.getKey().getTarget())+" "+sent.getValue());
//
//                System.out.println(Tools.getFeatureNamesMulti(paragraph, new int [] {sent.getKey().getSource(), sent.getKey().getTarget()}, Q.number,  Q.correct, false));
//                rankedSentencePairs.add(sent.getKey());
//                i++;
//                if(i>=topSentencesForRanking) break;
//            }
//            System.out.println("************************************");
//        }
//
//        return rankedSentencePairs;
//    }
//
//    static void rankSentencesNaive(Paragraph paragraph) {
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//        for(Question Q : paragraph.questions) {
//            System.out.println("Question: "+Q.question);
//            Annotation correctAns = Q.choices.get(Q.correct);
//            System.out.println("Correct Answer: "+correctAns);
//            System.out.println("Choices: "+Q.choices);
//            HashMap<Integer, Double> sent2Scores = new HashMap<Integer, Double>();
//            for(int i=0;i<sentences.size(); i++) {
//                //count matches
//                double cnt = 0;
//                String sentence = String.valueOf(sentences.get(i));
//                for(String w : String.valueOf(Q.question).split(" "))
//                    if(sentence.contains(w))
//                        cnt++;
////                for(String w : String.valueOf(correctAns).split(" "))
////                    if(sentence.contains(w))
////                        cnt++;
//                sent2Scores.put(i, cnt);
//            }
//            Map<Integer, Double> sortedScores = Tools.sortByValue(sent2Scores);
//            int i=0;
//            for(Map.Entry<Integer, Double> sent : sortedScores.entrySet()) {
//                System.out.println(sentences.get(sent.getKey())+" "+sent.getValue());
//                i++;
//                if(i>=topSentences) break;
//            }
//            System.out.println("************************************");
//        }
//    }
//
//    //for all paragraphs
//    static void rankSentencesAcrossParagraphs(ArrayList<Paragraph> paragraphs) {
//        for(Paragraph paragraph: paragraphs) {
//            rankSentences(paragraph);
//            rankAnswers(paragraph);
//            System.out.println("\n");
//        }
//    }
//
//    //rank answers by first finding best sentences given the correct answer and then choosing the best scoring choice (sanity check)
//    static void rankAnswers(Paragraph paragraph) {
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//        for(Question Q : paragraph.questions) {
//            System.out.println("Question: "+Q.question);
//            Annotation correctAns = Q.choices.get(Q.correct);
//            System.out.println("Correct Answer: "+correctAns);
//            HashMap<Integer, Double> sent2Scores = new HashMap<Integer, Double>();
//            for(int i=0;i<sentences.size(); i++)
//                sent2Scores.put(i, Tools.featureWeightProduct(SentRel.getFeatures(paragraph, i, Q.number, Q.correct)));
//            Map<Integer, Double> sortedScores = Tools.sortByValue(sent2Scores);
//
//            double [] ansScores = new double[4];
//            for(int j=0; j< 4;j++) {
//                int i = 0;
//                for (Map.Entry<Integer, Double> sent : sortedScores.entrySet()) {
//                    ansScores[j] += Tools.featureWeightProduct(SentRel.getFeatures(paragraph, sent.getKey(), Q.number, j));
//
//                    i++;
//                    if (i >= topSentences) break;
//                }
//                System.out.println(Q.choices.get(j) + " : " +ansScores[j]);
//            }
//            System.out.println("************************************");
//        }
//    }
//
//    //Rank answers based on keyword match or something as simple
//    static void rankAnswersNaive(Paragraph paragraph) {
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//        for(Question Q : paragraph.questions) {
//            System.out.println("Question: "+Q.question);
//            Annotation correctAns = Q.choices.get(Q.correct);
//            System.out.println("Correct Answer: "+correctAns);
//            HashMap<Integer, Double> sent2Scores = new HashMap<Integer, Double>();
//            for(int i=0;i<sentences.size(); i++) {
//                //count matches
//                double cnt = 0;
//                String sentence = String.valueOf(sentences.get(i));
//                for(String w : String.valueOf(Q.question).split(" "))
//                    if(sentence.contains(w))
//                        cnt++;
//                for(String w : String.valueOf(correctAns).split(" "))
//                    if(sentence.contains(w))
//                        cnt++;
//                sent2Scores.put(i, cnt);
//
//            }
////                sent2Scores.put(i, Tools.featureWeightProduct(SentRel.getSentFeatures(Q.question, correctAns, sentences.get(i))));
//            Map<Integer, Double> sortedScores = Tools.sortByValue(sent2Scores);
//
//            double [] ansScores = new double[4];
//            for(int j=0; j< 4;j++) {
//                int i = 0;
//                for (Map.Entry<Integer, Double> sent : sortedScores.entrySet()) {
//                    ansScores[j] += Tools.featureWeightProduct(SentRel.getFeatures( paragraph, sent.getKey(), Q.number, j));
//
//                    i++;
//                    if (i >= topSentences) break;
//                }
//                System.out.println(Q.choices.get(j) + " : " +ansScores[j]);
//            }
//            System.out.println("************************************");
//        }
//    }
//
//    //Sanity check for scoring A given sentence
//    static double scoreAnswerBasic(Paragraph paragraph, int [] sentNums, int quesNum, int ansNum) {
//        double score = 0;
//
//        //just count matches TODO
//        Question Q = paragraph.questions.get(quesNum);
//        CoreMap sentQ = Q.question.get(CoreAnnotations.SentencesAnnotation.class).get(0);
//        CoreMap sentA = Q.choices.get(ansNum).get(CoreAnnotations.SentencesAnnotation.class).get(0);
//
//        HashSet<String> wordsQ = new HashSet<String>();
//        HashSet<String> wordsA = new HashSet<String>();
//        HashSet<String> bigramsA = new HashSet<String>();
//        HashSet<String> bigramsQ = new HashSet<String>();
//        HashSet<String> bigramsZ = new HashSet<String>();
//
//
//        String prevZ = null, prevQ = null, prevA = null; //previous words, for bigrams
//        for(CoreLabel tokenQ: sentQ.get(CoreAnnotations.TokensAnnotation.class)) {
//            String word = tokenQ.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase();
//            wordsQ.add(word);
//            if(prevQ!=null)
//                bigramsQ.add(prevQ+" "+word);
//            prevQ = word;
//        }
//        for(CoreLabel tokenA: sentA.get(CoreAnnotations.TokensAnnotation.class)) {
//            String word = tokenA.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase();
//            String pos = tokenA.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//
//            wordsA.add(word);
//            if(prevA!=null)
//                bigramsA.add(prevA+" "+word);
//            prevA = word;
//        }
//
//
//        for(int sentNum : sentNums) {
//            CoreMap sentZ = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class).get(sentNum);
//            HashSet<String> wordsZ = new HashSet<String>();
//            prevZ = null;
//            for(CoreLabel tokenZ: sentZ.get(CoreAnnotations.TokensAnnotation.class)) {
//                String word = tokenZ.get(CoreAnnotations.TextAnnotation.class).toLowerCase();
//                wordsZ.add(word);
//
//                if(prevZ!=null)
//                    bigramsZ.add(prevZ+" "+word);
//                prevZ = word;
//            }
//
//            //TODO : filter by POS tag
//            //only between sentence and answer
//            wordsZ.retainAll(wordsA);
//            score += wordsZ.size();
//
//            bigramsZ.retainAll(bigramsA);
//            score += bigramsZ.size();
//        }
//
//        return score;
//    }
//
//
//    //Update the ansScores with a double value using using the ranked sentences provided.
////    static IntPair getAnsScoresPair(Map<IntPair, Double> sentPair2Scores, Paragraph paragraph, int quesNum, int ansNum, double [] ansScores) {
////
////        Map<IntPair, Double> sortedScores = Tools.sortByValue(sentPair2Scores);
////
////        //score the answer choice
////        int cnt = 0;
////        int a=-1, b=-1;
////        for (Map.Entry<IntPair, Double> sent : sortedScores.entrySet()) {
////            ansScores[ansNum] += Tools.featureWeightProduct(SentRel.getFeaturesUniversal(paragraph, new int[]{sent.getKey().getSource(), sent.getKey().getTarget()}, quesNum, ansNum));
////            cnt++;
////
//////            ///dummy vars
////            a = sent.getKey().getSource();
////            b = sent.getKey().getTarget();
////
////            if (cnt >= topSentences) break;
////        }
////        return new IntPair(a, b);
////    }
//
//
//
//
//
//    static MutablePair<Double, Double> evaluateAnswers(Paragraph paragraph) {
//        return evaluateAnswers(paragraph, -1);
//    }
//
////    //IMP : Evaluate answers by choosing best out of the 4 choices according to model (use for final eval)
////    //IMP : uses multisentence features function
////    static MutablePair<Integer, Integer> evaluateAnswersMulti(Paragraph paragraph) {
////        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
////        int correctOne = 0, correctMulti = 0;
////        for(Question Q : paragraph.questions) {
////            Annotation correctAns = Q.choices.get(Q.correct);
////
////
////            double [] ansScores = new double[4];
////            for(int j=0; j< 4;j++) {
////                int cnt = 0;
////                HashMap<Integer, Double> sent2Scores = new HashMap<Integer, Double>();
////                for(int i=0;i<sentences.size(); i++)
////                    sent2Scores.put(i, Tools.featureWeightProduct(SentRel.getFeatures( paragraph, i, Q.number,j, true))); //true/false to toggle useAnswer
////                Map<Integer, Double> sortedScores = Tools.sortByValue(sent2Scores);
////
////                //score the answer choice using only the top k sentences (scored according to the choice itself)
////                ArrayList<CoreMap> sentenceList = new ArrayList<CoreMap>();
////                for (Map.Entry<Integer, Double> sent : sortedScores.entrySet()) {
////                    sentenceList.add(sentences.get(sent.getKey()));
////                    cnt++;
////                    if (cnt >= topSentences) break;
////                }
////                ansScores[j] += Tools.featureWeightProduct(SentRel.getFeaturesMulti(Q.question, Q.choices.get(j), sentenceList));
////
////            }
////            int bestScoreIndex = Tools.maxIndex(ansScores);
////            if(bestScoreIndex == Q.correct)
////                if(Q.type == 1)
////                    correctOne++;
////                else
////                    correctMulti++;
////            else if(true || Main.DEBUG)
////            {
////                System.err.println("Question: "+Q.question);
////                System.err.println("Correct Answer: "+correctAns);
////                for(int j=0;j<4;j++)
////                    System.err.println(Q.choices.get(j) + " : " +ansScores[j]);
////                System.err.println("****************************");
////            }
////        }
////        return new MutablePair<Integer, Integer>(correctOne, correctMulti);
////    }
//
//    static double evaluate(ArrayList<Paragraph> paragraphs) {
//        SentRel.TEST = true;  //testing mode
//        double correct = 0, correctOne=0, correctMulti = 0, total = 0;
//        double totalOne = 0, totalMulti = 0;
//
//        int paraNum = 0;
//
//        for(Paragraph paragraph : paragraphs) {
//            paragraph.initCaches();
//
//
//            MutablePair<Double, Double> ans;
//            if(!Main.AnnotatedEval) {
//                //IMP : change here to switch between using single and multiple sentences in features
//                 ans = evaluateAnswers(paragraph);
////                ans = evaluateAnswersMulti(paragraph);
//            }
//            else {
//                //IMP : evaluate with annotations
//                ans = evaluateAnswers(paragraph, paraNum);
//            }
//            correctOne += ans.getLeft();
//            correctMulti += ans.getRight();
//            correct += (ans.getLeft()+ans.getRight());
//
//            System.err.println("CorrectONe, multi, total: "+correctOne+" "+correctMulti+" "+correct);
//
//            for(Question Q: paragraph.questions) {
//                if(Q.type == 1)
//                    totalOne++;
//                else
//                    totalMulti++;
//            }
//
////            System.out.println("\n");
//            total += 4;  //4 questions for each paragraph
//
//
//            paraNum++;
//            //TODO: take care of this
//            if(Main.AnnotatedEval) {
//                if (paraNum > 24) break; //IMP : for annotated eval only
//            }
//        }
//
//        double accuracy = (correct*100)/total;
//        double singleAccuracy = (correctOne*100)/totalOne;
//        System.err.println("Correct: " + correct + "/" + total);
//        System.err.println("Accuracy: "+accuracy);
//        System.err.println("Single accuracy: "+singleAccuracy);
//        System.err.println("Multi accuracy: "+(correctMulti*100)/totalMulti);
//        System.err.println("");
//        SentRel.TEST = false; //testing mode off
//        return accuracy;
//    }
//
//
//    /**
//     *  Update the ansScores with a double value using using the ranked sentences provided.
//     */
//    static IntPair getAnsScores(Map<Integer, Double> sent2Scores, Paragraph paragraph, int quesNum, int ansNum, double [] ansScores) {
//        //TODO: calc for all answers at the same time to speedup?
//
//        //score the answer choice by marginalizing over sentences
//        int cnt = 0;
//        int a=-1, b=-1;
//        double [] s = new double[sent2Scores.size()]; // to get the scores sentence-wise
//        double bestSentenceScore = - Double.MAX_VALUE;
//        int N = sent2Scores.size();
//
//        ArrayList<Double> numQZ = new ArrayList<Double>();
//        ArrayList<Double> denQZ = new ArrayList<Double>();
//
//        double sentLogZ = Tools.logSumOfExponentials(sent2Scores);
//
//        Set<String> qNegWords = SentRel.getNegWords(paragraph.questions.get(quesNum).question
//                .get(CoreAnnotations.SentencesAnnotation.class).get(0)
//                .get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class));
//        int multiplier = 1;
//
//        if(!MULTI_SENTENCE_EVAL || (Main.MIXED_SINGLE_MULTI && paragraph.questions.get(quesNum).type==1)) {
//            for (Map.Entry<Integer, Double> sent : sent2Scores.entrySet()) {
//                //TODO : multi sentence eval
//
//                double[] choiceScore = new double[4];
//                for (int choiceNum = 0; choiceNum < 4; choiceNum++)
//                    choiceScore[choiceNum] = Tools.featureWeightProduct(SentRel.getFeaturesAQZSingle(paragraph, sent.getKey(), quesNum, choiceNum));
//
//                //handle negations
////                Set<String> zNegWords = SentRel.getNegWords(paragraph.sentDepGraphs.get(sent.getKey()));
////                if((!qNegWords.isEmpty() && zNegWords.isEmpty()))
////                    multiplier = 1;
////                else
////                    multiplier = 1;
//
//                s[sent.getKey()] = multiplier * (sent.getValue()
//                        + Tools.featureWeightProduct(SentRel.getFeaturesAQZSingle(paragraph, sent.getKey(), quesNum, ansNum))
//                        - Tools.logSumOfExponentials(choiceScore));
//
//                cnt++;
//                if (s[sent.getKey()] > bestSentenceScore) {
//                    a = sent.getKey();
//                    bestSentenceScore = s[a];
//                }
//            }
//
//            ansScores[ansNum] = Tools.logSumOfExponentials(s);
////            if(qNegWords.size()>0)
////                ansScores[ansNum] *= -1;
//        }
//        else { //multi sentence eval
//            for (int z1 = 0; z1 < N; z1++) {
//                ArrayList<Double> numQR = new ArrayList<Double>();
//                ArrayList<Double> denQR = new ArrayList<Double>();
//
//                for(int r = 0 ;r < Relation.NUM_RELATIONS; r++) {
//
//                    ArrayList<Double> numZ2 = new ArrayList<Double>();
//                    ArrayList<Double> denZ2 = new ArrayList<Double>();
//
//                    for (int z2 : SentRel.getNeighborSentences(z1, N)) {
//
//                        ArrayList<Double> valuesChoice = new ArrayList<Double>();
//
//                        for (int choiceNum = 0; choiceNum < 4; choiceNum++) {
//                            HashMap<Integer, Double> choiceFeatures = SentRel.getFeaturesAQZSingle(paragraph, z2, quesNum, choiceNum);
////                            Tools.updateMap(choiceFeatures, SentRel.getFeaturesAQZSingle(paragraph, z1, quesNum, choiceNum)); //IMP : using z1 also
//                            valuesChoice.add(Tools.featureWeightProduct(choiceFeatures));
//                        }
//
//                        double valZ2 = Tools.featureWeightProduct(MultiSentence.getFeaturesMulti(paragraph, new int[]{z1,z2}, quesNum))
//                                + Tools.featureWeightProduct(Relation.getRelationFeatures(paragraph, new int[]{z1,z2}, r));
//
//                        numZ2.add(valZ2 + valuesChoice.get(ansNum) - Tools.logSumOfExponentials(valuesChoice));
//                        denZ2.add(valZ2);
//                    }
//
//                    double valQR = Tools.featureWeightProduct(Relation.getFeaturesQR(paragraph, quesNum, r));
//
//                    numQR.add(valQR + Tools.logSumOfExponentials(numZ2) - Tools.logSumOfExponentials(denZ2));
//                    denQR.add(valQR);
//                }
//
//                double valQZ = Tools.featureWeightProduct(SentRel.getFeaturesQZSingle(paragraph, z1, quesNum));
//                numQZ.add(valQZ  + Tools.logSumOfExponentials(numQR) - Tools.logSumOfExponentials(denQR));
//                denQZ.add(valQZ);
//
//
//
//            }
//            ansScores[ansNum] = Math.exp(Tools.logSumOfExponentials(numQZ) - Tools.logSumOfExponentials(denQZ));
//
//        }
//
////        ansScores[ansNum] = bestSentenceScore;
//
//        return new IntPair(a, b);
//    }
//
//
//    //IMP : Evaluate answers by choosing best out of the 4 choices according to model (use for final eval)
//    static MutablePair<Double, Double> evaluateAnswers(Paragraph paragraph, int paraNum) {
//        List<CoreMap> sentences = null;
//        if(!Main.AnnotatedEval) {
//            sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//        }
//
//        double correctOne = 0, correctMulti = 0;
//        int qNum = 0;
//
//
//        for(Question Q : paragraph.questions) {
//
//            Annotation correctAns = Q.choices.get(Q.correct);
//
//            //TODO : need to update this to work with feature function taking in sentence indices.
//            if(Main.AnnotatedEval)
//                sentences = Main.paragraph2SentencesAnnotated.get(paraNum).get(qNum).get(CoreAnnotations.SentencesAnnotation.class);
//            qNum++;
//            assert sentences != null;
//            // -------------------------------------------------------------------------------------
//
//
//            double [] ansScores = new double[4];
//            int []bestSentences = new int[4];
//            int []bestSentenceNeighbors = new int[4];
//
//            HashMap<Integer, Double> sent2Scores = new HashMap<Integer, Double>();
//            HashMap<IntPair, Double> sentPairs2Scores = new HashMap<IntPair, Double>(); //TODO: use this later for multi-sentence case
//
//            //single sentence at a time
//            if(!SCORE_PAIRS_DIRECT) {
//                for (int i = 0; i < sentences.size(); i++)
////                    if(topSentenceIndices.contains(i)) //IMP: comment if considering all sentences
//                    sent2Scores.put(i, Tools.featureWeightProduct(SentRel.getFeaturesQZSingle(paragraph, i, Q.number))); //true/false to toggle useAnswer
//            }
//
//            for(int key : sent2Scores.keySet())
//                Q.sent2ExpScores.put(Integer.toString(key) + sentences.get(key).get(CoreAnnotations.TextAnnotation.class), Math.exp(sent2Scores.get(key)));
//            Q.sent2ExpScores = (HashMap<String, Double>) Tools.sortByValue(Tools.normalizeHashMap(Q.sent2ExpScores));
//
//
//            for(int j=0; j< 4;j++) {
//                //IMP: getAnsScores will automatically update the ansScores array with scores for each answer choice, according to the model
//                IntPair retValue = getAnsScores(sent2Scores, paragraph, Q.number, j, ansScores);
//                bestSentences[j] = retValue.getSource();
//                bestSentenceNeighbors[j] = retValue.getTarget();
//
//                HashMap<String, Double> tmpSentScores = new HashMap<String, Double>();
//                for (int i = 0; i < sentences.size(); i++)
//                    tmpSentScores.put(Integer.toString(i) + sentences.get(i).get(CoreAnnotations.TextAnnotation.class),
//                            Tools.featureWeightProduct(SentRel.getFeaturesAQZSingle(paragraph, i, Q.number, j)));
//                Q.ans2sent2ExpScores.add((HashMap<String, Double>) Tools.sortByValue(Tools.normalizeHashMap(tmpSentScores)));
//            }
//
//
//
//
//            double score = Tools.scoreAnswer(ansScores, Q.correct);
//
////            int bestScoreIndex = Tools.maxIndex(ansScores);
////            if(bestScoreIndex == Q.correct)
//            if(Q.type == 1)
//                correctOne += score;
//            else
//                correctMulti += score;
//
//            if(score < 1 - 1e-6 && Main.DEBUG)
//            {
//                System.err.println("Question: "+Q.question);
////                System.err.println(Q.depGraph);
//                System.err.println("Correct Answer: "+correctAns);
//                for(int j=0;j<4;j++) {
//                    System.err.println(Q.choices.get(j) + " : " + ansScores[j]);
//                    if(!MULTI_SENTENCE_EVAL) {
//                        System.err.println(sentences.get(bestSentences[j]).toString());
//                        System.err.println(Tools.getFeatureNames(paragraph, bestSentences[j], Q.number, j));
//                        System.err.println("");
////                        System.err.println(paragraph.sentDepGraphs.get(bestSentences[j]));
//                    }
//                    else {
////                        System.err.println(sentences.get(bestSentences[j]).toString());
////                        System.err.println(sentences.get(bestSentenceNeighbors[j]).toString());
////                        System.err.println(Tools.getFeatureNamesMulti(paragraph, new int[]{bestSentences[j], bestSentenceNeighbors[j]}, Q.number, j, true));
//                    }
//                }
//                System.err.println("****************************");
//
//                //qtype cnt
//                int[] qType = Relation.getQuestionType(Q.question);
//                Tools.incrementMap(qType2IncorrectCnt, qType[0]);
//            }
//
//            //relDistQ
//            //hacky: using 100 as the first num in pair to refer to questions
//            paragraph.relDist.put(new IntPair(100, Q.number), Relation.relDistQ(paragraph, Q.number));
//
////            //get the relation distribution relDistZ
////            int N = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class).size();
////            for(int z1=0;z1 < N; z1++)
////                for (int z2 : SentRel.getNeighborSentences(z1, N)) {
////                    paragraph.relDist.put(new IntPair(z1, z2), Relation.relDistZ(paragraph, new int[]{z1, z2}));
////                }
//        }
//
//
//
//
//
//        return new MutablePair<Double, Double>(correctOne, correctMulti);
//    }
//}
