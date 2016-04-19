package diuit.duolc.com.demo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.duolc.DiuitChat;
import com.duolc.DiuitMessage;
import com.duolc.DiuitMessagingAPI;
import com.duolc.DiuitMessagingAPICallback;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import diuit.duolc.com.demopibot.MainActivity;
import diuit.duolc.com.demopibot.R;
import diuit.duolc.com.view.LoadMoreListView;

/**
 * Created by zxcvbnius on 4/19/16.
 */
public class ChatRoomFragment extends Fragment {

    public static final String TAG = "ChatRoomFragment";

    // ui
    private TextView chatroomNameText;
    private Button sendButton;
    private EditText messageEdit;
    private LoadMoreListView messageListView;

    // adapter
    private DiuitChat diuitChat;
    private MessageListAdapter messageListAdapter;
    private ArrayList<DiuitMessage> diuitMessageArrayList = new ArrayList<>();

    // image loader
    private DisplayImageOptions imageOptions;
    private ImageLoader imageLoader;

    /*get messages*/
    private Date before = new Date();
    private int count = 10;
    private int page = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle)
    {
        View view = inflater.inflate(R.layout.fragment_chat, null);
        this.chatroomNameText = (TextView) view.findViewById(R.id.chatroom_name_text);
        this.messageEdit = (EditText) view.findViewById(R.id.message_input);
        this.sendButton = (Button) view.findViewById(R.id.send_button);
        this.messageListView = (LoadMoreListView) view.findViewById(R.id.message_listview);
        this.messageListView.setDivider(null);
        /*set sending button*/
        this.setSendingClick();
        return view;
    }

    private void setSendingClick()
    {
        this.sendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.e(MainActivity.TAG, "onClick");
                String text = messageEdit.getText().toString();
                if (text.isEmpty())
                {
                    Toast.makeText(getActivity(), "Message cannot be empty!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    diuitChat.sendText(text, new DiuitMessagingAPICallback<DiuitMessage>()
                    {
                        @Override
                        public void onSuccess(final DiuitMessage diuitMessage)
                        {
                            Log.e(MainActivity.TAG, "sendingMessageSuccessfully");
                            Log.e(MainActivity.TAG, "message data : " + diuitMessage.getData());
                            Log.e(MainActivity.TAG, "message mime : " + diuitMessage.getMime());
                            Log.e(MainActivity.TAG, "message encode : " + diuitMessage.getEncoding());

                            if (!hasAddThisMessage(diuitMessage))
                            {
                                getActivity().runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        diuitMessageArrayList.add(0, diuitMessage);
                                        messageListAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(final int code, final JSONObject resultObj)
                        {
                            Log.e(MainActivity.TAG, resultObj.toString());
                            getActivity().runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Log.e(MainActivity.TAG, "getSendingMessageFailed");
                                    Toast.makeText(getActivity(), resultObj.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }

                messageEdit.setText("");
                hideSoftKeyboard();

            }
        });
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


    public void bindChat(DiuitChat diuitChat)
    {
        this.diuitChat = diuitChat;
    }
    public DiuitChat getBindChat() { return this.diuitChat; }

    @Override
    public void onActivityCreated(Bundle bundle)
    {
        super.onActivityCreated(bundle);
        this.setImageLoader();

        try
        {
            this.chatroomNameText.setText( this.diuitChat.getMeta().getString("name") );
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        this.messageListAdapter = new MessageListAdapter(this.getActivity(), this.diuitMessageArrayList);
        this.messageListView.setAdapter(this.messageListAdapter);

        /*get messages*/
        this.diuitChat.listMessagesInChat(before, count, page, new DiuitMessagingAPICallback<ArrayList<DiuitMessage>>()
        {
            @Override
            public void onSuccess(final ArrayList<DiuitMessage> diuitMessageArrayList)
            {
                if( getActivity() != null )
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ChatRoomFragment.this.diuitMessageArrayList.clear();
                            ChatRoomFragment.this.diuitMessageArrayList.addAll(diuitMessageArrayList);
                            messageListAdapter.notifyDataSetChanged();
                            messageListView.onLoadMoreComplete();
                        }
                    });
                }
            }

            @Override
            public void onFailure(final int code, final JSONObject resultObj)
            {
                if( getActivity() != null )
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (resultObj != null)
                            {
                                Toast.makeText(getActivity(), "Connect Error", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                if( resultObj != null)
                                    Toast.makeText(getActivity(), resultObj.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        /*set loadMore Listview*/
        this.messageListView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener()
        {
            @Override
            public void onLoadMore()
            {
                page++;
                diuitChat.listMessagesInChat(before, count, page, new DiuitMessagingAPICallback<ArrayList<DiuitMessage>>()
                {
                    @Override
                    public void onSuccess(final ArrayList<DiuitMessage> diuitMessageArrayList)
                    {
                        if( getActivity() != null )
                        {
                            getActivity().runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // ChatRoomFragment.this.messageArrayList.clear();
                                    ChatRoomFragment.this.diuitMessageArrayList.addAll(diuitMessageArrayList);
                                    messageListAdapter.notifyDataSetChanged();
                                    messageListView.onLoadMoreComplete();
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(final int code, final JSONObject resultObj)
                    {
                        if( getActivity() != null )
                        {
                            getActivity().runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (resultObj != null)
                                    {
                                        Toast.makeText(getActivity(), "Connect Error", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        if( resultObj != null)
                                            Toast.makeText(getActivity(), resultObj.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

    }

    public void receivingMessage(final DiuitMessage diuitMessage)
    {
        if( !hasAddThisMessage(diuitMessage) )
        {
            this.messageListAdapter.notifyDataSetChanged();
        }
    }

    private boolean hasAddThisMessage(DiuitMessage diuitMessage)
    {
        synchronized(this)
        {
            boolean alreadyAdded = false;
            int size = this.diuitMessageArrayList.size();
            for(int i = 0 ; i < size ; i++ )
            {
                DiuitMessage msg = this.diuitMessageArrayList.get(i);
                if( msg.getId() == diuitMessage.getId() )
                {
                    alreadyAdded = true;
                    break;
                }
            }
            if(!alreadyAdded)
            {
                this.diuitMessageArrayList.add(0, diuitMessage);
            }
            return alreadyAdded;
        }
    }


    class MessageListAdapter extends ArrayAdapter<DiuitMessage>
    {
        public MessageListAdapter(Context context, ArrayList<DiuitMessage> diuitMessageArrayList)
        {
            super(context, 0, diuitMessageArrayList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            final DiuitMessage diuitMessage = this.getItem(position);

            final ViewHolder viewHolder;
            if(convertView == null )
            {
                convertView = View.inflate( this.getContext(), R.layout.item_message, null);
                viewHolder = new ViewHolder();
                // viewHolder.selfIcon = (ImageView) convertView.findViewById(R.id.message_self_icon);
                // viewHolder.othersIcon = (ImageView) convertView.findViewById(R.id.message_others_icon);
                viewHolder.selfIconText = (TextView) convertView.findViewById(R.id.message_self_icon);
                viewHolder.othersIconText = (TextView) convertView.findViewById(R.id.message_others_icon);
                viewHolder.message_content = (TextView) convertView.findViewById(R.id.message_content);
                viewHolder.message_time = (TextView) convertView.findViewById(R.id.message_time);
                viewHolder.message_content_img = (ImageView) convertView.findViewById(R.id.message_content_img);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if( DiuitMessagingAPI.getCurrentUser() == null )
            {
                return convertView;
            }

            /*message icon*/
            if( diuitMessage.getSenderDiuitUser() == null ) // system message
            {
                viewHolder.selfIconText.setVisibility(View.GONE);
                viewHolder.othersIconText.setVisibility(View.GONE);
                viewHolder.message_content.setBackgroundResource(R.drawable.item_message_system);
            }
            else // message
            {
                if( DiuitMessagingAPI.getCurrentUser().getId() == diuitMessage.getSenderDiuitUser().getId() )
                {
                    viewHolder.selfIconText.setVisibility(View.VISIBLE);
                    //String name = DiuitMessagingAPI.getCurrentUser().getSerial();
                    viewHolder.selfIconText.setText("");
                    int resourceId = R.mipmap.user;//DiuitMessagingAPI.getCurrentUser().getSerial();
                    viewHolder.selfIconText.setBackgroundResource(resourceId);
                    viewHolder.othersIconText.setVisibility(View.GONE);
                    viewHolder.message_content.setBackgroundResource(R.drawable.item_message_sending);
                }
                else
                {
                    viewHolder.selfIconText.setVisibility(View.GONE);
                    viewHolder.othersIconText.setVisibility(View.VISIBLE);
                    //String name = DiuitMessagingAPI.getCurrentUser().getSerial();
                    viewHolder.othersIconText.setText("");
                    int resourceId = R.mipmap.pi;//DiuitMessagingAPI.getCurrentUser().getSerial();
                    viewHolder.othersIconText.setBackgroundResource(resourceId);
                    viewHolder.message_content.setBackgroundResource(R.drawable.item_message_receving);
                }
            }

            /*message content*/
            if( MimeType.isSystemByMimeType(diuitMessage.getMime()) )
            {
                viewHolder.message_content.setText(diuitMessage.getData());
                viewHolder.message_content.setVisibility(View.VISIBLE);
                viewHolder.message_content_img.setVisibility(View.GONE);
                viewHolder.message_content_img.setImageBitmap(null);
                viewHolder.message_content.setAutoLinkMask(0);
            }
            else if( MimeType.isTextByMimeType(diuitMessage.getMime()) )
            {
                viewHolder.message_content.setText( diuitMessage.getData() );
                viewHolder.message_content.setVisibility(View.VISIBLE);
                viewHolder.message_content_img.setVisibility(View.GONE);
                viewHolder.message_content_img.setImageBitmap(null);
                viewHolder.message_content.setAutoLinkMask(Linkify.ALL);
            }
            else if(  MimeType.isImageByMimeType(diuitMessage.getMime()) )
            {
                viewHolder.message_content.setVisibility(View.GONE);
                viewHolder.message_content_img.setVisibility(View.VISIBLE);
                imageLoader.displayImage(diuitMessage.getData(), viewHolder.message_content_img, imageOptions);
            }

            /*message time*/
            Date createdAt = diuitMessage.getCreatedAt();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            viewHolder.message_time.setText(  getDate( sdf.format(createdAt) ) );

            return convertView;
        }

        private String getDate(String dateString)
        {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date value = null;
            try
            {
                value = formatter.parse(dateString);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormatter.setTimeZone(TimeZone.getDefault());
            String dt = dateFormatter.format(value);

            return dt;
        }

        class ViewHolder
        {
            // ImageView selfIcon;
            // ImageView othersIcon;
            TextView selfIconText;
            TextView othersIconText;
            TextView message_content;
            TextView message_time;
            ImageView message_content_img;
        }
    }


    private void hideSoftKeyboard()
    {
        View view = this.getActivity().getCurrentFocus();
        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
