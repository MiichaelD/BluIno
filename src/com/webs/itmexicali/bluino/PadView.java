package com.webs.itmexicali.bluino;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

@SuppressLint("WrongCall")
public class PadView extends SurfaceView implements Callback, Runnable {

	private boolean run;
	private SurfaceHolder sh;
	private Paint p, pRed, pBlue, pControls;
	private int touchX[] = new int[2], touchY[] = new int[2], origenY[] = new int[2], origenX[] = new int[2],	textSize, w ,h;
	private Rect screen, bar1, bar2, aux, notif;
	private Ball ball1, ball2, curBall[] = new Ball[2];
	private Thread tDraw;
	private Bitmap bluinoBMP;
	public static final int UMBRAL_TACTIL = 70;
	private String canTextL = "", canTextR = "";
	private Context mContext;

	
	/********************************************CONSTRUCTORS*****************************/
	public PadView(Context context) {
		super(context);
		mContext = context;
		initHolder(context);
	}
	
	public PadView(Context context, AttributeSet attrs, int defStyle){
        super( context , attrs , defStyle );
        mContext = context;
        initHolder(context);
    }

    public PadView ( Context context , AttributeSet attrs ){
        super( context , attrs );
        mContext = context;
        initHolder(context);
    }
    /**************************************************************************************/
	
    
    /** Init this SurfaceView's Holder    */
	public final void initHolder(Context context){
		sh = getHolder();
		sh.addCallback(this);
		//sh.setFormat(PixelFormat.TRANSLUCENT);
		initPaints();
	}
	
	

	public void initPaints() {
		p = new Paint();
		p.setColor(Color.WHITE);
		p.setTextAlign(Align.CENTER);
		p.setTypeface(Typeface.createFromAsset(this.getContext().getAssets(), "fonts/KellySlab-Regular.ttf"));
		p.setTextSize(textSize);
		p.setAntiAlias(true);
		pRed = new Paint();
		pRed.setColor(Color.RED);
		pRed.setAntiAlias(true);
		pBlue = new Paint();
		pBlue.setColor(Color.BLUE);
		pBlue.setAntiAlias(true);
		pControls = new Paint();
		pControls.setColor(Color.argb(220, 100, 180, 180));
		pControls.setAntiAlias(true);

	}

	public void onDraw(Canvas canvas) {
		try {
			canvas.drawColor(Color.DKGRAY);
			if (BluetoothService.mBlueService != null &&
					BluetoothService.mBlueService.getState() == BluetoothService.STATE_CONNECTED) {
				canvas.drawCircle(screen.exactCenterX(), screen.height() / 8, ball1.getRect().width() / 2, pBlue);
				canvas.drawText(getContext().getString(R.string.title_connected), screen.centerX(), (getHeight() / 8) + textSize * 2, p);
			} else {
				canvas.drawCircle(getWidth() / 2, getHeight() / 4, ball1.getRect().width() / 2, pRed);
				canvas.drawText( getContext().getString(R.string.title_not_connected), screen.centerX(), (getHeight() / 4) + textSize * 2, p);
			}
			canvas.drawRect(bar1, p);
			canvas.drawRect(bar2, p);
			canvas.drawRect(ball1.getRect(),pControls);
			//canvas.drawCircle(ball1.getRect().centerX(), ball1.getRect().centerY(), ball1.getRect().width() / 2, pControls);
			//canvas.drawRect(ball2.getRect(),pControls);
			canvas.drawCircle(ball2.getRect().centerX(), ball2.getRect().centerY(), ball2.getRect().width() / 2, pControls);
			
			canvas.drawBitmap(bluinoBMP, screen.centerX()-w*7/2, 6*getHeight()/8, null);
			
			drawText(canvas, p);
		} catch (Exception e) {
			Log.e(Main.TAG, "onDraw - ", e);
		}
	}

	private void drawText(Canvas canvas, Paint paint) {
		canvas.drawText(canTextL, ball1.getRect().centerX(), 7 * getHeight() / 8, paint);
		canvas.drawText(canTextR, ball2.getRect().centerX(), 7 * getHeight() / 8, paint);
	}

