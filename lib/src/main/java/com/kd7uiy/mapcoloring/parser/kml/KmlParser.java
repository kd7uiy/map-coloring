package com.kd7uiy.mapcoloring.parser.kml;

import android.util.Log;
import android.util.Xml;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class KmlParser {

    private static final String TAG = KmlParser.class.getName();

    private final ParserTask parserTask;

    private int parserProgress = 0;

    public KmlParser(ParserTask parserTask) {
        this.parserTask = parserTask;
    }

    public ArrayList<Country> parse(InputStream in) throws IOException,
            XmlPullParserException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return parseCountries(parser);
        } finally {
            in.close();
        }
    }

    private ArrayList<Country> parseCountries(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        ArrayList<Country> countries = new ArrayList<Country>();

        Log.d(TAG, parser.getName());
        parser.require(XmlPullParser.START_TAG, null, "kml");
        parser.next();

        Log.d(TAG, parser.getName());
        parser.require(XmlPullParser.START_TAG, null, "Document");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Placemark")) {
                countries.add(parseCountry(parser));

                parserTask.publishProgress(parserProgress++);
            } else {
                skip(parser);
            }
        }

        return countries;
    }

    private Country parseCountry(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "Placemark");

        Country country = new Country();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                country.name = parseName(parser);

                Log.d(TAG, country.name);
            } else if (name.equals("MultiGeometry")) {
                country.borders = parseBorder(parser);
            } else {
                skip(parser);
            }
        }

        parser.require(XmlPullParser.END_TAG, null, "Placemark");

        return country;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException,
            IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;

                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private String parseName(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "name");
        String name = parseText(parser);
        parser.require(XmlPullParser.END_TAG, null, "name");
        return name;
    }

    private List<List<LatLng>> parseBorder(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "MultiGeometry");
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, "Point");
        skip(parser);

        List<List<LatLng>> borders = new ArrayList<List<LatLng>>();
        while (parser.next() == XmlPullParser.START_TAG
                && "Polygon".equals(parser.getName())) {
            List<LatLng> border = parsePolygon(parser);
            borders.add(border);
        }
        parser.require(XmlPullParser.END_TAG, null, "MultiGeometry");

        return borders;
    }

    private List<LatLng> parsePolygon(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "Polygon");
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, "outerBoundaryIs");
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, "LinearRing");
        parser.next();
        List<LatLng> border = parseCoordinates(parser);
        parser.next();
        parser.require(XmlPullParser.END_TAG, null, "LinearRing");
        parser.next();
        parser.require(XmlPullParser.END_TAG, null, "outerBoundaryIs");

        while (parser.next() != XmlPullParser.END_TAG
                || !"Polygon".equals(parser.getName())) {
        }
        parser.require(XmlPullParser.END_TAG, null, "Polygon");

        return border;
    }

    private List<LatLng> parseCoordinates(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "coordinates");
        String borderStr = parseText(parser);
        parser.require(XmlPullParser.END_TAG, null, "coordinates");
        List<LatLng> border = new ArrayList<LatLng>();
        String[] coordsStrs = borderStr.split(" ");
        for (String coordsStr : coordsStrs) {
            String[] coords = coordsStr.split(",");
            double longitude = Double.parseDouble(coords[0]);
            double latitude = Double.parseDouble(coords[1]);
            LatLng point = new LatLng(latitude, longitude);
            border.add(point);
        }

        return border;
    }

    private String parseText(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        String result = null;
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

}
