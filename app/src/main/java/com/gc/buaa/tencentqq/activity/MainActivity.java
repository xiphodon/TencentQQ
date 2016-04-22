package com.gc.buaa.tencentqq.activity;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gc.buaa.tencentqq.R;
import com.gc.buaa.tencentqq.drag.DragLayout;
import com.gc.buaa.tencentqq.drag.MyLinearLayout;
import com.gc.buaa.tencentqq.utils.Cheeses;
import com.gc.buaa.tencentqq.utils.Utils;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // 查找Draglayout, 设置监听
        DragLayout mDragLayout = (DragLayout) findViewById(R.id.dl);
        final ListView mLeftList = (ListView) findViewById(R.id.lv_left);
        final ListView mMainList = (ListView) findViewById(R.id.lv_main);
        final ImageView mHeaderImage = (ImageView) findViewById(R.id.iv_header);
        MyLinearLayout mLinearLayout = (MyLinearLayout) findViewById(R.id.mll);

        // 设置引用
        mLinearLayout.setDraglayout(mDragLayout);

        mDragLayout.setDragStatusListener(new DragLayout.OnDragStatusChangeListener() {
            @Override
            public void onClose() {
                Utils.showToast(MainActivity.this,"onClose");

                // 让图标晃动
//				mHeaderImage.setTranslationX(translationX)
                ObjectAnimator mAnim = ObjectAnimator.ofFloat(mHeaderImage, "translationX", 15.0f);
                //插补器(循环插补器：循环4次)
                mAnim.setInterpolator(new CycleInterpolator(4));
                mAnim.setDuration(500);
                mAnim.start();

            }

            @Override
            public void onOpen() {
                Utils.showToast(MainActivity.this,"onOpen");
                // 左面板ListView随机定位到一个条目
                Random random = new Random();

                int nextInt = random.nextInt(50);
                mLeftList.smoothScrollToPosition(nextInt);
            }

            @Override
            public void onDraging(float percent) {
                Log.i("test","percent:" + percent);
                // 更新图标的透明度
                // 1.0 -> 0.0
                ViewHelper.setAlpha(mHeaderImage, 1 - percent);
            }
        });


        mLeftList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView mText = ((TextView)view);
                mText.setTextColor(Color.WHITE);
                return view;
            }
        });

        mMainList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.NAMES){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView mText = ((TextView)view);
                mText.setTextColor(Color.BLACK);
                return view;
            }
        });

    }


}
