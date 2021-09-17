package edu.temple.convoy;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class ConvoyControlFragment extends Fragment {
    private View frame;
    private Button startBtn;
    private Button joinBtn;
    private Button leaveBtn;
    private ConvoyInterface parentActivity;


    public ConvoyControlFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(context instanceof ConvoyInterface){
            parentActivity = (ConvoyInterface) context;
        }
        else{
            throw new RuntimeException("You must implement ConvoyInterface interface before attaching this fragment");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        frame = inflater.inflate(R.layout.fragment_convoy_control, container, false);
        startBtn = frame.findViewById(R.id.startBtn);
        joinBtn = frame.findViewById(R.id.joinBtn);
        leaveBtn = frame.findViewById(R.id.leaveBtn);
        startBtn.setBackgroundColor(Color.GREEN);
        leaveBtn.setBackgroundColor(Color.RED);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.start();
            }
        });
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.join();
            }
        });
        leaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.leave();
            }
        });


        return frame;
    }

    interface ConvoyInterface{
        void join();
        void leave();
        void start();
    }
}