package diuit.duolc.com.demo.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.duolc.DiuitChat;
import com.duolc.DiuitMessage;
import com.duolc.DiuitMessagingAPI;
import com.duolc.DiuitMessagingAPICallback;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import diuit.duolc.com.demopibot.MainActivity;
import diuit.duolc.com.demopibot.R;

/**
 * Created by zxcvbnius on 4/19/16.
 */
public class ChatRoomListFragment extends Fragment {

    private ListView charListView;
    private ArrayList<DiuitChat> diuitChatArrayList = new ArrayList<>();
    private ChatListAdapter chatListAdapter;

    /**Facebook**/
    LoginButton loginButton;


    private CallbackListener callbackListener;
    public interface CallbackListener
    {
        void entryChatRoom(DiuitChat diuitChat);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle)
    {
        View view = inflater.inflate(R.layout.fragment_chat_list, null);
        this.charListView = (ListView) view.findViewById(R.id.chat_listview);
        this.loginButton = (LoginButton) view.findViewById(R.id.login_button);
        this.loginButton.setPublishPermissions("publish_actions");
        //this.loginButton.setReadPermissions("user_friends","public_profile","user_friends","email");
        this.loginButton.setFragment(this);
        this.loginButton.registerCallback(DemoPi.callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e(MainActivity.TAG, "FBLogin onSuccess");
                Log.e(MainActivity.TAG, "authToken:" + loginResult.getAccessToken().getToken());
                Log.e(MainActivity.TAG, "userId:" + loginResult.getAccessToken().getUserId());
            }
            @Override
            public void onCancel() {Log.e(MainActivity.TAG, "FBLogin onCancel");}

            @Override
            public void onError(FacebookException error) {Log.e(MainActivity.TAG, "FBLogin onError:" + error.getMessage());}
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getActivity());
        DemoPi.callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(DemoPi.callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.e(MainActivity.TAG, "====== onSuccess ======");
                    }

                    @Override
                    public void onCancel() {
                        Log.e(MainActivity.TAG, "====== onCancel ======");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.e(MainActivity.TAG, "====== onError ====== " + exception.getMessage()) ;
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(MainActivity.TAG, "====== onActivityResult ======");
        DemoPi.callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        this.callbackListener = (CallbackListener) this.getActivity();

        DiuitMessagingAPI.listChats(new DiuitMessagingAPICallback<ArrayList<DiuitChat>>()
        {
            @Override
            public void onSuccess(final ArrayList<DiuitChat> diuitChatArrayList)
            {
                if (getActivity() != null)
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            for(DiuitChat diuitChat: diuitChatArrayList) {
                                Log.e(MainActivity.TAG, "" + diuitChat.isBlockedByMe());
                            }
                            Log.e(MainActivity.TAG, "getChatListSuccessfully");
                            ChatRoomListFragment.this.diuitChatArrayList.clear();
                            ChatRoomListFragment.this.diuitChatArrayList.addAll(diuitChatArrayList);
                            chatListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onFailure(final int code, final JSONObject resultObj)
            {
                Log.e(MainActivity.TAG, "getChatListFailed");
                if (getActivity() != null)
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(getActivity(), resultObj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        this.chatListAdapter = new ChatListAdapter( this.getActivity(), this.diuitChatArrayList);
        this.charListView.setAdapter( chatListAdapter );
        this.charListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                DiuitChat diuitChat = diuitChatArrayList.get(position);
                callbackListener.entryChatRoom(diuitChat);
            }
        });
        this.charListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                DiuitChat diuitChat = diuitChatArrayList.get(position);
                return true;
            }
        });
    }

    public void receivingMessage(final DiuitMessage diuitMessage)
    {
        int size = this.diuitChatArrayList.size();
        for(int i = 0 ; i < size ; i++ )
        {
            DiuitChat diuitChat = this.diuitChatArrayList.get(i);
            if( diuitChat.getId() == diuitMessage.getDiuitChat().getId() )
            {
                diuitChat.setLastDiuitMessage(diuitMessage);
                this.chatListAdapter.notifyDataSetChanged();
                /*handle system message*/
                try
                {
                    JSONObject object = new JSONObject( diuitMessage.getData() );
                    String type = object.getString("type");
                    if( type.contains("user.joined") )
                    {
                        diuitChat.addMember(object.getString("userId"));
                    }
                    else if( type.contains("user.left"))
                    {
                        String userSerial = object.getString("userId");
                        diuitChat.removeMember(userSerial);
                    }
                    else if( type.contains("user.kicked") )
                    {
                        String userSerial = object.getString("userId");
                        diuitChat.removeMember(userSerial);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    // -------------------- ADAPTER ------------------- //
    class ChatListAdapter extends ArrayAdapter<DiuitChat>
    {
        public ChatListAdapter(Context context, ArrayList<DiuitChat> diuitChatArrayList)
        {
            super(context, 0, diuitChatArrayList);
        }

        @Override
        public View getView(int position, View convetView, ViewGroup parent)
        {
            DiuitChat diuitChat = this.getItem(position);

            ViewHolder viewHolder;
            if(convetView == null )
            {
                convetView = LayoutInflater.from( getContext() ).inflate(R.layout.item_chatroom, null);
                viewHolder = new ViewHolder();
                viewHolder.chatroomName = (TextView) convetView.findViewById(R.id.chatroom_name_text);
                viewHolder.lastMessage = (TextView) convetView.findViewById(R.id.chatroom_lastmessage_text);
                convetView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convetView.getTag();
            }

            try
            {
                String chatroomName = diuitChat.getMeta().getString("name");
                Log.e(MainActivity.TAG, " chatroomName : " + chatroomName);
                viewHolder.chatroomName.setText( chatroomName );
                DiuitMessage lastDiuitMessage = diuitChat.getLastDiuitMessage();
                if( lastDiuitMessage != null )
                {
                    viewHolder.lastMessage.setText( lastDiuitMessage.getData() );
                }
                else
                {
                    viewHolder.lastMessage.setText( "" );
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            return convetView;
        }

        class ViewHolder
        {
            TextView chatroomName;
            TextView lastMessage;
        }
    }
}
