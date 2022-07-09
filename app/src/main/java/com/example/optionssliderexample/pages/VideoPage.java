package com.example.optionssliderexample.pages;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.optionssliderexample.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.sgordon4.slideview.SlideView;

public class VideoPage extends Fragment {

    String mediaName = "sample_mp4.mp4";
    ExoPlayer exoPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.videopage, container, false);

        StyledPlayerView playerView = view.findViewById(R.id.video_view);

        exoPlayer = new ExoPlayer.Builder(getContext()).build();
        playerView.setPlayer(exoPlayer);

        Uri mediaUri = Uri.parse("file:///android_asset/"+mediaName);
        MediaItem mediaItem = MediaItem.fromUri(mediaUri);
        exoPlayer.addMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();


        SlideView slideView = view.findViewById(R.id.slide_view);
        slideView.setSliderAutoAssessHeight(false);
        slideView.setSliderLipHeight(0);


        return view;
    }
}
