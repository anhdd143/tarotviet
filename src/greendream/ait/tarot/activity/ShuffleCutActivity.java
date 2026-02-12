package greendream.ait.tarot.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import greendream.ait.tarot.R;
import greendream.ait.tarot.data.ConfigData;
import greendream.ait.tarot.view.CutCardLayout;
import greendream.ait.tarot.view.ShuffleCardLayout;

public class ShuffleCutActivity extends Activity implements SensorEventListener {

	private static final int STATE_SHUFFLE = 0;
	private static final int STATE_CUT = 1;
	private int currentState = STATE_SHUFFLE;

	private ShuffleCardLayout shuffleLayout;
	private CutCardLayout cutLayout;
	private Button btnShuffleCut;
	private Button btnDraw;
	private TextView txtInstruction;

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private float lastX, lastY, lastZ;
	private long lastShakeTime;
	private static final int SHAKE_THRESHOLD = 800;

	private int spreadId;
	private int cardCount;
	private boolean isCutDone = false;
	private Vibrator vibrator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_shuffle_cut);
		
		// Ensure ConfigData is loaded
		ConfigData.loadSettingData(this);

		// Get intent data
		Intent intent = getIntent();
		spreadId = intent.getIntExtra("spread_id", 0);
		cardCount = intent.getIntExtra("card_count", 3); // Default 3 if not provided

		initViews();
		initSensor();
		
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	private void initViews() {
		shuffleLayout = (ShuffleCardLayout) findViewById(R.id.shuffleCardLayout);
		cutLayout = (CutCardLayout) findViewById(R.id.cutCardLayout);
		btnShuffleCut = (Button) findViewById(R.id.btnShuffleCut);
		btnDraw = (Button) findViewById(R.id.btnDraw);
		txtInstruction = (TextView) findViewById(R.id.txtInstruction);

		// Initial state
		setShuffleState();

		btnShuffleCut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentState == STATE_SHUFFLE) {
					// Switch to Cut
					setCutState();
				} else {
					// Switch back to Shuffle
					setShuffleState();
				}
			}
		});

		btnDraw.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finishShuffleCut();
			}
		});
		
		// Tap to shuffle as well
		shuffleLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				performShuffle();
			}
		});
		
		// Cut listener
		cutLayout.setOnCutListener(new CutCardLayout.OnCutListener() {
			@Override
			public void onCut() {
				isCutDone = true;
				// Auto-draw after small delay? Or just enable Draw button
				// For now, auto-draw to match Galaxy Tarot
				btnDraw.postDelayed(new Runnable() {
					@Override
					public void run() {
						finishShuffleCut();
					}
				}, 500);
			}
		});
	}

	private void initSensor() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager != null) {
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
	}

	private void setShuffleState() {
		currentState = STATE_SHUFFLE;
		shuffleLayout.setVisibility(View.VISIBLE);
		cutLayout.setVisibility(View.GONE);
		btnShuffleCut.setText("Kinh bài >");
		txtInstruction.setText("Lắc máy để xào bài...");
		isCutDone = false;
		cutLayout.reset();
	}

	private void setCutState() {
		currentState = STATE_CUT;
		shuffleLayout.setVisibility(View.GONE);
		cutLayout.setVisibility(View.VISIBLE);
		btnShuffleCut.setText("< Xào lại");
		txtInstruction.setText("Chọn 1 tụ để kinh bài");
	}

	private void performShuffle() {
		if (currentState == STATE_SHUFFLE) {
			shuffleLayout.shuffle();
			if (vibrator != null) vibrator.vibrate(50);
		}
	}

	private void finishShuffleCut() {
		// Populate ConfigData static arrays directly
		ConfigData.randomCardIdArray = new int[cardCount];
		ConfigData.randomCardDimensionsArray = new int[cardCount];
		
		List<Integer> deck = new ArrayList<Integer>();
		for (int i = 0; i < 78; i++) deck.add(i);
		Collections.shuffle(deck);
		
		Random rand = new Random();
		
		for (int i = 0; i < cardCount; i++) {
			ConfigData.randomCardIdArray[i] = deck.get(i);
			
			// Random dimension (reverse or upright)
			if (ConfigData.IS_REVERSE_CARD) {
				ConfigData.randomCardDimensionsArray[i] = rand.nextInt(2); // 0 or 1
			} else {
				ConfigData.randomCardDimensionsArray[i] = 0; // Always upright
			}
		}

		// Start SpreadCardsActivity
		Intent intent = new Intent(this, SpreadCardsActivity.class);
		intent.putExtra("spreadId", spreadId);
		// No need to pass card IDs, they act via ConfigData
		startActivity(intent);
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (sensorManager != null && accelerometer != null) {
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (sensorManager != null) {
			sensorManager.unregisterListener(this);
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			if ((curTime - lastShakeTime) > 100) {
				long diffTime = (curTime - lastShakeTime);
				lastShakeTime = curTime;

				float x = event.values[0];
				float y = event.values[1];
				float z = event.values[2];
				
				float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

				if (speed > SHAKE_THRESHOLD) {
					performShuffle();
				}

				lastX = x;
				lastY = y;
				lastZ = z;
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
