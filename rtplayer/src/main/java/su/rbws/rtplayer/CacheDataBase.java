package su.rbws.rtplayer;

import static android.content.Context.MODE_PRIVATE;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import su.rbws.rtplayer.preference.PreferencesAbstract;

public class CacheDataBase {

    // глобальный флаг использования кэша
    private final boolean USE_CACHE = true;

    private static final String databaseName = "cache.db";
    private SQLiteDatabase sqlitedb;
    String databasePath = "";

    public CacheDataBase() {
        File f = RTApplication.getContext().getDataDir();
        databasePath = FileUtils.includePathDelimiter(f.getAbsolutePath()) + databaseName;

        checkCacheDataBase();
    }

    private static final String createSystem = "create table if not exists [systemOptions] ([name] text not null, [value] text not null);";
    private static final String createCountries = "create table if not exists [countries] ([name] text not null, [code] int not null);";
    private static final String createCountryIndex = "create index if not exists [countries_index] on [countries] ('name' desc);";

    private void open() {
        sqlitedb = RTApplication.getContext().openOrCreateDatabase(databasePath, MODE_PRIVATE, null);
    }

    private void close() {
        sqlitedb.close();
    }

    private void checkCacheDataBase() {
        open();
        try {
            sqlitedb.execSQL(createSystem);
            sqlitedb.execSQL(createCountries);
            sqlitedb.execSQL(createCountryIndex);
        }
        finally {
            close();
        }
    }

    private boolean tableExists(String tableName) {
        boolean result = false;
        Cursor query = sqlitedb.rawQuery("select [sql] from [sqlite_master] where [type] = ? and [name] = ?;", new String[] {"table", tableName});
        if (query.moveToFirst()) {
            result = true;
        }
        return result;
    }

    public boolean hasCountryCache() {
        boolean result = false;
        if (!USE_CACHE)
            return result;

        open();
        try {
            Cursor query = sqlitedb.rawQuery("select count(*) from [countries];", null);
            if (query.moveToFirst()) {
                if (query.getInt(0) > 0)
                    result = true;
            }
        }
        finally {
            close();
        }

        return result;
    }

    public Map<String, Integer> getCountryCodes() {
        Map<String, Integer> result = new HashMap<>();
        int indexName, indexCode;

        open();
        try {
            Cursor query = sqlitedb.rawQuery("select * from [countries];", null);
            if (query.moveToFirst()) {
                indexName = query.getColumnIndex("name");
                indexCode = query.getColumnIndex("code");

                while (query.moveToNext()) {
                    result.put(query.getString(indexName), query.getInt(indexCode));
                }
            }
        }
        finally {
            close();
        }

        return result;
    }

    public void createCountryCache(Map<String, Integer> list) {
        SQLiteStatement stmt;

        open();
        try {
            sqlitedb.execSQL("delete from [countries];");
            sqlitedb.beginTransaction();
            try {
                stmt = sqlitedb.compileStatement("insert into [countries] (name, code) values (?, ?);");
                for (Map.Entry<String, Integer> entry : list.entrySet()) {
                    stmt.bindString(1, entry.getKey());
                    stmt.bindLong(2, entry.getValue());
                    stmt.execute();
                }
                sqlitedb.setTransactionSuccessful();
            } finally {
                sqlitedb.endTransaction();
            }

            // вставка или обновление времени создания базы
            String date = Long.toString(new Date().getTime());
            sqlitedb.execSQL("update or ignore [systemOptions] set [name] = ?, [value] = ?;", new String[] {"dateCountries", date});
            sqlitedb.execSQL("insert or ignore into [systemOptions] (name, value) values (?, ?);", new String[] {"dateCountries", date});
        }
        finally {
            close();
        }

        open();
        boolean b = hasCountryCache();
        close();
    }

    public String getSystemParameter(String name) {
        String result = "";
        int indexValue;

        open();

        try {
            Cursor query = sqlitedb.rawQuery("select * from [systemOptions] where [name] = ?;", new String[] {name});
            if (query.moveToFirst()) {
                indexValue = query.getColumnIndex("value");
                result = query.getString(indexValue);
            }
        }
        finally {
            close();
        }

        return result;
    }
}