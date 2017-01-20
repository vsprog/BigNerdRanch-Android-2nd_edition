package com.devel.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class ImageViewFragment extends DialogFragment {

    public final static String ARG_PATH = "com.devel.criminalintent.ARG_PATH";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity());
        String path = (String) getArguments().getSerializable(ARG_PATH);
        final View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_image_view, null);
        final ImageView imageView = (ImageView) view.findViewById(R.id.image_view_dialog);

        final Bitmap bitmap = PictureUtils.getScaledBitmap(path, getActivity());
        imageView.setImageBitmap(bitmap);

        dialog.setContentView(imageView);
        return dialog;
    }

    public static ImageViewFragment newInstance(String path) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PATH, path);

        ImageViewFragment fragment = new ImageViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
