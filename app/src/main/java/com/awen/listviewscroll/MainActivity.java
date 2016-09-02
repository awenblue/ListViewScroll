package com.awen.listviewscroll;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends Activity
        implements ListViewScrollHelper.NextPage, ListViewScrollHelper.InitAdapter {
    private final static int SUCCESS = 0;
    private final static int FAILED = 1;

    ListView listView;
    ListViewScrollHelper helper;
    MyHandler handler;
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new MyHandler();
        listView = (ListView) findViewById(R.id.lv_list);
        helper = new ListViewScrollHelper(listView, this, this);
        try {
            request();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BaseAdapter initAdapter(final List list) {
        BaseAdapter baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int i) {
                return list.get(i);
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                ViewHolder holder;
                if (view == null) {
                    view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_info, null);
                    holder = new ViewHolder();
                    holder.tv = (TextView) view.findViewById(R.id.tv_info);
                    view.setTag(holder);

                } else {
                    holder = (ViewHolder) view.getTag();
                }
                holder.tv.setText((String)list.get(i));
                return view;
            }
        };
        return baseAdapter;
    }

    class ViewHolder {
        TextView tv;
    }

    @Override
    public void next() {
        //添加自己的请求逻辑
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    request();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void request() throws IOException {

        Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(FAILED);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handler.sendEmptyMessage(SUCCESS);
            }
        });
    }

    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    List<String> list = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12","13","14","15");
                    helper.refreshPage(list, 75);
                    break;
                case FAILED:
                    break;
            }
        }
    }
}
