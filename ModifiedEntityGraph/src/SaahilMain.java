/**
 * Created by SaahilM on 4/14/16.
 */
public class SaahilMain {

    public static void main(String[] args)
    {
        String filename = args[0];
        System.out.println(filename);
        SentRel.initStanfordPipeline();
        try {
            Paragraph.readParagraphs(filename);
        }
        catch (java.io.IOException exception)
        {
            System.out.println("HAD ERROR");
        }
    }
}
