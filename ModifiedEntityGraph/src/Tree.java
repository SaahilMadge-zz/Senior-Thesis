import org.fun4j.Cons;

import java.io.Serializable;

/**
 * Created by ghostof2007 on 2/23/15.
 */
public class Tree  implements Serializable{

    Tree left, right, parent;
    String text;
    int depth;
    boolean isString = false;

    Tree(Tree par, int d) {
        parent = par;
        depth = d;
    }

    Tree(String s, Tree par, int d) {
        text = s;
        parent = par;
        depth  = d;
    }

}
