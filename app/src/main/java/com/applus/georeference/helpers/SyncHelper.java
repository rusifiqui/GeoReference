package com.applus.georeference.helpers;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.applus.georeference.sync.BaseVolleyActivity;

/**
 * Created by jvilam on 02/11/2016.
 *
 */

public class SyncHelper extends BaseVolleyActivity {


    private static RequestQueue reQueue;

    public static boolean sendProject(Context c, long idProject){

        reQueue = Volley.newRequestQueue(c);

        return true;
    }
}
