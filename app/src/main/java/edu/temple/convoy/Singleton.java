package edu.temple.convoy;

import java.util.ArrayList;
import java.util.PriorityQueue;

class Singleton {

    private static Singleton single_instance = null;


    public ArrayList<String> playList = new ArrayList<>();


    private Singleton()
    {

    }

    public void addAudio(String audio){
        playList.add(audio);
    }
    public void removeAudio(String audio){
        playList.remove(audio);
    }

    public static Singleton getInstance()
    {
        if (single_instance == null)
            single_instance = new Singleton();

        return single_instance;
    }
}
