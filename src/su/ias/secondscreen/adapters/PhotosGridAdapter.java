package su.ias.secondscreen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import su.ias.secondscreen.R;
import su.ias.secondscreen.data.ImageData;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA. User: n.senchurin Date: 29.08.13 Time: 18:18
 */

public class PhotosGridAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater layoutInflater;
    public ArrayList<ImageData> imagesArr;
    public String projQr;
    private int lastPosition = -1;

    public PhotosGridAdapter(Context c,String qrCode, ArrayList<ImageData> imagesArr) {

        mContext = c;
        layoutInflater = LayoutInflater.from(mContext);
        //BitmapDrawer.INSTANCE.setDefaultImageRes(R.drawable.logo_filter, ImageView.ScaleType.CENTER_INSIDE);
        this.imagesArr = imagesArr;
        projQr = qrCode;
    }

    public int getCount() {
        return imagesArr.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }


    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_grid_item, null);
        }

        final ImageView photoIV = (ImageView) convertView.findViewById(R.id.photo_image);


        final String logoImagePath = mContext.getExternalCacheDir().getPath() + "/logos" + position+"_" + projQr+ ".png";
        final String logoImageUrl = imagesArr.get(position).previewUrl;
        ImageLoader.getInstance().displayImage(logoImageUrl,photoIV);


/*
        ViewTreeObserver observer = photoIV.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {

                photoIV.getViewTreeObserver().removeOnPreDrawListener(this);
                BitmapDrawer.INSTANCE.drawBitmap(logoImagePath, logoImageUrl, photoIV, BitmapDrawer.DRAW_CENTER, true, true);
                return true;
            }
        });
*/


/*
        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        convertView.startAnimation(animation);
        lastPosition = position;
*/


        return convertView;
    }

}
