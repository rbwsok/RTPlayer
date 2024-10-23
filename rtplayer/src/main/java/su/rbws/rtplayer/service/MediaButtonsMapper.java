package su.rbws.rtplayer.service;

import android.util.Xml;
import android.view.KeyEvent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import su.rbws.rtplayer.Utils;

public class MediaButtonsMapper {

    public enum MediaButtonActionType {mbaNone, mbaNext, mbaPrev, mbaPlayPause, mbaMute}

    public static class MediaButton {
        public int keyCode = 0;
        public MediaButtonActionType action = MediaButtonActionType.mbaNone;
    }

    public static class MediaButtonAction {
        public MediaButtonAction(int code, MediaButtonActionType type, String description) {
            this.description = description;
            this.action = type;
            this.code = code;
        }
        public int code;
        public String description;
        public MediaButtonActionType action = MediaButtonActionType.mbaNone;
    }

    // список соответствий кнопок и действий
    public ArrayList<MediaButton> mediaButtons = new ArrayList<>();

    // список действий (в толм же порядке что и MediaButtonAction)
    public ArrayList<MediaButtonAction> mediaButtonActions = new ArrayList<>();

    public MediaButtonsMapper() {
        setDefault();

        mediaButtonActions.clear();

        MediaButtonAction action;
        action = new MediaButtonAction(0, MediaButtonActionType.mbaNone, "None");
        mediaButtonActions.add(action);
        action = new MediaButtonAction(1, MediaButtonActionType.mbaNext, "Next");
        mediaButtonActions.add(action);
        action = new MediaButtonAction(2, MediaButtonActionType.mbaPrev, "Previous");
        mediaButtonActions.add(action);
        action = new MediaButtonAction(3, MediaButtonActionType.mbaPlayPause, "Play/Pause");
        mediaButtonActions.add(action);
        action = new MediaButtonAction(4, MediaButtonActionType.mbaMute, "Mute");
        mediaButtonActions.add(action);

        if (!loadedSerializationXML.isEmpty()) {
            deserialization(loadedSerializationXML);
            loadedSerializationXML = "";
        }
    }

    public MediaButtonAction getAction(MediaButtonActionType action) {
        MediaButtonAction result = mediaButtonActions.get(0);

        for (MediaButtonAction actionitem : mediaButtonActions) {
            if (actionitem.action == action) {
                result = actionitem;
                break;
            }
        }

        return result;
    }

    public MediaButtonAction getAction(int actioncode) {
        MediaButtonAction result = mediaButtonActions.get(0);

        for (MediaButtonAction actionitem : mediaButtonActions) {
            if (actionitem.code == actioncode) {
                result = actionitem;
                break;
            }
        }

        return result;
    }

    public MediaButton newButton() {
        return new MediaButton();
    }

    public void setDefault() {
        mediaButtons.clear();

        MediaButton button;

        button = new MediaButton();
        button.keyCode = KeyEvent.KEYCODE_MEDIA_NEXT;
        button.action = MediaButtonActionType.mbaNext;
        mediaButtons.add(button);

        button = new MediaButton();
        button.keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
        button.action = MediaButtonActionType.mbaPrev;
        mediaButtons.add(button);

        button = new MediaButton();
        button.keyCode = KeyEvent.KEYCODE_VIDEO_APP_4;
        button.action = MediaButtonActionType.mbaPlayPause;
        mediaButtons.add(button);
    }

    public String serialization() {
        String result = "";

        StringWriter writer = new StringWriter();

        XmlSerializer serializer = Xml.newSerializer();
        try {
            serializer.setOutput(writer);
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, "root");

            for (MediaButton button : mediaButtons) {
                serializer.startTag(null, "button");
                serializer.attribute(null, "keyCode", Integer.toString(button.keyCode));
                serializer.attribute(null, "action", Integer.toString(getAction(button.action).code));
                serializer.endTag(null, "button");
            }

            serializer.endTag(null,"root");
            serializer.endDocument();
            serializer.flush();

            result = writer.toString();
        }
        catch (IOException e) { }

        return result;
    }

    public void deserialization(String xml) {
        mediaButtons.clear();

        StringReader reader = new StringReader(xml);
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(reader);
            int eventType = parser.getEventType();
            MediaButton button = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("button")) {
                            button = new MediaButton();

                            String attrName, attrValue;

                            for (int i = 0; i < parser.getAttributeCount(); ++i) {
                                attrName = parser.getAttributeName(i);
                                if (attrName.equals("keyCode")) {
                                    attrValue = parser.getAttributeValue(i);
                                    button.keyCode = Utils.parseInt(attrValue);
                                } else
                                if (attrName.equals("action")) {
                                    attrValue = parser.getAttributeValue(i);
                                    int actioncode = Utils.parseInt(attrValue);
                                    button.action = getAction(actioncode).action;
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("button")) {
                            mediaButtons.add(button);
                        }
                        break;
                }
                eventType = parser.next();
            }
        }
        catch (Exception e) { }
        //catch (XmlPullParserException e) { }
    }

    public static String loadedSerializationXML = "";

}