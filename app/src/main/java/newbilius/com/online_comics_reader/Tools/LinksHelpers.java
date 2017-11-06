package newbilius.com.online_comics_reader.Tools;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class LinksHelpers {
    public static void goToUrl(Activity activity, String url) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public static void sendEmail(Activity activity,
                                 String emailTo,
                                 String subject) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailTo});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        activity.startActivity(Intent.createChooser(emailIntent, "Написать письмо"));
    }
}
