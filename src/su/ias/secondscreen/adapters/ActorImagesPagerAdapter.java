package su.ias.secondscreen.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import su.ias.secondscreen.R;
import su.ias.secondscreen.app.AppSingleton;
import su.ias.secondscreen.utils.BitmapDrawer;

import java.util.ArrayList;

public class ActorImagesPagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<String> imagesArr;
    private int posForCache;

    public ActorImagesPagerAdapter(Context context, int posForCache, ArrayList<String> imagesArr) {
        this.context = context;
        this.imagesArr = imagesArr;
        this.posForCache = posForCache;
    }



    @Override
    public int getCount() {
        return imagesArr.size();
    }



    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (object);
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = null;
        itemView = inflater.inflate(R.layout.layout_image_pager, container, false);

        final ImageView photoImg = (ImageView) itemView.findViewById(R.id.page_image);
        final String url = imagesArr.get(0);
        final String path = context.getCacheDir() + "/" + "actor_slide_preview"  + "_" + AppSingleton.getInstance().currentQR+"_"+ position + "_"+posForCache+ ".png";


        ViewTreeObserver observer = photoImg.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                photoImg.getViewTreeObserver().removeOnPreDrawListener(this);
                BitmapDrawer.INSTANCE.drawBitmap(path, url, photoImg, BitmapDrawer.DRAW_ALL, true, true);
                return false;
            }

        });

        ((ViewPager) container).addView(itemView);

        return itemView;

    }




    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Remove viewpager_item.xml from ViewPager
        (container).removeView((View) object);

    }


}









