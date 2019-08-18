package com.livelocation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class userAdapter extends RecyclerView.Adapter<userAdapter.Myholder> {
    ArrayList<UsersList> mList;
    Context ctx;

    userAdapter(Context ctx, ArrayList<UsersList> responses){
        this.mList = responses;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View xyz = inflater.inflate(R.layout.users,viewGroup,false);
        return new Myholder(xyz);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder myholder, final int i) {

        myholder.users.setText(mList.get(i).getName());
        myholder.users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LiveUsers)ctx).getToken(mList.get(i).getEntry());
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size() ;
    }

    public class Myholder extends RecyclerView.ViewHolder {
        TextView users;

        public Myholder(@NonNull View itemView) {
            super(itemView);

            users= itemView.findViewById(R.id.users);

        }
    }
}
