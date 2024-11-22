package su.rbws.rtplayer.datasource;

import android.net.Uri;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import su.rbws.rtplayer.MetadataExtractor;
import su.rbws.rtplayer.RTApplication;
import su.rbws.rtplayer.SoundItem;

public class SoundSourceManager {

    // отображаемый список файлов
    private final List<SoundItem> viewableList;

    // источники данных
    private final List<SoundSourceAbstract> soundSources;

    public MetadataExtractor metadataExtractor;

    public List<SoundItem> favoriteList = new ArrayList<>();

    public SoundSourceManager() {
        viewableList = new ArrayList<>();

        metadataExtractor = new MetadataExtractor();

        // создание источников данных
        soundSources = new ArrayList<>();
        soundSources.add(new SoundSourceLocalFileSystem());
        soundSources.add(new SoundSourceInternetRadio());
    }

    private SoundItem parentItem;

    public List<SoundItem> getViewableList() {
        return viewableList;
    }

    public List<SoundItem> createViewableRootList() {
        viewableList.clear();

        for (SoundSourceAbstract dataSource : soundSources) {
            List<SoundItem> list = dataSource.createViewableItems(null);
            viewableList.addAll(list);
        }

        parentItem = null;

        return viewableList;
    }

    public List<SoundItem> createViewableFavoritesRadioStationsList() {
        viewableList.clear();

        SoundItem item = new SoundItem();
        item.name = "[ .. ]";
        item.location = "";
        item.state = SoundItem.SourceItemType.fiRadioParentDirectory;
        viewableList.add(item);

        viewableList.addAll(favoriteList);
        return viewableList;
    }

    public boolean createViewableList(SoundItem item) {
        viewableList.clear();

        parentItem = item;

        if (item.state == SoundItem.SourceItemType.fiRadioFavorites) {
            createViewableFavoritesRadioStationsList();
        } else {
            for (SoundSourceAbstract dataSource : soundSources) {
                if (dataSource.isCanProcess(item)) {
                    List<SoundItem> list = dataSource.createViewableItems(item);
                    if (list == null)
                        createViewableRootList();
                    else
                        viewableList.addAll(list);
                    break;
                }
            }
        }
        return !viewableList.isEmpty();
    }

    public void createViewableList(String name) {
        viewableList.clear();

        for (SoundSourceAbstract dataSource : soundSources) {
            if (dataSource.isCanProcess(name)) {
                SoundItem item = dataSource.createDefaultItem(name);
                parentItem = item;
                viewableList.addAll(dataSource.createViewableItems(item));
            }
        }
    }

    public void removeItem(SoundItem item) {
        if (item == null)
            return;

        for (SoundSourceAbstract dataSource : soundSources) {
            if (dataSource.isCanProcess(item)) {
                dataSource.removeItem(item);
            }
        }

        String filename = item.getFullName();

        for (int i = viewableList.size() - 1; i >= 0; --i) {
            if (filename.equals(viewableList.get(i).getFullName())) {
                viewableList.remove(i);
            }
        }
    }

    public void removeItem(String name) {
        for (SoundSourceAbstract dataSource : soundSources) {
            if (dataSource.isCanProcess(name)) {
                SoundItem item = dataSource.createDefaultItem(name);
                dataSource.removeItem(item);
            }
        }

        for (int i = viewableList.size() - 1; i >= 0; --i) {
            if (name.equals(viewableList.get(i).getFullName())) {
                viewableList.remove(i);
            }
        }
    }

    public boolean isFile(String name) {
        boolean result = true;

        Uri u = Uri.parse(name);
        String s = u.getScheme();
        if (s != null && (s.equals("http") || s.equals("https")))
            result = false;

        return result;
    }

    public void appendViewableItems() {
        if (parentItem == null)
            return;

        for (SoundSourceAbstract dataSource : soundSources) {
            if (dataSource.isCanProcess(parentItem)) {
                viewableList.addAll(dataSource.appendViewableItems(parentItem));
            }
        }
    }

    public void changeFavoriteList(SoundItem item) {
        boolean find;
        find = favoriteList.removeIf(i -> (item.name.equals(i.name) &&
                item.location.equals(i.location)));
        if (find) {
            item.checked = false;
            viewableList.removeIf(v -> (item.name.equals(v.name) &&
                    item.location.equals(v.location)));
        }
        else {

            SoundItem newitem = new SoundItem(item);
            newitem.state = SoundItem.SourceItemType.fiRadioFavoriteStation;
            favoriteList.add(newitem);
        }

        String xml = RTApplication.getSoundSourceManager().serializationFavoriteXML();
        RTApplication.getPreferencesData().setFavoritesXML(xml);
    }

    private String loadedSerializationFavoriteXML = "";

    public void deserializationFavoriteXML(String xml) {
        loadedSerializationFavoriteXML = xml;
        favoriteList.clear();

        StringReader reader = new StringReader(xml);
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(reader);
            int eventType = parser.getEventType();
            SoundItem item = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("item")) {
                            item = new SoundItem();
                            item.state = SoundItem.SourceItemType.fiRadioFavoriteStation;

                            String attrName;

                            for (int i = 0; i < parser.getAttributeCount(); ++i) {
                                attrName = parser.getAttributeName(i);
                                if (attrName.equals("name")) {
                                    item.name = parser.getAttributeValue(i);
                                } else
                                if (attrName.equals("location")) {
                                    item.location = parser.getAttributeValue(i);
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("item")) {
                            favoriteList.add(item);
                        }
                        break;
                }
                eventType = parser.next();
            }
        }
        catch (Exception e) { }
    }

    public String serializationFavoriteXML() {
        String result = "";

        StringWriter writer = new StringWriter();

        XmlSerializer serializer = Xml.newSerializer();
        try {
            serializer.setOutput(writer);
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, "root");

            for (SoundItem item : favoriteList) {
                serializer.startTag(null, "item");
                serializer.attribute(null, "name", item.name);
                serializer.attribute(null, "location", item.location);
                serializer.endTag(null, "item");
            }

            serializer.endTag(null,"root");
            serializer.endDocument();
            serializer.flush();

            result = writer.toString();
        }
        catch (IOException e) { }

        return result;
    }

}
