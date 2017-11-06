package newbilius.com.online_comics_reader.Database;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.sql.SQLException;

public class ComicsDataProvider {
    private RuntimeExceptionDao<Comics, Integer> comicsDao;

    public ComicsDataProvider(RuntimeExceptionDao<Comics, Integer> comicsDao) {
        this.comicsDao = comicsDao;
    }

    public Comics getComicsByUrl(String url) {
        Comics comics;
        try {
            comics = comicsDao
                    .queryBuilder()
                    .where()
                    .eq(Comics.URL_FIELD_NAME, url)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return comics;
    }

    public Comics getOrCreateComicsByUrl(String url) {
        Comics comics;
        try {
            comics = comicsDao
                    .queryBuilder()
                    .where()
                    .eq(Comics.URL_FIELD_NAME, url)
                    .queryForFirst();
            if (comics == null) {
                comics = new Comics();
                comics.Url = url;
                comics.Page = 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return comics;
    }
}
