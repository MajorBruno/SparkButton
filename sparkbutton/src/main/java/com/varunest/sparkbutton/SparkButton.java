package com.varunest.sparkbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.varunest.sparkbutton.heplers.CircleView;
import com.varunest.sparkbutton.heplers.DotsView;
import com.varunest.sparkbutton.heplers.Utils;

/**
 * @author varun 7th July 2016
 * @author mbruno 18th Aug 2016
 */
public class SparkButton extends FrameLayout implements View.OnClickListener {
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    private static final int INVALID_RESOURCE_ID = -1;

    private static final float INITIAL_CIRCLE_FACTOR = 1.4f;
    public static final float INITIAL_DOT_FACTOR = .08f;
    public static final float INITIAL_DOT_VIEW_FACTOR = 3f;

    private float circleSizeFactor = INITIAL_CIRCLE_FACTOR;
    private float dotsSizeFactor = INITIAL_DOT_FACTOR;
    private float dotViewSizeFactor = INITIAL_DOT_VIEW_FACTOR;

    int imageResourceIdActive = INVALID_RESOURCE_ID;
    int imageResourceIdInactive = INVALID_RESOURCE_ID;
    int imageSize;
    int dotsSize;
    int circleSize;
    int secondaryColor;
    int primaryColor;

    DotsView dotsView;
    CircleView circleView;
    ImageView imageView;

    boolean pressOnTouch = true;
    float animationSpeed = 1;
    boolean isChecked = true;

    private AnimatorSet animatorSet;
    private SparkEventListener listener;

    SparkButton(Context context) {
        super(context);
    }

