package com.bigdata.carpark;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.bigdata.carpark.model.CarPark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rach on 25/4/2018.
 */

public class SelectCarParkActivity extends AppCompatActivity implements LoadCarParkJSON.Listener, AdapterView.OnItemClickListener {

    private ListView mListView;

    public static final String URL = "http://demo6823195.mockable.io/";

    private List<HashMap<String, String>> mAndroidMapList = new ArrayList<>();

    private static final String KEY_NUM = "car_park_no";
    private static final String KEY_ADDR = "address";
    private static final String KEY_TYPE = "car_park_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_car_park);

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        new LoadCarParkJSON(this).execute(URL);
    }

    @Override
    public void onLoaded(List<CarPark> androidList) {

        for (CarPark android : androidList) {

            HashMap<String, String> map = new HashMap<>();
            map.put(KEY_NUM, android.getCar_park_no());
            map.put(KEY_ADDR, android.getAddress());
            map.put(KEY_TYPE, android.getCar_park_type());
            mAndroidMapList.add(map);
        }

        loadListView();
    }

    @Override
    public void onError() {

        Toast.makeText(this, "Error !", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Toast.makeText(this, mAndroidMapList.get(i).get(KEY_ADDR),Toast.LENGTH_LONG).show();
        //TODO Pass selected car park to main activity
        /*Intent intent = new Intent(SelectCarParkActivity.this, MainActivity.class);
        intent.putExtra("message", mAndroidMapList.get(i).get(KEY_ADDR));
        startActivity(intent);*/
    }

    private void loadListView() {

        ListAdapter adapter = new SimpleAdapter(SelectCarParkActivity.this, mAndroidMapList, R.layout.list_car_park,
                new String[] {KEY_NUM, KEY_ADDR, KEY_TYPE},
                new int[] { R.id.car_park_num,R.id.car_park_addr, R.id.car_park_type });

        mListView.setAdapter(adapter);

    }
}

