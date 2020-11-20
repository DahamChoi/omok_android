package com.goody.omok;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.TensorFlowLite;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    public omok state = new omok();

    public ArrayList<CardView> stone_card_list = new ArrayList<CardView>();
    public CardView spalshdown_card = null;
    public boolean is_human_turn = false;

    public int level_button_index = 0;
    public int turn_select_button_index = 0;
    public int gameState = 0;   //  0 : ready_game, 1 : playing_game

    public Button level_1_button;
    public Button level_2_button;
    public Button level_3_button;

    public Button turn_first_button;
    public Button turn_second_button;

    public Button game_start_button;

    public TextView game_state_textview;

    public FrameLayout go_board_framelayout;
    public ImageView go_board_imageview;

    public Button splashdown_button;

    public Interpreter newbie_interpreter;
    public Interpreter normal_interpreter;
    public Interpreter best_interpreter;

    public static Activity activity;

    public final static int PV_EVALUATE_COUNT = 100;

    static public int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    int predict_mcts(){

        int evaluate_count = PV_EVALUATE_COUNT;

        Node root_node = new Node(new omok(this.state.pieces,this.state.enemy_pieces),0);

        while(true){
            for(int i = 0; i < evaluate_count; i++){
                root_node.evaluate();
            }

            ArrayList<Integer> scores = Node.nodes_to_scores(root_node.child_nodes);
            ArrayList<Integer> legal_actions = state.smart_legal_actions();

            if(scores.size() > 10){
                scores.set(0,0);
            }

            if(Node.integer_array_list_sum(scores) == 0){
                continue;
            }
            else{
                return legal_actions.get(Node.integer_argmax(scores));
            }
        }
    }

    public class AI_Task extends AsyncTask<Void,Void,Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {

            int index = 0;

            switch(level_button_index){
                case 0:
                    index = predict_mcts();
                    break;
                case 1:
                    index = predict_mcts();
                    break;
                case 2:
                    index = predict_mcts();
                    break;
            }

            state = state.next(index);
            return index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer index) {
            super.onPostExecute(index);

            // finish action
            CardView card = stone_card_list.get(index);
            card.setAlpha(1);
            if (turn_select_button_index == 1) {
                card.getChildAt(0).setBackgroundColor(0xFF424242);   // 흑
            } else {
                card.getChildAt(0).setBackgroundColor(0xFFFAF6F6);
            }

            if(state.is_done()){
                game_state_textview.setText("AI의 승리!");
                game_start_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));
                game_start_button.setText("게임 시작");
                gameState = 0;

                state = new omok();
            }else{
                game_state_textview.setText("당신의 턴 진행중...");
                is_human_turn = true;
            }
        }
    }

    public void turn_of_human(int index){
        state = state.next(index);
        if(turn_select_button_index == 0) {
            spalshdown_card.getChildAt(0).setBackgroundColor(0xFF424242);   // 흑
        }else{
            spalshdown_card.getChildAt(0).setBackgroundColor(0xFFFAF6F6);
        }
        spalshdown_card = null;

        splashdown_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));

        if(state.is_done()){
            game_state_textview.setText("인간의 승리!");

            game_start_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));
            game_start_button.setText("게임 시작");
            gameState = 0;

            state = new omok();
        }
        else{
            game_state_textview.setText("AI턴 진행중...");
            is_human_turn = false;

            turn_of_ai();
        }
    }

    public void turn_of_ai(){
        new AI_Task().execute();

        if(state.is_done()){
            game_state_textview.setText("AI의 승리!");

            game_start_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));
            game_start_button.setText("게임 시작");
            gameState = 0;
        }
    }

    public void start_game(){
        // game_start_button_ui
        game_start_button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        game_start_button.setText("게임 종료");

        // game_state = 게임중
        gameState = 1;

        // 돌 초기화
        for(int i = 0; i < stone_card_list.size(); i++){
            stone_card_list.get(i).setAlpha(0);
        }

        // 선공
        if(turn_select_button_index == 0) {
            game_state_textview.setText("당신의 턴 진행중...");
            is_human_turn = true;
        // 후공
        }else{
            game_state_textview.setText("AI 턴 진행중...");
            is_human_turn = false;

            turn_of_ai();
        }
    }

    public void end_game(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        activity = this;

        level_1_button = findViewById(R.id.level_1_button);
        level_2_button = findViewById(R.id.level_2_button);
        level_3_button = findViewById(R.id.level_3_button);

        // 게임난이도 선택 UI
        level_1_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(level_button_index != 0 && gameState == 0){
                    level_button_index = 0;
                    level_1_button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    level_2_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));
                    level_3_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));
                }
            }
        });
        level_2_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(level_button_index != 1 && gameState == 0){
                    level_button_index = 1;
                    level_1_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));
                    level_2_button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    level_3_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));
                }
            }
        });
        level_3_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(level_button_index != 2 && gameState == 0){
                    level_button_index = 2;
                    level_1_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));
                    level_2_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));
                    level_3_button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });

        // 선후공 결정 선택 UI
        turn_first_button = findViewById(R.id.turn_first_button);
        turn_second_button = findViewById(R.id.turn_second_button);
        turn_first_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(turn_select_button_index != 0 && gameState == 0){
                    turn_select_button_index = 0;
                    turn_first_button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    turn_second_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));

                    is_human_turn = true;
                }
            }
        });
        turn_second_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(turn_select_button_index != 1 && gameState == 0){
                    turn_select_button_index = 1;
                    turn_first_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));
                    turn_second_button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                    is_human_turn = false;
                }
            }
        });

        game_start_button = findViewById(R.id.game_start_button);
        game_state_textview = findViewById(R.id.game_state_textview);

        go_board_framelayout = findViewById(R.id.go_board_framelayout);
        go_board_imageview = findViewById(R.id.go_board_imageview);

        // 착수 버튼
        splashdown_button = findViewById(R.id.splashdown_button);
        splashdown_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(spalshdown_card != null){
                    // 착수
                    int index = stone_card_list.indexOf(spalshdown_card);
                    turn_of_human(index);
                }
            }
        });

        // 게임시작, 게임종료 버튼
        game_start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameState == 0) {
                    start_game();
                }else{
                    game_start_button.setBackgroundColor(getResources().getColor(R.color.colorThirdLabel));
                    game_start_button.setText("게임 시작");
                    gameState = 0;

                    game_state_textview.setText("게임이 시작되길 기다리는 중...");
                    state = new omok();
                }
            }
        });

        for(int i = 0; i < 225; i++) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            CardView cardframe = (CardView) inflater.inflate(R.layout.stone_cardview, null);
            go_board_framelayout.addView(cardframe);

            FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                    dpToPx(this,25),dpToPx(this,25)
            );
            int ix = i % 15;                int iy = i / 15;
            int x = 10 + (int)(ix * 26.42); int y = 9 + (int)(iy * 26.42);
            x = dpToPx(this,x);     y = dpToPx(this,y);
            cardParams.setMargins(x,y,0,0);
            cardframe.setLayoutParams(cardParams);
            cardframe.setAlpha(0);
            int finalI = i;
            cardframe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(gameState == 1 && is_human_turn){
                        splashdown_button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        ArrayList<Integer> legal_actions = state.legal_actions();
                        if(v.getAlpha() == 0 && legal_actions.contains(finalI)){
                            FrameLayout cardFrame =
                                    (FrameLayout)stone_card_list.get(finalI).getChildAt(0);
                            cardFrame.setBackgroundColor(0x807C7C7C);
                            v.setAlpha(1);
                            if(spalshdown_card != null) {
                                spalshdown_card.setAlpha(0);
                            }
                            spalshdown_card = (CardView) v;
                        }
                    }
                }
            });

            stone_card_list.add(cardframe);
        }
    }
}