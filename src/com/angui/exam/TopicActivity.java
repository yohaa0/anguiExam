package com.angui.exam;

import java.util.Calendar;

import com.angui.exam.adapter.TopicFragmentCallBacks;
import com.angui.exam.controller.TopicController;
import com.angui.exam.util.FileUtil;
import com.angui.exam.util.UiUtil;
import com.angui.exam.util.WindowUtil;
import com.angui.exam.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class TopicActivity extends FragmentActivity {
	private ViewPager topic_pager;
	private int mode;
	private int subClass; // 章节练习中的章节，强化练习中的类型
	private TopicController tc;
	private FileUtil fu;
	private TextView tv_title;
	private Button btn_topic_changeLabel;
	private Button btn_switch_answer_show;
	private boolean answerShowFlag = false;
	private TopicFragmentCallBacks topicFragmentCallBacks;
	private Chronometer ch_topic_test;
	// for seek
	private PopupWindow seekPopupWindow;
	private ImageButton btn_seek;
	private View seekView;
	private ImageButton ib_seek_ok;
	private ImageButton ib_seek_cancel;
	private SeekBar sb_seek;
	private TextView tv_progress;
	private int nowTopic;
	private int totalTopic;
	private int newTopic;
	private WindowUtil wu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 去标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mode = getIntent().getExtras().getInt("mode");
		subClass = getIntent().getExtras().getInt("subClass");
		if (mode != TopicController.MODE_PRACTICE_TEST) {
			setContentView(R.layout.activity_topic);
		} else {
			setContentView(R.layout.activity_topic_test);
		}

		// 设置背景
		getWindow().setBackgroundDrawableResource(R.drawable.bg_base);

		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(getResources().getStringArray(R.array.topic_title)[mode]);

		tc = new TopicController(this, mode, subClass);
		topic_pager = (ViewPager) findViewById(R.id.topic_pager);

		topicFragmentCallBacks = getTopicFragmentCallBacks();
		topic_pager.setAdapter(getPagerAdapter());
		topic_pager.setOnPageChangeListener(getOnPageChangeListener());

		// for seek
		btn_seek = (ImageButton) findViewById(R.id.btn_seek);
		seekView = getLayoutInflater()
				.inflate(R.layout.popup_window_seek, null);
		seekPopupWindow = new PopupWindow(seekView, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);

		initItem();

	}

	// 截屏
	public void shotView(View view) {
		fu = new FileUtil(this);
		Bitmap bm = fu.shotAndSave(fu.getPic_path());
		Intent toShare = new Intent(this, ShareFriendActivity.class);
		startActivity(toShare);
		// 保存完毕，及时回收
		if (!bm.isRecycled()) {
			bm.recycle();
		}
	}

	// 返回
	public void toBack(View view) {
		if (mode != TopicController.MODE_PRACTICE_TEST) {
			tc.dataSave(topic_pager.getCurrentItem());
			finish();
		}
	}
//前一题
	public void toPreTopic(View view) {
		int page = topic_pager.getCurrentItem();
		if (page == 0) {
			UiUtil.showToastShort(this, R.string.topic_first_question);
		} else {
			topic_pager.setCurrentItem(page - 1);
		}
	}
//下一题
	public void toNextTopic(View view) {
		int page = topic_pager.getCurrentItem();
		if (page == tc.getTopicList().size() - 1) {
			UiUtil.showToastShort(this, R.string.topic_last_question);
		} else {
			topic_pager.setCurrentItem(page + 1);
		}
	}
//任意跳转
	public void toChangeLabel(View view) {

		int daoId = tc.getDaoId(topic_pager.getCurrentItem() + 1);
		if (mode == TopicController.MODE_WRONG_TOPIC) {
			int flag = tc.getInWrongFlag(daoId);
			if (flag == 0) {
				tc.setInWrongFlag(daoId);
				btn_topic_changeLabel.setText(R.string.topic_del_wrong);
			} else {
				tc.resetInWrongFlag(daoId);
				btn_topic_changeLabel.setText(R.string.topic_add_wrong);
			}
		} else {
			int flag = tc.getCollectedFlag(daoId);
			if (flag == 0) {
				tc.setCollectedFlag(daoId);
				btn_topic_changeLabel.setText(R.string.topic_cancel_collect);
			} else {
				tc.resetCollectedFlag(daoId);
				btn_topic_changeLabel.setText(R.string.topic_set_collect);
			}
		}
	}
