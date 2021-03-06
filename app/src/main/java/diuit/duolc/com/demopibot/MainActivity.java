package diuit.duolc.com.demopibot;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.duolc.DiuitChat;
import com.duolc.DiuitMessage;
import com.duolc.DiuitMessagingAPI;
import com.duolc.DiuitMessagingAPICallback;

import org.json.JSONObject;

import diuit.duolc.com.demo.fragment.ChatRoomPullMoreFragment;
import diuit.duolc.com.demo.fragment.ChatRoomLoadMoreFragment;
import diuit.duolc.com.demo.fragment.ChatRoomListFragment;
import diuit.duolc.com.demo.fragment.ImageFragment;
import diuit.duolc.com.demo.fragment.VideoFragment;

/**
 * Created by zxcvbnius on 4/18/16.
 */
public class MainActivity extends FragmentActivity implements ChatRoomListFragment.CallbackListener, ChatRoomLoadMoreFragment.CallbackListener, ChatRoomPullMoreFragment.CallbackListener
{
    public static final String TAG = "DemoPiPot";
    private ChatRoomListFragment chatRoomListFragment;
    //private ChatRoomFragment chatRoomFragment;
    private ChatRoomPullMoreFragment chatRoomFragment;
    private ImageFragment imageFragment;
    private VideoFragment videoFragment;
    @Override
    public void onCreate(Bundle bundle) {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor( this.getResources().getColor(R.color.grey_title));
        super.onCreate(bundle);
        this.setContentView(R.layout.activity_main);
        this.loginWithAutoToken("DemoPiUser");
    }
    @Override
    protected void onDestroy() {
        DiuitMessagingAPI.unregisterReceivingMessage(chatReceivingCallback);
        DiuitMessagingAPI.disConnect();
        super.onDestroy();
    }

    private void loginWithAutoToken(String authToken) {
        DiuitMessagingAPI.loginWithAuthToken(authToken, new DiuitMessagingAPICallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatRoomListFragment = new ChatRoomListFragment();
                        MainActivity.this.getSupportFragmentManager().beginTransaction().add(R.id.tabcontent, chatRoomListFragment).commit();
                        DiuitMessagingAPI.registerReceivingMessage(chatReceivingCallback);
                    }
                });
            }

            @Override
            public void onFailure(int code, final JSONObject result) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText( MainActivity.this, "Auth failed by :" + result.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private DiuitMessagingAPICallback<DiuitMessage> chatReceivingCallback = new DiuitMessagingAPICallback<DiuitMessage>() {
        @Override
        public void onSuccess(final DiuitMessage diuitMessage) {
            MainActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    chatRoomListFragment.receivingMessage(diuitMessage);

                    if (chatRoomFragment != null && chatRoomFragment.isVisible()) {
                        if (chatRoomFragment.getBindChat().getId() == diuitMessage.getDiuitChat().getId()) {
                            chatRoomFragment.receivingMessage(diuitMessage);
                            Log.e(MainActivity.TAG, "Receiving Message:");
                            Log.e(MainActivity.TAG, "data: " + diuitMessage.getData());
                            Log.e(MainActivity.TAG, "meta: " + diuitMessage.getMeta());
                            if( diuitMessage.getData().contains("If you want to stop the video")) {
                                showVideoFragment();
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void onFailure(int code, JSONObject result) {
            Log.e(TAG, "Receiving Message err: " + result.toString());
        }
    };

    @Override
    public void entryChatRoom(DiuitChat diuitChat)
    {
        this.chatRoomFragment = new ChatRoomPullMoreFragment();
        this.chatRoomFragment.bindChat(diuitChat);
        this.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.in_from_right, R.anim.nothing, R.anim.nothing, R.anim.out_to_right)
                .add( R.id.tabcontent , this.chatRoomFragment, ChatRoomLoadMoreFragment.TAG)
                .hide( this.chatRoomListFragment )
                .show( this.chatRoomFragment )
                .addToBackStack(ChatRoomLoadMoreFragment.TAG)
                .commit();
    }

    @Override
    public void showImageFragment(String url)
    {
        this.imageFragment = new ImageFragment();
        this.imageFragment.bindUrl(url);
        this.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.in_from_right, R.anim.nothing, R.anim.nothing, R.anim.out_to_right)
                .add( R.id.tabcontent , this.imageFragment, ImageFragment.TAG)
                .hide( this.chatRoomFragment )
                .show( this.imageFragment )
                .addToBackStack(ImageFragment.TAG)
                .commit();
    }

    public void showVideoFragment()
    {
        this.videoFragment = new VideoFragment();
        this.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.in_from_right, R.anim.nothing, R.anim.nothing, R.anim.out_to_right)
                .add( R.id.tabcontent , this.videoFragment, VideoFragment.TAG)
                .hide( this.chatRoomFragment )
                .show( this.videoFragment )
                .addToBackStack(VideoFragment.TAG)
                .commit();
    }
}
