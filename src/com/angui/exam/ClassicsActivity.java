package com.angui.exam;
import java.util.Map;

import com.angui.exam.model.QuestionBankService;
import com.angui.exam.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ClassicsActivity extends BaseActivity {
	private QuestionBankService questionBankService;
	private TextView tv_title;
	private TextView tv_classics_question;
	private TextView tv_classics_answer;
	private Button btn_classics_show_answer;
	private boolean bl_answer;
	private EditText editText; //新加用户输入
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		bl_answer = true;
		setContentView(R.layout.activity_classics);
		questionBankService = new QuestionBankService();
		Map<String, Object> enerty = questionBankService.getEntry(this, String.valueOf(getIntent().getIntExtra("questionId", 1)));
		tv_title= (TextView) findViewById(R.id.tv_title);
		tv_classics_question = (TextView) findViewById(R.id.tv_classics_question);
		tv_classics_answer = (TextView) findViewById(R.id.tv_classics_answer);
		btn_classics_show_answer = (Button) findViewById(R.id.btn_classics_show_answer);
		//
		editText = (EditText) findViewById(R.id.tv_classics_useranswer); 
		//新加用户输入
		tv_title.setText(getResources().getString(R.string.classics_title));
		tv_classics_question.setText(enerty.get("question").toString());
		tv_classics_question.setTextSize(22);
		tv_classics_answer.setText(enerty.get("answer").toString());
		tv_classics_answer.setTextSize(22);
		tv_classics_answer.setVisibility(LinearLayout.INVISIBLE);
		
	}
	
	public void showAnswer(View view) {
		if (bl_answer) {
			btn_classics_show_answer.setText(getResources().getString(
					R.string.classics_not_show_answer));
			tv_classics_answer.setVisibility(LinearLayout.VISIBLE);
			bl_answer = false;
			//Toast用户输入,字符比较出错，待查
			String S_answer=(tv_classics_answer.getText().toString()).trim();
			String S_useranswer=(editText.getText().toString()).trim();
			String inputText = "你的答案："+S_useranswer+" 正确";
			String inputText2="你的答案："+S_useranswer+"\n正确答案："+S_answer;
			if(S_useranswer.equals(S_answer))
			  Toast.makeText(ClassicsActivity.this, inputText, 
					Toast.LENGTH_SHORT).show(); 
			else
				Toast.makeText(ClassicsActivity.this, inputText2, 
						Toast.LENGTH_SHORT).show(); 
		      //
		} else {
			btn_classics_show_answer.setText(getResources().getString(
					R.string.classics_show_answer));
			tv_classics_answer.setVisibility(LinearLayout.INVISIBLE);
			bl_answer = true;
		}
	}

	
}