//显示答案
	public void toSwitchAnswerShow(View view) {
		if (mode == TopicController.MODE_PRACTICE_TEST) {
			Log.e("Topic", "Please check layout");
			return;
		}
		int currentItem = topic_pager.getCurrentItem();
		if (answerShowFlag) {
			answerShowFlag = false;
			btn_switch_answer_show.setText(R.string.topic_answer_show);
		} else {
			answerShowFlag = true;
			btn_switch_answer_show.setText(R.string.topic_answer_hide);
		}
		tc.setAnswerShow(answerShowFlag);
		topic_pager.setAdapter(getPagerAdapter());

		topic_pager.setCurrentItem(currentItem);
	}

	public void toSeek(View v) {
		if (seekPopupWindow.isShowing()) {
			return;
		}
		nowTopic = topic_pager.getCurrentItem() + 1;
		totalTopic = tc.getTopicList().size();
		wu = new WindowUtil(this);
		Point size = wu.getDefaultDisplaySize();
		seekPopupWindow.showAtLocation(btn_seek, Gravity.BOTTOM, 0,
				Math.min(size.x, size.y) * 45 / 320);
		ib_seek_ok = (ImageButton) seekView.findViewById(R.id.ib_seek_ok);
		ib_seek_cancel = (ImageButton) seekView
				.findViewById(R.id.ib_seek_cancel);
		sb_seek = (SeekBar) seekView.findViewById(R.id.sb_seek);
		tv_progress = (TextView) seekView.findViewById(R.id.tv_progress);
		final String topic_seek = getString(R.string.topic_seek);
		tv_progress.setText(topic_seek + "     " + nowTopic + "/" + totalTopic);
		sb_seek.setMax(totalTopic - 1);
		sb_seek.setProgress(nowTopic - 1);
		sb_seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				topic_pager.setCurrentItem(newTopic - 1);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				newTopic = seekBar.getProgress() + 1;
				tv_progress.setText(topic_seek + "     " + newTopic + "/"
						+ totalTopic);
			}
		});
		ib_seek_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				seekPopupWindow.dismiss();
			}
		});
		ib_seek_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				topic_pager.setCurrentItem(nowTopic - 1);
				seekPopupWindow.dismiss();
			}
		});
	}

	private TopicFragmentCallBacks getTopicFragmentCallBacks() {
		return new TopicFragmentCallBacks() {

			@Override
			public void snapToScreen(int position) {
				// TODO Auto-generated method stub
				topic_pager.setCurrentItem(position);
			}
		};
	}

	private OnPageChangeListener getOnPageChangeListener() {
		return new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				int daoId = tc.getDaoId(topic_pager.getCurrentItem() + 1);
				if (mode == TopicController.MODE_WRONG_TOPIC) {
					int flag = tc.getInWrongFlag(daoId);
					if (flag == 0) {
						btn_topic_changeLabel.setText(R.string.topic_add_wrong);
					} else {
						btn_topic_changeLabel.setText(R.string.topic_del_wrong);
					}
				} else {
					int flag = tc.getCollectedFlag(daoId);
					if (flag == 0) {
						btn_topic_changeLabel
								.setText(R.string.topic_set_collect);
					} else {
						btn_topic_changeLabel
								.setText(R.string.topic_cancel_collect);
					}
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub

			}
		};
	}

	private FragmentPagerAdapter getPagerAdapter() {
		FragmentPagerAdapter fpa = tc.getPagerAdapter(
				getSupportFragmentManager(), topicFragmentCallBacks);
		fpa.notifyDataSetChanged();
		return fpa;
	}

	private void initItem() {
		// for changeLabel
		int initItem = 0;
		btn_topic_changeLabel = (Button) findViewById(R.id.btn_topic_changeLabel);
		if (mode != TopicController.MODE_PRACTICE_TEST) {

			initItem = tc.dataLoad();
			topic_pager.setCurrentItem(initItem);
			if (mode == TopicController.MODE_WRONG_TOPIC) {
				btn_topic_changeLabel.setText(R.string.topic_del_wrong);
			} else {
				int daoId = tc.getDaoId(initItem + 1);
				int flag = tc.getCollectedFlag(daoId);
				if (flag == 0) {
					btn_topic_changeLabel.setText(R.string.topic_set_collect);
				} else {
					btn_topic_changeLabel
							.setText(R.string.topic_cancel_collect);
				}
			}
			// for switch_answer_show
			btn_switch_answer_show = (Button) findViewById(R.id.btn_switch_answer_show);
			btn_switch_answer_show.setText(R.string.topic_answer_show);
		} else {
			ch_topic_test = (Chronometer) findViewById(R.id.ch_topic_test);
			ch_topic_test.start();
			int daoId = tc.getDaoId(initItem + 1);
			int flag = tc.getCollectedFlag(daoId);
			if (flag == 0) {
				btn_topic_changeLabel.setText(R.string.topic_set_collect);
			} else {
				btn_topic_changeLabel.setText(R.string.topic_cancel_collect);
			}
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (mode != TopicController.MODE_PRACTICE_TEST) {
			tc.dataSave(topic_pager.getCurrentItem());
			super.onBackPressed();
		} else {
			submitTest();
		}
	}

	public void toSubmit(View v){
		submitTest();
	}
	private void submitTest() {
		new AlertDialog.Builder(TopicActivity.this)
				.setTitle(getResources().getString(R.string.topic_exit_exam))
				.setNegativeButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								String useTime = ch_topic_test.getText()
										.toString();
								int totalCount = tc.getRightCount()
										+ tc.getWrongCount();
								int totalScore = tc.getRightCount() * 2;
								Calendar c = Calendar.getInstance();
								String dateTime = c.get(Calendar.YEAR) + "-"
										+ ((Integer)c.get(Calendar.MONTH)+1) + "-"
										+ c.get(Calendar.DAY_OF_MONTH);
								tc.addTestScore(totalScore, tc.getRightCount(),
										tc.getWrongCount(), totalCount,
										dateTime, useTime);
								Intent intent = new Intent(TopicActivity.this,
										DetailsRecordActivity.class);
								intent.putExtra("MODE", 1);
								startActivity(intent);
								finish();
							}
						})
				.setPositiveButton(getResources().getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
							}
						}).show();
	}
	
}
