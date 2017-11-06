package newbilius.com.online_comics_reader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import newbilius.com.online_comics_reader.Database.Comics;
import newbilius.com.online_comics_reader.Database.ComicsDataProvider;
import newbilius.com.online_comics_reader.Database.DatabaseHelper;
import newbilius.com.online_comics_reader.Net.BaseUrls;
import newbilius.com.online_comics_reader.Net.NetHelpers;
import newbilius.com.online_comics_reader.Tools.FirebaseEventsHelper;
import newbilius.com.online_comics_reader.Tools.Plural;
import newbilius.com.online_comics_reader.UI.IOnSwipeAction;
import newbilius.com.online_comics_reader.UI.NetMessage;
import newbilius.com.online_comics_reader.UI.SwipeGestureListener;
import newbilius.com.online_comics_reader.UI.MessageHelper;

import static android.text.InputType.TYPE_CLASS_NUMBER;

//todo прибраться и разбить на классы
//todo утечка контекста?

public class ReadingActivity extends AppCompatActivity {

    private static String EXTRA_COMICS_URL = "EXTRA_COMICS_URL";
    String comicsUrl;
    int currentPage = 1;
    private GestureDetectorCompat gestureDetectorCompat;

    @BindView(R.id.innerImageView)
    ImageView innerImageView;
    @BindView(R.id.outerImageView)
    ImageView outerImageView;
    @BindView(R.id.progressBar)
    View progressBar;
    @BindView(R.id.mainView)
    View mainView;
    @BindView(R.id.scrollView)
    ScrollView scrollView;

    private String currentImageUrl;
    private Comics comics;
    private MessageHelper messageHelper;
    private EditText selectPageEditText;
    private boolean Rescale = false;
    private Target picassoOnImageLoadingTarget;
    private AlertDialog.Builder sharingAlertDialogBuilder;
    private RuntimeExceptionDao<Comics, Integer> comicsDao;
    private ReadingActivity activity;
    private FirebaseEventsHelper firebaseEventsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFullscreen();
        setContentView(R.layout.activity_reading);
        ButterKnife.bind(this);
        getSupportActionBar().hide();
        comicsUrl = getIntent().getStringExtra(EXTRA_COMICS_URL);
        activity = this;

        firebaseEventsHelper = new FirebaseEventsHelper(this);
        messageHelper = new MessageHelper(this);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        comicsDao = databaseHelper.getComicsDao();
        ComicsDataProvider comicsDataProvider = new ComicsDataProvider(comicsDao);

        sharingAlertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Отправить ссылку")
                .setItems(sharingMenuItems, onSharingMenuClickListener);

        comics = comicsDataProvider.getOrCreateComicsByUrl(comicsUrl);
        currentPage = comics.Page;
        changeRescale(comics.Rescale);
        loadPage();

