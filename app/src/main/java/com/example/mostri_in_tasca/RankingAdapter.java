package com.example.mostri_in_tasca;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RankingAdapter extends RecyclerView.Adapter<ViewHolder> {
    private LayoutInflater mInflater;
    private ArrayList<Player> players;
    private Activity parentActivity;


    public RankingAdapter(Context context, Activity parentActivity, ArrayList<Player> players) {
        this.mInflater = LayoutInflater.from(context);
        this.players = players;
        this.parentActivity = parentActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_ranking, parent, false);
        return new ViewHolder(view, parentActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = players.get(position);
        holder.setPlayer(player, position);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }
}