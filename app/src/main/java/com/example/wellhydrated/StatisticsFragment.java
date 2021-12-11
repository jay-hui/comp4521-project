package com.example.wellhydrated;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SQLiteDatabase dbReadable;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatisticsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatisticsFragment newInstance(String param1, String param2) {
        StatisticsFragment fragment = new StatisticsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        dbReadable = ((MainActivity)getActivity()).dbHelper.getReadableDatabase();
        updateGraph(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    // SQL Queries:
    // TODO: What if the user skips WellHydrated for a few days? My opinion is to fill it up with amount=0 records, during onStart() of the app

    //===================================Past 7 Days==================================================
    //SELECT drink_date, SUM(amount) AS total_amount
    //FROM wellhydrated_records
    //WHERE drink_date BETWEEN (SELECT date("now", "-6 day")) AND (SELECT date("now"))
    //GROUP BY drink_date
    //ORDER BY drink_date
    //================================================================================================

    //===================================Past 30 Days=================================================
    //SELECT drink_date, SUM(amount) AS total_amount
    //FROM wellhydrated_records
    //WHERE drink_date BETWEEN (SELECT date("now", "-29 day")) AND (SELECT date("now"))
    //GROUP BY drink_date
    //ORDER BY drink_date
    //================================================================================================

    //===================================Past 12 Months===============================================
    //SELECT strftime("%Y-%m", drink_date) AS ym, avg(total_amount)
    //FROM (SELECT drink_date, SUM(amount) AS total_amount
    //      FROM wellhydrated_records
    //      WHERE drink_date BETWEEN (SELECT date("now", "-11 month", "start of month")) AND (SELECT date("now"))
    //      GROUP BY drink_date)
    //GROUP BY ym
    //ORDER BY ym
    //================================================================================================

    public void updateGraph(int viewMode) {
        String query;

        // Pick the right SQL query first
        if (viewMode == 0) {
            // Daily amount drunk in the past 7 days
            query = "SELECT drink_date, SUM(amount) AS total_amount " +
                    "FROM wellhydrated_records " +
                    "WHERE drink_date BETWEEN (SELECT date('now', '-6 day')) AND (SELECT date('now')) " +
                    "GROUP BY drink_date " +
                    "ORDER BY drink_date";

        } else if (viewMode == 1) {
            // Daily amount drunk in the past 30 days
            query = "SELECT drink_date, SUM(amount) AS total_amount " +
                    "FROM wellhydrated_records " +
                    "WHERE drink_date BETWEEN (SELECT date('now', '-29 day')) AND (SELECT date('now')) " +
                    "GROUP BY drink_date " +
                    "ORDER BY drink_date";

        } else if (viewMode == 2) {
            // Average of daily amount for each months in the past 12 months
            query = "SELECT strftime('%Y-%m', drink_date) AS ym, avg(total_amount) " +
                    "FROM (SELECT drink_date, SUM(amount) AS total_amount " +
                    "      FROM wellhydrated_records" +
                    "      WHERE drink_date BETWEEN (SELECT date('now', '-11 month', 'start of month')) AND (SELECT date('now')) " +
                    "      GROUP BY drink_date) " +
                    "GROUP BY ym " +
                    "ORDER BY ym";
        } else
            // Invalid viewMode
            return;

        Cursor cursor = dbReadable.rawQuery(query, null);
        int cursorSize = cursor.getCount();

        if (cursorSize == 0) {
            // TODO: Handle when there is no data retrieved from the query
        }

        // Assumed cursorSize != 0
        DataPoint[] dataPoints = new DataPoint[cursorSize];
        String[] xLabels = new String[cursorSize];

        int x = 0;
        while (cursor.moveToNext()) {
            String label = cursor.getString(0);
            xLabels[x] = label;

            int y = cursor.getInt(1);
            dataPoints[x] = new DataPoint(x, y);

            x++;
        }
        cursor.close();

        GraphView graph = (GraphView) getView().findViewById(R.id.statsGraph);
        // Remove the previous graph, if any (?)
        graph.removeAllSeries();

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
        graph.addSeries(series);

        // Use static labels for horizontal (and vertical) labels
        //StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        //staticLabelsFormatter.setHorizontalLabels(xLabels);
        //graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        int haha = getId();//2131231023
        Log.d("TAGGGGGGGGGGG", String.valueOf(haha));
    }


}