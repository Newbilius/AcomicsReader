package newbilius.com.online_comics_reader.Lists;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import newbilius.com.online_comics_reader.R;
import newbilius.com.online_comics_reader.UI.IComicsOnListClickListener;

class ComicsListAdapter extends RecyclerView.Adapter {

    private FavoriteComicsListDataProvider favoriteComicsListDataProvider;
    private IComicsOnListClickListener comicsOnListClickListener;
    private List<ComicsListData> data;

    ComicsListAdapter(FavoriteComicsListDataProvider favoriteComicsListDataProvider,
                      IComicsOnListClickListener IComicsOnListClickListener) {
        this.favoriteComicsListDataProvider = favoriteComicsListDataProvider;
        this.comicsOnListClickListener = IComicsOnListClickListener;
        data = favoriteComicsListDataProvider.GetList();
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
                comicsOnListClickListener.onClick(getItem(pos));
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int pos = (int) view.getTag();
                comicsOnListClickListener.onLongClick(getItem(pos));
                return true;
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

    public List<String> getCurrentListUrls() {
        ArrayList<String> newList = new ArrayList<String>();
        for (ComicsListData comics : data)
            newList.add(comics.Url);
        return newList;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    void search(String searchText) {
        favoriteComicsListDataProvider.changeSearch(searchText);
        reloadData();
    }

    void reloadData() {
        data = favoriteComicsListDataProvider.GetList();
        notifyDataSetChanged();
    }
}
