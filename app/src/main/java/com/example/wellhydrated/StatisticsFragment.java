package com.example.wellhydrated;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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

        Spinner spinner = (Spinner) ((MainActivity)getActivity()).findViewById(R.id.spinnerGraphTypes);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.stats_graph_types_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateGraph((int) id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    public void updateGraph(int viewMode) {
        String query;
        // SQL Queries:

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

        // Pick the right SQL query first
        if (viewMode == 0) {
            // Daily amount drunk in the past 7 days
            query = "SELECT " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + ", SUM(" + WellHydratedDBEntries.COLUMN_NAME_AMOUNT + ") AS total_amount " +
                    "FROM " + WellHydratedDBEntries.TABLE_NAME + " " +
                    "WHERE " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " BETWEEN (SELECT date('now', 'localtime', '-6 day')) AND (SELECT date('now', 'localtime')) " +
                    "GROUP BY " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " " +
                    "ORDER BY " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE;

        } else if (viewMode == 1) {
            // Daily amount drunk in the past 30 days
            query = "SELECT " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + ", SUM(" + WellHydratedDBEntries.COLUMN_NAME_AMOUNT + ") AS total_amount " +
                    "FROM " + WellHydratedDBEntries.TABLE_NAME + " " +
                    "WHERE " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " BETWEEN (SELECT date('now', 'localtime', '-29 day')) AND (SELECT date('now', 'localtime')) " +
                    "GROUP BY " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " " +
                    "ORDER BY " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE;

        } else if (viewMode == 2) {
            // Average of daily amount for each months in the past 12 months
            query = "SELECT strftime('%Y-%m', "+ WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + ") AS ym, avg(total_amount) " +
                    "FROM (SELECT " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + ", SUM(" + WellHydratedDBEntries.COLUMN_NAME_AMOUNT + ") AS total_amount " +
                    "      FROM " + WellHydratedDBEntries.TABLE_NAME +
                    "      WHERE " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " BETWEEN (SELECT date('now', 'localtime', '-11 month', 'start of month')) AND (SELECT date('now', 'localtime')) " +
                    "      GROUP BY " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + ") " +
                    "GROUP BY ym " +
                    "ORDER BY ym";
        } else
            // Invalid viewMode
            return;

        Cursor cursor = dbReadable.rawQuery(query, null);
        int cursorSize = cursor.getCount();

        Log.d("DB", "Retrieved " + String.valueOf(cursorSize) + " rows");

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

        GraphView graph = (GraphView) getActivity().findViewById(R.id.statsGraph);

        // Remove the previous graph, if any (?)
        graph.removeAllSeries();

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
        series.setDrawDataPoints(true);
        series.setAnimated(true);
        series.setOnDataPointTapListener((series1, dataPoint) -> {
            graph.setTooltipText(String.format("%s: %dml", xLabels[(int) dataPoint.getX()], (int) dataPoint.getY()));

            float yAxisSize = (float) graph.getViewport().getMaxYAxisSize();
            float xAxisSize = (float) graph.getViewport().getMaxXAxisSize();
            if (yAxisSize == 0) yAxisSize = (float) series1.getHighestValueY();
            if (xAxisSize == 0) xAxisSize = (float) series1.getHighestValueX();

            // Programmatically trigger the tooltip at an appropriate position
            graph.performLongClick((float) dataPoint.getX() / xAxisSize * (float) graph.getGraphContentWidth(), graph.getBottom() - (float) dataPoint.getY() / yAxisSize * (float) graph.getGraphContentHeight());

        });
        graph.addSeries(series);

        //graph.getGridLabelRenderer().setTextSize(25.0f);
        // TODO: Add titles, other info to the graph

        // Use static labels for horizontal (and vertical) labels
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(xLabels);
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
    }
}