    public SparkButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        getStuffFromXML(attrs);
        init();
    }

    public SparkButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getStuffFromXML(attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SparkButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getStuffFromXML(attrs);
        init();
    }

    void init() {
        circleSize = (int) (imageSize * circleSizeFactor);
        dotsSize = (int) (imageSize * dotViewSizeFactor);

        LayoutInflater.from(getContext()).inflate(R.layout.layout_spark_button, this, true);
        circleView = (CircleView) findViewById(R.id.vCircle);
        circleView.setColors(secondaryColor, primaryColor);
        circleView.getLayoutParams().height = circleSize;
        circleView.getLayoutParams().width = circleSize;

        dotsView = (DotsView) findViewById(R.id.vDotsView);
        dotsView.getLayoutParams().width = dotsSize;
        dotsView.getLayoutParams().height = dotsSize;
        dotsView.setColors(secondaryColor, primaryColor);
        dotsView.setMaxDotSize((int) (imageSize * dotsSizeFactor));

        imageView = (ImageView) findViewById(R.id.ivImage);

        imageView.getLayoutParams().height = imageSize;
        imageView.getLayoutParams().width = imageSize;
        if (imageResourceIdActive != INVALID_RESOURCE_ID) {
            imageView.setImageResource(imageResourceIdActive);
        }
        setOnTouchListener();
        setOnClickListener(this);
    }

    /**
     * Call this function to start spark animation
     */
    public void playAnimation() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }

        imageView.animate().cancel();
        imageView.setScaleX(0);
        imageView.setScaleY(0);
        circleView.setInnerCircleRadiusProgress(0);
        circleView.setOuterCircleRadiusProgress(0);
        dotsView.setCurrentProgress(0);

        animatorSet = new AnimatorSet();

        ObjectAnimator outerCircleAnimator = ObjectAnimator.ofFloat(circleView, CircleView.OUTER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
        outerCircleAnimator.setDuration((long) (250 / animationSpeed));
        outerCircleAnimator.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator innerCircleAnimator = ObjectAnimator.ofFloat(circleView, CircleView.INNER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
        innerCircleAnimator.setDuration((long) (200 / animationSpeed));
        innerCircleAnimator.setStartDelay((long) (200 / animationSpeed));
        innerCircleAnimator.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator starScaleYAnimator = ObjectAnimator.ofFloat(imageView, ImageView.SCALE_Y, 0.2f, 1f);
        starScaleYAnimator.setDuration((long) (350 / animationSpeed));
        starScaleYAnimator.setStartDelay((long) (250 / animationSpeed));
        starScaleYAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator starScaleXAnimator = ObjectAnimator.ofFloat(imageView, ImageView.SCALE_X, 0.2f, 1f);
        starScaleXAnimator.setDuration((long) (350 / animationSpeed));
        starScaleXAnimator.setStartDelay((long) (250 / animationSpeed));
        starScaleXAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator dotsAnimator = ObjectAnimator.ofFloat(dotsView, DotsView.DOTS_PROGRESS, 0, 1f);
        dotsAnimator.setDuration((long) (900 / animationSpeed));
        dotsAnimator.setStartDelay((long) (50 / animationSpeed));
        dotsAnimator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);

        animatorSet.playTogether(
                outerCircleAnimator,
                innerCircleAnimator,
                starScaleYAnimator,
                starScaleXAnimator,
                dotsAnimator
        );

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                circleView.setInnerCircleRadiusProgress(0);
                circleView.setOuterCircleRadiusProgress(0);
                dotsView.setCurrentProgress(0);
                imageView.setScaleX(1);
                imageView.setScaleY(1);
            }
        });

        animatorSet.start();
    }

    /**
     * Change Button State (Works only if both active and disabled image resource is defined)
     * @param flag
     */
    public void setChecked(boolean flag) {
        isChecked = flag;
        imageView.setImageResource(isChecked ? imageResourceIdActive : imageResourceIdInactive);
    }

    /**
     * Sets the primary and secondary colors used in the
     * button's animation. These should be the resolved color
     * resources.
     *
     * @param primaryColor primary color resource
     * @param secondaryColor secondary color resource
     */
    public void setColors(int primaryColor, int secondaryColor) {
        this.secondaryColor = primaryColor;
        this.primaryColor = secondaryColor;

        circleView.setColors(primaryColor, secondaryColor);
        dotsView.setColors(primaryColor, secondaryColor);
    }

    /**
     * Sets the drawable that should be used for the button's
     * active state. The resource ID of the image should be used.
     *
     * @param imageId resource ID of the image
     */
    public void setActiveImageId(int imageId) {
        imageResourceIdActive = imageId;
    }

    /**
     * Sets the drawable that should be used for the button's
     * inactive state. The resource ID of the image should be used.
     *
     * @param imageId resource ID of the image
     */
    public void setInactiveImageId(int imageId) {
        imageResourceIdInactive = imageId;
    }

    /**
     * Sets the speed at which the animation should play.
     *
     * @param animationSpeed speed of animation
     */
    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    /**
     * Scales the size of the circle animation by the given factor,
     * with 1.0 being the original scale.
     *
     * @param scale scale factor to apply
     */
    public void setCircleScale(float scale) {
        scale = Math.max(scale, 0);
        circleSizeFactor = scale * INITIAL_CIRCLE_FACTOR;
        circleSize = (int) (imageSize * circleSizeFactor);

        final ViewGroup.LayoutParams params = circleView.getLayoutParams();
        params.height = circleSize;
        params.width = circleSize;
        circleView.setLayoutParams(params);
    }

    /**
     * Scales the size of the individual dot effects by the given factor,
     * with 1.0 being the original scale.
     *
     * @param scale scale factor to apply
     */
    public void setDotsScale(float scale) {
        scale = Math.max(scale, 0);
        dotsSizeFactor = scale * INITIAL_DOT_FACTOR;

        dotsView.setMaxDotSize((int) (imageSize * dotsSizeFactor));
    }

    /**
     * Scales the size of the dot container view by the given factor,
     * with 1.0 being the original scale.
     *
     * @param scale scale factor to apply
     */
    public void setDotContainerScale(float scale) {
        scale = Math.max(scale, 0);
        dotViewSizeFactor = scale * INITIAL_DOT_VIEW_FACTOR;
        dotsSize = (int) (imageSize * dotViewSizeFactor);

        final ViewGroup.LayoutParams params = dotsView.getLayoutParams();
        params.width = dotsSize;
        params.height = dotsSize;
        dotsView.setLayoutParams(params);
    }

    public void setEventListener(SparkEventListener listener) {
        this.listener = listener;
    }

    public void pressOnTouch(boolean pressOnTouch) {
        this.pressOnTouch = pressOnTouch;
        init();
    }

    @Override
    public void onClick(View v) {
        if (imageResourceIdInactive != INVALID_RESOURCE_ID) {
            isChecked = !isChecked;

            imageView.setImageResource(isChecked ? imageResourceIdActive : imageResourceIdInactive);

            if (animatorSet != null) {
                animatorSet.cancel();
            }
            if (isChecked) {
                playAnimation();
            }
        } else {
            playAnimation();
        }
        if (listener != null) {
            listener.onEvent(imageView, isChecked);
        }
    }

    private void setOnTouchListener() {
        if (pressOnTouch) {
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            imageView.animate().scaleX(0.8f).scaleY(0.8f).setDuration(150).setInterpolator(DECCELERATE_INTERPOLATOR);
                            setPressed(true);
                            break;

                        case MotionEvent.ACTION_MOVE:
                            float x = event.getX();
                            float y = event.getY();
                            boolean isInside = (x > 0 && x < getWidth() && y > 0 && y < getHeight());
                            if (isPressed() != isInside) {
                                setPressed(isInside);
                            }
                            break;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            imageView.animate().scaleX(1).scaleY(1).setInterpolator(DECCELERATE_INTERPOLATOR);
                            if (isPressed()) {
                                performClick();
                                setPressed(false);
                            }
                            break;
                    }
                    return true;
                }
            });
        } else {
            setOnTouchListener(null);
        }
    }

    private void getStuffFromXML(AttributeSet attr) {
        TypedArray a = getContext().obtainStyledAttributes(attr, R.styleable.sparkbutton);
        imageSize = a.getDimensionPixelOffset(R.styleable.sparkbutton_sparkbutton_iconSize, Utils.dpToPx(getContext(), 50));
        imageResourceIdActive = a.getResourceId(R.styleable.sparkbutton_sparkbutton_activeImage, INVALID_RESOURCE_ID);
        imageResourceIdInactive = a.getResourceId(R.styleable.sparkbutton_sparkbutton_inActiveImage, INVALID_RESOURCE_ID);
        primaryColor = ContextCompat.getColor(getContext(), a.getResourceId(R.styleable.sparkbutton_sparkbutton_primaryColor, R.color.spark_primary_color));
        secondaryColor = ContextCompat.getColor(getContext(), a.getResourceId(R.styleable.sparkbutton_sparkbutton_secondaryColor, R.color.spark_secondary_color));
        pressOnTouch = a.getBoolean(R.styleable.sparkbutton_sparkbutton_pressOnTouch, true);
        animationSpeed = a.getFloat(R.styleable.sparkbutton_sparkbutton_animationSpeed, 1);
    }
}