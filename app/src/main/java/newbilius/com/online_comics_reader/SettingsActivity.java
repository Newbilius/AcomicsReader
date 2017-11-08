package newbilius.com.online_comics_reader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import newbilius.com.online_comics_reader.Tools.FirebaseEventsHelper;
import newbilius.com.online_comics_reader.Tools.LinksHelpers;
import newbilius.com.online_comics_reader.Tools.PicassoCacheHelper;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.cacheSizeTextView)
    TextView cacheSizeTextView;

    @BindView(R.id.deleteCacheButton)
    Button deleteCacheButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        new FirebaseEventsHelper(this).openAboutPage();
        new CalcCacheSizeAsyncTask().execute();
    }

    @OnClick(R.id.deleteCacheButton)
    public void deleteCache() {
        new CacheDeletingAsyncTask().execute();
    }

    @OnClick(R.id.buttonGoToSite)
    public void onClickGoToSite() {
        LinksHelpers.goToUrl(this, "https://acomics.ru/");
    }

    @OnClick(R.id.buttonSendEmail)
    public void onClickSendEmail() {
        LinksHelpers.sendEmail(this,
                "newbilius@gmail.com",
                "Android soft: " + getString(R.string.app_name));
    }

    @OnClick(R.id.buttonGoToStore)
    public void onClickGoToStore() {
        LinksHelpers.goToUrl(this, "market://search?q=pub:%D0%94%D0%BC%D0%B8%D1%82%D1%80%D0%B8%D0%B9+%D0%9C%D0%BE%D0%B8%D1%81%D0%B5%D0%B5%D0%B2");
    }

    //todo не учитывает сценария, когда пользователь запускает процесс удаления, потом выходит с экрана, возвращается - и снова начинает процесс удаления
    private class CalcCacheSizeAsyncTask extends AsyncTask<Void, Void, Void> {

        private long size;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            cacheSizeTextView.setText("Объём кэша: [...считается...]");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            size = PicassoCacheHelper.getSize();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            cacheSizeTextView.setText(String.format("Объём кэша: %s мб.", Math.round(size / 1024.0f / 1024 * 100) / 100.0));
        }
    }

    private class CacheDeletingAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            deleteCacheButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            PicassoCacheHelper.deleteCache();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new CalcCacheSizeAsyncTask().execute();
            deleteCacheButton.setEnabled(true);
        }
    }
}
