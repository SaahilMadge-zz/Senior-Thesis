//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.util.CoreMap;
//import lbfgsb.Bound;
//import lbfgsb.LBFGSBException;
//import lbfgsb.Minimizer;
//import lbfgsb.Result;
//import org.apache.commons.lang3.tuple.Pair;
//
//import javax.swing.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by ghostof2007 on 10/17/14.
// */
//public class Opt {
//
//
//    static double functionValue; //update every EStep - to be used by optimizer
//    static HashMap<Integer, Double> featureGradients = new HashMap<Integer, Double>();
//
//    static void calculateProbsSoft(Paragraph paragraph) {
//
//
//        //calculate  just the likelihood function first (no gradients)
//        for (Question Q : paragraph.questions) {
//
//            //IMP: Uncomment to train only on single sentence questions
//            if(Main.MIXED_SINGLE_MULTI && Q.type!=1) continue;
//
//            double logDenominator = 0.;
//            int N = paragraph.annotation.get(CoreAnnotations.SentencesAnnotation.class).size();
//
//            double []  denominatorParts = new double[N];
//            HashMap<Integer, Double> denominatorFeatures = new HashMap<Integer, Double>();
//
//            //denominator case
//            for (int sentNum = 0; sentNum < N; sentNum++) {
//                HashMap<Integer, Double> features = SentRel.getFeaturesQZSingle(paragraph, sentNum, Q.number);
//                denominatorParts[sentNum] = Tools.featureWeightProduct(features);
//
//                //gradients (all are type 1 features here)
//                Tools.updateMap(denominatorFeatures, features, Math.exp(denominatorParts[sentNum]));
//
//            }
//
//
//            logDenominator = Tools.logSumOfExponentials(denominatorParts);
//            assert !Double.isNaN(logDenominator);
//            Tools.updateMap(featureGradients, denominatorFeatures, -1./Math.exp(logDenominator));
//
//            //numerator part
//            double [] numeratorParts = new double[N];
//            HashMap<Integer, Double> numeratorFeatures = new HashMap<Integer, Double>();
//
//            for (int sentNum = 0; sentNum < N; sentNum++) {
//                double partScore = 0.;
//                HashMap<Integer, Double> numeratorFeatures2 = new HashMap<Integer, Double>();
//
//                HashMap<Integer, Double> features1 = SentRel.getFeaturesQZSingle(paragraph, sentNum, Q.number);
//                HashMap<Integer, Double> features2 = SentRel.getFeaturesAQZSingle(paragraph, sentNum, Q.number, Q.correct); //IMP :using correct answer
//
//                partScore = Tools.featureWeightProduct(features1) + Tools.featureWeightProduct(features2);
//
//                double[] partScores = new double[Q.choices.size()];
//                for (int ansNum = 0; ansNum < Q.choices.size(); ansNum++) {
//
//                    HashMap<Integer, Double> features = SentRel.getFeaturesAQZSingle(paragraph, sentNum, Q.number, ansNum);
//                    partScores[ansNum] = Tools.featureWeightProduct(features);
//
//                    //Type 2 gradient
//                    Tools.updateMap(numeratorFeatures2, features, -Math.exp(partScore + partScores[ansNum])); //second part in num
//                }
//
//                double logAnsSum = Tools.logSumOfExponentials(partScores);
//                assert !Double.isNaN(logAnsSum);
//                numeratorParts[sentNum] = partScore - logAnsSum; //this is log B
//
//                //Type 1 gradient
//                Tools.updateMap(numeratorFeatures, features1, Math.exp(numeratorParts[sentNum]));  //keep accumulating type1 gradients over Z
//
//                //Type 2 gradient
//                Tools.updateMap(numeratorFeatures2, features2, Math.exp(logAnsSum + partScore)); //first part in num
//
//                Tools.updateMap(numeratorFeatures, numeratorFeatures2, 1. / Math.exp(2 * logAnsSum)); //1/(anssum)^2
//            }
//
//
//            double logNumerator = Tools.logSumOfExponentials(numeratorParts);
//
//            assert !Double.isNaN(logNumerator);
//
//            //update the function value
//            functionValue += (logNumerator - logDenominator);
//
//            //gradient
//            Tools.updateMap(featureGradients, numeratorFeatures, 1./Math.exp(logNumerator)); //type 1 and type 2
//
//        }
//    }
//
//    static void MStep() throws LBFGSBException {
//        //clear all static variables
//        functionValue = 0;
//        featureGradients.clear();
//
//        System.err.println("Starting EM_ON...");
//
//        Minimizer alg = new Minimizer();
//        alg.getStopConditions().setMaxIterations(200);
//        alg.setDebugLevel(1);
//
//        ArrayList<Bound> bounds = new ArrayList<Bound>(); //bounds for opt
//
//        double [] weights = new double[SentRel.weights.size()];
//        for(int i=0;i<weights.length;i++) {
//            weights[i] = SentRel.weights.get(i);
//            bounds.add(new Bound((double) 0, null)); //only setting lower bound to be 0
//        }
////        alg.setBounds(bounds); //IMP: use this to force positive weights
//
//        Result ret = alg.run(new Function(), weights);
//        double finalValue = ret.functionValue;
//        double [] finalGradient = ret.gradient;
//        System.out.println("EM finalValue : "+Double.toString(finalValue));
//        System.out.println("EM finalGradientSum: "+Double.toString(Tools.squareSum(finalGradient)));
//
//
//        //Store the final weights (for debugging)
//        for(String feature : SentRel.feature2Index.keySet())
//            SentRel.feature2Weight.put(feature, ret.point[SentRel.feature2Index.get(feature)]);
//        //sort the weights
//        SentRel.feature2Weight = Tools.sortByValue(SentRel.feature2Weight);
//    }
//
//}
