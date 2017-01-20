package com.devel.photogallery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.MenuItemHoverListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends VisibleFragment {

    private static final String TAG = "PhotoGalleryFragment";
    private static final int COLUMN_WIDTH = 100;

    private int lastFetchedPage = 1;
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private MenuItem mSearchItem;
    private ProgressDialog mProgressDialog;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();

        /*Intent i = PollService.newIntent(getActivity());
        getActivity().startService(i);*/

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>(){
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                        if (!isAdded())
                            return;
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        photoHolder.bindDrawable(drawable);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final int standardColumns = 3;

        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), standardColumns));
    //23.3---------------------------------------------------------------------------
      /*  mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Point size = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getSize(size);
                int newColumns = (int) Math.floor(size.x*3/COLUMN_WIDTH);
                if (newColumns != standardColumns) {
                    GridLayoutManager layoutManager = (GridLayoutManager)mPhotoRecyclerView.getLayoutManager();
                    layoutManager.setSpanCount(newColumns);
                }
            }
        });*/

        mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int spanCount = convertPxToDp(mPhotoRecyclerView.getWidth()) / COLUMN_WIDTH;
                        GridLayoutManager layoutManager = (GridLayoutManager)mPhotoRecyclerView.getLayoutManager();
                        layoutManager.setSpanCount(spanCount);
                    }
                });

    //23.2---------------------------------------------------------------------------
    /*    mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
           @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                PhotoAdapter adapter = (PhotoAdapter)recyclerView.getAdapter();
                int lastPosition = adapter.getLastBoundPosition();
                GridLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
                int loadBufferPosition = 1;
                if (lastPosition >= adapter.getItemCount() - layoutManager.getSpanCount()- loadBufferPosition) {
                   // аналог updateItems()
                    new FetchItemsTask(QueryPreferences.getStoredQuery(getActivity()))
                            .execute(lastPosition++);
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });*/

        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView,dx,dy);
                if (!recyclerView.canScrollVertically(1)) {
                    //lastFetchedPage++;

                    PhotoAdapter adapter = (PhotoAdapter)recyclerView.getAdapter();
                    mPhotoRecyclerView.scrollToPosition(adapter.getLastBoundPosition());
                    //updateItems();
                    new FetchItemsTask(QueryPreferences.getStoredQuery(getActivity()))
                            .execute(lastFetchedPage++);
                    //QueryPreferences.setFetchedPage(getActivity(),lastFetchedPage);
                }
            }
        });

        setupAdapter();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
        mProgressDialog.dismiss();
    }

    private int convertPxToDp(float sizeInPx) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return (int) (sizeInPx / displayMetrics.density);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);

        mSearchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) mSearchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);
                QueryPreferences.setStoredQuery(getActivity(), query);
                collapseSearchView(); //25
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {  //вызывается при каждом изменени поисковой строки
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity()))
            toggleItem.setTitle(R.string.stop_polling);
        else
            toggleItem.setTitle(R.string.start_polling);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return  true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();        //обновление меню
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
//--------------------------------------------25--------------------------------
    private void collapseSearchView() {
        mSearchItem.collapseActionView();  // collapse the action view
        // hide the soft keyboard
        View view = getActivity().getCurrentFocus();
        if (view != null){
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }


    private void setupAdapter() {
        if (isAdded())
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView){
            super(itemView);
            //mTitleTextView = (TextView) itemView;
            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable){
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View v) {
           // Intent i = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoPagerUri());
            Intent i = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPagerUri());
            startActivity(i);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;
        private int lastBoundPosition;

        public int getLastBoundPosition() {
            return lastBoundPosition;
        }

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            /*TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);*/
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);

            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            photoHolder.bindGalleryItem(galleryItem);
            Drawable placeholder = getResources().getDrawable(android.R.drawable.ic_menu_add); //R.drawable.bill_up_close
            photoHolder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getUrl());

            lastBoundPosition = position;
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
        /*    try {
                String result = new FlickrFetchr().getUrlString("https://www,bignerdranch.com");
                Log.i(TAG, "Fetched contents of URL: " + result);
            }catch (IOException e){
                Log.e(TAG, "Failed to fetch URL: ", e);
            }
            return null;*/

            //return new FlickrFetchr().fetchItems(lastFetchedPage);

            if (mQuery == null){
                return new FlickrFetchr().fetchRecentPhotos(lastFetchedPage);  //
            }
            else {
                return new FlickrFetchr().searchPhotos(mQuery, lastFetchedPage);  //
            }
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("downloading...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }

        //onPostExecute(..) is run after doInBackground(..) and it runs on the main thread
        @Override
        protected void onPostExecute(List<GalleryItem> items) {
         //   mItems = items;
         //   setupAdapter();
        //----------23.2----------------------------
            if (lastFetchedPage > 1){
                mItems.addAll(items);
                mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
            }
            else{
                mItems = items;
                setupAdapter();
            }

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }
}
