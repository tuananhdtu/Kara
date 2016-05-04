package com.example.anhle.kara;

/**
 * Created by anhle on 4/27/16.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LyricUtils {
    private static final String TAG = LyricUtils.class.getSimpleName();


    public static ArrayList<Lyric> parseLyric(JSONArray jsonArray) throws JSONException {
        ArrayList<Lyric> lyrics = new ArrayList<Lyric>();

        for(int i=0; i<jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Lyric lyric = new Lyric();
            lyric.sex = jsonObject.getString("sex");

            JSONArray words = jsonObject.getJSONArray("words");
            for (int j=0; j<words.length(); j++){
                JSONObject jsonObjectWord = words.getJSONObject(j);
                Word word = new Word();
                word.startTime = jsonObjectWord.getInt("startTime");
                word.stringTime = jsonObjectWord.getString("stringTime");
                word.text = jsonObjectWord.getString("text");
                lyric.words.add(word);
            }

            lyrics.add(lyric);
        }

        return lyrics;
    }
    public static int getSentenceIndex(ArrayList<Lyric> lyric, long ts, int index, int offset) {
        if (lyric == null || ts < 0 || index < -1)
            return -1;
        List<Lyric> list = lyric;

        if (index >= list.size())
            index = list.size() - 1;
        if (index == -1)
            index = 0;

        int found = -2;

        if (list.get(index).words.get(0).getStartTime() + offset > ts) {
            for (int i = index; i > -1; --i) {
                if (list.get(i).words.get(0).getStartTime() + offset <= ts) {
                    found = i;
                    break;
                }
            }
            // First line of lyric is bigger than starting time.
            if (found == -2)
                found = -1;
        } else {
            for (int i = index; i < list.size() - 1; ++i) {
                //Log.d(TAG, String.format("ts: %d, offset: %d, curr_ts: %d, next_ts: %d", ts, offset, list.get(i).getFromTime(), list.get(i + 1).getFromTime()));
                if (list.get(i + 1).words.get(0).getStartTime() + offset > ts) {
                    found = i;
                    break;
                }
            }
            // If not found, return last mLyricIndex
            if (found == -2) {
                found = list.size() - 1;
            }
        }

        return found;
    }

    public static int getSentenceIndexChild(List<Word> words, long ts, int index, int offset) {
        if (words == null || ts < 0 || index < -1)
            return -1;
        List<Word> list = words;

        if (index >= list.size())
            index = list.size() - 1;
        if (index == -1)
            index = 0;

        int found = -2;

        if (words.get(index).getStartTime() + offset > ts) {
            for (int i = index; i > -1; --i) {
                if (words.get(index).getStartTime() + offset <= ts) {
                    found = i;
                    break;
                }
            }
            // First line of lyric is bigger than starting time.
            if (found == -2)
                found = -1;
        } else {
            for (int i = index; i < list.size() - 1; ++i) {
                //Log.d(TAG, String.format("ts: %d, offset: %d, curr_ts: %d, next_ts: %d", ts, offset, list.get(i).getFromTime(), list.get(i + 1).getFromTime()));
                if (words.get(i + 1).getStartTime() + offset > ts) {
                    found = i;
                    break;
                }
            }
            // If not found, return last mLyricIndex
            if (found == -2) {
                found = list.size() - 1;
            }
        }

        return found;
    }
}
