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

package com.udacity.stockhawk.ui.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Remote view service for the stock widget.
 *
 * @author Marcello Morena
 */
public class StockRemoteViewService extends RemoteViewsService {

    private final static DecimalFormat DOLLAR_FORMAT = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    private final static DecimalFormat PERCENTAGE_FORMAT = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
    static {
        PERCENTAGE_FORMAT.setMaximumFractionDigits(2);
        PERCENTAGE_FORMAT.setMinimumFractionDigits(2);
        PERCENTAGE_FORMAT.setPositivePrefix("+");
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {}

            @Override
            public void onDataSetChanged() {
                if(data!=null){
                    data.close();
                }

                final long idToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(
                        Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null, null, Contract.Quote.COLUMN_SYMBOL);

                Binder.restoreCallingIdentity(idToken);
            }

            @Override
            public void onDestroy() {
                if(data!=null) {
                    data.close();
                }
                data = null;
            }

            @Override
            public int getCount() {
                if(data==null)
                    return 0 ;
                else
                    return data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.list_item_widget);

                String symbol = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                Float value = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                Float percentageChange = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));

                // Set the background color based on the percantage variation
                if (percentageChange > 0) {
                    remoteViews.setInt(R.id.tv_widget_diff,"setBackgroundResource",R.drawable.percent_change_pill_green);
                } else {
                    remoteViews.setInt(R.id.tv_widget_diff,"setBackgroundResource",R.drawable.percent_change_pill_red);
                }
                String percentage = PERCENTAGE_FORMAT.format(percentageChange/100);

                // Set text and description for each TextView
                remoteViews.setTextViewText(R.id.tv_widget_symbol,symbol);
                remoteViews.setContentDescription(R.id.tv_widget_symbol, getString(R.string.description_widget_symbol,symbol));

                remoteViews.setTextViewText(R.id.tv_widget_value, DOLLAR_FORMAT.format(value));
                remoteViews.setContentDescription(R.id.tv_widget_value, getString(R.string.description_widget_value, DOLLAR_FORMAT.format(value)));

                remoteViews.setTextViewText(R.id.tv_widget_diff, percentage);
                remoteViews.setContentDescription(R.id.tv_widget_diff, getString(R.string.description_widget_diff, percentage));

                // Set the intent for click
                Intent fillInIntent = new Intent();
                fillInIntent.putExtra(getString(R.string.intent_stock_chart_info),symbol);
                remoteViews.setOnClickFillInIntent(R.id.ll_widget_row,fillInIntent);

                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_widget);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) {
                    return data.getLong(data.getColumnIndex(Contract.Quote._ID));
                } else {
                    return position;
                }
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
