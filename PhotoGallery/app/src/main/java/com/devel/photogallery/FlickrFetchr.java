package com.devel.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlickrFetchr {

   private static final String TAG = "FlickrFetch";
    private static final String API_KEY = "********************************";
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")      //url-адрес для уменьшения изображения
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + ":with " + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0)
                out.write(buffer, 0 , bytesRead);
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos() {   //int page
        String url = buildUrl(FETCH_RECENTS_METHOD, null);  //, page
        return  downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query) {  //, int page
        String url = buildUrl(SEARCH_METHOD, query);  //, page
        return downloadGalleryItems(url);
    }

   /* public List<GalleryItem> fetchItems(int page){
        List<GalleryItem> items = new ArrayList<>();
        try {
//https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=c5e677f254b6bd2a828097457903e34d&format=json&nojsoncallback=1
            String url = Uri.parse("https://api.flickr.com/services/rest")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")      //url-адрес для уменьшения изображения
                    .appendQueryParameter("page",Integer.toString(page))
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Recieved JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);

            parseItems(items, jsonBody);
            //items = parseGsonItems(jsonBody);  //23.1


        }catch (JSONException je){
            Log.e(TAG, "Failed to parse JSON", je);
        }catch (IOException e){
            Log.e(TAG, "Failed to fetch items", e);
        }

        return items;
    }*/

    public List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Recieved JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        }catch (JSONException je){
            Log.e(TAG, "Failed to parse JSON", je);
        }catch (IOException e){
            Log.e(TAG, "Failed to fetch items", e);
        }

        return items;
    }

    private String buildUrl(String method, String query) {  //, int page
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
               // .appendQueryParameter("page",Integer.toString(page))
                .appendQueryParameter("method", method);
        if (method.equals(SEARCH_METHOD))
            uriBuilder.appendQueryParameter("text", query);

        return uriBuilder.build().toString();

    }

    //-----------------------23.2--------------------------------
    public List<GalleryItem> fetchRecentPhotos(int page) {
        String url = buildUrl(FETCH_RECENTS_METHOD, null, page);
        return  downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query, int page) {
        String url = buildUrl(SEARCH_METHOD, query, page);
        return downloadGalleryItems(url);
    }

    private String buildUrl(String method, String query, int page) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("page",Integer.toString(page))
                .appendQueryParameter("method", method);
        if (method.equals(SEARCH_METHOD))
            uriBuilder.appendQueryParameter("text", query);

        return uriBuilder.build().toString();

    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException{
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s"))
                continue;

            item.setUrl(photoJsonObject.getString("url_s"));
            item.setOwner(photoJsonObject.getString("owner"));
            items.add(item);
        }
    }

    //--------------------------------------------------------------------------------
    private List<GalleryItem> parseGsonItems(JSONObject jsonBody) throws JSONException {
        Gson gson = new GsonBuilder().create();
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
        return Arrays.asList(gson.fromJson(photoJsonArray.toString(), GalleryItem[].class));
    }
}
