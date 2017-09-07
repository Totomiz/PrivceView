package com.zt.tz.privceview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by asus on 2016-05-30 21:06
 * QQ:xxxxxxxx
 */
public class PriceView extends View {

    private Bitmap grenn_bg;
    private Bitmap gray_bg;
    private Bitmap btn;
    private Bitmap bg_number;
    private Paint paint;
    //价格区间
    private static final int FIRST_STAGE = 0;
    private static final int SECOND_STAGE = 200;
    private static final int THRID_STAGE = 500;
    private static final int FOUR_STAGE = 1000;
    private static final int FIFTH_STAGE = 10000;
    private static final int PRICE_PADDING = 20;
    //缩放比
    private float scaleH = 0.7f;
    private int heightPixels;
    private int span;
    private float btn_x;
    private int price_up;
    private int price_down;
    private float up_y;
    private float down_y;
    private int gray_bgWidth;
    private int gray_bgHeight;
    private static final float REAL_PER = 0.95f;
    private float half_round;

    public PriceView(Context context) {
        this(context, null);
    }

    public PriceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PriceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        heightPixels = context.getResources().getDisplayMetrics().heightPixels;
        //初始化图片资源
        init();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.priceBar);
        price_up = array.getInt(R.styleable.priceBar_price_up, 1000);
        price_down = array.getInt(R.styleable.priceBar_price_down, 500);
    }

    private void init() {
        grenn_bg = BitmapFactory.decodeResource(getResources(), R.drawable.axis_after);
        gray_bg = BitmapFactory.decodeResource(getResources(), R.drawable.axis_before);
        btn = BitmapFactory.decodeResource(getResources(), R.drawable.btn);
        bg_number = BitmapFactory.decodeResource(getResources(), R.drawable.bg_number);
        // 画笔
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GRAY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeHight = MeasureSpec.getMode(heightMeasureSpec);
        int sizeHight = MeasureSpec.getSize(heightMeasureSpec);
        //实际图片的宽和高
        gray_bgWidth = gray_bg.getWidth();
        gray_bgHeight = gray_bg.getHeight();
        int measureHeight = (modeHight == MeasureSpec.EXACTLY) ? sizeHight : gray_bgHeight;
        measureHeight = Math.min(measureHeight, sizeHight);
        int measureWidth = measureHeight * 2 / 3;
        span = (gray_bgHeight - gray_bgWidth) / 4;
        half_round = gray_bgHeight * (1 - REAL_PER) / 2;// 宽度是高度的0.95背
        scaleH = (float) measureHeight / gray_bgHeight;
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        //1绘制灰色滑竿
        canvas.scale(scaleH, scaleH);
        float bg_left = (this.getWidth() / scaleH - gray_bg.getWidth()) / 2;
        canvas.drawBitmap(gray_bg, bg_left, 0, paint);
        //2.画文字
        String[] numbers = new String[]{
                "不限",
                String.valueOf(FOUR_STAGE),
                String.valueOf(THRID_STAGE),
                String.valueOf(SECOND_STAGE),
                String.valueOf(FIRST_STAGE),
        };
        paint.setTextSize(50 / scaleH);
        paint.setColor(Color.BLACK);
        for (int i = 0; i < numbers.length; i++) {
            int text_x = (int) (bg_left + gray_bg.getWidth() + (btn.getWidth() - gray_bg.getWidth()) / 2);
            float text_y = gray_bg.getWidth() / 2 + -paint.ascent() / 2 - paint.descent() / 2 + span * i;
            canvas.drawText(numbers[i], text_x, text_y, paint);
        }
        //3绘制大圆
        btn_x = (this.getWidth() / scaleH - btn.getWidth()) / 2;
        //根据上下价格获取y坐标
        up_y = getBtnYLocationByPrice(price_up);
        down_y = getBtnYLocationByPrice(price_down);
        canvas.drawBitmap(btn, btn_x, up_y - btn.getHeight() / 2, paint);
        canvas.drawBitmap(btn, btn_x, down_y - btn.getHeight() / 2, paint);
        //4画绿色滑竿
        Rect src = new Rect(0, (int) up_y + btn.getHeight() / 2, grenn_bg.getWidth(), (int) down_y - btn.getHeight() / 2);
        Rect dst = new Rect((int) bg_left, (int) up_y + btn.getHeight() / 2, (int) (bg_left + grenn_bg.getWidth()), (int) down_y - btn.getHeight() / 2);
        canvas.drawBitmap(grenn_bg, src, dst, paint);
        //5,画左边的价格矩形
        Rect rect_up = getRectByMidLine(up_y);
        canvas.drawBitmap(bg_number, null, rect_up, paint);
        Rect rect_down = getRectByMidLine(down_y);
        canvas.drawBitmap(bg_number, null, rect_down, paint);
        //6,上矩形的价格
        float text_u_x = (rect_up.width() * 3 / 4 - paint.measureText(String.valueOf(price_up))) / 2;

        float text_u_y = rect_up.top - paint.ascent() + PRICE_PADDING;
        //7, 下矩形的价格
        float text_d_x = (rect_down.width() * 3 / 4 - paint.measureText(String.valueOf(price_down))) / 2;

        float text_d_y = rect_down.top - paint.ascent() + PRICE_PADDING;
        canvas.drawText(String.valueOf(price_up), text_u_x, text_u_y, paint);
        canvas.drawText(String.valueOf(price_down), text_d_x, text_d_y, paint);
        canvas.restore();
    }

    /**
     * 根据中线的位置得到矩形
     *
     * @param y
     * @return
     */
    private Rect getRectByMidLine(float y) {
        Rect rect = new Rect();
        rect.left = 0;
        rect.right = (int) btn_x;
        float text_h = paint.descent() - paint.ascent();

        rect.top = (int) (y - text_h / 2) - PRICE_PADDING;
        rect.bottom = (int) (y + text_h / 2) + PRICE_PADDING;
        return rect;
    }

    private float getBtnYLocationByPrice(int price) {
        float y = 0;
        if (price < FIRST_STAGE) {
            price = FIRST_STAGE;
        }
        if (price > FIFTH_STAGE) {
            price = FIFTH_STAGE;
        }
        if (price >= FIRST_STAGE && price < SECOND_STAGE) {
            y = gray_bgHeight - span * price / (SECOND_STAGE - FIRST_STAGE) - half_round;
        } else if (price >= SECOND_STAGE && price < THRID_STAGE) {//200~500   price =300
            y = gray_bgHeight - span * (price - SECOND_STAGE) / (THRID_STAGE - SECOND_STAGE) - span - half_round;
        } else if (price >= THRID_STAGE && price < FOUR_STAGE) {//500~1000
            y = gray_bgHeight - span * (price - THRID_STAGE) / (FOUR_STAGE - THRID_STAGE) - 2 * span - half_round;
        } else {//1000~10000
            y = half_round + span * (FIFTH_STAGE - price) / (FIFTH_STAGE - FOUR_STAGE);
        }
        return y;
    }

    private boolean isUpTouched = false;
    private boolean isDownTouched = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX() / scaleH;
                float y = event.getY() / scaleH;
                if (x > btn_x && x <= btn_x + btn.getWidth()) {
                    if (y >= up_y - btn.getHeight() / 2 && y <= up_y + btn.getHeight() / 2) {
                        isUpTouched = true;
                        isDownTouched = false;
                    }
                    if (y >= down_y - btn.getHeight() / 2 && y <= down_y + btn.getHeight() / 2) {
                        isDownTouched = true;
                        isUpTouched = false;
                    }
                    if(price_up==0){
                        isDownTouched = false;
                        isUpTouched = true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float y2 = event.getY();
                //压缩后的y坐标
                y2 = y2 / scaleH;
                if (isUpTouched) {
                    price_up = getPriceByPotion(y2);
                    if (price_up < price_down) {
                        price_up = price_down;
                    }
                }
                if (isDownTouched) {
                    price_down = getPriceByPotion(y2);
                    if (price_down > price_up) {
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
        return true;
    }

    private int getPriceByPotion(float y) {
        float half_height = this.getHeight() * (1 - REAL_PER) / 2;
        int price;
        if (y < half_height) {//y>10000
            price = FIFTH_STAGE;
        } else if (y > half_height && y < half_height + span) {//1000~10000
            //10000- (10000-1000)*1/3
            price = (int) (FIFTH_STAGE - (FIFTH_STAGE - FOUR_STAGE) * (y - half_height) / span);
        } else if (y > half_height + span && y < half_height + 2 * span) {//500~1000
            price = (int) (FOUR_STAGE - (FOUR_STAGE - THRID_STAGE) * (y - half_height - span) / span);
        } else if (y > half_height + 2 * span && y < half_height + 3 * span) {////200~500
            price = (int) (THRID_STAGE - (THRID_STAGE - SECOND_STAGE) * (y - half_height - 2 * span) / span);
        } else {//0~200
            price = (int) (SECOND_STAGE - (SECOND_STAGE - FIRST_STAGE) * (y - half_height - 3 * span) / span);
            //price = (int)(FIRST_STAGE + (SECOND_STAGE - FIRST_STAGE)*(this.getHeight()-y-half_height)/span);
        }
        if (price < FIRST_STAGE) {
            price = FIRST_STAGE;
        }
        return price;
    }
}
