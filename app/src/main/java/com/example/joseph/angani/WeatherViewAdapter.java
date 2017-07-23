package com.example.joseph.angani;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by joseph on 7/22/17.
 * This class populates the weekely forecast recycler view
 */

public class WeatherViewAdapter extends RecyclerView.Adapter<WeatherViewAdapter.MyWeatherViewHolder> {
    List<Weather> weeklyWeather;
    Context context;
    public WeatherViewAdapter(Context context,List<Weather> weeklyWeather){
        this.context=context;
        this.weeklyWeather=weeklyWeather;
    }

    @Override
    public MyWeatherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context c=parent.getContext();
        LayoutInflater inflater=LayoutInflater.from(c);
        View view=inflater.inflate(R.layout.weekly,parent,false);
        MyWeatherViewHolder viewHolder=new MyWeatherViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyWeatherViewHolder holder, int position) {
        Weather weather=weeklyWeather.get(position);
        TextView dayOfWeek,status,high,low;
        ImageView statusIcon;
        dayOfWeek=holder.dayOfWeek;
        dayOfWeek.setText(weather.getDayOfWeek());
        status=holder.status;
        status.setText(weather.getStatus());
        statusIcon=holder.statusIcon;
        statusIcon.setImageResource(weather.getStatusIcon());
        high=holder.tempHigh;
        high.setText(String.valueOf((int)weather.getTempHigh())+(char)0x00B0);
        low=holder.tempLow;
        low.setText(String.valueOf((int)weather.getTempLow())+(char)0x00B0);
    }

    @Override
    public int getItemCount() {
        return weeklyWeather.size();
    }

    public class MyWeatherViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView dayOfWeek;
        TextView status;
        TextView tempHigh;
        TextView tempLow;
        ImageView statusIcon;

        public MyWeatherViewHolder(View itemView) {
            super(itemView);
            dayOfWeek= (TextView) itemView.findViewById(R.id.day_of_week);
            status= (TextView) itemView.findViewById(R.id.sky_status);
            tempHigh= (TextView) itemView.findViewById(R.id.temp_high);
            tempLow= (TextView) itemView.findViewById(R.id.temp_low);
            statusIcon= (ImageView) itemView.findViewById(R.id.daily_weather_info);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
