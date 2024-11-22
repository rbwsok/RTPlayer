package su.rbws.rtplayer;

import static android.content.Context.MODE_PRIVATE;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.sfuhrm.radiobrowser4j.AdvancedSearch;
import de.sfuhrm.radiobrowser4j.ConnectionParams;
import de.sfuhrm.radiobrowser4j.EndpointDiscovery;
import de.sfuhrm.radiobrowser4j.RadioBrowser;
import de.sfuhrm.radiobrowser4j.Paging;
import de.sfuhrm.radiobrowser4j.Station;

// работа с интернет радио https://github.com/sfuhrm/radiobrowser4j
public class NetRadioBrowser {

    private static final String databaseName = "database.db";
    private String databasePath;
    private SQLiteDatabase sqlitedb;

    public NetRadioBrowser() {
        File f = RTApplication.getContext().getDataDir();
        databasePath = FileUtils.includePathDelimiter(f.getAbsolutePath()) + databaseName;
        copyDataBase();
        // база данных только читается
        sqlitedb = RTApplication.getContext().openOrCreateDatabase(databasePath, MODE_PRIVATE, null);
    }

    private static final int TIMEOUT_DEFAULT = 5000;
    private static final int LIMIT_DEFAULT = 512;
    private static final String AGENT = "mozilla";

    EndpointDiscovery endpointDiscovery;
    Optional<String> endpoint;
    RadioBrowser radioBrowser;
    ConnectionParams.ConnectionParamsBuilder builder;
    public Map<String, Integer> countryCodes, codecs, languages, tags;
    public List<Station> stations;

    private boolean createRadioBrowser() {
        boolean result = false;
        try {
            if (endpointDiscovery == null)
                endpointDiscovery = new EndpointDiscovery(AGENT);
            if (endpoint == null)
                endpoint = endpointDiscovery.discover();
            if (builder == null) {
                builder = ConnectionParams.builder();
                builder.apiUrl(endpoint.get());
                builder.timeout(TIMEOUT_DEFAULT);
                builder.userAgent(AGENT);
            }
            if (radioBrowser == null) {
                radioBrowser = new RadioBrowser(builder.build());
            }
            result = true;
        } catch (Exception e) {
            Log.i("rtplayer_tag", e.getMessage());
        }

        return result;
    }

    // копируем базу данных из assets в более приличное место
    private void copyDataBase() {
        try {
            InputStream myInput = RTApplication.getContext().getAssets().open(databaseName);

            File f = new File(databasePath);
            if (f.exists())
                f.delete();

            OutputStream outputStream = new FileOutputStream(f);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            myInput.close();
        } catch (Exception e) {
        }
    }

    public String getCountry(String code) {
        String result = "";

        String name = "", fullname = "";
        int indexName, indexFullname;

        Cursor query = sqlitedb.rawQuery("select * from [countries] where [alpha2] = ?", new String[] {code});
        if (query.moveToFirst()) {
            indexName = query.getColumnIndex("name");
            indexFullname = query.getColumnIndex("full_name");

            if (indexName >= 0)
                name = query.getString(indexName);
            if (indexFullname >= 0)
                fullname = query.getString(indexFullname);

            if (!name.isEmpty() && !fullname.isEmpty() && !name.equals(fullname))
                result = name + " (" + fullname + ")";
            else
                result = name;
        }
        return result;
    }

    public boolean getCountryList() {
        boolean result = true;
        if (countryCodes != null)
            countryCodes.clear();

        boolean needUpdateCache = false;
        needUpdateCache = !RTApplication.getChacheDataBase().hasCountryCache();

        // обноаление кеша стран не чаще одного раза в день
        if (!needUpdateCache) {
            String dateCountries = RTApplication.getChacheDataBase().getSystemParameter("dateCountries");
            needUpdateCache = dateCountries.isEmpty();
            if (!needUpdateCache) {
                Date currentDate = new Date();
                Date dbDate = new Date(Long.parseLong(dateCountries));

                Instant instant1 = currentDate.toInstant();
                Instant instant2 = dbDate.toInstant();
                Duration duration = Duration.between(instant1, instant2);
                long diffDays = duration.toDays();
                needUpdateCache = diffDays > 0;
            }
        }

        if (!needUpdateCache) {
            countryCodes = RTApplication.getChacheDataBase().getCountryCodes();
        } else {
            Thread t = new Thread(() -> {
                try {
                    if (createRadioBrowser()) {
                        countryCodes = radioBrowser.listCountryCodes();
                    }
                } catch (Exception e) {
                    Log.i("rtplayer_tag", e.getMessage());
                }
            });

            t.start();

            try {
                t.join();
            } catch (Exception e) {
                Log.i("rtplayer_tag", e.getMessage());
            }

            RTApplication.getChacheDataBase().createCountryCache(countryCodes);
        }

        if (countryCodes.isEmpty())
            result = false;

        return result;
    }

    public boolean getStations(String country, int offset, int limit) {
        boolean result = true;

        Thread t = new Thread(() -> {
            try {
                if (createRadioBrowser()) {
                    AdvancedSearch advancedSearch = AdvancedSearch.builder()
                            .countryCode(country).hideBroken(true).build();
                    stations = radioBrowser.listStationsWithAdvancedSearch(Paging.at(offset, limit), advancedSearch);
                }
           }
            catch (Exception e) {
                Log.i("rtplayer_tag", e.getMessage());
            }
        });

        t.start();

        try {
            t.join();
        } catch (Exception e) {
            Log.i("rtplayer_tag", e.getMessage());
        }

        if (stations == null || stations.isEmpty())
            result = false;

        return result;
    }
}