        gestureDetectorCompat = new GestureDetectorCompat(this, new SwipeGestureListener(new IOnSwipeAction() {
            @Override
            public void FromRightToLeft() {
                int oldPage = currentPage;
                if (currentPage < comics.PagesCount)
                    currentPage++;
                else
                    messageHelper.ShowToast("Вы достигли последней страницы - дальше пути нет");
                if (reloadIfNeed(oldPage)) {
                    int nextPage = currentPage + 1;
                    if (nextPage <= comics.PagesCount)
                        loadPageInBackground(nextPage);
                }
            }

            @Override
            public void FromLeftToRight() {
                int oldPage = currentPage;
                if (currentPage > 1)
                    currentPage--;
                else
                    messageHelper.ShowToast("Вы уже на первой странице - отсюда можно двигаться только вперёд!");
                reloadIfNeed(oldPage);
            }

            private boolean reloadIfNeed(int oldPage) {
                if (oldPage != currentPage) {
                    changePage(currentPage);
                    return true;
                }
                return false;
            }
        }));

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetectorCompat.onTouchEvent(motionEvent);
            }
        });
        outerImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (view.isEnabled())
                    gestureDetectorCompat.onTouchEvent(motionEvent);
                return true;
            }
        });

        picassoOnImageLoadingTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                int pixel = bitmap.getPixel(1, 1);
                mainView.setBackgroundColor(pixel);

                innerImageView.setImageBitmap(bitmap);
                outerImageView.setImageBitmap(bitmap);
                scrollView.setSmoothScrollingEnabled(false);
                scrollView.fullScroll(View.FOCUS_UP);
                scrollView.setSmoothScrollingEnabled(true);
                afterComplete();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                afterComplete();
            }

            private void afterComplete() {
                progressBar.setVisibility(View.GONE);
                innerImageView.setEnabled(true);
                outerImageView.setEnabled(true);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
    }

    private void changeRescale(boolean rescale) {
        Rescale = rescale;
        comics.Rescale = rescale;
        comicsDao.createOrUpdate(comics);
        scrollView.setVisibility(Rescale ? View.GONE : View.VISIBLE);
        outerImageView.setVisibility(Rescale ? View.VISIBLE : View.GONE);
    }

    private void loadPageInBackground(int page) {
        Picasso.with(this)
                .load(getPageUrl(page))
                .fetch();
    }

    private void loadPage() {
        new DownloadImageFromPageAsyncTask(this).execute(getPageUrl(currentPage));
    }

    private String getPageUrl(int page) {
        return BaseUrls.BASE_URL + comicsUrl + page;
    }

    private void initFullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private class DownloadImageFromPageAsyncTask extends AsyncTask<String, Void, Void> {

        private Context context;

        DownloadImageFromPageAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            innerImageView.setEnabled(false);
            outerImageView.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String url = strings[0];
            try {
                Document doc = Jsoup
                        .connect(url)
                        .data("ageRestrict", "17")
                        .post();
                Elements image = doc.select("img#mainImage");
                String imagePath = image.attr("src");
                currentImageUrl = BaseUrls.BASE_URL + imagePath;
                String pages = doc.select("span.issueNumber").text();
                comics.PagesCount = Integer.valueOf(pages.split("/")[1]);
                comicsDao.createOrUpdate(comics);
            } catch (IOException e) {
                e.printStackTrace();
                if (!NetHelpers.NetworkIsAvailable())
                    NetMessage.showAlertForNetworkNotAvailable(activity);
                else
                    doInBackground(url);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (currentImageUrl != null)
                Picasso.with(context)
                        .load(currentImageUrl)
                        .into(picassoOnImageLoadingTarget);
        }
    }

    String[] menuItems = new String[]{
            "Переключить метод отображения",
            "Добавить в избранное",
            "Перейти на странице по номеру",
            "Отправить",
            "Выход"
    };

    String[] favoriteMenuItems = new String[]{
            "Переключить метод отображения",
            "Удалить из избранного",
            "Перейти на странице по номеру",
            "Отправить ссылку",
            "Выход"
    };

    String[] sharingMenuItems = new String[]{
            "Комикс",
            "Страницу",
            "Изображение"
    };

    private DialogInterface.OnClickListener onSharingMenuClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, comics.Title);
            switch (i) {
                case 0:
                    intent.putExtra(Intent.EXTRA_TEXT, BaseUrls.BASE_URL + comicsUrl);
                    firebaseEventsHelper.share("comics", BaseUrls.BASE_URL + comicsUrl);
                    break;
                case 1:
                    intent.putExtra(Intent.EXTRA_TEXT, BaseUrls.BASE_URL + comicsUrl + comics.Page);
                    firebaseEventsHelper.share("page", BaseUrls.BASE_URL + comicsUrl + comics.Page);
                    break;
                case 2:
                    intent.putExtra(Intent.EXTRA_TEXT, currentImageUrl);
                    firebaseEventsHelper.share("image", currentImageUrl);
                    break;
            }

            startActivity(Intent.createChooser(intent, "Отправить ссылку на " + sharingMenuItems[i].toLowerCase()));
        }
    };

    private DialogInterface.OnClickListener onBackMenuClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch (i) {
                case 0:
                    changeRescale(!Rescale);
                    firebaseEventsHelper.changeViewMode(String.valueOf(Rescale), comics.Title);
                    break;
                case 1:
                    comics.Favorite = !comics.Favorite;
                    if (comics.Favorite)
                        firebaseEventsHelper.addToFavorite(comics.Title);
                    else
                        firebaseEventsHelper.removeFromFavorite(comics.Title);
                    comicsDao.createOrUpdate(comics);
                    messageHelper.ShowShortToast(comics.Favorite
                            ? "Добавлен в избранное"
                            : "Удалён из избранного");
                    break;
                case 2:
                    showPageSelectAlertDialog();
                    break;
                case 3:
                    sharingAlertDialogBuilder.show();
                    break;
                case 4:
                    finish();
                    break;
            }

        }
    };

    private void showPageSelectAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setPositiveButton("Перейти",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int newPage = -1;
                                try {
                                    newPage = Integer.valueOf(selectPageEditText.getText().toString());
                                } catch (NumberFormatException exception) {
                                    messageHelper.ShowToast("Введена какая-то чушь вместо номера страницы :-/ Не надо так.");
                                    return;
                                }
                                if (newPage > comics.PagesCount) {
                                    messageHelper.ShowToast("Всего в произведении " + comics.PagesCount + " " + Plural.get(comics.PagesCount, "страница", "страницы", "страниц") + ", вы же ввели " + newPage);
                                    return;
                                }
                                if (newPage < 1)
                                    newPage = 1;
                                if (newPage != currentPage) {
                                    changePage(newPage);
                                }
                            }
                        })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

        selectPageEditText = new EditText(this);
        selectPageEditText.setInputType(TYPE_CLASS_NUMBER);
        selectPageEditText.setHint("Страница " + currentPage + " из " + comics.PagesCount);

        alertDialogBuilder.setView(selectPageEditText);
        AlertDialog dialog = alertDialogBuilder.create();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
        }
    }

    private void changePage(int newPage) {
        currentPage = newPage;
        loadPage();
        comics.Page = currentPage;
        comicsDao.createOrUpdate(comics);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder backMenuAlertDialogBuilder = new AlertDialog.Builder(this)
                .setItems(comics.Favorite
                        ? favoriteMenuItems
                        : menuItems, onBackMenuClickListener);
        backMenuAlertDialogBuilder.show();
    }

    public static void Open(Context context,
                            String url) {
        Intent intent = new Intent(context, ReadingActivity.class);
        intent.putExtra(EXTRA_COMICS_URL, url);
        context.startActivity(intent);
    }
}