package com.example.dillonwastrack.libusy.callbacks;

import com.example.dillonwastrack.libusy.models.Library;

import java.util.ArrayList;

/**
 * Created by dillonwastrack on 11/3/16.
 */

public interface LocationCallback {

    void onSuccess(ArrayList<Library> result);
}
