package newbilius.com.online_comics_reader.Lists;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import newbilius.com.online_comics_reader.Database.Comics;
import newbilius.com.online_comics_reader.Database.ComicsDataProvider;
import newbilius.com.online_comics_reader.Database.DatabaseHelper;
import newbilius.com.online_comics_reader.Net.NetHelpers;
import newbilius.com.online_comics_reader.R;
import newbilius.com.online_comics_reader.ReadAdListener;
import newbilius.com.online_comics_reader.ReadingActivity;
import newbilius.com.online_comics_reader.UI.IComicsOnListClickListener;
import newbilius.com.online_comics_reader.UI.IOnStatusChangeListener;
import newbilius.com.online_comics_reader.UI.NetMessage;

//todo надо прикручивать какой-нибудь биндинг или реактивность, а то ой как сложно всё
public class SearchListActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    android.support.v7.widget.RecyclerView recyclerView;

    @BindView(R.id.nothingTextView)
    TextView nothingTextView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.adView)
    com.google.android.gms.ads.AdView adView;

    private SearchListAdapter adapter;
    private EditText selectPageEditText;
    private ComicsDataProvider comicsDataProvider;
    private RuntimeExceptionDao<Comics, Integer> comicsDao;
    private SearchListActivity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

        adView.setAdListener(new ReadAdListener(adView));
        adView.loadAd(new AdRequest.Builder().build());

        activity = this;
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        comicsDao = databaseHelper.getComicsDao();
        comicsDataProvider = new ComicsDataProvider(comicsDao);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchListAdapter(
                new IComicsOnListClickListener() {
                    @Override
                    public void onClick(ComicsListData data) {
                        if (NetHelpers.NetworkIsAvailable())
                            openReadingActivity(data);
                        else
                            NetMessage.showAlertForNetworkNotAvailable(activity);
                    }
                },
                new IOnStatusChangeListener() {
                    @Override
                    public void onChange(boolean inProcess) {
                        progressBar.setVisibility(inProcess
                                ? View.VISIBLE
                                : View.GONE);
                        swipeRefreshLayout.setVisibility(inProcess
                                ? View.GONE
                                : View.VISIBLE);
                        if (inProcess)
                            nothingTextView.setVisibility(View.GONE);
                        else {
                            boolean haveItems = adapter.getItemCount() > 0;
                            nothingTextView.setVisibility(haveItems ? View.GONE : View.VISIBLE);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, this);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                adapter.reload();
            }
        });

        selectPageEditText = getSearchEditText();

        selectPageEditText.setHint("Поищем нечто интересное!");

        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowCustomEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setCustomView(selectPageEditText, layout);
        }
        selectPageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.search(editable.toString());
            }
        });
        nothingTextView.setText("Ничего не нашлось :-(");
    }

    private EditText getSearchEditText() {
        EditText editText = new EditText(this);
        int transparentColor = Color.argb(0, 255, 255, 252);
        int whiteColor = Color.rgb(255, 255, 252);
        int hintColor = Color.parseColor("#9FA8DA");
        editText.setTextColor(whiteColor);
        editText.setHintTextColor(hintColor);
        editText.setBackgroundColor(transparentColor);
        editText.setLines(1);

        Field mCursorDrawableResField;
        try {
            mCursorDrawableResField = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableResField.setAccessible(true);
            mCursorDrawableResField.set(editText, R.drawable.white_cursor);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return editText;
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.search(selectPageEditText.getText().toString());
    }

    private void openReadingActivity(ComicsListData data) {
        Comics comics = comicsDataProvider.getOrCreateComicsByUrl(data.Url);

        comics.CoverUrl = data.CoverUrl;
        comics.Description = data.Description;
        comics.Rating = data.Rating;
        comics.Title = data.Title;
        comics.Completed = data.Completed;
        comicsDao.createOrUpdate(comics);

        ReadingActivity.Open(this, data.Url);
    }
}
