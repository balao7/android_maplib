/*
 * Project:  NextGIS Mobile
 * Purpose:  Mobile GIS for Android.
 * Author:   Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * *****************************************************************************
 * Copyright (c) 2012-2015. NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.maplib.datasource.ngw;

import android.os.Parcel;
import android.os.Parcelable;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LayerWithStyles
        extends Resource
{
    List<Long> mStyles;

    protected LayerWithStyles(Parcel in)
    {
        super(in);
        mStyles = new ArrayList<>();
        int count = in.readInt();
        for(int i = 0; i < count; i++){
            mStyles.add(in.readLong());
        }
    }

    public LayerWithStyles(
            JSONObject data,
            Connection connection)
    {
        super(data, connection);
    }


    public LayerWithStyles(
            long remoteId,
            Connection connection)
    {
        super(remoteId, connection);
    }


    public static final Parcelable.Creator<LayerWithStyles> CREATOR =
            new Parcelable.Creator<LayerWithStyles>()
            {
                public LayerWithStyles createFromParcel(Parcel in)
                {
                    return new LayerWithStyles(in);
                }


                public LayerWithStyles[] newArray(int size)
                {
                    return new LayerWithStyles[size];
                }
            };


    @Override
    public void writeToParcel(
            Parcel parcel,
            int i)
    {
        super.writeToParcel(parcel, i);
        if (null == mStyles)
            parcel.writeInt(0);
        else {
            parcel.writeInt(mStyles.size());
            for (Long style : mStyles) {
                parcel.writeLong(style);
            }
        }
    }


    @Override
    public int getChildrenCount()
    {
        return 0;
    }


    @Override
    public INGWResource getChild(int i)
    {
        return null;
    }


    public void fillStyles()
    {
        mStyles = new ArrayList<>();
        try {
            String sURL = mConnection.getURL() + "/resource/" + mRemoteId + "/child/";
            HttpGet get = new HttpGet(sURL);
            get.setHeader("Cookie", mConnection.getCookie());
            get.setHeader("Accept", "*/*");
            HttpResponse response = mConnection.getHttpClient().execute(get);
            HttpEntity entity = response.getEntity();
            JSONArray children = new JSONArray(EntityUtils.toString(entity));
            for (int i = 0; i < children.length(); i++) {
                //Only store style id
                //To get more style properties need to create style class extended from Resource
                //Style extends Resource
                //mStyles.add(new Style(styleObject, mConnection);
                JSONObject styleObject = children.getJSONObject(i);
                JSONObject JSONResource = styleObject.getJSONObject("resource");
                long remoteId = JSONResource.getLong("id");
                mStyles.add(remoteId);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public int getStyleCount(){
        if(null == mStyles)
            return 0;
        return mStyles.size();
    }

    public long getStyleId(int i){
        return mStyles.get(i);
    }

    public String getTMSUrl(int styleNo)
    {
        return mConnection.getURL() + "/resource/" + mStyles.get(styleNo) + "/tms?z={z}&x={x}&y={y}";
    }

    public String getGeoJSONUrl()
    {
        return mConnection.getURL() + "/resource/" + mRemoteId + "/geojson/";
    }
}