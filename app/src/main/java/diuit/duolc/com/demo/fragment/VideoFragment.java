package diuit.duolc.com.demo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import diuit.duolc.com.demopibot.R;

/**
 * Created by zxcvbnius on 4/19/16.
 */
public class VideoFragment extends Fragment {

    public static final String TAG = "VideoFragment";
    //private VideoView videoView;
    private WebView webView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_video, null);
        //this.videoView = (VideoView) view.findViewById(R.id.videoView);
        this.webView = (WebView) view.findViewById(R.id.webView);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle bundle)
    {
        super.onActivityCreated(bundle);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setBuiltInZoomControls(false);
        this.webView.getSettings().setSupportZoom(false);
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "      <meta charset=\"utf-8\">\n" +
                "      <title>Hello Bot</title>\n" +
                "      <style media=\"screen\">\n" +
                "      html, body { height: 100%; text-align: center; }\n" +
                "      html { display: table; margin: auto; }\n" +
                "      body { display: table-cell; vertical-align: middle;}\n" +
                "      </style>\n" +
                "</head>\n" +
                "<body bgcolor=\"#000000\">\n" +
                "    <img style=\"max-width: 100%;-webkit-user-select:none;\" src=\"http://54.153.28.72:3012/?action=stream\">\n" +
                "</body>\n" +
                "</html>\n";
        this.webView.loadData( html,"text/html", "utf-8");
        //this.videoView.setVideoURI(Uri.parse(DemoPi.videoUrl));
        //this.videoView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        /*
        if(this.videoView.isPlaying()) {
            Log.e(MainActivity.TAG, "Video is playing");
            this.videoView.stopPlayback();
        }
        */
    }
}
