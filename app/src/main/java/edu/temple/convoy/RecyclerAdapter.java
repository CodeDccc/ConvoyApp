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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    Context context;
   // String recordingFile;
    String[] strings;
    ArrayList<String> list = new ArrayList<>();
    ArrayList<String> audioItem = new ArrayList<>();
    //MediaPlayer mediaPlayer = new MediaPlayer();

    //Map<String, String>list = new HashMap<>();

    public RecyclerAdapter(Context context, ArrayList<String> list, ArrayList<String> audioItem){
        this.context = context;
        //this.recordingFile = recordingFile;
        this.list = list;
        this.audioItem = audioItem;
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
        holder.audioText.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView audioText;

        public MyViewHolder(@NonNull View itemView){
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
                    MediaPlayer mediaPlayer = new MediaPlayer();
                   // mediaPlayer.reset();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mediaPlayer.setDataSource(audioItem.get(getAdapterPosition()));
                        Log.d("u", "lok her realfile " + audioItem.get(getAdapterPosition()));
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("u", "lok her real adaptor" + getAdapterPosition());
                    Log.d("u", "lok her real " + audioText.getText());
                }
            });
        }
    }
    /*mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            finish(); // finish current activity
        }
    });*/
}
