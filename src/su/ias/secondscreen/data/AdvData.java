package su.ias.secondscreen.data;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 22.05.2014
 * Time: 16:44
 */
public class AdvData implements Comparable<AdvData> {

    public String title;
    public Long startDateMillsSecs;
    public long endDateMillsSecs;
    public int dayOfYear;
    public boolean addedToCalendar;


    @Override
    public int compareTo(AdvData another) {
        return startDateMillsSecs.compareTo(another.startDateMillsSecs);
    }


}
