package su.ias.secondscreen.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 22.05.2014
 * Time: 14:10
 */
public class SquareRelativeLayout extends RelativeLayout
{

    public SquareRelativeLayout(final Context context)
    {
        super(context);
    }

    public SquareRelativeLayout(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SquareRelativeLayout(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    }



    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec){
        final int width = getDefaultSize(getSuggestedMinimumWidth(),widthMeasureSpec);
        setMeasuredDimension(width, width);
    }

/*    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh){
        super.onSizeChanged(w, w, oldw, oldh);
    }*/

}