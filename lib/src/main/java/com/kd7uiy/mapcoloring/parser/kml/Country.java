package com.kd7uiy.mapcoloring.parser.kml;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class Country implements Parcelable {

    public static final Parcelable.Creator<Country> CREATOR = new Creator<Country>() {

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }

        @Override
        public Country createFromParcel(Parcel source) {
            Country country = new Country();
            country.name = source.readString();

            int borderCount = source.readInt();
            country.borders = new ArrayList<List<Point>>();

            for (int i = 0; i < borderCount; ++i) {
                @SuppressWarnings("unchecked")
                List<Point> border = source.readArrayList(Point.class
                        .getClassLoader());
                country.borders.add(border);
            }
            return country;
        }
    };

    public String name;

    public List<List<Point>> borders;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(borders.size());

        for (List<Point> border : borders) {
            dest.writeList(border);
        }
    }

}
