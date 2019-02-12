package com.example.test_app;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.lang.String;




public class MainActivity extends AppCompatActivity {
    static int countHolder = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener()
        {
            int count =0;

            public void onClick(View v)
            {
                countHolder++;
                Handler handle = new Handler();
                TextView textV = findViewById(R.id.helloText);
                Runnable r = new Runnable()
                {
                    @Override
                    public void run()
                    {
                       //count = 0; breaks the logic below
                    }
                };

                if(count == 0)
                {
                    handle.postDelayed(r, 250);
                    textV.setText("Change Text");
                    count = 1;
                }
                else if(count == 1)
                {
                    handle.postDelayed(r, 250);
                    textV.setText(R.string.helloText);
                    count = 0;
                }

            }
        });

        //code for up button
        final Button upButton = findViewById(R.id.upButton);
        upButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /*Handler handle = new Handler();
                TextView textV = findViewById(R.id.helloText);
                Runnable r = new Runnable() {
                    @Override
                    public void run() {

                    }
                };

                handle.postDelayed(r, 250);
                countHolder = 100;
                textV.setText(Integer.toString(countHolder));
                */
            }
        });

        upButton.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                Handler handle = new Handler();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {


                        TextView textV = findViewById(R.id.helloText);
                        while(countHolder <= 200)
                        {
                            countHolder++;
                            textV.setText(Integer.toString(countHolder));


                        }


                    }

                };
                handle.postDelayed(r, 250);




                return true;

            }

        });

        //code for leftButton
        final Button leftButton = findViewById(R.id.leftButton);
        leftButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Handler handle = new Handler();
                TextView textV = findViewById(R.id.helloText);
                Runnable r = new Runnable() {
                    @Override
                    public void run() {

                    }
                };

                handle.postDelayed(r, 250);
                textV.setText(R.string.leftButton);
            }
        });

        //code for right button
        final Button rightButton = findViewById(R.id.rightButton);
        rightButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Handler handle = new Handler();
                TextView textV = findViewById(R.id.helloText);
                Runnable r = new Runnable() {
                    @Override
                    public void run() {

                    }
                };

                handle.postDelayed(r, 250);
                textV.setText(R.string.rightButton);
            }
        });

        final Button downButton = findViewById(R.id.downButton);
        downButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Handler handle = new Handler();
                TextView textV = findViewById(R.id.helloText);
                Runnable r = new Runnable() {
                    @Override
                    public void run() {

                    }
                };

                handle.postDelayed(r, 250);
                textV.setText(R.string.downButton);
            }
        });




    }
}
