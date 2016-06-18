package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener {

	private LinearLayout weatherInfoLayout;
	/**
	 * 用于显示城市名
	 */
	private TextView cityNameText;
	/**
	 * 用于发布信息
	 */
	private TextView publishText;
	/**
	 * 用于显示天气描述信息
	 */
	private TextView weatherDespText;
	/**
	 * 用于显示气温
	 */
	private TextView temperatureText;
	/**
	 * 用于显示当前日期
	 */
	private TextView currentDateText;
	/**
	 * 用于显示风力信息
	 */
	private TextView windText;
	/**
	 * 用于显示PM2.5浓度
	 */
	private TextView pm25Text;
	/**
	 * 切换城市按钮
	 */
	private Button switchCity;
	/**
	 * 更新天气按钮
	 */
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化各控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temperatureText = (TextView) findViewById(R.id.temperature);
		currentDateText = (TextView) findViewById(R.id.current_date);
		windText = (TextView) findViewById(R.id.wind);
		pm25Text = (TextView) findViewById(R.id.pm25);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		String countyName = getIntent().getStringExtra("county_name");
		if(!TextUtils.isEmpty(countyName)){
			//有县级地名时就去查询天气
			publishText.setText("同步中...");
			publishText.setVisibility(View.VISIBLE);
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherInfo(countyName);
		} else{
			//没有县级地名时就直接显示本地天气
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        switch (v.getId()){
        case R.id.switch_city:
        	Intent intent = new Intent(this, ChooseAreaActivity.class);
        	intent.putExtra("from_weather_activity", true);
        	startActivity(intent);
        	finish();
        	break;
        case R.id.refresh_weather:
        	publishText.setText("同步中...");
        	publishText.setVisibility(View.VISIBLE);
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
        	SharedPreferences prefs = PreferenceManager.
        			getDefaultSharedPreferences(this);
        	String countyName = prefs.getString("city_name", "");
        	if(!TextUtils.isEmpty(countyName)){
        		queryWeatherInfo(countyName);
        	}
        	break;
        default:
        	break;
        }
	}
	
	
	/**
	 * 查询县级地名所对应的天气
	 */
	private void queryWeatherInfo(String countyName){
		String address = "http://api.map.baidu.com/telematics/v3/weather?location=" + countyName +"&output=json&ak=6tYzTvGZSOpYB5Oc2YGGOKt8";
		queryFromServer(address);
	}
	
	/**
	 * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
	 */
	private void queryFromServer(final String address){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){
			@Override
			public void onFinish(final String response){
				
					if(!TextUtils.isEmpty(response)){
						//从服务器返回的数据中天气信息
						Utility.handleWeatherResponse(WeatherActivity.this, response);
						runOnUiThread(new Runnable(){
							@Override
							public void run(){
								showWeather();
							}
						});
					}
			}
			
			@Override
			public void onError(Exception e){
				runOnUiThread(new Runnable(){
					@Override
					public void run(){
						publishText.setText("同步失败");
						publishText.setVisibility(View.VISIBLE);
						cityNameText.setVisibility(View.INVISIBLE);
						weatherInfoLayout.setVisibility(View.INVISIBLE);
					}
				});
			}
		});
	}
	
	/**
	 * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上
	 */
	private void showWeather(){
		SharedPreferences prefs = PreferenceManager.
				getDefaultSharedPreferences(this);
				cityNameText.setText(prefs.getString("city_name", ""));
				temperatureText.setText(prefs.getString("temperature", ""));
				weatherDespText.setText(prefs.getString("weather_desp", ""));
				currentDateText.setText(prefs.getString("current_date", ""));
				windText.setText(prefs.getString("wind", ""));
				pm25Text.setText("pm2.5:" + prefs.getString("pm25", ""));
				weatherInfoLayout.setVisibility(View.VISIBLE);
				cityNameText.setVisibility(View.VISIBLE);
				publishText.setVisibility(View.INVISIBLE);
				Intent intent = new Intent(this, AutoUpdateService.class);
				startService(intent);
	}

}
