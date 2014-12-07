package su.ias.secondscreen;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 02.06.2014
 * Time: 12:54
 */
public interface IListener {

    public void completeHandler(String jsonFromApi);
    public void errorHandler(String errorStr);


}
