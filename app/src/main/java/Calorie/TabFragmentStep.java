package Calorie;

import java.io.File;
import com.calorie.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  计步器的Fragment
 */
public class TabFragmentStep extends Fragment implements OnClickListener,
		OnChronometerTickListener, OnCheckedChangeListener {
	private static final String TAG = TabFragmentStep.class.getSimpleName();

	//配置存储
	SharedPreferences mySharedPreferences;
	SharedPreferences.Editor editor;

	private View view;
	
	//子线程
	private Thread thread;

	//UI控件
	private TextView tvPercent;
	private ProgressBar pbPercent;
	private TextView tvGoal;
	private TextView tvSteps;
	private Button btReset;
	
	private Chronometer cmPasstime;
	private Button btControl;

	private TextView tvCalorie;
	private TextView tvDistance;
	private TextView tvSpeed;

	private TextView tvSex;
	private TextView tvHeight;
	private TextView tvWeight;
	private TextView tvAge;
	private TextView tvSensitive;
	private TextView tvLightive;
	private TextView tvSteplen;

	private RadioGroup rgMode;
	private RadioButton rbStepNormal;
	private RadioButton rbStepPocket;
	public static TextView tvLight;

	//public static ChartView cvLight;

	//选择菜单
	private AlertDialog.Builder dialog;
	private NumberPicker numberPicker;

	
	private float calorie;//卡路里
	private float distance;//路程
	private float speed;//速度

	private String sex;//性别
	private float height;//身高
	private float weight;//体重
	private float steplen;//步长
	private int age;//年龄
	final private  float sensitive=8;//灵敏度


	private int steps;//步数
	private int seconds;//秒数



	public static boolean isOpenMap = false;//地图是否同时开启了
	
	// 计步时会触发，同时设置相关UI
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			steps = AccelerometerSensorListener.CURRENT_SETP;
			float percent = steps * 100 / pbPercent.getMax();
			tvPercent.setText(String.valueOf(percent) + "%");
			pbPercent.setProgress(steps);
			tvSteps.setText("" + steps);

			calAddData();
		};
	};

	public TabFragmentStep() {
	}


	/**
	 * 计算卡路里，路程，均速等
	 */
	protected void calAddData() {
		// TODO Auto-generated method stub
		// 公式来源：http://zhidao.baidu.com/question/97028686.html?fr=ala0

		distance = steps * steplen / (100);
		tvDistance.setText(distance + "");
		float msSpeed;

		if (seconds == 0) {
			msSpeed = 0;
		} else {
			msSpeed = distance / seconds;
		}
		float kmhSpeed = (float) (3.6 * msSpeed);
		speed = kmhSpeed;
		tvSpeed.setText(speed + "");

//		double K = 30.0 / (400.0 / (msSpeed * 60));
//		calorie = (float) (weight * 1000 * (seconds / 3600) * K);
		calorie = (float) (weight * steps * steplen * 0.01 * 0.01);
		
		tvCalorie.setText(calorie + "");

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.view = inflater.inflate(R.layout.tab_fragment_step, container,
				false);
		Log.i(TAG, "onCreateView");

		mSubThread();
		initView();
	return view;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i(TAG, "onStart");

		restorePersonalData();
		initPersonalData();

	}

	// 读取sharepreference数据初始化相关配置
	private void restorePersonalData() {
		// TODO Auto-generated method stub
		mySharedPreferences = getActivity().getSharedPreferences(
				"personalData", Activity.MODE_PRIVATE);
		// editor = mySharedPreferences.edit();

		sex = mySharedPreferences.getString("sex", "男");
		height = mySharedPreferences.getFloat("height", 175);
		weight = mySharedPreferences.getFloat("weight", 65);
		steplen = mySharedPreferences.getFloat("steplen", 80);
		age = mySharedPreferences.getInt("age", 20);




	}

	// 根据配置数据初始化UI显示
	private void initPersonalData() {
		tvSex.setText(sex);
		tvHeight.setText(height + "");
		tvWeight.setText(weight + "");
		tvSteplen.setText(steplen + "");
		tvAge.setText(age + "");

		AccelerometerSensorListener.SENSITIVITY = sensitive;
		//tvLightive.setText(lightive + "");
//		LIGHT_BORDER = lightive;
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		// savePersonalData();
		super.onStop();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub

		super.onPause();
	}

	// 保存相关配置到sharepreference
	private void savePersonalData() {
		// TODO Auto-generated method stub
		Log.i(TAG, "save data");

		editor = mySharedPreferences.edit();

		editor.putString("sex", sex);
		editor.putFloat("height", height);
		editor.putFloat("weight", weight);
		editor.putFloat("steplen", steplen);
		editor.putInt("age", age);
		editor.putFloat("sensitive", sensitive);
		//editor.putFloat("lightive", lightive);
		editor.commit();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	
	/**
	 * 
	 */
	private void mSubThread() {
		if (thread == null) {
			thread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (AccelerometerSensorService.isRun) {
							Message msg = new Message();
							handler.sendMessage(msg);
						}

					}
				}
			});
			thread.start();
		}

	}

	
	/**
	 * 初始化UI控件
	 */
	private void initView() {
		tvPercent = (TextView) view.findViewById(R.id.tv_percent);
		pbPercent = (ProgressBar) view.findViewById(R.id.pb_percent);
		tvGoal = (TextView) view.findViewById(R.id.tv_goal);
		tvGoal.setOnClickListener(this);
		tvSteps = (TextView) view.findViewById(R.id.tv_steps);
		tvSteps.setOnClickListener(this);
		btReset = (Button) view.findViewById(R.id.bt_reset);
		btReset.setOnClickListener(this);
		cmPasstime = (Chronometer) view.findViewById(R.id.cm_passtime);
		btControl = (Button) view.findViewById(R.id.bt_control);
		btControl.setOnClickListener(this);
		tvCalorie = (TextView) view.findViewById(R.id.tv_calorie);
		tvDistance = (TextView) view.findViewById(R.id.tv_distance);
		tvSpeed = (TextView) view.findViewById(R.id.tv_speed);

		tvSex = (TextView) view.findViewById(R.id.tv_sex);
		tvSex.setOnClickListener(this);
		tvHeight = (TextView) view.findViewById(R.id.tv_height);
		tvHeight.setOnClickListener(this);
		tvWeight = (TextView) view.findViewById(R.id.tv_weight);
		tvWeight.setOnClickListener(this);
		tvAge = (TextView) view.findViewById(R.id.tv_age);
		tvAge.setOnClickListener(this);
		//tvSensitive = (TextView) view.findViewById(R.id.tv_sensitive);


		//tvLightive.setOnClickListener(this);
		tvSteplen = (TextView) view.findViewById(R.id.tv_steplen);
		tvSteplen.setOnClickListener(this);

		rgMode = (RadioGroup) view.findViewById(R.id.step_mode);

		//rbStepNormal = (RadioButton) view.findViewById(R.id.step_normal);

//		tvLight = (TextView) view.findViewById(R.id.tv_light);

//		cvLight = (ChartView) view.findViewById(R.id.cv_light);

		pbPercent.setMax(10000);
		cmPasstime.setOnChronometerTickListener(this);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			// 相当于Fragment的onResume
			// Log.v("tag", "resume");
			// Toast.makeText(getActivity(), "resume",
			// Toast.LENGTH_SHORT).show();
		} else {
			// 相当于Fragment的onPause
			// Log.v("tag", "pause");

		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}




	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(getActivity(),
				AccelerometerSensorService.class);

		switch (view.getId()) {
		case R.id.tv_goal:
			// 设置目标
			final EditText editText = new EditText(getActivity());
			editText.setText(tvGoal.getText());
			new AlertDialog.Builder(getActivity())
					.setTitle("請輸入您的目標")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(editText)
					.setPositiveButton("確定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									tvGoal.setText(editText.getText()
											.toString().trim());
									pbPercent.setMax(Integer.parseInt(editText
											.getText().toString().trim()));
								}
							}).setNegativeButton("取消", null).show();
			break;
		case R.id.bt_reset:
			// 重置按钮
			reset();
			if (isOpenMap) {
				TabFragmentMap.bt_ctrlTrack.performClick();//同时关闭轨迹记录
			}
			break;
		case R.id.bt_control:
			// 控制按钮，开始，暂停，继续
			if (btControl.getText().equals("開始")) {
				Toast.makeText(getActivity(), "開始軌跡記錄",
						Toast.LENGTH_SHORT).show();

				if (!TabFragmentMap.isRecording) {
					TabFragmentMap.showflag = false;
					TabFragmentMap.bt_ctrlTrack.performClick();// 模拟点击
					TabFragmentMap.showflag = true;
					isOpenMap = true;
				}
				getActivity().startService(intent);
				cmPasstime.setBase(SystemClock.elapsedRealtime());
				cmPasstime.start();
				btControl.setText("暫停");
			} else if (btControl.getText().equals("暫停")) {
				getActivity().stopService(intent);
				cmPasstime.stop();
				btControl.setText("繼續");
			} else if (btControl.getText().equals("繼續")) {
				getActivity().startService(intent);
				cmPasstime.start();
				btControl.setText("暫停");
			}

			break;
		case R.id.tv_sex:
			// 设置性别
			dialog = new AlertDialog.Builder(getActivity());
			final String[] sexlist = { "男", "女" };
			// 设置一个下拉的列表选择项
			dialog.setItems(sexlist, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					tvSex.setText(sexlist[which]);
					sex = sexlist[which];
					savePersonalData();
				}
			});
			dialog.show();
			break;
		case R.id.tv_age:
			// 设置年龄
			dialog = new AlertDialog.Builder(getActivity());
			numberPicker = new NumberPicker(getActivity());
			numberPicker.setFocusable(true);
			numberPicker.setFocusableInTouchMode(true);
			numberPicker.setMaxValue(150);
			numberPicker.setValue(Integer.parseInt(tvAge.getText().toString()
					.trim()));
			numberPicker.setMinValue(1);
			dialog.setView(numberPicker);
			dialog.setPositiveButton("確定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							tvAge.setText(numberPicker.getValue() + "");
							age = numberPicker.getValue();
							savePersonalData();
						}
					});
			dialog.show();
			break;
		case R.id.tv_height:
			// 设置身高
			dialog = new AlertDialog.Builder(getActivity());
			numberPicker = new NumberPicker(getActivity());
			numberPicker.setFocusable(true);
			numberPicker.setFocusableInTouchMode(true);
			numberPicker.setMaxValue(200);
			numberPicker.setValue((int) Float.parseFloat(tvHeight.getText()
					.toString().trim()));
			numberPicker.setMinValue(20);
			dialog.setView(numberPicker);
			dialog.setPositiveButton("確定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							tvHeight.setText(numberPicker.getValue() + "");
							height = numberPicker.getValue();
							savePersonalData();
						}
					});
			dialog.show();
			break;
		case R.id.tv_weight:
			// 设置体重
			dialog = new AlertDialog.Builder(getActivity());
			numberPicker = new NumberPicker(getActivity());
			numberPicker.setFocusable(true);
			numberPicker.setFocusableInTouchMode(true);
			numberPicker.setMaxValue(200);
			numberPicker.setValue((int) Float.parseFloat(tvWeight.getText()
					.toString().trim()));
			numberPicker.setMinValue(20);
			dialog.setView(numberPicker);
			dialog.setPositiveButton("確定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							tvWeight.setText(numberPicker.getValue() + "");
							weight = numberPicker.getValue();
							savePersonalData();
						}
					});
			dialog.show();
			break;
		case R.id.tv_steplen:
			// 设置步长
			dialog = new AlertDialog.Builder(getActivity());
			numberPicker = new NumberPicker(getActivity());
			numberPicker.setFocusable(true);
			numberPicker.setFocusableInTouchMode(true);
			numberPicker.setMaxValue(100);
			numberPicker.setValue((int) Float.parseFloat(tvSteplen.getText()
					.toString().trim()));
			numberPicker.setMinValue(15);
			dialog.setView(numberPicker);
			dialog.setPositiveButton("確定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							tvSteplen.setText(numberPicker.getValue() + "");
							steplen = numberPicker.getValue();
							savePersonalData();

						}
					});
			dialog.show();
			break;

		}

	}

	/**
	 * 重置步数等信息
	 */
	private void reset() {
		Intent intent = new Intent(getActivity(),
				AccelerometerSensorService.class);
		getActivity().stopService(intent);
		AccelerometerSensorListener.reset();
		steps = 0;
		seconds = 0;

		tvPercent.setText("0.0");
		pbPercent.setProgress(0);
		tvGoal.setText("10000");
		tvSteps.setText("0.0");
		// tvPasstime.setText("00:00:00");
		cmPasstime.setBase(SystemClock.elapsedRealtime());
		cmPasstime.stop();
		btControl.setText("開始");
		tvCalorie.setText("0.0");
		tvDistance.setText("0.0");
		tvSpeed.setText("0.0");



	}

	/**
	 * 格式化时间显示
	 */
	@Override
	public void onChronometerTick(Chronometer arg0) {
		// TODO Auto-generated method stub
		seconds++;
		cmPasstime.setText(formatseconds());
	}

	public String formatseconds() {
		String hh = seconds / 3600 > 9 ? seconds / 3600 + "" : "0" + seconds
				/ 3600;
		String mm = (seconds % 3600) / 60 > 9 ? (seconds % 3600) / 60 + ""
				: "0" + (seconds % 3600) / 60;
		String ss = (seconds % 3600) % 60 > 9 ? (seconds % 3600) % 60 + ""
				: "0" + (seconds % 3600) % 60;

		return hh + ":" + mm + ":" + ss;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkID) {

	}}

