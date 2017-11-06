package newbilius.com.online_comics_reader.Net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import newbilius.com.online_comics_reader.SimpleComicsReaderApplication;

public class NetHelpers {
    public static boolean NetworkIsAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                SimpleComicsReaderApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null)
            return false;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
