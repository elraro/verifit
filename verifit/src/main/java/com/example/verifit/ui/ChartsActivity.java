package com.example.verifit.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import com.example.verifit.R;
import com.example.verifit.model.WorkoutDay;
import com.example.verifit.model.WorkoutSet;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChartsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    private static final int RANGE_WEEK = 0;
    private static final int RANGE_MONTH = 1;
    private static final int RANGE_YEAR = 2;
    private static final int RANGE_ALL = 3;

    // Line colours that stay readable on both the light and the dark theme
    private static final int[] LINE_COLORS = {
            0xFF3D9EE0, 0xFFEF5350, 0xFF66BB6A, 0xFFFFA726, 0xFFAB47BC, 0xFF26C6DA
    };

    private static final int MAX_SERIES = 5;

    private int range = RANGE_WEEK;

    // Storage and parsing always use the US locale so stored dates are never affected by the device language
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.range_toggle);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.range_week) range = RANGE_WEEK;
            else if (checkedId == R.id.range_month) range = RANGE_MONTH;
            else if (checkedId == R.id.range_year) range = RANGE_YEAR;
            else range = RANGE_ALL;
            drawCharts();
        });

        onCreateStuff();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        onCreateStuff();
    }

    public void onCreateStuff()
    {
        // Bottom Navigation Bar Intents
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.charts);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        ScrollView scrollView = findViewById(R.id.scrollView);
        if(MainActivity.dataStorage.getWorkoutDays().size() == 0){
            scrollView.setVisibility(View.GONE);
        }
        else
        {
            scrollView.setVisibility(View.VISIBLE);
            drawCharts();
        }
    }

    // Builds the ordered list of bucket keys (days or months) for the selected range, oldest first
    private List<String> buildBuckets(boolean monthly)
    {
        Calendar cal = Calendar.getInstance();
        Calendar start = Calendar.getInstance();

        if (range == RANGE_WEEK) {
            start.add(Calendar.DAY_OF_YEAR, -6);
        } else if (range == RANGE_MONTH) {
            start.add(Calendar.DAY_OF_YEAR, -29);
        } else if (range == RANGE_YEAR) {
            start.add(Calendar.MONTH, -11);
        } else {
            // Start at the first recorded workout
            Date first = null;
            for (WorkoutDay day : MainActivity.dataStorage.getWorkoutDays()) {
                try {
                    Date d = dayFormat.parse(day.getDate());
                    if (first == null || d.before(first)) first = d;
                } catch (ParseException ignored) { }
            }
            if (first != null) start.setTime(first);
        }

        // Months are compared at the first day so every month appears exactly once
        if (monthly) {
            start.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }

        List<String> keys = new ArrayList<>();
        while (!start.after(cal)) {
            keys.add(monthly ? monthFormat.format(start.getTime()) : dayFormat.format(start.getTime()));
            start.add(monthly ? Calendar.MONTH : Calendar.DAY_OF_YEAR, 1);
        }
        return keys;
    }

    private String bucketKey(String date, boolean monthly)
    {
        return monthly ? date.substring(0, 7) : date;
    }

    private String[] bucketLabels(List<String> keys, boolean monthly)
    {
        SimpleDateFormat in = monthly ? monthFormat : dayFormat;
        SimpleDateFormat out = new SimpleDateFormat(monthly ? "MMM yy" : "d MMM", Locale.getDefault());
        String[] labels = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            try {
                labels[i] = out.format(in.parse(keys.get(i)));
            } catch (ParseException e) {
                labels[i] = keys.get(i);
            }
        }
        return labels;
    }

    private void drawCharts()
    {
        boolean monthly = range == RANGE_YEAR || range == RANGE_ALL;
        List<String> keys = buildBuckets(monthly);
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) index.put(keys.get(i), i);

        int n = keys.size();
        float[] volume = new float[n];
        float[] workouts = new float[n];
        Map<String, float[]> bodyparts = new LinkedHashMap<>();
        Map<String, float[]> exercises = new LinkedHashMap<>();

        for (WorkoutDay day : MainActivity.dataStorage.getWorkoutDays()) {
            Integer i = index.get(bucketKey(day.getDate(), monthly));
            if (i == null) continue;

            volume[i] += day.getDayVolume().floatValue();
            workouts[i] += 1;

            for (WorkoutSet set : day.getSets()) {
                if (!bodyparts.containsKey(set.getCategory())) bodyparts.put(set.getCategory(), new float[n]);
                bodyparts.get(set.getCategory())[i] += 1;

                if (!exercises.containsKey(set.getExerciseName())) exercises.put(set.getExerciseName(), new float[n]);
                exercises.get(set.getExerciseName())[i] += 1;
            }
        }

        String[] labels = bucketLabels(keys, monthly);

        Map<String, float[]> volumeSeries = new LinkedHashMap<>();
        volumeSeries.put(getString(R.string.series_volume), volume);
        Map<String, float[]> workoutSeries = new LinkedHashMap<>();
        workoutSeries.put(getString(R.string.series_workouts), workouts);

        drawLineChart((LineChart) findViewById(R.id.lineChartVolume), volumeSeries, labels, false);
        drawLineChart((LineChart) findViewById(R.id.lineChartWorkouts), workoutSeries, labels, false);
        drawLineChart((LineChart) findViewById(R.id.lineChartBodyparts), topSeries(bodyparts), labels, true);
        drawLineChart((LineChart) findViewById(R.id.lineChartExercises), topSeries(exercises), labels, true);
    }

    // Keeps only the series with the most total sets so the legend stays readable
    private Map<String, float[]> topSeries(Map<String, float[]> all)
    {
        List<Map.Entry<String, float[]>> entries = new ArrayList<>(all.entrySet());
        entries.sort((a, b) -> Float.compare(sum(b.getValue()), sum(a.getValue())));
        Map<String, float[]> top = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(MAX_SERIES, entries.size()); i++) {
            top.put(entries.get(i).getKey(), entries.get(i).getValue());
        }
        return top;
    }

    private float sum(float[] values)
    {
        float total = 0;
        for (float v : values) total += v;
        return total;
    }

    private void drawLineChart(LineChart chart, Map<String, float[]> series, String[] labels, boolean showLegend)
    {
        int textColor = getColor(R.color.core_black);
        int gridColor = getColor(R.color.core_grey_20);

        LineData data = new LineData();
        int colorIndex = 0;
        for (Map.Entry<String, float[]> s : series.entrySet()) {
            ArrayList<Entry> entries = new ArrayList<>();
            for (int i = 0; i < s.getValue().length; i++) {
                entries.add(new Entry(i, s.getValue()[i]));
            }
            LineDataSet set = new LineDataSet(entries, s.getKey());
            int color = LINE_COLORS[colorIndex++ % LINE_COLORS.length];
            set.setColor(color);
            set.setCircleColor(color);
            set.setCircleHoleColor(color);
            set.setLineWidth(2.5f);
            set.setCircleRadius(labels.length > 40 ? 0f : 3f);
            set.setDrawCircles(labels.length <= 40);
            set.setDrawValues(false);
            set.setMode(LineDataSet.Mode.LINEAR);
            data.addDataSet(set);
        }

        chart.clear();
        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setScaleYEnabled(false);
        chart.setPinchZoom(false);
        chart.setNoDataTextColor(textColor);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(textColor);
        xAxis.setAxisLineColor(gridColor);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(Math.min(labels.length, 6), false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setAxisMinimum(-0.25f);
        xAxis.setAxisMaximum(labels.length - 1 + 0.25f);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextColor(textColor);
        chart.getAxisLeft().setGridColor(gridColor);
        chart.getAxisLeft().setAxisLineColor(gridColor);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setDrawZeroLine(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(showLegend);
        legend.setTextColor(textColor);
        legend.setWordWrapEnabled(true);
        legend.setForm(Legend.LegendForm.LINE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        chart.animateX(400);
        chart.invalidate();
    }

    // Navigates to given activity based on the selected menu item
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        if(item.getItemId() == R.id.home)
        {
            Intent in = new Intent(this,MainActivity.class);
            startActivity(in);
            overridePendingTransition(0,0);
        }
        else if(item.getItemId() == R.id.exercises)
        {
            Intent in = new Intent(this, ExercisesActivity.class);
            startActivity(in);
            overridePendingTransition(0,0);
        }
        else if(item.getItemId() == R.id.diary)
        {
            Intent in = new Intent(this,DiaryActivity.class);
            startActivity(in);
            overridePendingTransition(0,0);
        }
        else if(item.getItemId() == R.id.charts)
        {
            Intent in = new Intent(this,ChartsActivity.class);
            startActivity(in);
            overridePendingTransition(0,0);
        }
        else if(item.getItemId() == R.id.me)
        {
            Intent in = new Intent(this, SettingsActivity.class);
            startActivity(in);
            overridePendingTransition(0,0);
        }
        return true;
    }

    // Menu Stuff
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.charts_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.settings)
        {
            Intent in = new Intent(this,SettingsActivity.class);
            startActivity(in);
        }
        else if(item.getItemId() == R.id.personal_records)
        {
            Intent in = new Intent(this,PersonalRecordsActivity.class);
            startActivity(in);
        }
        return super.onOptionsItemSelected(item);
    }
}