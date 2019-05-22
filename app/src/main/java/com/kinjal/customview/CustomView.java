package com.kinjal.customview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class CustomView extends View {

    private Context context;

    private int animateDuration = 1000;

    private float incrementalAngle;
    private int size;

    /* Paint */
    private Paint outerPaint, middlePaint, innerPaint;

    /* RectF */
    private RectF outerRect, middleRect, innerRect;

    /* Stroke width */
    private float thickness;

    /* Start and sweep angles of slice*/
    private float outerStartAngle, outerSweepAngle,
            middleStartAngle, middleSweepAngle,
            innerStartAngle, innerSweepAngle;

    private int outerSliceSolidColor;
    private int middleSliceSolidColor;
    private int innerSliceSolidColor;

    private int outerShadowColor = 0;
    private int middleShadowColor = 0;
    private int innerShadowColor = 0;

    private float shadowRadius = 10f;
    private float shadowDx = 5f;
    private float shadowDy = 0f;

    private int current_slice = 2;
    private boolean showShadow = false;

    public CustomView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(
                    attrs, R.styleable.CustomView, defStyleAttr, 0);

            // animation
            animateDuration = a.getInteger(R.styleable.CustomView_anim_duration, 1000);

            // Solid color
            outerSliceSolidColor = a.getColor(R.styleable.CustomView_outer_slice_solid_color, 0);
            middleSliceSolidColor = a.getColor(R.styleable.CustomView_middle_slice_solid_color, 0);
            innerSliceSolidColor = a.getColor(R.styleable.CustomView_inner_slice_solid_color, 0);

            // Shadow color
            int shadowColor = a.getColor(R.styleable.CustomView_outer_slice_shadow_color, 0);
            outerShadowColor = middleShadowColor = innerShadowColor = shadowColor;

            outerShadowColor = a.getColor(R.styleable.CustomView_outer_slice_shadow_color, Color.parseColor("#06A8B5"));
            middleShadowColor = a.getColor(R.styleable.CustomView_middle_slice_shadow_color, Color.parseColor("#AA9E00"));
            innerShadowColor = a.getColor(R.styleable.CustomView_inner_slice_shadow_color, Color.parseColor("#EA6A12"));

            // Show shadow
            showShadow = a.getBoolean(R.styleable.CustomView_enable_shadow, false);

            // Incremental angle
            incrementalAngle = a.getFloat(R.styleable.CustomView_incremental_angle, 45);
        }

        /// Init slice width/thickness
        thickness = 60f;

        // Init start and sweep angles fo each slices
        innerStartAngle = 80f;
        innerSweepAngle = 250f;

        middleStartAngle = 240f;
        middleSweepAngle = 270f;

        outerStartAngle = 30f;
        outerSweepAngle = 160f;

        // init rectFs
        outerRect = new RectF();
        middleRect = new RectF();
        innerRect = new RectF();

        // Init Paint
        outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        middlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private void updatePaint() {
        // Stroke with for outer slice is taken this to avoid the slice going out og the view
        float outerThickness = thickness + (0.8f * thickness);
        outerPaint.setStrokeWidth(outerThickness);
        outerPaint.setStyle(Paint.Style.STROKE);
        outerPaint.setStrokeCap(Paint.Cap.BUTT);

        if (outerSliceSolidColor == 0) {
            outerPaint.setShader(new LinearGradient(0, 0, 0,
                    outerThickness * 2, Color.parseColor("#03C9EE"), Color.parseColor("#00F0BD"),
                    Shader.TileMode.MIRROR));
        } else {
            outerPaint.setColor(outerSliceSolidColor);
        }

        // Middle slice
        middlePaint.setStrokeWidth(thickness);
        middlePaint.setStyle(Paint.Style.STROKE);
        middlePaint.setStrokeCap(Paint.Cap.BUTT);

        if (middleSliceSolidColor == 0) {
            middlePaint.setShader(new LinearGradient(0, 0, 0,
                    outerThickness * 2, Color.parseColor("#FAE04B"), Color.parseColor("#FFAA3E"),
                    Shader.TileMode.MIRROR));
        } else {
            middlePaint.setColor(middleSliceSolidColor);
        }

        // Inner slice
        innerPaint.setStrokeWidth(thickness / 2);
        innerPaint.setStyle(Paint.Style.STROKE);
        innerPaint.setStrokeCap(Paint.Cap.BUTT);

        if (innerSliceSolidColor == 0) {
            innerPaint.setShader(new LinearGradient(0, 0, 0,
                    outerThickness * 2f, Color.parseColor("#FE3C00"), Color.parseColor("#FF7900"),
                    Shader.TileMode.MIRROR));
        } else {
            innerPaint.setColor(innerSliceSolidColor);
        }

        if (showShadow) {
            outerPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, outerShadowColor);
            middlePaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, middleShadowColor);
            innerPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, innerShadowColor);
        } else {
            outerPaint.setShadowLayer(0, shadowDx, shadowDy, outerShadowColor);
            middlePaint.setShadowLayer(0, shadowDx, shadowDy, middleShadowColor);
            innerPaint.setShadowLayer(0, shadowDx, shadowDy, innerShadowColor);
        }
        // Shadow doesn't work if hardware acceleration is on,
        // so we disable it using following method
        setLayerType(View.LAYER_TYPE_SOFTWARE, outerPaint);
        setLayerType(View.LAYER_TYPE_SOFTWARE, middlePaint);
        setLayerType(View.LAYER_TYPE_SOFTWARE, innerPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // calculate size of the View
        int xPad = getPaddingLeft() + getPaddingRight();
        int yPad = getPaddingTop() + getPaddingBottom();
        int width = getMeasuredWidth() - xPad;
        int height = getMeasuredHeight() - yPad;

        size = (width < height) ? width : height;
        setMeasuredDimension(size + xPad, size + yPad);

        thickness = size / 6f;
        // update rect bounds after calculating size
        updateRectAngleBounds();
        // update paint
        updatePaint();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO: write switch case
        animateSlice();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw outer slice
        canvas.drawArc(outerRect, outerStartAngle, outerSweepAngle, false, outerPaint);
        // Draw middle slice
        canvas.drawArc(middleRect, middleStartAngle, middleSweepAngle, false, middlePaint);
        // Draw inner slice
        canvas.drawArc(innerRect, innerStartAngle, innerSweepAngle, false, innerPaint);
    }

    /**
     * Set three rectangle bounds for drawing three slices.
     */
    private void updateRectAngleBounds() {
        float halfThickness = thickness / 2;

        // Calculate mid rect dimens
        outerRect.set(thickness, thickness,
                size - thickness, size - thickness);

        // Calculate mid rect dimens
        float midRectTopLeft = thickness + halfThickness;
        float midRectBottomRight = size - thickness - halfThickness;
        middleRect.set(midRectTopLeft, midRectTopLeft, midRectBottomRight, midRectBottomRight);

        // Calculate small rect dimens
        float innerRectTopLeft = midRectTopLeft + halfThickness;
        float innerRectBottomRight = midRectBottomRight - halfThickness;
        innerRect.set(innerRectTopLeft, innerRectTopLeft, innerRectBottomRight, innerRectBottomRight);
    }

    /**
     * Creates rotate animation in sequence
     */
    private void animateSlice() {
        // Determines from where to start the rotation
        float startAngle = 0;
        // Determines where to end the rotation
        float finalAngle = 0;

        // Calculating start and final angle for current slice
        if (current_slice == 2) {
            if (middleStartAngle >= 360) {
                middleStartAngle -= 360;
            }
            startAngle = middleStartAngle;
        } else if (current_slice == 1) {
            if (outerStartAngle >= 360) {
                outerStartAngle -= 360;
            }
            startAngle = outerStartAngle;
        } else if (current_slice == 3) {
            if (innerStartAngle >= 360) {
                innerStartAngle -= 360;
            }
            startAngle = innerStartAngle;
        }
        finalAngle = startAngle + incrementalAngle;


        final ValueAnimator animator = ValueAnimator.ofFloat(startAngle, finalAngle);
        animator.setDuration(animateDuration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // updates the value of current slice
                if (current_slice == 2) {
                    middleStartAngle = (float) animator.getAnimatedValue();
                } else if (current_slice == 1) {
                    outerStartAngle = (float) animator.getAnimatedValue();
                } else if (current_slice == 3) {
                    innerStartAngle = (float) animator.getAnimatedValue();
                }
                invalidate();
            }
        });
        // Sets next slice to be rotated after the animation is over
        animator.addListener(new AnimatorListenerAdapter() {
                                 @Override
                                 public void onAnimationEnd(Animator animation) {
                                     super.onAnimationEnd(animation);
                                     // Sets next slice to be rotated
                                     if (current_slice == 2) {
                                         current_slice = 1;
                                     } else if (current_slice == 1) {
                                         current_slice = 3;
                                     } else if (current_slice == 3) {
                                         current_slice = 2;
                                     }
                                 }
                             }
        );
        animator.start();
    }

    /* Setter methods */
    public void setAnimateDuration(int animateDuration) {
        this.animateDuration = animateDuration;
    }

    public void setOuterSliceSolidColor(@ColorRes int outerSliceSolidColor) {
        this.outerSliceSolidColor = ContextCompat.getColor(context, outerSliceSolidColor);
    }

    public void setMiddleSliceSolidColor(@ColorRes int middleSliceSolidColor) {
        this.middleSliceSolidColor = ContextCompat.getColor(context, middleSliceSolidColor);
    }

    public void setInnerSliceSolidColor(@ColorRes int innerSliceSolidColor) {
        this.innerSliceSolidColor = ContextCompat.getColor(context, innerSliceSolidColor);
    }

    public void setOuterShadowColor(int outerShadowColor) {
        this.outerShadowColor = ContextCompat.getColor(context, outerShadowColor);
    }

    public void setMiddleShadowColor(int middleShadowColor) {
        this.middleShadowColor = ContextCompat.getColor(context, middleShadowColor);
    }

    public void setInnerShadowColor(int innerShadowColor) {
        this.innerShadowColor = ContextCompat.getColor(context, innerShadowColor);
    }

    public void setShadowColor(int innerShadowColor) {
        outerShadowColor = middleShadowColor = innerShadowColor = ContextCompat.getColor(context, innerShadowColor);
    }

    public void setIncrementalAngle(float incrementalAngle) {
        this.incrementalAngle = incrementalAngle;
    }

}
