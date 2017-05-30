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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.StockChart;

/**
 * Widget provider for the stock widget.
 *
 * @author Marcello Morena
 */

public class StockWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_stock);

            Intent clickIntentTemplate = new Intent(context, StockChart.class);

            PendingIntent clickPendingIntentTemplate =
                    TaskStackBuilder
                        .create(context)
                        .addNextIntentWithParentStack(clickIntentTemplate)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setPendingIntentTemplate(R.id.lv_widget_stock, clickPendingIntentTemplate);

            // Set the collection and update the widget
            setRemoteAdapter(context, remoteViews);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);

        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            appWidgetManager.notifyAppWidgetViewDataChanged(
                    appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass())),
                    R.id.lv_widget_stock
            );
        }
    }

    private void setRemoteAdapter(Context context, RemoteViews views) {
        views.setRemoteAdapter(R.id.lv_widget_stock, new Intent(context, StockRemoteViewService.class));
    }

}
