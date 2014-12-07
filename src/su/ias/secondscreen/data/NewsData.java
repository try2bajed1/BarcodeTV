package su.ias.secondscreen.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 13.05.2014
 * Time: 17:05
 */
public class NewsData implements Serializable {

    public long date;
    public String title;
    public String text;
    public ArrayList<String> imagesArr;
    public String videoUrl;
    public String videoPreivewUrl;


}
