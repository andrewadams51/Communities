package com.aadamsdev.communities.fragments;

/**
 * Created by Andrew Adams on 6/18/2017.
 */

import com.aadamsdev.communities.chat.ChatArrayAdapter;
import com.aadamsdev.communities.chat.ChatClient;
import com.aadamsdev.communities.chat.ChatMessage;
import com.aadamsdev.communities.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class ChatFragment extends Fragment implements View.OnClickListener, ChatClient.ChatClientCallback {

    private final String DEBUG_TAG = "ChatFragment";
    int count = 0;

    private View view;

    private ChatArrayAdapter chatArrayAdapter;

    private ListView chatListView;

    private EditText messageEditText;
    private ImageButton sendMessageButton;

    private String[] menuItems;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private ChatClient chatClient;

    private String currentUsername = null;

    public static ChatFragment newFragment() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            Toast.makeText(getContext(), "Hiding action bar...", Toast.LENGTH_SHORT).show();
        }

        chatClient = ChatClient.getInstance();
        chatClient.setContext(getContext());
        chatClient.connect();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_fragment, container, false);

        setupDrawerSlider(view);

        chatListView = (ListView) view.findViewById(R.id.chat_scrollview);
        chatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        chatArrayAdapter = new ChatArrayAdapter(getContext());
        chatListView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                chatListView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });

        sendMessageButton = (ImageButton) view.findViewById(R.id.send_button);
        sendMessageButton.setOnClickListener(this);

        messageEditText = (EditText) view.findViewById(R.id.message_field);
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    sendMessageButton.setVisibility(View.GONE);
                } else {
                    sendMessageButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        chatClient.registerCallback(this);

        return view;
    }

    @Override
    public void onNewMessage(String username, String message, String timestamp, int userIconId) {
        ChatMessage chatMessage = new ChatMessage(getContext(), username, message + " " + count, timestamp, null);
        ++count;

        chatArrayAdapter.add(chatMessage);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.send_button):
                String message = messageEditText.getText().toString();
                messageEditText.getText().clear();

                Log.i("ChatFragment", message);
                if (currentUsername == null) {
                    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                    currentUsername = sharedPref.getString(getString(R.string.current_username_key), "");
                }
                chatClient.sendMessage(currentUsername, message);
                break;
        }
    }

    private void setupDrawerSlider(View view) {
        menuItems = getResources().getStringArray(R.array.menu_items);
        drawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        drawerList = (ListView) view.findViewById(R.id.left_drawer);

        drawerToggle = new ActionBarDrawerToggle(
                getActivity(),                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                Log.i("ChatFragment", "Drawer closed");
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.i("ChatFragment", "Drawer open");
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.addDrawerListener(drawerToggle);

        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        } catch (NullPointerException ex) {
            Log.i("ChatFragment", ex.toString());
        }


        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<>(getContext(), R.layout.drawer_list_item, menuItems));
        // Set the list's click listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }


}