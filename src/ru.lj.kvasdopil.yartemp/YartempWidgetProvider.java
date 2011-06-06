package ru.lj.kvasdopil.yartemp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.IdentityScope;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YartempWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        RemoteViews updateView = buildUpdate(appWidgetIds, context);
        appWidgetManager.updateAppWidget(appWidgetIds, updateView);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final String action = intent.getAction();

        if(AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action))
        {
            int ids[] = {}; //
            this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
        }
        super.onReceive(context, intent);
    }

    private RemoteViews buildUpdate(int[] ids, Context context)
    {
        RemoteViews updateView;
        int val;

        try {
            val = Integer.decode(GetTemp("http://yartemp.com/webdata/"));
            if(val > 0)
                temp = String.format("+%s °C", val);
            else
                temp = String.format("%s °C", val);
        }
        catch(Exception e) {}

        Time time = new Time();
        time.setToNow();

        Toast.makeText(context, "woo", Toast.LENGTH_SHORT).show();

        updateView = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small);
        updateView.setTextViewText(R.id.Temp, temp);
        updateView.setTextViewText(R.id.Updated, time.format("обновлено в %H:%M:%S"));

        //Подготавливаем Intent для Broadcast
        Intent intent = new Intent(context, YartempWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //intent.putExtra("ids", ids);

        //создаем наше событие
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        //регистрируем наше событие
        updateView.setOnClickPendingIntent(R.id.SmallBase, pendingIntent);

        return updateView;
    }



    public String GetTemp(String urlsite) throws Exception
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

            final Pattern pattern = Pattern.compile("^([0-9]+).*");
            Matcher matcher = pattern.matcher(allpage.toString());
            if(!matcher.find())
                throw new RuntimeException();

            return matcher.group(1);
    }

    private String temp = "--";
}