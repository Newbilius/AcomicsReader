package newbilius.com.online_comics_reader.Tools;

import android.app.Activity;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseEventsHelper {
    private final FirebaseAnalytics firebaseAnalytics;

    public FirebaseEventsHelper(Activity activity) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(activity);
    }

    public void share(String type, String url) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("sharing_type", type);
            bundle.putString("url", url);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
        } catch (Exception ignored) {

        }
    }

    public void changeViewMode(String type, String title) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("sharing_type", type);
            bundle.putString("title", title);
            firebaseAnalytics.logEvent("changeViewMode", bundle);
        } catch (Exception ignored) {

        }
    }

    public void addToFavorite(String title) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("title", title);
            firebaseAnalytics.logEvent("favorite_add", bundle);
        } catch (Exception ignored) {

        }
    }

    public void removeFromFavorite(String title) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("title", title);
            firebaseAnalytics.logEvent("favorite_remove", bundle);
        } catch (Exception ignored) {

        }
    }

    public void search(String searchText) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("searchText", searchText);
            firebaseAnalytics.logEvent("search", bundle);
        } catch (Exception ignored) {

        }
    }

    public void openAboutPage() {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("title", "AboutPage");
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        } catch (Exception ignored) {

        }
    }
}