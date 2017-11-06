package newbilius.com.online_comics_reader.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "acomicsReader.db";
    private static final int DATABASE_VERSION = 1;
    private RuntimeExceptionDao<Comics, Integer> comicsConfigDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, Comics.class);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Comics.class, true);
            TableUtils.createTableIfNotExists(connectionSource, Comics.class);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public RuntimeExceptionDao<Comics, Integer> getComicsDao() {
        if (comicsConfigDao == null) {
            comicsConfigDao = getRuntimeExceptionDao(Comics.class);
        }
        return comicsConfigDao;
    }
}