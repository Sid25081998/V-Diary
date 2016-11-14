package com.example.sridh.vdiary;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class show_subject extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_subject);

        //intent from the all sub list  with "position" as key......
        int position=getIntent().getIntExtra("position",0);
        subject clicked= vClass.subList.get(position);
        initialize(clicked);
    }
    //Initialize the popup activity to show the contents of te subject
    void initialize(subject sub){
        /*if(Build.VERSION.SDK_INT>=21){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(),R.color.taskbar_orange));
        }*/
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        getWindow().setLayout(width,(int)(.5*height));
        Toolbar bar=(Toolbar)findViewById(R.id.showToolbar);
        bar.inflateMenu(R.menu.menu_show_subject);
        bar.setTitle(sub.code);
        ((TextView)findViewById(R.id.showTitle)).setText(sub.title);
        ((TextView)findViewById(R.id.showTeacher)).setText(sub.teacher);
    }
}
