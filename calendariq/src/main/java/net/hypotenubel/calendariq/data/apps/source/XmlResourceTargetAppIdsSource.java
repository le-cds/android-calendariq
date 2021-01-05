package net.hypotenubel.calendariq.data.apps.source;

import android.app.Application;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.data.apps.model.TargetApps;
import net.hypotenubel.calendariq.util.Utilities;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Knows how to load the list of target apps from our XML resource.
 */
public class XmlResourceTargetAppIdsSource implements ITargetAppIdsSource {

    private static final String LOG_TAG = Utilities.logTag(XmlResourceTargetAppIdsSource.class);

    // Tag and attribute names used in the XML file
    private static final String XML_TAG_TARGET_APPS = "TargetApps";
    private static final String XML_TAG_APP = "App";
    private static final String XML_ATTRIBUTE_ID = "id";

    private final TargetApps targetApps;

    @Inject
    public XmlResourceTargetAppIdsSource(Application application) {
        targetApps = loadTargetApps(application.getResources());
    }

    @Override
    public TargetApps getTargetApps() {
        return targetApps;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // XML Parsing

    private TargetApps loadTargetApps(Resources res) {
        List<String> targetApps = null;

        try (XmlResourceParser xmlParser = res.getXml(R.xml.target_apps)) {
            // Advance to the first start tag
            while (xmlParser.getEventType() != XmlPullParser.START_TAG) {
                xmlParser.next();
            }

            targetApps = parseTargetApps(xmlParser);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception parsing target apps: " + e.getMessage());
        }

        return new TargetApps(targetApps);
    }

    private List<String> parseTargetApps(XmlResourceParser xmlParser) throws IOException, XmlPullParserException {
        List<String> targetApps = new ArrayList<>();

        xmlParser.require(XmlPullParser.START_TAG, null, XML_TAG_TARGET_APPS);
        while (xmlParser.next() != XmlPullParser.END_TAG) {
            targetApps.add(parseTargetApp(xmlParser));
        }
        xmlParser.require(XmlPullParser.END_TAG, null, XML_TAG_TARGET_APPS);

        return targetApps;
    }

    private String parseTargetApp(XmlResourceParser xmlParser) throws IOException, XmlPullParserException {
        xmlParser.require(XmlPullParser.START_TAG, null, XML_TAG_APP);
        String id = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_ID);
        xmlParser.next();
        xmlParser.require(XmlPullParser.END_TAG, null, XML_TAG_APP);

        return id;
    }

}
