package com.zt.tz.privceview;

import java.util.GregorianCalendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
/**
 * Created by tony.zhang
 * Email:461609086@qq.com
 */
public class PriceBarView extends View {
	private Bitmap gray_bg;
	private Bitmap green_bg;
	private Bitmap btn;
	private Bitmap bg_number;
	private Paint paint;
	private float scale_h;
	private static final float REAL_PER = 0.95f;
	
	int bg_height;
	int bg_width;
	int span ;
	// 价格的区间值
	private static final int FIRST_STAGE = 0;
	private static final int SECOND_STAGE = 200;
	private static final int THRID_STAGE = 500;
	private static final int FOUR_STAGE = 1000;
	private static final int FIFTH_STAGE = 10000;
	private static final int PRICE_PADDING = 20;
	private int price_down;
	private int price_up;
	private float half_round;
	private float btn_x;
	private float y_up;
	private float y_down;

	public PriceBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
		//获取自定义属性的值
		TypedArray array= context.obtainStyledAttributes(attrs, R.styleable.priceBar);
		price_up = array.getInt(R.styleable.priceBar_price_up, 1000);
		price_down = array.getInt(R.styleable.priceBar_price_down, 500);
		array.recycle();
	}

	/**
	 * 初始化控件和图片资源
	 */

	private void initView() {
		// 初始化图片资源
		gray_bg = BitmapFactory.decodeResource(getResources(), R.drawable.axis_before);
		green_bg = BitmapFactory.decodeResource(getResources(), R.drawable.axis_after);
		btn = BitmapFactory.decodeResource(getResources(), R.drawable.btn);
		bg_number = BitmapFactory.decodeResource(getResources(), R.drawable.bg_number);
		
		
		// 画笔
		paint = new Paint();
		paint.setColor(Color.GRAY);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		/**
		 * 当前我们的控件你的宽和高，有2个因素决定 1，父容器给的宽高条件 2，自己高和宽
		 * 
		 * widthMeasureSpec：2位 ：模式 30位 ：尺寸 模式： MeasureSpec.AT_MOST : 填充与父容器
		 * match_parent /fill_parent MeasureSpec.EXACTLY ：确定值 60dp
		 * MeasureSpec.UNSPECIFIED :不确定值 wrap_content
		 */
		// 父容器给的宽和高
		int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);

		int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
		int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

		// 实际的图片的宽和高
		bg_height = gray_bg.getHeight();
		bg_width = gray_bg.getWidth();
		/**
		 * 高度的两种情况： 1, 固定值 : 高度为：固定值
		 * 
		 * 2，wrap_content , match_parent : 高度为：图片的高度
		 */

		int measureHeight = (modeHeight == MeasureSpec.EXACTLY) ? sizeHeight : bg_height;

		// 高度不能超出父容器给的高度
		measureHeight = Math.min(measureHeight, sizeHeight);

		int measureWidth = measureHeight * 2 / 3;
		// 得到压缩的比例
		scale_h = (float) measureHeight / bg_height;
		//分成四截
		span = (bg_height- bg_width)/4;
		//小半圆的半径
		half_round = bg_height *(1-REAL_PER)  /2 ;// 宽度是高度的0.95背
		
		// 设置容器的宽和高
		setMeasuredDimension(measureWidth, measureHeight);
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		// 缩放画布
		canvas.scale(scale_h, scale_h);

		// 1，绘制灰色的滑竿
		float bg_l = (this.getWidth() /scale_h -gray_bg.getWidth()) / 2;
		canvas.drawBitmap(gray_bg, bg_l, 0, paint);
		// 2,绘制右边的文字
		String[] numbers = new String[] {
				"不限",
				String.valueOf(FOUR_STAGE),
				String.valueOf(THRID_STAGE),
				String.valueOf(SECOND_STAGE),
				String.valueOf(FIRST_STAGE),
		};
		paint.setTextSize(20/scale_h);
		for (int i = 0; i < numbers.length; i++) {
			int text_x 	 = (int)bg_l *5/4;
			//文件绘制的时候 有一个基线的问题
			//float text_y =	bg_width/2 + i*span+(paint.descent() - paint.ascent())/2-paint.descent();
			float text_y =	bg_width/2 + i*span+(-paint.descent() - paint.ascent())/2;
			canvas.drawText(numbers[i], text_x, text_y, paint);
		}
		//3,画绿色大圆(上圆和下圆)
		
		btn_x = (this.getWidth()/scale_h - btn.getWidth())/2;
		y_up = getBtnYLocationByPrice(price_up);
		canvas.drawBitmap(btn, btn_x, y_up-btn.getHeight()/2, paint);		
		y_down = getBtnYLocationByPrice(price_down);
		canvas.drawBitmap(btn, btn_x, y_down-btn.getHeight()/2,paint);
		//4，画绿色滑竿
		Rect src = new Rect(0, (int)(y_up+btn.getHeight()/2), green_bg.getWidth(), 
				(int)(y_down-btn.getHeight()/2));
		
		Rect dst = new Rect((int)bg_l, (int)(y_up+btn.getHeight()/2), (int)(bg_l+green_bg.getWidth()), (int)(y_down-btn.getHeight()/2));
		canvas.drawBitmap(green_bg, src, dst, paint);
		//5,画左边的价格矩形
		Rect rect_up = getRectByMidLine(y_up);
		canvas.drawBitmap(bg_number, null, rect_up, paint);
		Rect rect_down = getRectByMidLine(y_down);
		canvas.drawBitmap(bg_number, null, rect_down, paint);
		
		//6,上矩形的价格
		float text_u_x = (rect_up.width() *3 /4 - paint.measureText(String.valueOf(price_up)))/2;
		
		float text_u_y = rect_up.top - paint.ascent() +PRICE_PADDING;
		//7, 下矩形的价格
		float text_d_x = (rect_down.width() *3 /4 - paint.measureText(String.valueOf(price_down)))/2;
		
		float text_d_y = rect_down.top - paint.ascent() +PRICE_PADDING;
		canvas.drawText(String.valueOf(price_up), text_u_x, text_u_y, paint);
		canvas.drawText(String.valueOf(price_down), text_d_x, text_d_y, paint);
		canvas.restore();
		super.onDraw(canvas);
	}
	/**
	 * 根据中线的位置得到矩形
	 * @param y_up
	 * @return
	 */
	private Rect getRectByMidLine(float y) {
		Rect rect = new Rect();
		rect.left = 0;
		rect.right = (int)btn_x;
		float text_h = paint.descent() - paint.ascent();
		
		rect.top = (int)(y - text_h/2) - PRICE_PADDING;
		rect.bottom = (int)(y + text_h/2) + PRICE_PADDING;
		return rect;
	}
	
	/**
	 * 根据价格来获取 y位置
	 * @param price_up2
	 * @return
	 */
	private float getBtnYLocationByPrice(int price) {
		float y = 0;
		if(price < FIRST_STAGE){
			price = FIRST_STAGE;
		}
		if(price > FIFTH_STAGE){
			price = FIFTH_STAGE;
		}
		//0~200
		if(price>=FIRST_STAGE && price<SECOND_STAGE ){
			y = bg_height - span * price/(SECOND_STAGE - FIRST_STAGE) - half_round;
		}else if(price>=SECOND_STAGE && price<THRID_STAGE){//200~500   price =300
			y = bg_height - span * (price-SECOND_STAGE)/(THRID_STAGE - SECOND_STAGE) - span- half_round;
		}else if(price>=THRID_STAGE && price<FOUR_STAGE){//500~1000
			y = bg_height - span * (price-THRID_STAGE)/(FOUR_STAGE - THRID_STAGE) - 2*span- half_round;
		}else{//1000~10000
			y = half_round + span *(FIFTH_STAGE - price) /(FIFTH_STAGE - FOUR_STAGE);
		}
		
		Log.i("tz", "y:"+y);
		
		return y;
	}
	/**
	 * 监听用户的触摸事件
	 */
	private boolean isDownTouched;
	private boolean isUpTouched;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action  = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			//判断 用户按下的时候 是否按到大圆
			float x = event.getX() / scale_h;
			float y = event.getY() / scale_h;
			//x范围：符合的条件
			if(x>btn_x && x <= btn_x+btn.getWidth()){
				//y范围 ：符合的条件：1，在上面的圆 2，在下面的大圆
				//1，在上面的圆
				if(y>= y_up-btn.getHeight()/2 && y<= y_up+btn.getHeight()/2){
					isUpTouched = true;
					isDownTouched = false;
				}
				//2,在下面的大圆
				if(y>= y_down-btn.getHeight()/2 && y<= y_down+btn.getHeight()/2){
					isUpTouched =false;
					isDownTouched = true;
				}
			}
			
			break;
		case MotionEvent.ACTION_MOVE:
			float y2 = event.getY();
			//压缩后的y坐标
			y2 = y2 /scale_h;
			if(isUpTouched){
				price_up = getPriceByPotion(y2);
				if(price_up < price_down){
					price_up = price_down;
				}
			}
			if(isDownTouched){
				price_down = getPriceByPotion(y2);
				if(price_down > price_up){
					price_down = price_up;
				}
			}
			//重绘价格 滑竿
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			isDownTouched = false;
			isUpTouched = false;

		default:
			break;
		}
		
		
		return true;//拦截 用户的触摸事件  （消耗事件）
	}
	/**
	 * 根据y的坐标得到对应的价格滑竿上的价格
	 * @param y2
	 * @return
	 */
	
	private int getPriceByPotion(float y) {
		float half_height = this.getHeight()*(1-REAL_PER)/2;
		int price ;
		if(y < half_height){//y>10000
			price = FIFTH_STAGE;
		}else if(y>half_height && y < half_height+ span){//1000~10000
			//10000- (10000-1000)*1/3
			price = (int)(FIFTH_STAGE - (FIFTH_STAGE - FOUR_STAGE)*(y-half_height)/span) ;
		}else if(y>half_height+span && y< half_height+2*span){//500~1000
			price =(int)(FOUR_STAGE - (FOUR_STAGE - THRID_STAGE)*(y-half_height-span)/span) ;
		}else if(y>half_height+2*span && y< half_height+3*span){////200~500
			price =(int)(THRID_STAGE - (THRID_STAGE - SECOND_STAGE)*(y-half_height-2*span)/span) ;
		}else{//0~200
			price =(int)(SECOND_STAGE - (SECOND_STAGE - FIRST_STAGE)*(y-half_height-3*span)/span) ;
			//price = (int)(FIRST_STAGE + (SECOND_STAGE - FIRST_STAGE)*(this.getHeight()-y-half_height)/span);
		}
		if(price < FIRST_STAGE){
			price = FIRST_STAGE;
		}
		
		
		
		return price;
	}
	
	

}
