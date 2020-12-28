
# 멍청한 알파오
오목 인공지능 모바일 이식 프로젝트, Tensorflow Lite 활용

# 설명
- [DahamChoi/omok: OMOK AI Project (github.com)](https://github.com/DahamChoi/omok) / 다음 프로젝트를 모바일로 이식시키는 프로젝트입니다.
- .h5 파일을 .tflite 파일로 변환하여 사용
- 기존 파이썬으로 작성된 탐색트리를 Java로 이식
- 난이도 / 선후공 설정 가능
- 바둑알은 CardView의 Round값을 최대로 하여 오목알처럼 보이게 사용, 이미지에셋 사용 X

# 듀얼네트워크 추론 핵심코드
- FloatBuffer를 활용하여 BufferStream을 통해 tflite값을 직접적으로 접근해야 듀얼네트워크의 값을 얻어올 수 있다.

\ **Node.java**

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

# 문제점
 - tflite로 변환하면서 모델구조가 손상, 유사값을 추론하는 것은 확인되었지만 기존의 .h5파일로 추론했을 때의 결과값과 추론결과가 상당히 다른 모습을 보여줌
 - 최소 100회의 추론을 진행해야 올바른 값을 추론해낼 수 있지만 모바일 기기의 성능에 한계가 걸려 10~20회정도의 추론에 10초정도의 시간이 소요되어 올바른 100회이상의 추론을 진행할 경우 정상적인 플레이가 어려움
 - 위의 두가지 문제점이 합쳐져 **'멍청한 알파오'가 탄생함**
