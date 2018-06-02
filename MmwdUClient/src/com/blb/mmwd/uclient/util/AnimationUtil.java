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
        
     // ����һ��AnimationSet����AnimationSet�Ǵ�Ŷ��Animations�ļ��ϣ�    
        AnimationSet animationSet = new AnimationSet(true);    
        // ����һ��ScaleAnimation������ĳ����Ϊ�������ţ�    
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.4f, 1, 1.4f,    
                Animation.RELATIVE_TO_SELF, 0.4f, Animation.RELATIVE_TO_SELF, 0.4f);    
        // ���ö���ִ��֮ǰ�ȴ���ʱ�䣨��λ�����룩    
       // scaleAnimation.setStartOffset(1000);    
        // ���ö���ִ�е�ʱ�䣨��λ�����룩    
        
        scaleAnimation.setDuration(500);    
        // ���fillAfter��Ϊtrue���򶯻�ִ�к󣬿ؼ���ͣ���ڶ���������״̬    
        // ������һ�·���������ֵ�����    
        // scaleAnimation.setFillAfter(true);����ͣ���ڶ���������״̬    
        // animationSet.setFillAfter(true);���ͣ���ڶ���������״̬    
        scaleAnimation.setFillAfter(true);    
                    // ��ScaleAnimation������ӵ�AnimationSet����    
                    animationSet.addAnimation(scaleAnimation);    
                    // ʹ��ImageView��startAnimation������ʼִ�ж���    
                    view.startAnimation(animationSet); 
                    
        
    }
}
