package gl.iglou.studio.nanosense.MP;


import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import gl.iglou.studio.nanosense.NanoSenseActivity;
import gl.iglou.studio.nanosense.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MPGUIFragment extends Fragment implements View.OnClickListener {

    Button mBtnPlay;
    Button mBtnNext;
    Button mBtnPrev;
    ImageView ImageViewAlbumArt;

    MPControlCallback mMPControlCallback;

    public MPGUIFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.mp_fragment, container, false);

        mBtnPlay = (Button) rootView.findViewById(R.id.btn_play);
        mBtnNext = (Button) rootView.findViewById(R.id.btn_next);
        mBtnPrev = (Button) rootView.findViewById(R.id.btn_prev);

        ImageViewAlbumArt = (ImageView) rootView.findViewById(R.id.imageView_album_art);

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMPControlCallback = ((NanoSenseActivity)getActivity()).getMPController();

        mMPControlCallback.updateAlbumArt();

        mBtnPlay.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnPrev.setOnClickListener(this);

        ((GradientDrawable)mBtnPlay.getBackground()).setColor(
                getResources().getColor(R.color.color_primary));
        ((GradientDrawable)mBtnNext.getBackground()).setColor(
                getResources().getColor(R.color.color_secondary));
        ((GradientDrawable)mBtnPrev.getBackground()).setColor(
                getResources().getColor(R.color.color_tertiary));
    }


    @Override
    public void onResume() {
        super.onResume();
        updateGUI();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_play:
                mMPControlCallback.onPlayClick();
                updatePlayButtonLabel();
                break;
            case R.id.btn_next:
                mMPControlCallback.onNextClick();
                break;
            case R.id.btn_prev:
                mMPControlCallback.onPrevClick();
                break;
        }
    }

    public void setAlbumArt(Bitmap image) {
        ImageViewAlbumArt.setImageBitmap(image);
    }

    public void updatePlayButtonLabel() {
        int state = mMPControlCallback.getMPState();
        if(state == MPFragment.STATE_PLAYER_PLAYING) {
            mBtnPlay.setText(getResources().getString(R.string.btn_play_stop));
        } else {
            mBtnPlay.setText(getResources().getString(R.string.btn_play));
        }
    }

    public void updateGUI() {
        updatePlayButtonLabel();
        mMPControlCallback.updateAlbumArt();
    }

    public interface MPControlCallback {
        public void onPlayClick();
        public void onNextClick();
        public void onPrevClick();
        public void updateAlbumArt();
        public int getMPState();
    }
}
