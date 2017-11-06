package newbilius.com.online_comics_reader.Lists;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import newbilius.com.online_comics_reader.SimpleComicsReaderApplication;
import newbilius.com.online_comics_reader.Net.BaseUrls;
import newbilius.com.online_comics_reader.Net.NetHelpers;
import newbilius.com.online_comics_reader.R;
import newbilius.com.online_comics_reader.Tools.FirebaseEventsHelper;
import newbilius.com.online_comics_reader.UI.IComicsOnListClickListener;
import newbilius.com.online_comics_reader.UI.IOnStatusChangeListener;
import newbilius.com.online_comics_reader.UI.NetMessage;

class SearchListAdapter extends RecyclerView.Adapter {
    private final FirebaseEventsHelper firebaseEventsHelper;
    private IComicsOnListClickListener onComicsOnListClickListener;
    private IOnStatusChangeListener onStatusChangeListener;
    private Activity activity;
    private List<ComicsListData> data = new ArrayList<>();
    private SearchAsyncTask searchAsyncTask;
    private String currentSearchText;

    //todo ссылка на activity - мягко говоря, не айс
    SearchListAdapter(IComicsOnListClickListener IComicsOnListClickListener,
                      IOnStatusChangeListener onStatusChangeListener,
                      Activity activity) {
        this.onComicsOnListClickListener = IComicsOnListClickListener;
        this.onStatusChangeListener = onStatusChangeListener;
        this.activity = activity;
        this.firebaseEventsHelper = new FirebaseEventsHelper(activity);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_comics_list, parent, false);
        ListsViewHolder holder = new ListsViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = (int) view.getTag();
                onComicsOnListClickListener.onClick(getItem(pos));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ListsViewHolder) holder).changeData(getItem(position), position);
    }

    private ComicsListData getItem(int position) {
        return data.get(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    //todo вот тут ОЧЕНЬ пригодилось бы реактивное программирование
    synchronized void search(String searchText) {
        onStatusChangeListener.onChange(true);
        currentSearchText = searchText;
        if (searchAsyncTask == null) {
            searchAsyncTask = new SearchAsyncTask();
            searchAsyncTask.execute(searchText);
        }
    }

    public void reload() {
        if (searchAsyncTask == null) {
            searchAsyncTask = new SearchAsyncTask();
            searchAsyncTask.execute(currentSearchText);
        }
    }

    private class SearchAsyncTask extends AsyncTask<String, Void, Void> {

        private String searchText;

        @Override
        protected Void doInBackground(String... strings) {
            searchText = strings[0];
            String url = null;
            try {
                if (searchText == null || searchText.isEmpty())
                    url = "https://acomics.ru/comics";
                else
                    url = "https://acomics.ru/search?keyword=" + URLEncoder.encode(searchText, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            try {
                Document doc = Jsoup.connect(url).get();
                Elements items = doc.select("table.catalog-elem");
                data.clear();
                for (Element item : items) {
                    if (item.id().equals("catalog-header"))
                        continue;
                    ComicsListData newComicsListData = new ComicsListData();

                    newComicsListData.Rating = item.select("div.also a").first().text();
                    String pages = item.select("span.total").first().text().split(" ")[0];
                    newComicsListData.Pages = SimpleComicsReaderApplication.getAppContext().getString(R.string.pagesPlaceholder, pages);
                    newComicsListData.Description = item.select("div.about").first().text();
                    newComicsListData.Title = item.select("div.title").first().text();
                    newComicsListData.Url = item.select("div a").first().attr("href")
                            .replace(BaseUrls.BASE_URL, "") + "/";
                    newComicsListData.Url = newComicsListData.Url.replace("//", "/");
                    newComicsListData.CoverUrl = item.select("td.catdata1 img").attr("src").replace(BaseUrls.BASE_URL, "");
                    newComicsListData.Completed = !item.select("span.closed").isEmpty();
                    try {
                        if (Integer.valueOf(pages) > 0)
                            data.add(newComicsListData);
                    } catch (NumberFormatException ignored) {

                    }
                }
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
            if (searchText.equals(currentSearchText)) {
                firebaseEventsHelper.search(searchText);
                searchAsyncTask = null;
                notifyDataSetChanged();
                onStatusChangeListener.onChange(false);
            } else {
                searchAsyncTask = new SearchAsyncTask();
                searchAsyncTask.execute(currentSearchText);
            }

        }
    }
}