package com.project.android_kidstories;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.project.android_kidstories.Api.Api;
import com.project.android_kidstories.Api.Responses.story.StoryBaseResponse;
import com.project.android_kidstories.DataStore.Repository;
import com.project.android_kidstories.Model.Story;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SingleStoryActivity extends AppCompatActivity {

    private ImageView story_pic, like_btn;
    private TextView story_author , story_content, error_msg;
    int story_id = 0;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private Repository repository;
    private Api storyApi;

    ImageButton btn_speak;
    TextView speak_text;
    TextToSpeech textToSpeech;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_story);
        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        repository = Repository.getInstance(this.getApplication());
        storyApi = repository.getStoryApi();
        story_id = getIntent().getIntExtra("story_id", 0);


        progressBar = findViewById(R.id.story_content_bar);
        progressBar.setVisibility(View.VISIBLE);

        story_author = findViewById(R.id.author);
        story_content = findViewById(R.id.story_content);
        story_pic = findViewById(R.id.story_pic);
        like_btn = findViewById(R.id.like_button);
        error_msg = findViewById(R.id.error_msg);
        //todo : check authorization for premium stories
        getStoryWithId(story_id);
    }

    public void getStoryWithId(int id){
        storyApi.getStory(id).enqueue(new Callback<StoryBaseResponse>() {
            @Override
            public void onResponse(Call<StoryBaseResponse> call, Response<StoryBaseResponse> response) {
                try{
                    Story currentStory = response.body().getData();
                    getSupportActionBar().setTitle(currentStory.getTitle());
                    story_author.setText(currentStory.getAuthor());
                    story_content.setText(currentStory.getBody());
                    Glide.with(getApplicationContext()).load(currentStory.getImageUrl()).placeholder(R.drawable.story_bg_ic).into(story_pic);
                    story_author.setVisibility(View.VISIBLE);
                    story_content.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                } catch(Exception e){
                    Toast.makeText(SingleStoryActivity.this, "Oops Something went wrong ... story specific issue",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    error_msg.setVisibility(View.VISIBLE);
                    story_author.setVisibility(View.INVISIBLE);
                    story_content.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<StoryBaseResponse> call, Throwable t) {
                progressBar.setVisibility(View.INVISIBLE);
                story_author.setVisibility(View.INVISIBLE);
                story_content.setVisibility(View.INVISIBLE);
                error_msg.setVisibility(View.VISIBLE);
                Toast.makeText(SingleStoryActivity.this, "Oops Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });


        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS)
                {
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Toast.makeText(SingleStoryActivity.this, "This Language is not Supported", Toast.LENGTH_SHORT).show();
                    }
                    else {
                    btn_speak.setEnabled(true);
                    textToSpeech.setPitch(0.6f);
                    textToSpeech.setSpeechRate(0.9f);
                    speak();}
                }
            }
        });

        speak_text = (TextView) findViewById(R.id.story_content);
        btn_speak = (ImageButton) findViewById(R.id.play_story);
        btn_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                speak();

            }
        });

    }

    private void speak() {

        String text = speak_text.getText().toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH,null,null);
        }
        else {textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH,null);}
    }

    @Override
    protected void onDestroy() {

        if (textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
