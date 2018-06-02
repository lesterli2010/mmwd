package com.blb.mmwd.uclient.util;

import com.blb.mmwd.uclient.R;

import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;

public class AnimationUtil {
    public static final boolean DEBUG = true;
    public static final String TAG = "AnimationUtils";

    public static Animation createSelectionFragmentAnimation(float pivotX, float pivotY) {
        if (DEBUG) {
            Log.d(TAG, "pivotX: " + pivotX + " pivotY: " + pivotY);
        }
        Animation scale = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, pivotX,
                pivotY);
        scale.setFillBefore(false);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scale);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.setDuration(500);
        return set;
    }
    
    public static void startScaleAnimation(View view) {
        
     // 创建一个AnimationSet对象（AnimationSet是存放多个Animations的集合）    
        AnimationSet animationSet = new AnimationSet(true);    
        // 创建一个ScaleAnimation对象（以某个点为中心缩放）    
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.4f, 1, 1.4f,    
                Animation.RELATIVE_TO_SELF, 0.4f, Animation.RELATIVE_TO_SELF, 0.4f);    
        // 设置动画执行之前等待的时间（单位：毫秒）    
       // scaleAnimation.setStartOffset(1000);    
        // 设置动画执行的时间（单位：毫秒）    
        
        scaleAnimation.setDuration(500);    
        // 如果fillAfter设为true，则动画执行后，控件将停留在动画结束的状态    
        // 运行了一下发现以下奇怪的现象    
        // scaleAnimation.setFillAfter(true);不会停留在动画结束的状态    
        // animationSet.setFillAfter(true);则会停留在动画结束的状态    
        scaleAnimation.setFillAfter(true);    
                    // 将ScaleAnimation对象添加到AnimationSet当中    
                    animationSet.addAnimation(scaleAnimation);    
                    // 使用ImageView的startAnimation方法开始执行动画    
                    view.startAnimation(animationSet); 
                    
        
    }
}
