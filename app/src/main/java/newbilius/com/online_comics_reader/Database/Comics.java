package newbilius.com.online_comics_reader.Database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "ComicsData")
public class Comics {

    public static final String URL_FIELD_NAME = "Url";
    public static final String FAVORITE_FIELD_NAME = "Favorite";

    @DatabaseField(id = true)
    public String Url;

    @DatabaseField
    public String CoverUrl;

    @DatabaseField
    public String Title;

    @DatabaseField
    public String Rating;

    @DatabaseField
    public String Description;

    @DatabaseField
    public int Page;

    @DatabaseField
    public int PagesCount;

    @DatabaseField
    public boolean Rescale;

    @DatabaseField
    public boolean Completed;

    @DatabaseField
    public boolean Favorite;

    public Comics() {
        // ORMLite needs a no-arg constructor
    }
}