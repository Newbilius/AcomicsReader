package newbilius.com.online_comics_reader.UI;

import android.app.Activity;

public class NetMessage {
    public static void showAlertForNetworkNotAvailable(Activity activity) {
        new MessageHelper(activity).ShowAlert("Проверьте интернет-соединение - без него кина не будет :-/");
    }
}
