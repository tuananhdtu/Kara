package com.example.anhle.kara;

/**
 * Created by anhle on 4/27/16.
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Lyric {
    private static final String TAG = Lyric.class.getSimpleName();

    public String sex;
    public List<Word> words = new ArrayList<Word>();

    public String showWord(){
        String word = "";
        for (int i=0; i< words.size();i++){
            word += words.get(i).text;
        }
        return word;
    }
}