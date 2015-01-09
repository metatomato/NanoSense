package gl.iglou.studio.nanosense.MP;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import gl.iglou.studio.nanosense.R;


public class MPFragment extends Fragment implements MPGUIFragment.MPControlCallback, MPInterface {

    public static final String TAG = "MPFragment";

    public static final String MEDIA_PATH = "NANOMADE/Music";

    public static final int STATE_PLAYER_STOPPED = 0;
    public static final int STATE_PLAYER_PLAYING = 1;

    public static final int MP3_DEFAULT_0 = R.raw.bebop;
    public static final int MP3_DEFAULT_1 = R.raw.futur_sound_jazz;
    public static final int MP3_DEFAULT_2 = R.raw.rodrigo_gabriela;

    public int[] mPlaylist;

    private int mCurrentTrack = 0;
    private int mPlayerState = STATE_PLAYER_STOPPED;

    MediaPlayer mMediaPlayer;
    MediaMetadataRetriever mMetaRetriver;

    ImageView ImageViewAlbumArt;
    byte[] art;

    private MPGUIFragment mMPGUIFragment;
    private File rootFile;
    private AssetManager mgr;
    private ArrayList<File> childrenFiles;

    public MPFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlaylist = new int[]{MP3_DEFAULT_0, MP3_DEFAULT_1, MP3_DEFAULT_2};
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkFileSystem(MEDIA_PATH);
        ParseFileSystem();

        mMetaRetriver = new MediaMetadataRetriever();

        mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.bebop);
    }

    public void setMPGUIFrag(MPGUIFragment frag) {
        mMPGUIFragment = frag;
    }

    public void checkFileSystem(String root_path)
    {
        final String PATH = Environment.getExternalStorageDirectory() + "/" + root_path ;
        Log.v(TAG, "Checking App File System at path " + PATH);

        if(null == rootFile){
            rootFile = new File(PATH);
            rootFile.mkdirs();
            Toast.makeText(getActivity(),PATH + " created",Toast.LENGTH_SHORT);
        }
    }

    private void ParseFileSystem() {
        childrenFiles = new ArrayList<File>(Arrays.asList(rootFile.listFiles()));
        for(File f :childrenFiles) {
            Log.v(TAG, "Files found in root : " + f.getName());
        }
    }

    public void updateAlbumArt() {
        Uri fileURI = Uri.parse("android.resource://" + getActivity().getPackageName() +"/"
                +mPlaylist[mCurrentTrack]);

        mMetaRetriver.setDataSource(getActivity(), fileURI);

        art = mMetaRetriver.getEmbeddedPicture();
        Bitmap albumArt = BitmapFactory
                .decodeByteArray(art, 0, art.length);
        mMPGUIFragment.setAlbumArt(albumArt);
    }

    private void getNext() {
        mCurrentTrack++;
        if(mCurrentTrack >= mPlaylist.length) mCurrentTrack = 0;
        initPlayer();
    }

    private void getPrev() {
        mCurrentTrack--;
        if(mCurrentTrack == -1) mCurrentTrack = mPlaylist.length - 1;
        initPlayer();
    }

    private void initPlayer() {
        Uri fileURI = Uri.parse("android.resource://" + getActivity().getPackageName() +"/"
                +mPlaylist[mCurrentTrack]);

        mMediaPlayer.reset();

        try{
            mMediaPlayer.setDataSource(getActivity(),fileURI);
            mMediaPlayer.prepare();
        }
        catch(Exception e){
            Log.e(TAG, "MP Error setting data source", e);
        }
    }

    private void play() {
        updateAlbumArt();
        mMediaPlayer.seekTo(0);
        mMediaPlayer.start();
        mPlayerState = STATE_PLAYER_PLAYING;
    }

    private void stop() {
        mMediaPlayer.pause();
        mPlayerState = STATE_PLAYER_STOPPED;
    }

    public void onPlayClick() {
        if(STATE_PLAYER_STOPPED == mPlayerState) {
            play();
        } else if(STATE_PLAYER_PLAYING == mPlayerState) {
            stop();
        }
    }

    public void onNextClick() {
        getNext();
        play();
    }

    public void onPrevClick() {
        getPrev();
        play();
    }
}
