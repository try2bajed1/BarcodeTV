package su.ias.secondscreen.app;

import android.app.Application;
import android.util.Log;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import su.ias.secondscreen.data.menu.ProjectThemeData;
import su.ias.secondscreen.db.DBAdapter;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 23.04.2014
 * Time: 11:19
 */
public class AppSingleton  extends Application {

	public static final int SMARTPHONE = 1;
	public static final int TABLET = 2;
 
	// тип устройства определяется при запуске QRReaderActivity
	// по умолчанию - SMARTPHONE
	public static int deviceType = SMARTPHONE;
	
	
    private static AppSingleton application;
    private DBAdapter dbAdapter;

    public String currentQR;
    public ProjectThemeData currentProjTheme;

    public static AppSingleton getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;


        // инициализируем адаптер б/д
        initDB();

//        File cacheDir = StorageUtils.getCacheDirectory(this, "UniversalImageLoader/Cache");
        File cacheDir = StorageUtils.getCacheDirectory(this);
        Log.i("@", "cache dir "+ cacheDir.getPath());

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory()
                .cacheOnDisc(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
//                .memoryCacheExtraOptions(480, 800) // width, height
//                .discCacheExtraOptions(480, 800, Bitmap.CompressFormat.JPEG,70) // width, height, compress format, quality
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // 2 Mb
                .discCache(new UnlimitedDiscCache(cacheDir))
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .imageDownloader(new BaseImageDownloader(getApplicationContext(), 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)
                .defaultDisplayImageOptions(options )
                .build();

        ImageLoader.getInstance().init(config);

//        ImageLoader.getInstance().dis

    }


    public DBAdapter getDBAdapter() {
        return dbAdapter;
    }

    private void initDB() {
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
    }



}