	public Bitmap resizeImage(Context ctx, int resId, int w, int h) {

        // load the origial Bitmap
        Bitmap BitmapOrg = BitmapFactory.decodeResource(ctx.getResources(),resId);

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        // calculate the scale
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the Bitmap
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);

        // make a Drawable from Bitmap to allow to set the Bitmap
        // to the ImageView, ImageButton or what ever
        return resizedBitmap;

        }
	
	@Override
	public void run() {
		Canvas canvas = null;
		while (run) {
			canvas = null;
			try {
				canvas = sh.lockCanvas(null);
				synchronized (sh) {
					onDraw(canvas);
				}
			} finally {
				if (canvas != null)
					sh.unlockCanvasAndPost(canvas);
			}
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		/*
		 * if (BluetoothService.mBlueService.getState() !=
		 * BluetoothService.STATE_CONNECTED) { Message msg =
		 * mHandler.obtainMessage(Main.MESSAGE_TOAST); Bundle bundle = new
		 * Bundle(); bundle.putString(Main.TOAST,
		 * getContext().getString(R.string.not_connected)); msg.setData(bundle);
		 * mHandler.sendMessage(msg); }
		 */{
			int action = event.getAction() & MotionEvent.ACTION_MASK;
			int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			int pointerId = event.getPointerId(pointerIndex);
			switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				if (Main.D)
					Log.v(Main.TAG,"+DragandDropVIEW OnTouchEvent(Action_Down) ID:"+ pointerId + " - Index" + pointerIndex);
				touchX[pointerId] = (int) event.getX(pointerIndex);
				touchY[pointerId] = (int) event.getY(pointerIndex);
				
				if(notif.contains(touchX[pointerId], touchY[pointerId])){
					if (Main.D)
						Log.i(Main.TAG,"+OnTouchEvent(Action_Down) SECREEETtouch!!; ");
					if (BluetoothService.mBlueService != null)
						if (BluetoothService.mBlueService.getState() == BluetoothService.STATE_CONNECTED)
							BluetoothService.mBlueService.write("S0008".getBytes());//secret code to run a secret routine in Arduino
					
				}
				
				aux = new Rect(ball1.getRect());
				aux.set(aux.left - UMBRAL_TACTIL, aux.top - UMBRAL_TACTIL,aux.right + UMBRAL_TACTIL, aux.bottom + UMBRAL_TACTIL);
				if (aux.contains(touchX[pointerId], touchY[pointerId])) {
					curBall[pointerId] = ball1;
					origenY[pointerId] = touchY[pointerId];
					origenX[pointerId] = touchX[pointerId];

				}

				aux = new Rect(ball2.getRect());
				aux.set(aux.left - UMBRAL_TACTIL, aux.top - UMBRAL_TACTIL,
						aux.right + UMBRAL_TACTIL, aux.bottom + UMBRAL_TACTIL);
				if (aux.contains(touchX[pointerId], touchY[pointerId])) {
					curBall[pointerId] = ball2;
					origenY[pointerId] = touchY[pointerId];
					origenX[pointerId] = touchX[pointerId];
				}
				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_CANCEL:
				touchX[pointerId] = -1;
				touchY[pointerId] = -1;
				if (curBall[pointerId] != null) {
					curBall[pointerId].moveToCenter();
					if (curBall[pointerId].getmoveType() == Ball.RIGHT_BAR)
						transformPWM(curBall[pointerId], bar2, Options.getInstance().getRBarValue());
					else
						transformPWM(curBall[pointerId], bar1, Options.getInstance().getLBarValue());
					curBall[pointerId] = null;
				}
				if (Main.D)
					Log.v(Main.TAG,	"+DragandDropVIEW OnTouchEvent(Action_Up) ID:"+ pointerId + " - Index" + pointerIndex);
				break;

			case MotionEvent.ACTION_MOVE:
				int pointerCount = event.getPointerCount();
				for (int i = 0; i < pointerCount; i++) {
					pointerIndex = i;
					pointerId = event.getPointerId(pointerIndex);
					touchX[pointerId] = (int) event.getX(pointerIndex);
					touchY[pointerId] = (int) event.getY(pointerIndex);
					if (curBall[pointerId] != null) {
						if(curBall[pointerId].getmoveType() == Ball.RIGHT_BAR){
							if(curBall[pointerId].puedoMover(touchX[pointerId] - origenX[pointerId], touchY[pointerId]- origenY[pointerId], bar2)){
								(curBall[pointerId]).move(touchX[pointerId]	- origenX[pointerId], touchY[pointerId]	- origenY[pointerId]);
								transformPWM(curBall[pointerId], bar2, Options.getInstance().getRBarValue());
							}
							
						}
						else if(curBall[pointerId].puedoMover(touchX[pointerId] - origenX[pointerId], touchY[pointerId] - origenY[pointerId], bar1)){
							(curBall[pointerId]).move(touchX[pointerId]	- origenX[pointerId], touchY[pointerId]	- origenY[pointerId]);
							transformPWM(curBall[pointerId], bar1, Options.getInstance().getLBarValue());
						}
						origenY[pointerId] = touchY[pointerId];
						origenX[pointerId] = touchX[pointerId];
/*						if (Main.D)
							Log.v(Main.TAG,"+DragandDropVIEW OnTouchEvent(Action_Move) ID:"+ pointerId + " - Index"+ pointerIndex);
	*/				}

				}
				break;
			}
		}
		return true;
	}

	public synchronized void transformPWM(Ball ball, Rect bar, int comp) {
		double dist;
		int x;
		String out = "";
		switch(ball.getmoveType()){
		case Ball.RIGHT_BAR:
			dist = ball.getRect().centerX() - bar.left;
			x = (int) (comp / (bar.width() / dist));
			out=intToString(x);
			canTextR = out;
			out = "H" + out;
				break;
		case Ball.LEFT_BAR:
			dist = bar.bottom - ball.getRect().centerY();
			x = (int) (comp / (bar.height() / dist));
			out=intToString(x);
			canTextL = out;
			out = "V" + out;
				break;
		default:
			return;
		
		}
		if (Main.D)
			Log.d(Main.TAG, "ratio 0-" + comp + "= " + out); 
		if (BluetoothService.mBlueService != null)
			if (BluetoothService.mBlueService.getState() == BluetoothService.STATE_CONNECTED)
				BluetoothService.mBlueService.write(out.getBytes());

	}

	public static String intToString(int x){
		//else
			if(x<1000)
				if (x < 100)
					if (x < 10)
						if(x<0)
							return ""+x;
						else
							return "000" + x;
					else
						return "00" + x;
				else
					return "0" + x;
			else
				return "" +x;
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		w = getWidth() / 60;		h = getHeight() / 2;
		int left = getWidth() / 6, top = (getHeight() / 4), right = left + w, bottom = 3 * top;
		textSize = getHeight() / 15;
		initPaints();
		screen = new Rect(0, 0, getWidth(), getHeight());
		bar1 = new Rect(left, top, right, bottom);
		bar2 = new Rect(3 * left, h - w / 2, 5 * left, h + w / 2);
		ball1 = new Ball(left - w, top - 5 - w + (bottom - top) / 2, right + w,
				top + 5 + w + (bottom - top) / 2, Ball.LEFT_BAR);
		ball2 = new Ball(4 * left - w - (w / 2), top - w - 5 + (bottom - top)
				/ 2, 4 * left + w + (w / 2), top + w + 5 + (bottom - top) / 2,	Ball.RIGHT_BAR);
		notif=new Rect(screen.centerX()-w-(UMBRAL_TACTIL/2), (screen.height() / 8)-w-5, (screen.centerX())+w+(UMBRAL_TACTIL/2), (screen.height() / 8)+w+5);
		
		bluinoBMP=resizeImage(this.getContext(),R.drawable.bluinotooth,7*w,2*getHeight()/8);

		tDraw = new Thread(this);
		run = true;
		tDraw.start();

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (Main.D)
			Log.v(Main.TAG, "+PGVIEW surfaceDestroyed");
		boolean retry = true;
		run = false;
		while (retry) {
			try {
				if (tDraw != null)
					tDraw.join();
				retry = false;
			} catch (InterruptedException e) {
				if (Main.D)
					Log.e(Main.TAG, "PGView-SD: " + e.getMessage());
			}
		}

	}

}
