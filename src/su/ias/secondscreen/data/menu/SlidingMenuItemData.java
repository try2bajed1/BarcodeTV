package su.ias.secondscreen.data.menu;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 16.05.2014
 * Time: 11:41
 */

public class SlidingMenuItemData implements Serializable {

    public int id;
    public int order;
    public String title;
    public String type;
    public boolean selected;


}
