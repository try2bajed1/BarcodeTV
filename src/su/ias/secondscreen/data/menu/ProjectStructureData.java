package su.ias.secondscreen.data.menu;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 16.05.2014
 * Time: 11:17
 */
public class ProjectStructureData implements Serializable {

    public String title;
    public String projectImageUrl;
    public ProjectThemeData themeData;

    public ArrayList<SlidingMenuItemData> slidingMenuItemsArr;



}
