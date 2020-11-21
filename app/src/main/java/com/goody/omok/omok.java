package com.goody.omok;
import java.util.ArrayList;
import java.util.Iterator;

class omok{

    public static int[] convertIntegers(ArrayList<Integer> integers) {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    public boolean equal_integer_array(int[] array1,int[] array2){
        if(array1.length != array2.length){
            return false;
        }

        for(int i = 0; i < array1.length; i++){
            if(array1[i] != array2[i]){
                return false;
            }
        }

        return true;
    }

    public int pieces[];
    public int enemy_pieces[];

    public omok(){
        pieces = new int[225];
        enemy_pieces = new int[225];
    }

    public omok(int[] pieces,int[] enemy_pieces){
        this.pieces = pieces;
        this.enemy_pieces = enemy_pieces;
    }

    public float[][][][] convert_input_shape(){
        float[][][][] input_shape = new float[1][15][15][2];

        for(int i = 0; i < 225; i++){
            int ix = i % 15;
            int iy = i / 15;
            input_shape[0][ix][iy][0] = this.pieces[i];
            input_shape[0][ix][iy][1] = this.enemy_pieces[i];
        }

        return input_shape;
    }

    public float[] convert_input_shape_float_buffer(){
        float[] input_shape = new float[1 * 15 * 15 * 2];
        for(int i = 0; i < 225; i++){
            int ix = i % 15;
            int iy = i / 15;
            input_shape[(30 * iy) + (2 * ix)] = this.pieces[i];
            input_shape[(30 * iy) + (2 * ix) + 1] = this.enemy_pieces[i];
        }

        return input_shape;
    }

    public int piece_count(int[] e_pieces){
        int count = 0;
        for(int i = 0; i < e_pieces.length; i++){
            if(e_pieces[i] == 1){
                count += 1;
            }
        }

        return count;
    }

    public int check_line_count(int x,int y,int dx,int dy,int[] e_pieces){
        int count = 0;
        while(true){
            if(!(y >= 0 && y <= 14 && x >= 0 && x <= 14) || e_pieces[x+(y*15)] == 0){
                break;
            }
            x = x + dx;
            y = y + dy;
            count += 1;
        }

        return count;
    }

    public boolean is_lose(){
        for(int i = 0; i < 225; i++){
            int ix = i % 15;
            int iy = i / 15;
            int dist_list[][] = {
                    {1,0,-1,0},
                    {0,1,0,-1},
                    {1,1,-1,-1},
                    {1,-1,-1,1}
            };

            for(int j = 0; j < 4; j++){
                int dx1 = dist_list[j][0];
                int dy1 = dist_list[j][1];
                int dx2 = dist_list[j][2];
                int dy2 = dist_list[j][3];

                int piece_count = check_line_count(ix,iy,dx1,dy1,this.enemy_pieces) + check_line_count(ix,iy,dx2,dy2,this.enemy_pieces) - 1;

                if(piece_count == 5){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean is_draw(){
        return ((piece_count(pieces) + piece_count(enemy_pieces)) == 225);
    }

    public boolean is_done(){
        return is_lose() || is_draw();
    }

    public omok next(int action){
        int[] temp_pieces = pieces.clone();
        temp_pieces[action] = 1;
        return new omok(enemy_pieces.clone(),temp_pieces.clone());
    }

    public ArrayList<Integer> check_line_type(int[] e_pieces,int[] e_enemy_pieces,int pos,int dx,int dy){
        int pos_x = pos % 15;
        int pos_y = pos / 15;
        int empty_count = 0;
        int start_x = 0;
        int start_y = 0;
        int dst_pos = 0;

        ArrayList<Integer> line_type = new ArrayList<Integer>();

        if(e_pieces[pos] == 0 && e_enemy_pieces[pos] == 0){
            empty_count += 1;
        }

        // 시작점 잡기
        while(true){
            pos_x = pos_x + dx;
            pos_y = pos_y + dy;
            dst_pos = pos_y * 15 + pos_x;

            if(pos_x < 0 && pos_y < 0){
                start_x = 0;
                start_y = 0;
                break;
            }
            if(pos_x < 0){
                start_x = 0;
                start_y = pos_y - dy;
                break;
            }
            if(pos_y < 0){
                start_x = pos_x - dx;
                start_y = 0;
                break;
            }
            if(pos_x > 14 && pos_y > 14){
                start_x = 14;
                start_y = 14;
                break;
            }
            if(pos_x > 14){
                start_x = 14;
                start_y = pos_y - dy;
                break;
            }
            if(pos_y > 14){
                start_x = pos_x - dx;
                start_y = 14;
                break;
            }

            if(e_pieces[dst_pos] == 0 && e_enemy_pieces[dst_pos] == 0){
                empty_count += 1;
                if(empty_count >= 2){
                    start_x = pos_x - dx;
                    start_y = pos_y - dy;
                    break;
                }
            }
            else if(e_enemy_pieces[dst_pos] == 1){
                start_x = pos_x - dx;
                start_y = pos_y - dy;
                break;
            }
            else if(e_pieces[dst_pos] == 1){
                empty_count = 0;
            }
        }

        empty_count = 0;
        dst_pos = start_y * 15 + start_x;
        if(dst_pos < 0 || dst_pos >= 225){
            return line_type;
        }

        pos_x = dst_pos % 15;
        pos_y = dst_pos / 15;

        // 시작점에서 라인 타입 찾기
        while(true){
            if(e_pieces[dst_pos] == 0 && e_enemy_pieces[dst_pos] == 0){
                empty_count += 1;
                if(empty_count >= 2){
                    break;
                }
                else{
                    line_type.add(0);
                }

            }
            else if(e_pieces[dst_pos] == 1){
                line_type.add(1);
                empty_count = 0;
            }
            else if(e_enemy_pieces[dst_pos] == 1){
                break;
            }

            pos_y = pos_y - dy;
            pos_x = pos_x - dx;
            dst_pos = pos_y * 15 + pos_x;

            if(pos_x > 14 || pos_y > 14 || pos_x < 0 || pos_y < 0){
                break;
            }
        }

        return line_type;
    }

    public int check_line_type_open_three(int[] e_pieces,int[] e_enemy_pieces,int index){
        ArrayList<ArrayList<Integer>> line_type = new ArrayList<ArrayList<Integer>>();
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,-1,0));
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,0,-1));
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,-1,-1));
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,-1,1));

        int[][] open_three_type = {
                {0,1,1,1,0},
                {0,1,0,1,1,0},
                {0,1,1,0,1,0}
        };

        int count = 0;
        for(int i = 0; i < line_type.size(); i++){
            for(int j = 0; j < open_three_type.length; j++){
                if(equal_integer_array(open_three_type[j],omok.convertIntegers(line_type.get(i)))){
                    count += 1;
                    break;
                }
            }
        }
        return count;
    }

    public int check_line_type_close_four(int[] e_pieces,int[] e_enemy_pieces,int index){
        ArrayList<ArrayList<Integer>> line_type = new ArrayList<ArrayList<Integer>>();
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,-1,0));
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,0,-1));
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,-1,-1));
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,-1,1));

        int[][] close_four_type = {
                {0,1,1,1,1},
                {1,1,1,1,0},
                {1,1,1,0,1},
                {1,1,0,1,1},
                {1,0,1,1,1}
        };

        int count = 0;
        for(int i = 0; i < line_type.size(); i++){
            for(int j = 0; j < close_four_type.length; j++){
                if(close_four_type[j].equals(omok.convertIntegers(line_type.get(i)))){
                    count += 1;
                    break;
                }
            }
        }
        return count;
    }

    public int check_line_type_open_four(int[] e_pieces,int[] e_enemy_pieces,int index){
        ArrayList<ArrayList<Integer>> line_type = new ArrayList<ArrayList<Integer>>();
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,-1,0));
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,0,-1));
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,-1,-1));
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,-1,1));

        int[][] open_four_type = {
                {0,1,1,1,1,0},
                {0,1,1,0,1,1,0},
                {0,1,1,1,0,1,0},
                {0,1,0,1,1,1,0}
        };

        int count = 0;
        for(int i = 0; i < line_type.size(); i++){
            for(int j = 0; j < open_four_type.length; j++){
                if(open_four_type[j].equals(omok.convertIntegers(line_type.get(i)))){
                    count += 1;
                    break;
                }
            }
        }

        return count;
    }

    public int check_line_type_win_open_four(int[] e_pieces,int[] e_enemy_pieces,int index){
        ArrayList<ArrayList<Integer>> line_type = new ArrayList<ArrayList<Integer>>();
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,-1,0));
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,0,-1));
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,-1,-1));
        line_type.add(check_line_type(e_pieces,e_enemy_pieces,index,-1,1));

        int count = 0;
        int[] win_open_four_type = {0,1,1,1,1,0};
        for(int i = 0; i < line_type.size(); i++){
            if(win_open_four_type.equals(omok.convertIntegers(line_type.get(i)))){
                count += 1;
                break;
            }
        }

        return count;
    }

    public boolean check_enmpty_stone(int pos){

        int pos_x = pos % 15;
        int pos_y = pos / 15;

        int[][] dist_list = {
                {1,1},{1,-1},{-1,-1},{-1,1},{1,0},{-1,0},{0,-1},{0,-1}
        };

        for(int i = 0; i < 8; i++){
            int dx = dist_list[i][0];
            int dy = dist_list[i][1];
            int dist_pos_x = pos_x;
            int dist_pos_y = pos_y;
            for(int j = 0; j < 2; j++){
                dist_pos_x += dx;
                dist_pos_y += dy;
                if(dist_pos_x < 0 || dist_pos_x > 14 || dist_pos_y < 0 || dist_pos_y > 14){
                    break;
                }

                if(this.pieces[dist_pos_x + (dist_pos_y * 15)] == 1 ||
                    this.enemy_pieces[dist_pos_x + (dist_pos_y * 15)] == 1){
                    return false;
                }
            }
        }

        return true;
    }

    public ArrayList<Integer> smart_legal_actions(){

        ArrayList<Integer> actions = new ArrayList<Integer>();
        ArrayList<Integer> default_legal_actions = this.legal_actions();
        ArrayList<Integer> remove_object = new ArrayList<Integer>();
        for(int i = 0; i < default_legal_actions.size(); i++) {
            if(check_enmpty_stone(default_legal_actions.get(i))){
                remove_object.add(default_legal_actions.get(i));
            }
        }
        for(int i = 0; i < remove_object.size(); i++){
            default_legal_actions.remove(remove_object.get(i));
        }

        int dist_list[][] = {
                {1, 0, -1, 0},
                {0, 1, 0, -1},
                {1, 1, -1, -1},
                {1, -1, -1, 1}
        };

        // 내가 5목을 만들 수 있는 상황이면 반드시 승리를 쟁취한다.
        for(int i = 0; i < 225; i++) {
            if(default_legal_actions.contains(i)) {
                int[] temp_pieces = this.pieces.clone();
                temp_pieces[i] = 1;

                int ix = i % 15;
                int iy = i / 15;

                for (int j = 0; j < 4; j++) {
                    int dx1 = dist_list[j][0];
                    int dy1 = dist_list[j][1];
                    int dx2 = dist_list[j][2];
                    int dy2 = dist_list[j][3];

                    int piece_count = check_line_count(ix, iy, dx1, dy1, temp_pieces)
                            + check_line_count(ix, iy, dx2, dy2, temp_pieces) - 1;
                    if (this.is_first_player()) {
                        if (piece_count == 5) {
                            actions.clear();
                            actions.add(i);
                            return actions;
                        }
                    } else {
                        if (piece_count >= 5) {
                            actions.clear();
                            actions.add(i);
                            return actions;
                        }
                    }
                }
            }
        }

        // 상대방이 i 액션을 했을 때 5목을 만든다면 반드시 막는다.
        for(int i = 0; i < 225; i++) {
            if (default_legal_actions.contains(i)) {
                int[] temp_pieces = this.enemy_pieces.clone();
                temp_pieces[i] = 1;

                int ix = i % 15;
                int iy = i / 15;

                for (int j = 0; j < 4; j++) {
                    int dx1 = dist_list[j][0];
                    int dy1 = dist_list[j][1];
                    int dx2 = dist_list[j][2];
                    int dy2 = dist_list[j][3];

                    int piece_count = check_line_count(ix, iy, dx1, dy1, temp_pieces)
                            + check_line_count(ix, iy, dx2, dy2, temp_pieces) - 1;
                    if (this.is_first_player()) {
                        if (piece_count == 5) {
                            actions.clear();
                            actions.add(i);
                            return actions;
                        }
                    } else {
                        if (piece_count >= 5) {
                            actions.clear();
                            actions.add(i);
                            return actions;
                        }
                    }
                }
            }
        }

        for(int i = 0; i < 225; i++) {
            if (default_legal_actions.contains(i)) {
                // 내가 열린 4를 만들 수 있는 상황이라면 반드시 공격한다.
                int[] temp_pieces = this.pieces.clone();
                temp_pieces[i] = 1;

                if(check_line_type_win_open_four(temp_pieces,this.enemy_pieces,i) >= 1){
                    actions.clear();
                    actions.add(i);
                    return actions;
                }

                // 내가 4:3을 만들 수 있다면 반드시 공격한다.
                int close_four_count = check_line_type_close_four(temp_pieces,this.enemy_pieces,i);
                int open_four_count = check_line_type_close_four(temp_pieces,this.enemy_pieces,i);
                int open_three_count = check_line_type_open_three(temp_pieces,this.enemy_pieces,i);

                if((close_four_count >= 1 || open_four_count >= 1) && open_three_count >= 1){
                    actions.clear();
                    actions.add(i);
                    return actions;
                }
            }
        }

        boolean is_have_open_three = false;

        for(int i = 0; i < 225; i++) {
            if (default_legal_actions.contains(i)) {
                // 열린 3은 막는다.
                int open_three_count = check_line_type_open_three(this.enemy_pieces,this.pieces,i);
                if(open_three_count >= 1){
                    actions.add(i);
                    is_have_open_three = true;
                }

                // 내가 선공일 때 상대방이 i 지점에 쌍삼을 만들 수 있다면 막는다.
                int[] temp_pieces = this.enemy_pieces.clone();
                temp_pieces[i] = 1;

                open_three_count = check_line_type_open_three(temp_pieces, this.pieces, i);

                if(this.is_first_player()) {
                    if(open_three_count >= 2){
                        actions.add(i);
                        is_have_open_three = true;
                    }
                }

                // 상대방이 i 지점에 4:3을 만들 수 있다면 막는다.
                int close_four_count = check_line_type_close_four(temp_pieces,this.pieces,i);
                int open_four_count = check_line_type_open_four(temp_pieces,this.pieces,i);

                if((close_four_count >= 1 || open_four_count >= 1) && open_three_count >= 1){
                    actions.add(i);
                    is_have_open_three = true;
                }
            }
        }

        // 내가 닫힌4를 만들 수 있는 상황이라면 상대방이 열린3or쌍삼이어도 공격할 수 있다.
        for(int i = 0; i < 225; i++) {
            if (default_legal_actions.contains(i) && is_have_open_three) {
                int[] temp_pieces = this.pieces.clone();
                temp_pieces[i] = 1;

                int close_four_count = check_line_type_close_four(temp_pieces,this.enemy_pieces,i);
                if(close_four_count >= 1){
                    actions.add(i);
                }
            }
        }

        // 내가 후공일떄 쌍삼을 만들 수 있다면 공격한다.
        for(int i = 0; i < 225; i++) {
            if (default_legal_actions.contains(i) && actions.size() == 0) {
                if(!this.is_first_player()){
                    int[] temp_pieces = this.pieces.clone();
                    temp_pieces[i] = 1;

                    int open_three_count = check_line_type_open_three(temp_pieces,this.enemy_pieces,i);
                    if(open_three_count >= 2){
                        actions.add(i);
                    }
                }
            }
        }

        if(actions.size() == 0){
            return default_legal_actions;
        }

        return actions;
    }

    public ArrayList<Integer> legal_actions(){
        ArrayList<Integer> actions = new ArrayList<Integer>();
        for(int i = 0; i < 225; i++){
            if(this.is_first_player()){
                if(this.pieces[i] == 0 && this.enemy_pieces[i] == 0){
                    int[] temp_pieces = this.pieces.clone();
                    temp_pieces[i] = 1;

                    int open_three_count = check_line_type_open_three(temp_pieces,enemy_pieces,i);
                    int close_four_count = check_line_type_close_four(temp_pieces,enemy_pieces,i);
                    int open_four_count = check_line_type_open_three(temp_pieces,enemy_pieces,i);

                    if(open_three_count < 2 && close_four_count + open_four_count < 2){
                        actions.add(i);
                    }
                }
            }
            else{
                if(this.pieces[i] == 0 && this.enemy_pieces[i] == 0){
                    actions.add(i);
                }
            }
        }

        return actions;
    }

    public ArrayList<Integer> envalue_actions(){
        ArrayList<Integer> actions = new ArrayList<Integer>();
        for(int i = 0; i < 225; i++){
            if(this.is_first_player()){
                if(this.pieces[i] == 0 && this.enemy_pieces[i] == 0){
                    int[] temp_pieces = this.pieces.clone();
                    temp_pieces[i] = 1;

                    int open_three_count = check_line_type_open_three(temp_pieces,enemy_pieces,i);
                    int close_four_count = check_line_type_close_four(temp_pieces,enemy_pieces,i);
                    int open_four_count = check_line_type_open_three(temp_pieces,enemy_pieces,i);

                    if(open_three_count >= 2 || close_four_count + open_four_count >= 2){
                        actions.add(i);
                    }
                }
            }
        }

        return actions;
    }

    boolean is_first_player(){
        return piece_count(pieces) == piece_count(enemy_pieces);
    }
}
