package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class StockDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CURSOR_LOADER_ID = 0;
    private Cursor mCursor;
    private LineChartView lineChartView;
    private LineSet mLineSet;
    int maxRange,minRange,step;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        mLineSet = new LineSet();
        lineChartView = (LineChartView) findViewById(R.id.linechart);
        Intent intent = getIntent();
        Bundle args = new Bundle();
        args.putString(getResources().getString(R.string.string_symbol), intent.getStringExtra(getResources().getString(R.string.string_symbol)));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.BIDPRICE, QuoteColumns.CREATED},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{args.getString(getResources().getString(R.string.string_symbol))},
                null);
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        setRange(mCursor);
        initLineChart();
        fillLineSet();
    }

    @Override public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void fillLineSet(){

        mCursor.moveToFirst();
        for (int i = 0; i < mCursor.getCount(); i++){
            float price = Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
           String [] as= mCursor.getColumnNames();
            String labelTime = mCursor.getString(1);
            mLineSet.addPoint( labelTime, price);
            mCursor.moveToNext();
        }
        mLineSet.setColor(getResources().getColor(R.color.line_color))
                .setDotsStrokeThickness(Tools.fromDpToPx(2))
                .setDotsStrokeColor(getResources().getColor(R.color.line_color))
                .setDotsColor(getResources().getColor(R.color.line_color))
                .setSmooth(true);
        lineChartView.addData(mLineSet);
        lineChartView.show();
    }

    private void initLineChart() {
        Paint gridPaint = new Paint();
        gridPaint.setColor(getResources().getColor(R.color.outline_color));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(1f));
        lineChartView.setBorderSpacing(1)
                .setAxisBorderValues(0, maxRange, getStep(0,maxRange))
                .setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setLabelsColor(getResources().getColor(R.color.label_color))
                .setXAxis(false)
                .setYAxis(false)
                .setBorderSpacing(Tools.fromDpToPx(5))
                .setGrid(ChartView.GridType.HORIZONTAL, gridPaint);
    }
    public void setRange(Cursor mCursor)
    {
        ArrayList<Float> mArrayList = new ArrayList<Float>();
        for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
               // The Cursor is now set to the right position
            mArrayList.add(Float.parseFloat(String.format(Locale.ENGLISH,"%s",mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)))));
        }
        maxRange = Math.round(Collections.max(mArrayList))+20;
        minRange = Math.round(Collections.min(mArrayList));
        if(minRange>100)
            minRange = minRange-100;

    }
    public  int getStep(int a, int b)
    {
        while (b > 0)
        {
            int temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }
}