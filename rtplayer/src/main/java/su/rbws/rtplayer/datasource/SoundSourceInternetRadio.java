package su.rbws.rtplayer.datasource;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import su.rbws.rtplayer.RTApplication;
import su.rbws.rtplayer.SoundItem;
import su.rbws.rtplayer.NetRadioBrowser;

// источник данных - интернет радио https://github.com/sfuhrm/radiobrowser4j
public class SoundSourceInternetRadio extends SoundSourceAbstract {

    NetRadioBrowser netRadioBrowser = new NetRadioBrowser();

    public SoundSourceInternetRadio() {

    }

    @Override
    public boolean isCanProcess(SoundItem item) {
        boolean result = false;
        switch (item.state) {
            case fiRadioRoot:
            case fiRadioCountry:
            case fiRadioStation:
            case fiRadioParentDirectory:
            case fiRadioStationParentDirectory:
                result = true;
            }

        return result;
    }

    @Override
    public boolean isCanProcess(String name) {
        boolean result = false;

        Uri u = Uri.parse(name);
        String s = u.getScheme();
        if (s != null && (s.equals("http") || s.equals("https")))
            result = true;

        return result;
    }

    @Override
    public SoundItem createDefaultItem(String name) {
        SoundItem result = new SoundItem();
        result.name = name;
        result.state = SoundItem.SourceItemType.fiRadioStation;
        return result;
    }

    private List<SoundItem> createViewableRootItems() {
        List<SoundItem> result = new ArrayList<>();

        SoundItem item;

        item = new SoundItem();
        item.name = "Интернет радио";
        item.location = "radiobrowser4j";
        item.state = SoundItem.SourceItemType.fiRadioRoot;
        result.add(item);

        item = new SoundItem();
        item.name = "Избранные станци";
        item.state = SoundItem.SourceItemType.fiRadioFavorites;
        result.add(item);

        return result;
    }

    @Override
    public List<SoundItem> createViewableItems(SoundItem parentItem) {
        if (parentItem == null)
            return createViewableRootItems();

        switch (parentItem.state) {
            case fiRadioRoot:
            case fiRadioStationParentDirectory:
                return createViewableCountryRadioItems();
            case fiRadioCountry:
                return createViewableStationsRadioItems(parentItem.location);
        }

        List<SoundItem> result = new ArrayList<>();

        return result;
    }

    private static final int currentLimit = 50;

    @Override
    public List<SoundItem> appendViewableItems(SoundItem parentItem) {
        List<SoundItem> result = new ArrayList<>();

        int currentOffset = RTApplication.getSoundSourceManager().getViewableList().size();
        if (netRadioBrowser.getStations(parentItem.location, currentOffset, currentLimit)) {
            result.clear();

            if (netRadioBrowser.stations != null) {
                netRadioBrowser.stations.forEach((s) -> {
                    SoundItem item;
                    item = new SoundItem();
                    item.name = s.getName();
                    item.location = s.getUrlResolved();
                    item.state = SoundItem.SourceItemType.fiRadioStation;
                    result.add(item);
                });
            }
        }

        return result;
    }

    public List<SoundItem> createViewableCountryRadioItems() {
        List<SoundItem> result = new ArrayList<>();

        result.clear();

        if (netRadioBrowser.getCountryList()) {
            netRadioBrowser.countryCodes.forEach((s, k) -> {
                SoundItem item = new SoundItem();
                item.name = netRadioBrowser.getCountry(s);
                item.location = s;
                item.state = SoundItem.SourceItemType.fiRadioCountry;
                result.add(item);
            });

            result.sort(new SoundItem.RecyclerItemCountryNameComparator());
        }

        SoundItem backitem = new SoundItem();
        backitem.name = "[ .. ]";
        backitem.location = "";
        backitem.state = SoundItem.SourceItemType.fiRadioParentDirectory;
        result.add(0, backitem);

        return result;
    }

    private String lastCountry;

    public List<SoundItem> createViewableStationsRadioItems(String country) {
        lastCountry = country;

        List<SoundItem> result = new ArrayList<>();

        if (netRadioBrowser.getStations(country, 0, currentLimit)) {
            result.clear();

            SoundItem backitem = new SoundItem();
            backitem.name = "[ .. ]";
            backitem.location = "";
            backitem.state = SoundItem.SourceItemType.fiRadioStationParentDirectory;
            result.add(backitem);

            if (netRadioBrowser.stations != null) {
                netRadioBrowser.stations.forEach((s) -> {
                    SoundItem item;
                    item = new SoundItem();
                    item.name = s.getName();
                    item.location = s.getUrlResolved();
                    item.state = SoundItem.SourceItemType.fiRadioStation;
                    item.checked = findFavoriteStation(item);
                    result.add(item);
                });
            }
        }

        return result;
    }

    @Override
    public void removeItem(SoundItem item) {

    }

    public boolean findFavoriteStation(SoundItem item) {
        for (SoundItem i : RTApplication.getSoundSourceManager().favoriteList) {
            if (item.name.equals(i.name) &&
                    item.location.equals(i.location)) {
                return true;
            }
        }
        return false;
    }

}
