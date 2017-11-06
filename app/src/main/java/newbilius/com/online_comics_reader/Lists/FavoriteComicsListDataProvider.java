package newbilius.com.online_comics_reader.Lists;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import newbilius.com.online_comics_reader.SimpleComicsReaderApplication;
import newbilius.com.online_comics_reader.Database.Comics;
import newbilius.com.online_comics_reader.R;

class FavoriteComicsListDataProvider {

    private final RuntimeExceptionDao<Comics, Integer> comicsDao;
    private List<ComicsListData> loadedData = new ArrayList<>();

    private String searchText = "";

    FavoriteComicsListDataProvider(RuntimeExceptionDao<Comics, Integer> comicsDao) {
        this.comicsDao = comicsDao;
    }

    synchronized List<ComicsListData> GetList() {
        reloadData();
        if (searchText == null)
            searchText = "";
        searchText = searchText.trim();
        if (searchText.isEmpty())
            return loadedData;

        //todo без стримов - совсем тоска :-/
        List<ComicsListData> newList = new ArrayList<>();
        for (ComicsListData item : loadedData) {
            if (item.Description.toLowerCase().contains(searchText.toLowerCase())
                    || item.Title.toLowerCase().contains(searchText.toLowerCase()))
                newList.add(item);
        }
        return newList;
    }

    //todo кэширование
    synchronized private void reloadData() {
        try {
            //todo без стримов - совсем тоска (2) :-/
            List<Comics> newData = comicsDao
                    .queryBuilder()
                    .where()
                    .eq(Comics.FAVORITE_FIELD_NAME, true)
                    .query();
            loadedData.clear();
            for (Comics item : newData) {
                //todo инициализаторы?
                ComicsListData newItem = new ComicsListData();
                newItem.Title = item.Title;
                newItem.Description = item.Description;
                newItem.Url = item.Url;
                newItem.CoverUrl = item.CoverUrl;
                newItem.Pages = SimpleComicsReaderApplication.getAppContext().getString(R.string.readedPagesPlaceholder,
                        item.Page + " / " + item.PagesCount);
                newItem.PagesBold = item.Page < item.PagesCount;
                newItem.Rating = item.Rating;
                newItem.Completed = item.Completed;
                loadedData.add(newItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    void changeSearch(String searchText) {
        this.searchText = searchText;
    }
}