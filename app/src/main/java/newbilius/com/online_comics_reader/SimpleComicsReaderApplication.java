package newbilius.com.online_comics_reader;

import android.app.Application;
import android.content.Context;

public class SimpleComicsReaderApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getAppContext() {
        return context;
    }
}
