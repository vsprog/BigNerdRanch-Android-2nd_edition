package com.devel.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

public class PhotoPageActivity extends SingleFragmentActivity {

    private PhotoPageFragment mFragment;

    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        //return PhotoPageFragment.newInstance(getIntent().getData());
        mFragment = PhotoPageFragment.newInstance(getIntent().getData());
        return mFragment;
    }
    //--------------------------------------------------28.1
    @Override
    public void onBackPressed() {
        if (mFragment.onBackPressed())
           return;
        else
            super.onBackPressed();
    }
}
