package com.shangguigu.youkumenu;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;


public class Tools {

    public static void showView(ViewGroup view) {
        showView(view, 0);
    }

    public static void showView(ViewGroup view, int offset) {
        ObjectAnimator animator=ObjectAnimator.ofFloat(view,"rotation",180,360);
        animator.setDuration(500);
        animator.setStartDelay(offset);

        animator.start();

        view.setPivotX(view.getWidth()/2);
        view.setPivotY(view.getHeight());

    }

    public static void hideView(ViewGroup view) {
        hideView(view, 0);
    }

    public static void hideView(ViewGroup view, int offset) {
        ObjectAnimator animator=ObjectAnimator.ofFloat(view,"rotation",0,180);
        animator.setDuration(500);
        animator.setStartDelay(offset);
        animator.start();

        view.setPivotX(view.getWidth()/2);
        view.setPivotY(view.getHeight());
    }
}
