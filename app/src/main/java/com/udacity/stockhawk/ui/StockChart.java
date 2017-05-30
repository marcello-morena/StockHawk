/*
 * Copyright 2017 NinetySlide
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockChart extends AppCompatActivity {

    @BindView(R.id.tv_chart_title)
    TextView chartTitle;

    @BindView(R.id.lc_chart)
    LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_chart);
        ButterKnife.bind(this);

        String symbol = getIntent().getStringExtra(getString(R.string.intent_stock_chart_info));

        // Set title and description
        chartTitle.setText(String.format(getString(R.string.chart_title), symbol));
        chart.setContentDescription(String.format(getString(R.string.description_chart), symbol));

        // Retrieve history for symbol
        Cursor historyData = getContentResolver().query(
                Contract.Quote.makeUriForStock(symbol),
                new String[] {Contract.Quote.COLUMN_HISTORY},
                null,
                null,
                null
        );

        // Extract chart entries and dates from cursor
        List<Entry> entries = new ArrayList<>();
        final List<String> dates = new ArrayList<>();
        while (historyData.moveToNext()) {
            String[] datesValuesArr = historyData.getString(historyData.getColumnIndex(Contract.Quote.COLUMN_HISTORY)).split("\n");
            List<String> datesValuesLst = Arrays.asList(datesValuesArr);
            Collections.reverse(datesValuesLst);
            getEntriesAndDates(datesValuesLst, entries, dates);
        }
        historyData.close();

        IAxisValueFormatter valueFormatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return dates.get((int) value);
            }
        };

        LineDataSet dataSet = new LineDataSet(entries,"");
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.material_green_700));

        XAxis axis = chart.getXAxis();
        axis.setGranularity((1f));
        axis.setDrawGridLines(false);
        axis.setValueFormatter(valueFormatter);
        axis.setPosition(XAxis.XAxisPosition.BOTTOM);

        chart.setExtraBottomOffset(8);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setBackgroundColor(Color.WHITE);
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
    }

    private void getEntriesAndDates(List<String> datesValuesLst, List<Entry> entries, List<String> dates) {
        int i = 0;
        for (String dateValueStr : datesValuesLst) {
            String[] dateValue = dateValueStr.split(", ");
            entries.add(new Entry((float) i, Float.valueOf(dateValue[1])));
            dates.add(formatDate(dateValue[0]));
            i++;
        }
    }

    private String formatDate(String millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(millis));
        return new SimpleDateFormat(getString(R.string.date_format_string), Locale.US).format(calendar.getTime());
    }

}
