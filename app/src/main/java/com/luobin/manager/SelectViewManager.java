package com.luobin.manager;

import android.view.View;

import com.luobin.dvr.R;

import java.util.ArrayList;
import java.util.List;

public class SelectViewManager {
    List<View> views = new ArrayList<>();
    int index = 0;
    int preIndex = 0;


    public SelectViewManager(List<View> views){
        this.views = views;
        move(index);
    }


    public void  moveLeft(){
        preIndex = index;
        index --;
        if (index < 0)
            index = views.size() -1;

        move(index);
    }

    public void moveRight(){
        preIndex = index;
        index ++;
        if (index > views.size() -1)
            index = 0;
        move(index);
    }

    public void moveUp(){
     /*   preIndex = index;
        index -= 2;
        if (index < 0)
            index = preIndex+2;
        move(index);*/
     moveLeft();
    }

    public void moveDown(){
       /* preIndex = index;
        index += 2;
        if (index > views.size() -1)
            index = preIndex -2 ;
        move(index);*/
       moveRight();
    }

    public void center(){
        views.get(index).performClick();
    }


    View moveView;
    private void move(int index){
        views.get(preIndex).setScaleX(1.0f);
        views.get(preIndex).setScaleY(1.0f);
        views.get(preIndex).setElevation(0.0f);
        moveView =  views.get(index);
        moveView.requestFocus();
        moveView.setScaleX(1.2f);
        moveView.setScaleY(1.2f);
        moveView.setElevation(1.0f);
    }



}
