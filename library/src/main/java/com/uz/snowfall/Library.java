package com.uz.snowfall;

import android.content.Context;
import android.graphics.*;
import android.graphics.BlurMaskFilter;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import java.util.*;

public class Library extends LinearLayout {
  private static final float BASE_SIZE = 20.0f;
  private static final float BASE_SPEED = 2.0f;
  private static final float ROTATION_SPEED = 1.0f;
  private static final int SNOWFLAKE_COUNT = 100;
  private static final float SWAY_AMPLITUDE = 15.0f;

  private final Random random;
  private Bitmap snowflakeBitmap;
  private final List<Snowflake> snowflakes;

  public Library(Context context) {
    super(context);
    this.snowflakes = new ArrayList<>();
    this.random = new Random();
    init();
  }

  public Library(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.snowflakes = new ArrayList<>();
    this.random = new Random();
    init();
  }

  public Library(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.snowflakes = new ArrayList<>();
    this.random = new Random();
    init();
  }

  private void init() {
    setWillNotDraw(false);
    this.snowflakeBitmap = createSnowflakeBitmapWithShadow();
  }

  @Override
  protected void onSizeChanged(int width, int height, int oldw, int oldh) {
    super.onSizeChanged(width, height, oldw, oldh);
    this.snowflakes.clear();

    for (int i = 0; i < SNOWFLAKE_COUNT; i++) {
      float scale = this.random.nextFloat() + 0.5f;
      this.snowflakes.add(
          new Snowflake(
              width * this.random.nextFloat(),
              height * this.random.nextFloat(),
              BASE_SPEED + (this.random.nextFloat() * BASE_SPEED),
              scale * BASE_SIZE,
              360.0f * this.random.nextFloat(),
              (this.random.nextFloat() + 0.5f) * ROTATION_SPEED,
              scale * SWAY_AMPLITUDE));
    }

    // Сортировка по размеру снежинок (большие спереди)
    Collections.sort(
        this.snowflakes,
            (a, b) -> Float.compare(b.size, a.size));
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    for (Snowflake snowflake : this.snowflakes) {
      snowflake.update(getWidth(), getHeight());
      snowflake.draw(canvas, this.snowflakeBitmap);
    }
    postInvalidateOnAnimation();
  }

  private Bitmap createSnowflakeBitmapWithShadow() {
    int shadowSize = 20 / 5;
    int fullSize = 20 + shadowSize * 2;

    Bitmap bitmap = Bitmap.createBitmap(fullSize, fullSize, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(BASE_SPEED);

    // Тень
    paint.setColor(Color.GRAY);
    paint.setMaskFilter(new BlurMaskFilter(shadowSize, BlurMaskFilter.Blur.NORMAL));
    float center = fullSize / 2.0f;
    float radius = 20 / 2.0f;
    drawSnowflakeShape(canvas, paint, center, center, radius);

    // Белая снежинка
    paint.setColor(Color.WHITE);
    paint.setMaskFilter(null);
    drawSnowflakeShape(canvas, paint, center, center, radius);

    return bitmap;
  }

  private void drawSnowflakeShape(Canvas canvas, Paint paint, float cx, float cy, float length) {
    for (int i = 0; i < 6; i++) {
      double angle = Math.toRadians(i * 60);
      float x = cx + (float) Math.cos(angle) * length;
      float y = cy + (float) Math.sin(angle) * length;
      canvas.drawLine(cx, cy, x, y, paint);
    }
  }

  private static class Snowflake {
    private float x, y;
    private final float speedY;
    private final float size;
    private float angle;
    private final float rotationSpeed;
    private final float swayAmplitude;
    private final float swayPhase;
    private float time;

    Snowflake(
        float x,
        float y,
        float speedY,
        float size,
        float angle,
        float rotationSpeed,
        float swayAmplitude) {
      this.x = x;
      this.y = y;
      this.speedY = speedY;
      this.size = size;
      this.angle = angle;
      this.rotationSpeed = rotationSpeed * (Math.random() > 0.5 ? 1 : -1);
      this.swayAmplitude = swayAmplitude;
      this.swayPhase = (float) (Math.random() * 2 * Math.PI);
      this.time = 0.0f;
    }

    void update(int width, int height) {
      this.y += this.speedY;
      this.angle += this.rotationSpeed;
      this.time += 0.05f;

      if (this.y > height) {
        this.y = 0;
        this.x = (float) (Math.random() * width);
        this.time = 0;
      }
    }

    void draw(Canvas canvas, Bitmap bitmap) {
      float swayOffset = (float) Math.sin(this.time + this.swayPhase) * this.swayAmplitude;

      canvas.save();
      canvas.translate(this.x + swayOffset, this.y);
      canvas.rotate(this.angle, this.size / 2.0f, this.size / 2.0f);

      Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
      RectF dst = new RectF(0, 0, this.size, this.size);
      canvas.drawBitmap(bitmap, src, dst, null);
      canvas.restore();
    }
  }
}
