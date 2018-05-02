package com.bigdata.carpark;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.bigdata.carpark.model.CarPark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static com.bigdata.carpark.MainActivity.minteger;

/**
 * Created by Rach on 25/4/2018.
 */

public class SelectCarParkActivity extends AppCompatActivity implements LoadCarParkJSON.Listener, AdapterView.OnItemClickListener {

    private ListView mListView;

    //public static final String URL = "http://demo6823195.mockable.io/";
    public static final String URL = "http://demo1273850.mockable.io/";

    public static List<HashMap<String, String>> mAndroidMapList = new ArrayList<>();


    private static final String KEY_NUM = "car_park_no";
    private static final String KEY_ADDR = "address";
    private static final String KEY_TYPE = "car_park_type";
    private static final String KEY_X_COORD = "x_coord";
    private static final String KEY_Y_COORD = "y_coord";

    private AppCompatActivity mainActivity;

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
            map.put(KEY_X_COORD, android.getX_coord());
            map.put(KEY_Y_COORD, android.getY_coord());
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

        Toast.makeText(this, mAndroidMapList.get(i).get(KEY_ADDR), Toast.LENGTH_LONG).show();
        SocketThread socketThread = new SocketThread(i);
        socketThread.start();
        MainActivity.position = i;

    }


    class SocketThread extends Thread {
        int position;

        public SocketThread(int position) {
            this.position = position;
        }

        public void run() {
            Socket socket = null;
            OutputStream output = null;
            try {
                socket = new Socket("192.168.43.187", 15000);
                output = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            PrintWriter writer = new PrintWriter(output, true);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, minteger);

            String time = calendar.get(Calendar.HOUR) + "" + String.format("%02d", calendar.get(Calendar.MINUTE)) + "" + String.format("%02d", calendar.get(Calendar.SECOND));
            writer.println(mAndroidMapList.get(position).get(KEY_NUM) + "," + time);
            InputStream is = null;
            String predictedAvailability = null;
            MainActivity.carParkName = mAndroidMapList.get(position).get(KEY_ADDR);
            try {
                is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr);
                predictedAvailability = reader.readLine();
                MainActivity.availability = predictedAvailability;
                socket.close();
                SelectCarParkActivity.this.finish();

            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("PREDICTION", predictedAvailability);
            // Toast.makeText(SelectCarParkActivity.this,predictedAvailability,Toast.LENGTH_SHORT).show();

        }
    }

    private void loadListView() {

        ListAdapter adapter = new SimpleAdapter(SelectCarParkActivity.this, mAndroidMapList, R.layout.list_car_park,
                new String[]{KEY_NUM, KEY_ADDR, KEY_TYPE},
                new int[]{R.id.car_park_num, R.id.car_park_addr, R.id.car_park_type});

        mListView.setAdapter(adapter);

    }
}

