package com.citsin.pedometer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.citsin.pedometer.R;
import com.citsin.pedometer.model.Sport;
import com.citsin.pedometer.util.Utils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Citsin on 2015/12/22.
 */
public class SportRecordAdapter extends RecyclerView.Adapter<SportRecordAdapter.ItemViewHolder> {


    private ArrayList<Sport> sports = new ArrayList<>();


    public void addAll(ArrayList<Sport> sports){
        this.sports.addAll(sports);
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ItemViewHolder(inflater.inflate(R.layout.item_sport, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Sport sport = sports.get(position);
        holder.stepView.setText("步数："+sport.getStep()+"步");
        holder.distanceView.setText("运动距离：\n"+sport.getDistance()+"米");
        holder.durationView.setText("时长：" + Utils.getDuration(sport.getDuration()));
        holder.speedView.setText("运动速度：\n"+sport.getSpeed()+"米/秒");
        holder.caloriesView.setText("消耗热量：\n"+sport.getCalories()+"千焦");
        holder.timeView.setText("运动时间："+Utils.getDateTimeFormat(sport.getTime()));
    }

    @Override
    public int getItemCount() {
        return sports.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.item_sport_step)
        TextView stepView;
        @Bind(R.id.item_sport_curation)
        TextView durationView;
        @Bind(R.id.item_sport_distance)
        TextView distanceView;
        @Bind(R.id.item_sport_speed)
        TextView speedView;
        @Bind(R.id.item_sport_calories)
        TextView caloriesView;
        @Bind(R.id.item_sport_time)
        TextView timeView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
