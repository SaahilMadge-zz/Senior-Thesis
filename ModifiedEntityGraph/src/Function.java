//import lbfgsb.DifferentiableFunction;
//import lbfgsb.FunctionValues;
//
//import java.util.HashMap;
//
///**
// * Created by ghostof2007 on 5/6/14.
// *
// * Function to be used for LBFGS
// */
//public class Function implements DifferentiableFunction {
//
//    // -------------------------------------- LBFGS-B ---------------------------------------------------------
//
//
//    @Override
//    public FunctionValues getValues(double[] point) {
//
//        assert (point.length == SentRel.weights.size());
//        //copy the weights
//        for(int i=0;i<point.length;i++)
//            SentRel.weights.set(i, point[i]);
//
//        //clear all static variables
//        Opt.functionValue = 0;
//        Opt.featureGradients.clear();
//
//        int i = 0;
//        //calculate values for EM_ON
//        for(Paragraph paragraph : SentRel.trainParagraphs) {
//            if(Main.MIXED_SINGLE_MULTI)  {
//                Opt.calculateProbsSoft(paragraph);
//                AutoDiff.autodiffR2(paragraph);
//
//            }
//            else {
//                if (!SentRel.MULTI_SENTENCE)
//                    Opt.calculateProbsSoft(paragraph);
//                else
//                    AutoDiff.autodiffR2(paragraph);
//            }
//            System.err.print("\r" + (i++) + "/" + SentRel.trainParagraphs.size());
//        }
//        System.err.println();
//
//
//        //Store the final weights (for debugging)
//        for(String feature : SentRel.feature2Index.keySet())
//            SentRel.feature2Weight.put(feature, point[SentRel.feature2Index.get(feature)]);
//        //sort the weights
//        SentRel.feature2Weight = Tools.sortByValue(SentRel.feature2Weight);
//
//        System.err.println("**********");
//
//        return new FunctionValues(functionValue(point), gradient(point));
//    }
//
//    double functionValue(double[] iterWeights) {
//
//        double sum = Opt.functionValue;
//        //regularization
//        for(double weight : iterWeights) {
//            sum -= SentRel.LAMBDA_OPT * Math.pow(weight, 2);
//        }
//        sum *= -1; //return negative since minimizing
//        System.err.println("F = "+sum);
//        assert(!Double.isNaN(sum));
//        return sum;
//
//    }
//
//    double[] gradient(double[] iterWeights) {
//        //just copy values
//        double [] grad = new double[iterWeights.length];
//        for(int i=0;i<grad.length; i++)
//            try {
//                grad[i] = -Opt.featureGradients.get(i); //negative since minimizing
//            }
//            catch (Exception e) {
//                System.out.println("Warning: missing feature in gradient: " + SentRel.index2Feature.get(i));
//            }
//
//        //regularization - adding positive values since the sign has been changed already
//        for(int i=0;i<grad.length; i++)
//            grad[i] += 2 * SentRel.LAMBDA_OPT * iterWeights[i];
//
//        System.err.println("|g| = "+Tools.squareSum(grad));
//        assert(!Double.isNaN(Tools.squareSum(grad)));
//        return grad;
//    }
//
//
//}
//
