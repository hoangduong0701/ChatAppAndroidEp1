package BotAi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BotActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcometextView;
    EditText messageEdittext;
    ImageButton sendBtn, back_btn;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    public static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();


    float x1, x2, y1, y2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot);
        messageList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view_bot);
        welcometextView = findViewById(R.id.welcome_text);
        messageEdittext = findViewById(R.id.message_edit_text);
        sendBtn = findViewById(R.id.send_btn);
        back_btn = findViewById(R.id.back_btn);


        back_btn.setOnClickListener(view -> {
            onBackPressed();
        });

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager linear = new LinearLayoutManager(this);
        linear.setStackFromEnd(true);
        recyclerView.setLayoutManager(linear);
        sendBtn.setOnClickListener(view -> {
            String question = messageEdittext.getText().toString().trim();
            addToChat(question, Message.SENT_BY_ME);
            messageEdittext.setText("");
            callApi(question);
            welcometextView.setVisibility(View.GONE);
        });




    }
    void  addToChat(String message, String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());

            }
        });

    }
    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response,Message.SENT_BY_BOT);
    }
    void callApi(String question){
        messageList.add(new Message("Typing...", Message.SENT_BY_BOT));
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");
            JSONArray messageArr = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("role", "user");
            obj.put("content", question);
            messageArr.put(obj);
            jsonBody.put("messages", messageArr);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer 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")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ApiCall", "Failed to execute API call", e);
                addResponse("Fail to load response" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    JSONObject jsonObject = null;
                    try {

                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0)
                                        .getJSONObject("message")
                                                .getString("content");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }else {

                    Log.e("ApiCall", "API call not successful: " + response.code());
                    addResponse("Fail to load response" + response.body().string());

                }
            }
        });



    }

    public boolean onTouchEvent(MotionEvent touchEvent){
        switch (touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if (x1 < x2){
                    onBackPressed();
                }


        }

        return false;
    }


}