package newbilius.com.online_comics_reader.UI;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;

public class MessageHelper {
    private final Context applicationContext;
    private final Context context;

    public MessageHelper(Context context) {
        this.context = context;
        applicationContext = context.getApplicationContext();
    }

    public void ShowToast(String text) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show();
    }

    public void ShowShortToast(String text) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show();
    }

    public void ShowAlert(String text) {
        try {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage(text)
                    .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .create();
            if (dialog.getWindow() != null)
                dialog.show();
        } catch (Exception ignored) {

        }
    }
}
