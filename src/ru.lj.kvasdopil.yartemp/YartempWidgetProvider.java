package ru.lj.kvasdopil.yartemp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class YartempWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        for(int i = 0, l = appWidgetIds.length; i < l; i++) {
            appWidgetManager.updateAppWidget(appWidgetIds[i], buildUpdate(appWidgetIds[i], context));
        }
    }

    private RemoteViews buildUpdate(int id, Context context)
    {
        RemoteViews updateView = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small);

        updateView.setTextViewText(R.id.Updated, context.getString(R.string.loading));

        Time time = new Time();
        time.setToNow();

        try {
            TempUpdate update = GetTemp("http://yartemp.com/webdata/");

            String temp = context.getString(R.string.current, (update.val > 0 ? "+" : ""), update.val);
            String deltaTemp = context.getString(R.string.hourRate, (update.delta > 0 ? "+" : ""), update.delta);

            updateView.setTextViewText(R.id.Temp, temp);
            updateView.setTextViewText(R.id.DeltaTemp, deltaTemp);
            updateView.setTextViewText(R.id.Updated, time.format(context.getString(R.string.updatedNote)));
        }
        catch(Exception e) {
            updateView.setTextViewText(R.id.Updated, context.getString(R.string.error));
        }

        /*//Подготавливаем Intent для Broadcast
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { id } );

        //создаем наше событие
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        //регистрируем наше событие
        updateView.setOnClickPendingIntent(R.id.icon, pendingIntent);*/

        return updateView;
    }

    public TempUpdate GetTemp(String urlsite) throws Exception
    {
        URL url = new URL(urlsite);
        URLConnection conn = url.openConnection();
        InputStreamReader rd = new InputStreamReader(conn.getInputStream());
        StringBuilder allpage = new StringBuilder();
        int n = 0;
        char[] buffer = new char[40000];
        while(n >= 0)
        {
            n = rd.read(buffer, 0, buffer.length);
            if(n>0)
                allpage.append(buffer, 0, n);
        }

        String[] res = allpage.toString().split(";");

        if(res.length < 3)
           throw new RuntimeException();

        return new TempUpdate(Math.round(Float.valueOf(res[0])), Math.round(Float.valueOf(res[2])));
    }

    class TempUpdate {
        public final int val;
        public final int delta;

        TempUpdate(int val, int delta) {
            this.val = val;
            this.delta = delta;
        }
    }
}