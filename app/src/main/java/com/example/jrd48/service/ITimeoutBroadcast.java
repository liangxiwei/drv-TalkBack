package com.example.jrd48.service;

import android.content.Intent;

public interface ITimeoutBroadcast{
        void onTimeout();
        void onGot(Intent intent);
    }