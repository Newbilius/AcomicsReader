package newbilius.com.online_comics_reader.Lists;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import newbilius.com.online_comics_reader.Database.Comics;
import newbilius.com.online_comics_reader.Database.ComicsDataProvider;
import newbilius.com.online_comics_reader.Database.DatabaseHelper;
import newbilius.com.online_comics_reader.SettingsActivity;
import newbilius.com.online_comics_reader.Net.BaseUrls;
import newbilius.com.online_comics_reader.Net.NetHelpers;
import newbilius.com.online_comics_reader.R;
import newbilius.com.online_comics_reader.ReadAdListener;
import newbilius.com.online_comics_reader.ReadingActivity;
import newbilius.com.online_comics_reader.Tools.FirebaseEventsHelper;
import newbilius.com.online_comics_reader.UI.IComicsOnListClickListener;
import newbilius.com.online_comics_reader.UI.MessageHelper;
import newbilius.com.online_comics_reader.UI.NetMessage;

//todo повторы кода :-/

public class FavoriteComicsListActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.nothingTextView)
    TextView nothingTextView;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.adView)
    com.google.android.gms.ads.AdView adView;

    private ComicsListAdapter adapter;
    private MenuItem addMenuItem;
    private MenuItem settingsMenuItem;
    private boolean inSearch;
    private String searchText;
    private RuntimeExceptionDao<Comics, Integer> comicsDao;
    private ComicsDataProvider comicsDataProvider;
    private FavoriteComicsListActivity activity;
    private FirebaseEventsHelper firebaseEventsHelper;
    private MessageHelper messageHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

        firebaseEventsHelper = new FirebaseEventsHelper(this);
        messageHelper = new MessageHelper(this);

        MobileAds.initialize(this, this.getString(R.string.addMobApplicationId));
        adView.setAdListener(new ReadAdListener(adView));
        adView.loadAd(new AdRequest.Builder().build());

        activity = this;
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        comicsDao = databaseHelper.getComicsDao();
        comicsDataProvider = new ComicsDataProvider(comicsDao);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComicsListAdapter(new FavoriteComicsListDataProvider(comicsDao),
                new IComicsOnListClickListener() {
                    @Override
                    public void onClick(ComicsListData data) {
                        if (NetHelpers.NetworkIsAvailable())
                            openReadingActivity(data);
                        else
                            NetMessage.showAlertForNetworkNotAvailable(activity);
                    }

                    @Override
                    public void onLongClick(ComicsListData data) {
                        showLongTapMenu(data);
                    }
                });
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new RefreshDataAsyncTask().execute();
            }
        });
    }

    String[] longTapMenuItems = new String[]{
            "Удалить из избранного",
            "Поделиться"
    };

    private Comics selectedForLongTapComics;

    private void showLongTapMenu(ComicsListData data) {
        selectedForLongTapComics = comicsDataProvider.getOrCreateComicsByUrl(data.Url);
        AlertDialog.Builder backMenuAlertDialogBuilder = new AlertDialog.Builder(this)
                .setItems(longTapMenuItems, onLongTapMenuClickListener);
        backMenuAlertDialogBuilder.show();
    }

    private DialogInterface.OnClickListener onLongTapMenuClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Comics comics = selectedForLongTapComics;
            switch (i) {
                case 0:
                    comics.Favorite = false;
                    firebaseEventsHelper.removeFromFavorite(comics.Title);
                    comicsDao.createOrUpdate(comics);
                    messageHelper.ShowShortToast("Удалён из избранного");
                    adapter.reloadData();
                    break;

                case 1:
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, comics.Title);
                    intent.putExtra(Intent.EXTRA_TEXT, BaseUrls.BASE_URL + comics.Url);
                    firebaseEventsHelper.share("comics", BaseUrls.BASE_URL + comics.Url);
                    startActivity(Intent.createChooser(intent, "Отправить ссылку на комикс"));
                    break;
            }

        }
    };

    private void openReadingActivity(ComicsListData data) {
        ReadingActivity.Open(this, data.Url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == addMenuItem.getItemId())
            startActivity(new Intent(this, SearchListActivity.class));

        if (item.getItemId() == settingsMenuItem.getItemId())
            startActivity(new Intent(this, SettingsActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);

        addMenuItem = menu.findItem(R.id.action_add);
        settingsMenuItem = menu.findItem(R.id.action_settings);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.search(newText);
        searchText = newText;
        changeSearchText();
        boolean haveItems = adapter.getItemCount() > 0;
        nothingTextView.setVisibility(haveItems ? View.GONE : View.VISIBLE);
        swipeRefreshLayout.setVisibility(haveItems ? View.VISIBLE : View.GONE);
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.reloadData();
        changeSearchText();
        boolean haveItems = adapter.getItemCount() > 0;
        nothingTextView.setVisibility(haveItems ? View.GONE : View.VISIBLE);
        swipeRefreshLayout.setVisibility(haveItems ? View.VISIBLE : View.GONE);
    }

    private void changeSearchText() {
        nothingTextView.setText(inSearch && searchText != null && !searchText.isEmpty()
                ? "Ничего не найдено"
                : "Добавьте новые комиксы кнопкой \"плюс\" в\u00A0правом верхнем углу");
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            inSearch = true;
            addMenuItem.setVisible(false);
            settingsMenuItem.setVisible(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            inSearch = false;
            addMenuItem.setVisible(true);
            settingsMenuItem.setVisible(true);
            changeSearchText();
            return true;
        }
        return false;
    }

    private class RefreshDataAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<String> urls = adapter.getCurrentListUrls();
            for (String url : urls) {
                String fullUrl = BaseUrls.BASE_URL + url + "about";
                try {
                    Comics comics = comicsDataProvider.getComicsByUrl(url);
                    if (comics == null)
                        continue;

                    //todo два запроса - некрасиво, зато удобно (кхм)
                    Document doc = Jsoup
                            .connect(fullUrl)
                            .data("ageRestrict", "18")
                            .post();
                    String title = doc.select("section#content div#contentMargin h2").text();
                    if (title.toLowerCase().contains("(закончен)"))
                        comics.Completed = true;

                    doc = Jsoup
                            .connect(BaseUrls.BASE_URL + url + 1)
                            .data("ageRestrict", "18")
                            .post();

                    String pages = doc.select("span.issueNumber").text();
                    comics.PagesCount = Integer.valueOf(pages.split("/")[1]);

                    comicsDao.createOrUpdate(comics);

                } catch (IOException e) {
                    e.printStackTrace();
                    if (!NetHelpers.NetworkIsAvailable()) {
                        NetMessage.showAlertForNetworkNotAvailable(activity);
                        break;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.reloadData();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}