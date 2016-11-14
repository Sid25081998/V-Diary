package com.example.sridh.vdiary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class scrapper extends AppCompatActivity {
    EditText regBox,passBox,captchaBox;
    WebView web,att;
    ImageView captcha,logo;
    TextView status;
    FloatingActionButton fab;
    List<String> attList = new ArrayList<String>();
    List<String> ctdList = new ArrayList<String>();
    boolean gotAttendance,gotSchedule;
    public static boolean tryrefresh=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        if(!tryrefresh){
            if(readfromPrefs()){
                Intent toDash= new Intent(scrapper.this,dash.class);
                startActivity(toDash);
                finish();
                return;
            }
        }
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrapper, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    void init(){
       setWebViews();
       web.setWebViewClient(new loginClient());
       new compileInf().execute();
       web.loadUrl("https://academicscc.vit.ac.in/student/stud_login.asp");
       setContentView(R.layout.activity_scrapper);
       setUp();
       status.setText("Fetching captcha...");
       load(true);
    } //STARTS LOADING PAGE ON THE WEBVIEWS

    void setUp(){
        fab = (FloatingActionButton) findViewById(R.id.login);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input="document.getElementsByName(\"regno\")[0].value=\""+regBox.getText().toString()+"\"; document.getElementsByName(\"passwd\")[0].value=\""+passBox.getText()+"\"; document.getElementsByName(\"vrfcd\")[0].value=\""+captchaBox.getText()+"\"; document.forms[0].submit();";
                web.evaluateJavascript(getcmd(input), new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //Data are inserted and login is in process
                        status.setText("Verifying Identity...");
                        load(true);
                    }
                });
            } //SETS THE CREDENTIAL TO THE FORM AND SUBMITS IT
        });
        regBox=(EditText)findViewById(R.id.regBox);
        passBox=(EditText)findViewById(R.id.passbox);
        captchaBox=(EditText)findViewById(R.id.captchaBox);
        captcha=(ImageView)findViewById(R.id.captcha);
        status=(TextView)findViewById(R.id.status);
        logo=(ImageView)findViewById(R.id.logo);
        passBox.setTextColor(Color.GRAY);
        captchaBox.setTextColor(Color.GRAY);
        setOnFocusListener(regBox,"Registration No.");
        setOnFocusListener(passBox,"Password");
        setOnFocusListener(captchaBox,"Captcha");
    } //SETS LAYOUT OF THE MAIN PAGE

    void setWebViews() {
        web= new WebView(this);
        att= new WebView(this);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setJavaScriptEnabled(true);
        att.getSettings().setDomStorageEnabled(true);
        att.getSettings().setJavaScriptEnabled(true);
        gotAttendance=false;
        gotSchedule=false;
    } //CHANGES THE SETTING OF THE WEBVIEWS

    void setOnFocusListener(final EditText textControl, final String value){
        textControl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String controlText=textControl.getText().toString();
                if(hasFocus){
                    textControl.setTextColor(Color.BLACK);
                    if(controlText.equals(value)){
                        textControl.setText("");
                    }
                }
                else{
                    textControl.setTextColor(Color.GRAY);
                    if(controlText.equals("")){
                        textControl.setText(value);
                    }
                }
            }
        });
    } //SETS ON FOCUS CHANGED LISTENERS FOR GIVEN EDIT-TEXT

    void setCaptcha(String imgString){
        byte[] decodedString = Base64.decode(imgString,0);
        Bitmap capImg= BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        captcha.setImageBitmap(capImg);
    } //CONVERTS THE BASE-64 STRING TO BITMAP IMAGE AND SETS TO CAPTCHA IMAGEVIEW

    void getFormTable1(){
        web.evaluateJavascript(getcmd("var rows=document.getElementsByTagName('table')[1].rows;var c;for(c=0;c<rows.length;c++){if(rows[c].cells.length==15){rows[c].deleteCell(0)}}"), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                web.evaluateJavascript("var rows=document.getElementsByTagName('table')[1].rows;var c;for(c=0;c<rows.length;c++){if(rows[c].cells.length==14){rows[c].deleteCell(0)}}", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[1].rows.length.toString()"), new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                int rows=Integer.parseInt(trim(value));
                                for(int row=1;row<rows-2;row++){
                                    final int rowa=row;
                                    web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[1].rows[" + row + "].cells[8].innerText.toString()"), new ValueCallback<String>() {
                                        @Override
                                        public void onReceiveValue(String value) {
                                            final String room=trim(value);
                                            if(!room.equals("NIL")){
                                                final subject sub= new subject();
                                                sub.room=room;
                                                //Toast.makeText(getApplicationContext(),room,Toast.LENGTH_LONG).show();
                                                //CODE
                                                web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[1].rows[" + rowa + "].cells[1].innerText.toString()"), new ValueCallback<String>() {
                                                    @Override
                                                    public void onReceiveValue(String code) {
                                                        sub.code=trim(code);
                                                    }
                                                });
                                                //NAME
                                                web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[1].rows[" + rowa + "].cells[2].innerText.toString()"), new ValueCallback<String>() {
                                                    @Override
                                                    public void onReceiveValue(String name) {
                                                        sub.title=trim(name);
                                                    }
                                                });
                                                //TEACHER
                                                web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[1].rows[" + rowa + "].cells[9].innerText.toString()"), new ValueCallback<String>() {
                                                    @Override
                                                    public void onReceiveValue(String teacher) {
                                                        String rawTeacher= trim(teacher).split("-")[0];
                                                        sub.teacher=rawTeacher.substring(0,rawTeacher.length()-1);
                                                    }
                                                });
                                                //TYPE
                                                web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[1].rows[" + rowa + "].cells[3].innerText.toString()"), new ValueCallback<String>() {
                                                    @Override
                                                    public void onReceiveValue(String rawtype) {
                                                        String type=trim(rawtype);
                                                        switch (type)
                                                        {
                                                            case "Embedded Theory":
                                                                sub.type = "ETH";
                                                                break;
                                                            case "Theory Only":
                                                                sub.type = "TH";
                                                                break;
                                                            case "Lab Only":
                                                                sub.type = "LO";
                                                                break;
                                                            case "Embedded Lab":
                                                                sub.type = "ELA";
                                                                break;
                                                            case "Soft Skill":
                                                                sub.type = "SS";
                                                                break;
                                                        }
                                                    }
                                                });
                                                vClass.subList.add(sub);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        });
        getFromTable2();
    } //GET DATA FROM ALL COURSES

    void getFromTable2(){
        web.evaluateJavascript(getcmd("document.getElementsByTagName('table')[2].rows[0].deleteCell(7);"), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //LUNCH DELETED
                for(int rowa=2;rowa<=6;rowa++){
                    final int row=rowa;
                    final List<subject> today= new ArrayList<subject>();
                    web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[2].rows[" + row + "].cells.length.toString()"), new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String rawCols) {
                            final int cols=Integer.parseInt(trim(rawCols));
                            final AtomicReference extraTime = new AtomicReference(0);
                            for(int col=1;col<cols;col++){
                                final int cell=col;
                                web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[2].rows[" + row + "].cells[" + cell + "].colSpan.toString()"), new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        int rawcolSpan=Integer.parseInt(trim(value));
                                        if(rawcolSpan>1) {
                                            extraTime.set(rawcolSpan - 1);
                                        }
                                        web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[2].rows[" + row + "].cells['" + cell + "'].bgColor"), new ValueCallback<String>() {
                                            @Override
                                            public void onReceiveValue(String color) {
                                                final String cellColor = trim(color);
                                                    web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[2].rows[" + row + "].cells[" + cell + "].innerText.toString()"), new ValueCallback<String>() {
                                                        @Override
                                                        public void onReceiveValue(String value) {
                                                            String text= trim(value);
                                                            final subject sub = new subject();
                                                            if(cellColor.equals("#CCFF33")) {
                                                                sub.code = text.substring(0, 7); //CODE
                                                                String rawType = text.split("-")[1];
                                                                String type = rawType.substring(1, rawType.length() - 1); //TYPE
                                                                sub.type = type;
                                                                //TIME
                                                                if (type.equals("ETH") || type.equals("SS") || type.equals("TH")) {
                                                                    web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[2].rows[0].cells[" + (cell + Integer.parseInt(String.valueOf(extraTime.get()))) + "].innerText.toString()"), new ValueCallback<String>() {
                                                                        @Override
                                                                        public void onReceiveValue(String value) {
                                                                            String time = trim(value);
                                                                            sub.time = time.substring(0, 7) + "-" + time.substring(13, time.length());
                                                                            today.add(sub);
                                                                        }
                                                                    });
                                                                } else {
                                                                    web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[2].rows[1].cells[" + (cell + Integer.parseInt(String.valueOf(extraTime.get()))) + "].innerText.toString()"), new ValueCallback<String>() {
                                                                        @Override
                                                                        public void onReceiveValue(String value) {
                                                                            String rawstime = trim(value);
                                                                            String sTime = rawstime.substring(0, 7);
                                                                            final AtomicReference<String> time = new AtomicReference<String>(sTime);
                                                                            web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[2].rows[1].cells[" + (cell + 1 + Integer.parseInt(String.valueOf(extraTime.get()))) + "].innerText.toString()"), new ValueCallback<String>() {
                                                                                @Override
                                                                                public void onReceiveValue(String value) {
                                                                                    String rawetime = trim(value);
                                                                                    String etime = rawetime.substring(13, rawetime.length());
                                                                                    sub.time = time.get() + "-" + etime;
                                                                                    today.add(sub);
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                            else{
                                                                sub.code="";
                                                                sub.title=text;
                                                                sub.type="";
                                                                web.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[2].rows[1].cells[" + (cell + Integer.parseInt(String.valueOf(extraTime.get()))) +"].innerText.toString()"), new ValueCallback<String>() {
                                                                    @Override
                                                                    public void onReceiveValue(String value) {
                                                                        String time=trim(value);
                                                                        sub.time = time.substring(0, 7) + "-" + time.substring(13, time.length());
                                                                        today.add(sub);
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                if(row==6 && cell==cols-1){
                                                    gotSchedule=true;
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                    vClass.timeTable.add(today);
                }
            }
        });
    } // GET DATA FROM TIME TABLE

    void getAttendance(){
        att.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[2].rows[2].cells[2].innerText"), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                vClass.semStart=trim(value);
                att.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[2].rows[2].cells[3].innerText"), new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        vClass.cat1=trim(value);
                        att.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[2].rows[3].cells[3].innerText"), new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                vClass.cat2=trim(value);
                                att.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[2].rows[4].cells[3].innerText"), new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        vClass.fat=trim(value);
                                        if(att.getUrl().equals("https://academicscc.vit.ac.in/student/attn_report.asp?sem=FS")){
                                            att.loadUrl("https://academicscc.vit.ac.in/student/attn_report.asp?sem=FS" + "&fmdt=" + vClass.semStart + "&todt=" + vClass.fat);
                                        }
                                        else {
                                            att.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[4].rows.length"), new ValueCallback<String>() {
                                                @Override
                                                public void onReceiveValue(String value) {
                                                    final int rows=Integer.parseInt(value);
                                                    for(int i=1;i<rows;i++){
                                                        final int j=i;
                                                        att.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[4].rows["+ j +"].cells[8].innerText"), new ValueCallback<String>() {
                                                            @Override
                                                            public void onReceiveValue(String value) {
                                                                attList.add(trim(value));
                                                                att.evaluateJavascript(getcmd("return document.getElementsByTagName('table')[4].rows[" + j +"].cells[7].innerText"), new ValueCallback<String>() {
                                                                    @Override
                                                                    public void onReceiveValue(String value) {
                                                                        ctdList.add(trim(value));
                                                                        if(j==rows-1){
                                                                            gotAttendance=true;
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    } //GET DATA FROM ATTENDANCE

    void load(boolean x){
        if(x==true){
            regBox.setVisibility(View.INVISIBLE);
            passBox.setVisibility(View.INVISIBLE);
            captchaBox.setVisibility(View.INVISIBLE);
            captcha.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.INVISIBLE);
            status.setVisibility(View.VISIBLE);
            logo.setVisibility(View.VISIBLE);
        }
        else{
            regBox.setVisibility(View.VISIBLE);
            passBox.setVisibility(View.VISIBLE);
            captchaBox.setVisibility(View.VISIBLE);
            captcha.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
            logo.setVisibility(View.INVISIBLE);
            status.setVisibility(View.INVISIBLE);
        }
    }  //SWITCHES THE CONTENT TO LOADING SCREEN AND BACK

    void placeCorrectly(subject sub,List<subject> i){
        i.remove(sub);
        int subHour = Integer.parseInt(formattedTime(sub).substring(0,2));
        int subMin=Integer.parseInt(formattedTime(sub).substring(3,5));
        for(int count=0;count<i.size();count++){
            int checkHour =Integer.parseInt(formattedTime(i.get(count)).substring(0,2));
            if(subHour==checkHour){
                int checkMin =Integer.parseInt(formattedTime(i.get(count)).substring(3,5));
                if(subMin<checkMin){
                    i.add(count,sub);
                    return;
                }
            }
            else if(subHour<checkHour){
                i.add(count,sub);
                return;
            }
        }
    } //PLACE THE LAB IN THERE CORRECT POSITION BY INSERTION SORT

    void writeToPrefs(){
        SharedPreferences.Editor editor = getSharedPreferences("prefs",MODE_PRIVATE).edit();
        Gson serializer = new Gson();
        editor.putString("allSubjects",serializer.toJson(vClass.subList));
        editor.putString("timeTable",serializer.toJson(vClass.timeTable));
        editor.commit();
    }  //COMMMIT THE INFORMTION TO SHARED PREFERNCE

    boolean readfromPrefs(){
        SharedPreferences prefs =  getSharedPreferences("prefs",MODE_PRIVATE);
        Gson deserializer = new Gson();
        String allSubJson =prefs.getString("allSubjects",null);
        String timetableJson =prefs.getString("timeTable",null);
        if(allSubJson!=null || timetableJson!=null){
            vClass.subList= deserializer.fromJson(allSubJson,new TypeToken<ArrayList<subject>>(){}.getType());
            vClass.timeTable= deserializer.fromJson(timetableJson, new TypeToken<ArrayList<ArrayList<subject>>>() {}.getType());
            return  true;
        }
        else return false;
    }  //READ THE PREFERENCES AND SET IT TO RELEVANT VARIABLES IN vClass

    String getcmd(String js){

        return "(function(){"+js+"})()";

    } //RETURNS THE COMMAND FORMAT OF THE JAVASCRIPT

    String trim(String str){
        str= str.substring(1);
        return str.substring(0,str.indexOf("\""));
    } //TRIMS THE GIVEN RESULT FROM JAVSCRIPT TO REMOVE QUOTES

    String formattedTime(subject sub){
        String rawTime= sub.time;
        String meridian =rawTime.substring(5,7);
        int hour = Integer.parseInt(rawTime.substring(0,2));
        if(meridian.equals("PM") && hour<12){
            hour = hour+12;
            String t=hour+rawTime.substring(2);
            return t;
        }
        return rawTime;
    } //GET THE 24-HOUR FORMAT OF THE TIME OF THE SUBJECT

    subject getSubject(String code,String type){
        for(subject i:vClass.subList){
            if(i.code.equals(code) && i.type.equals(type)){
                return i;
            }
        }
        return null;
    } //SEARCH SUBJECT IN SUBJECT LIST

    class loginClient extends WebViewClient{
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view,url);
            String webTitle =web.getTitle();
            if(tryrefresh && webTitle.equals("")){
                Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(scrapper.this,dash.class));
                finish();
            }
            else if(webTitle.equals("Webpage not available")){
                Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
            }
            else if(web.getUrl().equals("https://academicscc.vit.ac.in/student/stud_login.asp")) {
                web.evaluateJavascript(getcmd("return document.getElementsByName(\"message\")[0].value"), new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String message) {
                        if (!message.equals("\"\"") & !message.equals("null")) {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                load(false);
                String getCaptcha = getcmd("var img= document.getElementById('imgCaptcha'); var canvas = document.createElement('canvas'); canvas.width = img.naturalWidth; canvas.height = img.naturalHeight; canvas.getContext('2d').drawImage(img, 0, 0); return canvas.toDataURL('image/png').replace(/^data:image\\/(png|jpg);base64,/, '');");
                web.evaluateJavascript(getCaptcha, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String captchaString) {
                        setCaptcha(captchaString);
                    }
                });
            }
            else{
                status.setText("Buying Ingredients...");
                web.setWebViewClient(new scheduleClient());
                web.loadUrl("https://academicscc.vit.ac.in/student/course_regular.asp?sem=FS");
                att.setWebViewClient(new attendanceClient());
                att.loadUrl("https://academicscc.vit.ac.in/student/attn_report.asp?sem=FS");
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view,handler,error);
            handler.proceed();
        }

    } // WEBVIEWCLIENT TO CONTROL LOGIN PAGE

    class scheduleClient extends WebViewClient{
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            vClass.subList.clear();
            vClass.timeTable.clear();
            getFormTable1();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            handler.proceed();
        }
    } // WEBVIEWCLIENT TO GET THE SCHEDULE

    class attendanceClient extends WebViewClient{
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            handler.proceed();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            getAttendance();
            status.setText("Cooking Content...");
        }
    } //WEBVIEWCLIENT TO GET ATTENDANCE

    class compileInf extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            while(!gotAttendance && !gotSchedule){
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //SCRAPING COMPLETE
            for(int i=0;i<vClass.subList.size();i++){
                vClass.subList.get(i).ctd=Integer.parseInt(ctdList.get(i));
                vClass.subList.get(i).attString=attList.get(i)+"%";
                subject x =vClass.subList.get(i);
                Log.d("Sub",x.code+" "+x.type+" "+x.attString);
            }
            for(List<subject> i:vClass.timeTable){
                for(int count=0;count<i.size();count++){
                    subject sub=getSubject(i.get(count).code,i.get(count).type);
                    if(sub!=null){
                        i.get(count).attString=sub.attString;
                        i.get(count).teacher=sub.teacher;
                        i.get(count).title=sub.title;
                        if(sub.type.equals("ELA") || sub.type.equals("LO")){
                            i.remove(i.indexOf(i.get(count))+1);
                            placeCorrectly(i.get(count),i);
                        }
                        //Log.d("Subject",i.get(count).code+" "+" "+ i.get(count).title+" "+i.get(count).teacher+" "+i.get(count).attString+" "+i.get(count).time);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            writeToPrefs();
            //startActivity(new Intent(scrapper.this,dash.class)); //GO TO DASHBOARD
            startActivity(new Intent(scrapper.this,schedule.class));
            finish();
        }
    } //TRIMS AND FINALISES THE INFORMATION
}
