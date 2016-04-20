package diuit.duolc.com.demo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import diuit.duolc.com.demopibot.R;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by zxcvbnius on 4/19/16.
 */
public class ImageFragment extends Fragment {

    public static final String TAG = "ImageFragment";
    private PhotoView photoView;
    private String url;
    // image loader
    private DisplayImageOptions imageOptions;
    private ImageLoader imageLoader;
    public void bindUrl(String url) {
        this.url = url;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_image, null);
        this.photoView = (PhotoView) view.findViewById(R.id.photoView);
        return view;
    }
    // --------------------------- SET IMAGE LOADER --------------------------- //
    private void setImageLoader()
    {
        this.imageLoader = ImageLoader.getInstance();
        this.imageLoader.init(ImageLoaderConfiguration.createDefault(this.getActivity()));

        this.imageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
    }

    @Override
    public void onActivityCreated(Bundle bundle)
    {
        super.onActivityCreated(bundle);
        //this.callbackListener = (CallbackListener) this.getActivity();
        this.setImageLoader();
        this.imageLoader.displayImage( this.url, this.photoView, this.imageOptions);
    }
}
