package com.example.jrd48.chat.location;

public interface MyLocationInterface {

    void start(int intervar);

    void stop();

    void setOnGotListener(MyLocationListener func);
}
