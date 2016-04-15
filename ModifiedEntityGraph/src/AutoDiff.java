//import com.google.common.collect.Multiset;
//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.util.CoreMap;
//import nilgiri.math.DoubleReal;
//import nilgiri.math.DoubleRealFactory;
//import nilgiri.math.autodiff.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.ArrayList;
//
///**
// * Created by ghostof2007 on 1/30/15.
// */
//
//public class AutoDiff {
//
//    static final DoubleRealFactory RNFactory = DoubleRealFactory.instance();
//    static final DifferentialRealFunctionFactory<DoubleReal> DFFactory = new DifferentialRealFunctionFactory<DoubleReal>(RNFactory);
//
//
//    static ArrayList<Variable<DoubleReal>> weights = null;
//
//    static void autodiff(Paragraph paragraph) {
//        //variables are the weights
//
//        ArrayList<Variable<DoubleReal>> weights = new ArrayList<Variable<DoubleReal>>();
//        int cnt = 0;
//        for(double w : SentRel.weights) {
//            weights.add(DFFactory.var("w_" + (cnt++), new DoubleReal(w)));
//        }
//
//        //calculate  just the likelihood function
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//        int N = sentences.size();
//        for (Question Q : paragraph.questions) {
//            ArrayList<DifferentialFunction<DoubleReal>> numeratorParts = new ArrayList<DifferentialFunction<DoubleReal>>();
//            ArrayList<DifferentialFunction<DoubleReal>> denominatorParts = new ArrayList<DifferentialFunction<DoubleReal>>();
//            for(int sentNum=0; sentNum < sentences.size(); sentNum++) {
//                ArrayList<DifferentialFunction<DoubleReal>> numeratorParts2 = new ArrayList<DifferentialFunction<DoubleReal>>();
//                ArrayList<DifferentialFunction<DoubleReal>> denominatorParts2 = new ArrayList<DifferentialFunction<DoubleReal>>();
//
//                //numerator
//                HashMap<Integer, Double> features = SentRel.getFeaturesQZSingle(paragraph, sentNum, Q.number);
//
//                DifferentialFunction<DoubleReal> featureWeightProd1 = DFFactory.val(new DoubleReal(0.));
//                for (int featureNum : features.keySet()) {
//                    featureWeightProd1 = featureWeightProd1.plus(DFFactory.val(new DoubleReal(features.get(featureNum))).mul(weights.get(featureNum)));
//                }
//
//                for(int sentNum2 : SentRel.getNeighborSentences(sentNum, N)) {
//
//                    //answer features
//                    HashMap<Integer, Double> featuresCorrectAns = SentRel.getFeaturesAQZSingle(paragraph, sentNum2, Q.number, Q.correct);
//                    DifferentialFunction<DoubleReal> featureWeightProd2 = DFFactory.val(new DoubleReal(0.));
//                    for (int featureNum : featuresCorrectAns.keySet()) {
//                        featureWeightProd2 = featureWeightProd2.plus(DFFactory.val(new DoubleReal(featuresCorrectAns.get(featureNum))).mul(weights.get(featureNum)));
//                    }
//
//                    //sentence linking features (z1, z2)
//                    HashMap<Integer, Double> featuresSentLinking = MultiSentence.getFeaturesMulti(paragraph, new int[] {sentNum, sentNum2}, Q.number); //TODO: check this once
//                    DifferentialFunction<DoubleReal> featureWeightProd3 = DFFactory.val(new DoubleReal(0.));
//                    for (int featureNum : featuresSentLinking.keySet()) {
//                        featureWeightProd3 = featureWeightProd3.plus(DFFactory.val(new DoubleReal(featuresSentLinking.get(featureNum))).mul(weights.get(featureNum)));
//                    }
//
//                    //all choices
//                    ArrayList<DifferentialFunction<DoubleReal>> choiceParts = new ArrayList<DifferentialFunction<DoubleReal>>();
//                    for (int choiceNum = 0; choiceNum < 4; choiceNum++) {
//                        HashMap<Integer, Double> featuresChoice = SentRel.getFeaturesAQZSingle(paragraph, sentNum2, Q.number, choiceNum);
//                        DifferentialFunction<DoubleReal> featureWeightProdChoices = DFFactory.val(new DoubleReal(0.));
//                        for (int featureNum : featuresChoice.keySet()) {
//                            featureWeightProdChoices = featureWeightProdChoices.plus(DFFactory.val(new DoubleReal(featuresChoice.get(featureNum))).mul(weights.get(featureNum)));
//                        }
//                        choiceParts.add(featureWeightProdChoices);
//                    }
//
//                    numeratorParts2.add(AutoDiff.DFFactory.val(new DoubleReal(0)).plus(featureWeightProd2).plus(featureWeightProd3)
//                            .minus(Tools.logSumOfExponentialsAD(choiceParts)));
//
//                    //inside denominator (over z2)
//                    denominatorParts2.add(featureWeightProd3);
//                }
//
//                numeratorParts.add(featureWeightProd1.plus(Tools.logSumOfExponentialsAD(numeratorParts2))
//                        .minus(Tools.logSumOfExponentialsAD(denominatorParts2)));
//
//                //denominator (over z1)
//                denominatorParts.add(featureWeightProd1);
//            }
//
//            assert denominatorParts.size() == N;
//            assert numeratorParts.size() == N;
//
//            DifferentialFunction<DoubleReal> logDenominator = Tools.logSumOfExponentialsAD(denominatorParts);
//            DifferentialFunction<DoubleReal> logNumerator = Tools.logSumOfExponentialsAD(numeratorParts);
//
//            DifferentialFunction<DoubleReal> logFunc = logNumerator.minus(logDenominator); //IMP : logNumerator might be changed!
//
//            //update the function value
//            Opt.functionValue += logFunc.getValue().doubleValue();
//
//            //gradient
//            HashMap<Integer, Double> gradients = new HashMap<Integer, Double>();
//            for(int featureNum = 0; featureNum < weights.size(); featureNum++)
//                gradients.put(featureNum, logFunc.diff(weights.get(featureNum)).getValue().doubleValue());
//            Tools.updateMap(Opt.featureGradients, gradients); //type 1 and type 2
//
//        }
//    }
//
//
//    //with Relations
////    static void autodiffR(Paragraph paragraph) {
////        //variables are the weights
////
////        ArrayList<Variable<DoubleReal>> weights = new ArrayList<Variable<DoubleReal>>();
////        int cnt = 0;
////        for(double w : SentRel.weights) {
////            weights.add(DFFactory.var("w_" + (cnt++), new DoubleReal(w)));
////        }
////
////        //calculate  just the likelihood function
////        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
////        int N = sentences.size();
////
////        //for all (Q, A*) pairs
////        for (Question Q : paragraph.questions) {
////
////            ArrayList<DifferentialFunction<DoubleReal>> numeratorParts = new ArrayList<DifferentialFunction<DoubleReal>>();
////            ArrayList<DifferentialFunction<DoubleReal>> denominatorParts = new ArrayList<DifferentialFunction<DoubleReal>>();
////            for(int sentNum=0; sentNum < sentences.size(); sentNum++) {
////                ArrayList<DifferentialFunction<DoubleReal>> numeratorParts2 = new ArrayList<DifferentialFunction<DoubleReal>>();
////                ArrayList<DifferentialFunction<DoubleReal>> denominatorParts2 = new ArrayList<DifferentialFunction<DoubleReal>>();
////
////                //numerator
////                HashMap<Integer, Double> features = SentRel.getFeaturesQZSingle(paragraph, sentNum, Q.number);
////
////                DifferentialFunction<DoubleReal> featureWeightProd1 = DFFactory.val(new DoubleReal(0.));
////                for (int featureNum : features.keySet()) {
////                    featureWeightProd1 = featureWeightProd1.plus(DFFactory.val(new DoubleReal(features.get(featureNum))).mul(weights.get(featureNum)));
////                }
////
////                for(int rel =0; rel < Relation.NUM_RELATIONS; rel++)
////                for(int sentNum2 : SentRel.getNeighborSentences(sentNum, N)) {
////
////
////                    //answer features
//////                    HashMap<Integer, Double> featuresCorrectAns = SentRel.getFeaturesAQZSingle(paragraph, sentNum2, Q.number, Q.correct);
//////                    DifferentialFunction<DoubleReal> featureWeightProd2 = DFFactory.val(new DoubleReal(0.));
//////                    for (int featureNum : featuresCorrectAns.keySet()) {
//////                        featureWeightProd2 = featureWeightProd2.plus(DFFactory.val(new DoubleReal(featuresCorrectAns.get(featureNum))).mul(weights.get(featureNum)));
//////                    }
////
////                    //sentence linking features (z1, z2)
////                    HashMap<Integer, Double> featuresSentLinking = MultiSentence.getFeaturesMulti(paragraph, new int[] {sentNum, sentNum2}, Q.number); //TODO: check this once
////                    Tools.updateMap(featuresSentLinking, Relation.getRelationFeatures(paragraph, Q.number, new int[] {sentNum, sentNum2}, rel)); //TODO: add this into func above?
////                    DifferentialFunction<DoubleReal> featureWeightProd3 = DFFactory.val(new DoubleReal(0.));
////                    for (int featureNum : featuresSentLinking.keySet()) {
////                        featureWeightProd3 = featureWeightProd3.plus(DFFactory.val(new DoubleReal(featuresSentLinking.get(featureNum))).mul(weights.get(featureNum)));
////                    }
////
////
////                    //all choices
////                    ArrayList<DifferentialFunction<DoubleReal>> choiceParts = new ArrayList<DifferentialFunction<DoubleReal>>();
////                    for (int choiceNum = 0; choiceNum < 4; choiceNum++) {
////                        HashMap<Integer, Double> featuresChoice = SentRel.getFeaturesAQZSingle(paragraph, sentNum2, Q.number, choiceNum);
////                        DifferentialFunction<DoubleReal> featureWeightProdChoices = DFFactory.val(new DoubleReal(0.));
////                        for (int featureNum : featuresChoice.keySet()) {
////                            featureWeightProdChoices = featureWeightProdChoices.plus(DFFactory.val(new DoubleReal(featuresChoice.get(featureNum))).mul(weights.get(featureNum)));
////                        }
////                        choiceParts.add(featureWeightProdChoices);
////                    }
////
////                    numeratorParts2.add(AutoDiff.DFFactory.val(new DoubleReal(0))
////                            .plus(choiceParts.get(Q.correct))
////                            .plus(featureWeightProd3)
////                            .minus(Tools.logSumOfExponentialsAD(choiceParts)));
////
////                    //inside denominator (over z2)
////                    denominatorParts2.add(featureWeightProd3);
////                }
////
////                numeratorParts.add(featureWeightProd1.plus(Tools.logSumOfExponentialsAD(numeratorParts2))
////                        .minus(Tools.logSumOfExponentialsAD(denominatorParts2)));
////
////                //denominator (over z1)
////                denominatorParts.add(featureWeightProd1);
////            }
////
////            assert denominatorParts.size() == N;
////            assert numeratorParts.size() == N;
////
////            DifferentialFunction<DoubleReal> logDenominator = Tools.logSumOfExponentialsAD(denominatorParts);
////            DifferentialFunction<DoubleReal> logNumerator = Tools.logSumOfExponentialsAD(numeratorParts);
////
////            DifferentialFunction<DoubleReal> logFunc = logNumerator.minus(logDenominator); //IMP : logNumerator might be changed!
////
////            //update the function value
////            Opt.functionValue += logFunc.getValue().doubleValue();
////
////            //gradient
////            HashMap<Integer, Double> gradients = new HashMap<Integer, Double>();
////            for(int featureNum = 0; featureNum < weights.size(); featureNum++)
////                gradients.put(featureNum, logFunc.diff(weights.get(featureNum)).getValue().doubleValue());
////            Tools.updateMap(Opt.featureGradients, gradients); //type 1 and type 2
////
////        }
////    }
//
//
//    //with Relations, and two-way sentence matches
//    static void autodiffR2(Paragraph paragraph) {
//        //variables are the weights
//
//        if(weights == null) {
//            weights = new ArrayList<Variable<DoubleReal>>();
//            int cnt = 0;
//            for(double w : SentRel.weights) {
//                weights.add(DFFactory.var("w_" + (cnt++), new DoubleReal(w)));
//            }
//        }
//        else {
//            int cnt = 0;
//            for(double w : SentRel.weights) {
//                weights.get(cnt++).set(new DoubleReal(w));
//            }
//        }
//
//
//        if(paragraph.logNumerators ==null) {
//            paragraph.logNumerators = new HashMap<>();
//            paragraph.logDenominators = new HashMap<>();
//        }
//
//
//        //calculate  just the likelihood function
//        List<CoreMap> sentences = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class);
//        int N = sentences.size();
//
//        //for all (Q, A*) pairs
//        for (Question Q : paragraph.questions) {
//
//            if(Main.MIXED_SINGLE_MULTI && Q.type==1) continue; //IMP: train only on multi sentences
//
//            if(paragraph.logNumerators.size() < paragraph.questions.size()) {
//
//                ArrayList<DifferentialFunction<DoubleReal>> numeratorParts = new ArrayList<DifferentialFunction<DoubleReal>>();
//                ArrayList<DifferentialFunction<DoubleReal>> denominatorParts = new ArrayList<DifferentialFunction<DoubleReal>>();
//
//                //go over z1
//                for (int z1 = 0; z1 < sentences.size(); z1++) {
//
//
//                    //numerator
//                    HashMap<Integer, Double> features = SentRel.getFeaturesQZSingle(paragraph, z1, Q.number);
//
//                    DifferentialFunction<DoubleReal> featureWeightProdQZ = DFFactory.val(new DoubleReal(0.));
//                    for (int featureNum : features.keySet()) {
//                        featureWeightProdQZ = featureWeightProdQZ.plus(DFFactory.val(new DoubleReal(features.get(featureNum))).mul(weights.get(featureNum)));
//                    }
//
//
//                    ArrayList<DifferentialFunction<DoubleReal>> numeratorPartsQR = new ArrayList<DifferentialFunction<DoubleReal>>();
//                    ArrayList<DifferentialFunction<DoubleReal>> denominatorPartsQR = new ArrayList<DifferentialFunction<DoubleReal>>();
//
//                    //go over R
//                    for (int r = 0; r < Relation.NUM_RELATIONS; r++) {
//
//                        //P(R | Q) here
//                        //numerator
//                        HashMap<Integer, Double> featuresQR = Relation.getFeaturesQR(paragraph, Q.number, r);
//
//                        DifferentialFunction<DoubleReal> featureWeightProdQR = DFFactory.val(new DoubleReal(0.));
//                        for (int featureNum : featuresQR.keySet()) {
//                            featureWeightProdQR = featureWeightProdQR.plus(DFFactory.val(new DoubleReal(featuresQR.get(featureNum))).mul(weights.get(featureNum)));
//                        }
//
//                        ArrayList<DifferentialFunction<DoubleReal>> numeratorPartsZ2 = new ArrayList<DifferentialFunction<DoubleReal>>();
//                        ArrayList<DifferentialFunction<DoubleReal>> denominatorPartsZ2 = new ArrayList<DifferentialFunction<DoubleReal>>();
//
//                        //go over z2
//                        for (int z2 : SentRel.getNeighborSentences(z1, N)) {
//                            //sentence linking features (z1, z2) TODO: need to link question here somehow
//                            HashMap<Integer, Double> featuresSentLinking = MultiSentence.getFeaturesMulti(paragraph, new int[]{z1, z2}, Q.number);
//
//                            Tools.updateMap(featuresSentLinking, Relation.getRelationFeatures(paragraph, new int[]{z1, z2}, r)); //TODO: add this into func above?
//
//                            DifferentialFunction<DoubleReal> featureWeightProd3 = DFFactory.val(new DoubleReal(0.));
//                            for (int featureNum : featuresSentLinking.keySet()) {
//                                featureWeightProd3 = featureWeightProd3.plus(DFFactory.val(new DoubleReal(featuresSentLinking.get(featureNum))).mul(weights.get(featureNum)));
//                            }
//
//
//                            //all choices
//                            ArrayList<DifferentialFunction<DoubleReal>> choiceParts = new ArrayList<DifferentialFunction<DoubleReal>>();
//                            for (int choiceNum = 0; choiceNum < 4; choiceNum++) {
//                                //TODO: update this with better features to include Q - A relations
//                                HashMap<Integer, Double> featuresChoice = SentRel.getFeaturesAQZSingle(paragraph, z2, Q.number, choiceNum);
////                                Tools.updateMap(featuresChoice, SentRel.getFeaturesAQZSingle(paragraph, z1, Q.number, choiceNum)); //IMP: adding in z1 to answer
//                                //IMP: why does this not work? Giving infinty and Nan gradient!
//
//                                DifferentialFunction<DoubleReal> featureWeightProdChoices = DFFactory.val(new DoubleReal(0.));
//                                for (int featureNum : featuresChoice.keySet()) {
//                                    featureWeightProdChoices = featureWeightProdChoices.plus(DFFactory.val(new DoubleReal(featuresChoice.get(featureNum))).mul(weights.get(featureNum)));
//                                }
//                                choiceParts.add(featureWeightProdChoices);
//                            }
//
//                            numeratorPartsZ2.add(AutoDiff.DFFactory.val(new DoubleReal(0))
//                                    .plus(choiceParts.get(Q.correct))
//                                    .plus(featureWeightProd3)
//                                    .minus(Tools.logSumOfExponentialsAD(choiceParts)));
//
//                            //inside denominator (over z2)
//                            denominatorPartsZ2.add(featureWeightProd3);
//                        }
//
//                        //numerator over QR
//                        numeratorPartsQR.add(featureWeightProdQR.plus(Tools.logSumOfExponentialsAD(numeratorPartsZ2)
//                                .minus(Tools.logSumOfExponentialsAD(denominatorPartsZ2))));
//
//                        //denominator over QR
//                        denominatorPartsQR.add(featureWeightProdQR);
//
//                    }
//
//                    //numerator over z1
//                    numeratorParts.add(featureWeightProdQZ.plus(Tools.logSumOfExponentialsAD(numeratorPartsQR))
//                            .minus(Tools.logSumOfExponentialsAD(denominatorPartsQR)));
//
//                    //denominator (over z1)
//                    denominatorParts.add(featureWeightProdQZ);
//                }
//
//                assert denominatorParts.size() == N;
//                assert numeratorParts.size() == N;
//
//
//                paragraph.logNumerators.put(Q.number, Tools.logSumOfExponentialsAD(numeratorParts));
//                paragraph.logDenominators.put(Q.number, Tools.logSumOfExponentialsAD(denominatorParts));
//            }
//
//            DifferentialFunction<DoubleReal> logFunc = DFFactory.val(new DoubleReal(0)).plus(paragraph.logNumerators.get(Q.number))
//                    .minus(paragraph.logDenominators.get(Q.number));
//
//            //update the function value
//            Opt.functionValue += logFunc.getValue().doubleValue();
//
//            //gradient
//            HashMap<Integer, Double> gradients = new HashMap<Integer, Double>();
//            for(int featureNum = 0; featureNum < weights.size(); featureNum++)
//                gradients.put(featureNum, logFunc.diff(weights.get(featureNum)).getValue().doubleValue());
//            Tools.updateMap(Opt.featureGradients, gradients); //type 1 and type 2
//
//        }
//    }
//}