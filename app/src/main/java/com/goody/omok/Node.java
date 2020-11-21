package com.goody.omok;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Node {
    public omok state;
    public float p;     // 정책
    public float w;     // 가치 누계
    public int n;       // 시행 횟수
    public ArrayList<Node> child_nodes;
    public static Interpreter interpreter = null;

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = FullscreenActivity.activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public Node(omok state,float p){
        this.state = state;
        this.p = p;
        this.w = 0;
        this.n = 0;
        this.child_nodes = new ArrayList<Node>();
    }

    public float evaluate(){

        Log.e("NODE","EVALUATE");

        float value = 0;

        // Play out
        if(this.state.is_done()){
            value = this.state.is_lose() ? -1 : 0;

            this.w += value;
            this.n += 1;
            return value;
        }

        // 자녀 노드가 존재하지 않는 경우
        if(this.child_nodes.size() == 0){

            if(interpreter == null) {
                interpreter = getTfliteInterpreter("best.tflite");
                interpreter.resizeInput(0,new int[]{1,15,15,2});
                interpreter.allocateTensors();
            }

            int input_tensor_num_element = interpreter.getInputTensor(0).numElements();
            int output_tensor_num_element_value = interpreter.getOutputTensor(0).numElements();
            int output_tensor_num_element_policies = interpreter.getOutputTensor(1).numElements();

            FloatBuffer input_buffer = FloatBuffer.allocate(input_tensor_num_element);
            input_buffer.put(state.convert_input_shape_float_buffer());
            Map map_of_indices_to_outputs = new HashMap<>();
            FloatBuffer output_buffer_value = FloatBuffer.allocate(output_tensor_num_element_value);
            FloatBuffer output_buffer_policies = FloatBuffer.allocate(output_tensor_num_element_policies);
            map_of_indices_to_outputs.put(0,output_buffer_value);
            map_of_indices_to_outputs.put(1,output_buffer_policies);

            FloatBuffer[] inputs = new FloatBuffer[1];
            inputs[0] = input_buffer;

            interpreter.runForMultipleInputsOutputs(inputs,map_of_indices_to_outputs);

            value = output_buffer_value.get(0);

            ArrayList<Float> legal_policies = new ArrayList<Float>();
            ArrayList<Integer> legal_action = state.smart_legal_actions();
            ArrayList<Integer> police_legal_action = new ArrayList<Integer>();

            for(int i = 0; i < output_tensor_num_element_policies; i++){
                if(legal_action.contains(i)){
                    legal_policies.add(output_buffer_policies.get(i));
                    police_legal_action.add(i);
                }
            }

            float sum_legal_policies = float_array_list_sum(legal_policies);
            for(int i = 0; i < legal_policies.size(); i++){
                float percent = legal_policies.get(i) / sum_legal_policies;
                legal_policies.set(i,percent);
            }

            this.w += value;
            this.n += 1;

            for(int i = 0; i < legal_policies.size(); i++){
                this.child_nodes.add(new Node(state.next(police_legal_action.get(i)),legal_policies.get(i)));
            }

            return value;

        }else{
            // 자녀 노드가 존재하는 경우
            value = -this.next_child_node().evaluate();

            this.w += value;
            this.n += 1;
            return value;
        }
    }

    public Node next_child_node(){
        float C_PUCT = 1.0f;
        int t = integer_array_list_sum(nodes_to_scores(this.child_nodes));
        ArrayList<Float> pucb_values = new ArrayList<Float>();
        for(int i = 0; i < this.child_nodes.size(); i++){
            Node child_node = child_nodes.get(i);
            float pucb_value;
            if(child_node.n != 0) {
                pucb_value = ((-child_node.w / child_node.n) +
                        (C_PUCT * child_node.p * (float)Math.sqrt(t)) / (1 + child_node.n));
            }
            else{
                pucb_value = ((-child_node.n) +
                        (C_PUCT * child_node.p * (float)Math.sqrt(t)) / (1 + child_node.n));
            }

            pucb_values.add(pucb_value);
        }

        return this.child_nodes.get(float_argmax(pucb_values));
    }

    public static ArrayList<Integer> nodes_to_scores(ArrayList<Node> child_node){
        ArrayList<Integer> scores = new ArrayList<Integer>();
        for(int i = 0; i < child_node.size(); i++){
            scores.add(child_node.get(i).n);
        }

        return scores;
    }

    public static float float_array_list_sum(ArrayList<Float> array){
        float sum = 0;
        for(int i = 0; i < array.size(); i++){
            sum += array.get(i);
        }

        return sum;
    }

    public static int integer_array_list_sum(ArrayList<Integer> array){
        int sum = 0;
        for(int i = 0; i < array.size(); i++){
            sum += array.get(i);
        }

        return sum;
    }

    public static int float_argmax(ArrayList<Float> array){
        int max_index = 0;
        float max_number = 0;

        for(int i = 0; i < array.size(); i++){
            if(array.get(i) > max_number){
                max_index = i;
                max_number = array.get(i);
            }
        }

        return max_index;
    }

    public static int integer_argmax(ArrayList<Integer> array){
        int max_index = 0;
        int max_number = 0;

        for(int i = 0; i < array.size(); i++){
            if(array.get(i) > max_number){
                max_index = i;
                max_number = array.get(i);
            }
        }

        return max_index;
    }
}
