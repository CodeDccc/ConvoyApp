package edu.temple.convoy;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    Context context;
   // String recordingFile;
    String[] strings;
    //ArrayList<String> list1 = new ArrayList<>();

    Map<String, String>list = new HashMap<>();

    public RecyclerAdapter(Context context, Map<String, String>list){
        this.context = context;
        //this.recordingFile = recordingFile;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.record_clip, parent, false);
        return new MyViewHolder(view);//, myItem) ;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.MyViewHolder holder, int position) {
        //holder.audioText.setText(list.);
        for (Map.Entry<String, String> pair : list.entrySet()) {
            holder.audioText.setText(pair.getValue() +" " + pair.getKey());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView audioText;

        public MyViewHolder(@NonNull View itemView){//, ItemClicked listener) {
            super(itemView);
            audioText = itemView.findViewById(R.id.audioTime);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* if(listener!=null){
                        int position = getAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }*/

                    for (Map.Entry<String, String> pair : list.entrySet()) {
                        if(pair.getKey().equals(audioText.getText())){
                            //playList.add(pair.getValue());
                            AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

                            if(manager.isMusicActive())

                        }
                    }
                    Log.d("u", "lok her real " + audioText.getText());
                }
            });
        }
    }
    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            finish(); // finish current activity
        }
    });
}
