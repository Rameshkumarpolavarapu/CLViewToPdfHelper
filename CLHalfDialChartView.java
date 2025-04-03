package com.ramesh.graph;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.focus.centra.crm.data.model.dashboard.kpichart.CLBaseBulletKPIValueDTO;
import com.focus.centra.crm.data.model.dashboard.kpichart.CLSingleBulletKPIDTO;

import java.text.NumberFormat;

/**
 *   Created by Ramesh Kumar on 02-Apr-2025 4:43:48 PM
 */

@SuppressLint("ViewConstructor")
public class CLHalfDialChartView extends View {
    private final Paint backgroundPaint = new Paint();
    private final Paint progressPaint = new Paint();
    private final Paint progressBgPaint = new Paint();
    private final Paint targetPaint = new Paint();
    private final Paint innerProgressPaint = new Paint();
    private final Paint outerProgressPaint = new Paint();
    private final Paint startLinePaint = new Paint();
    private final Paint endLinePaint = new Paint();
    private final Paint indicatorPaint = new Paint();

    // Add padding fields
    private final int viewPadding = 200; // Padding around the entire view
    private final int innerPadding = 20; // Padding inside the view for elements
    private float minValue = 0;
    private float maxValue = 0;
    private float value = 0;
    private float targetValue = 0;
    private int numberOfIndicators = 5;
    private int progressBarColor = Color.BLUE;

    private final float startAngle = 180f;
    private final RectF arcRect = new RectF();
    private float animatedValue = 0;

    private final int iDialWidth = 55;

    private final CLBaseBulletKPIValueDTO clBaseBulletKPIValueDTO;
    private final CLSingleBulletKPIDTO clSingleBulletKPIDTO;

    public CLHalfDialChartView(Context context,
                             CLBaseBulletKPIValueDTO clBaseBulletKPIValueDTO,
                             CLSingleBulletKPIDTO clSingleBulletKPIDTO) {
        super(context);
        this.clBaseBulletKPIValueDTO = clBaseBulletKPIValueDTO;
        this.clSingleBulletKPIDTO = clSingleBulletKPIDTO;

        this.minValue = (float) (clSingleBulletKPIDTO.getMinValue());
        this.value = (float) (clSingleBulletKPIDTO.getActualValue());
        this.targetValue = (float) (clSingleBulletKPIDTO.getTargetValue());
        this.numberOfIndicators = 5;

        int intValue = (int) (clSingleBulletKPIDTO.getMaxValue());
        this.maxValue = (intValue % 2 == 0) ? intValue : intValue + 1;

        this.progressBarColor = clBaseBulletKPIValueDTO.getBulletColor();
        setPadding(viewPadding, viewPadding, viewPadding, viewPadding);
        initView();
    }

    private void initView()
    {

        // Initialize paints
        backgroundPaint.setColor(Color.parseColor("#4D808080"));
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(iDialWidth);
        backgroundPaint.setAntiAlias(true);

//        progressPaint.setColor(progressBarColor);
        progressPaint.setColor(Color.BLACK);
//        progressPaint.setAlpha(204); // 80% opacity
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(iDialWidth);
        progressPaint.setAntiAlias(true);

        progressBgPaint.setColor(progressBarColor);
        progressBgPaint.setStyle(Paint.Style.STROKE);
        progressBgPaint.setStrokeWidth(iDialWidth);
        progressBgPaint.setAntiAlias(true);

        targetPaint.setColor(Color.RED);
        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setStrokeWidth(3);
        targetPaint.setAntiAlias(true);

        innerProgressPaint.setColor(Color.GRAY);
        innerProgressPaint.setStyle(Paint.Style.STROKE);
        innerProgressPaint.setStrokeWidth(2);
        innerProgressPaint.setAntiAlias(true);

        outerProgressPaint.setColor(Color.GRAY);
        outerProgressPaint.setStyle(Paint.Style.STROKE);
        outerProgressPaint.setStrokeWidth(2);
        outerProgressPaint.setAntiAlias(true);

        startLinePaint.setColor(Color.GRAY);
        startLinePaint.setStyle(Paint.Style.STROKE);
        startLinePaint.setStrokeWidth(2);
        startLinePaint.setAntiAlias(true);

        endLinePaint.setColor(Color.GRAY);
        endLinePaint.setStyle(Paint.Style.STROKE);
        endLinePaint.setStrokeWidth(2);
        endLinePaint.setAntiAlias(true);

        indicatorPaint.setColor(Color.GRAY);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setStrokeWidth(3);
        indicatorPaint.setAntiAlias(true);

        // Initialize labels
        TextView valueLabel = new TextView(getContext());
        valueLabel.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);

        TextView targetValueLabel = new TextView(getContext());
        targetValueLabel.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        targetValueLabel.setTextColor(Color.BLACK);
        valueLabel.setTextColor(Color.BLACK);


        updateChart();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
//        setMeasuredDimension(size, size / 2); // Half circle
        // Account for padding in measured dimensions
        setMeasuredDimension(size + 2 * viewPadding, (size / 2) + 2 * viewPadding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Account for padding in all calculations
        float availableWidth = getWidth() - 2 * viewPadding;
        float availableHeight = getHeight() - 2 * viewPadding;

        float radius = (availableWidth / 2f) - innerPadding;
        float centerX = viewPadding + availableWidth / 2f;
        float centerY = viewPadding + availableHeight - innerPadding;

        // Draw background arc (with padding adjustment)
        arcRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(arcRect, startAngle, 180, false, backgroundPaint);

        // Draw progress arc
        float sweepAngle1 = 180 * (animatedValue - minValue) / (maxValue - minValue);
        canvas.drawArc(arcRect, startAngle, sweepAngle1, false, progressPaint);

        // Draw bg progress arc
        float sweepAngle = 180 * (animatedValue - minValue) / (maxValue - minValue);
        canvas.drawArc(arcRect, startAngle, sweepAngle-1, false, progressBgPaint);

        // Draw inner and outer arcs
        float innerRadius = radius - iDialWidth;
        float outerRadius = radius + iDialWidth;
        RectF innerRect = new RectF(centerX - innerRadius, centerY - innerRadius,
                                   centerX + innerRadius, centerY + innerRadius);
        RectF outerRect = new RectF(centerX - outerRadius, centerY - outerRadius,
                                   centerX + outerRadius, centerY + outerRadius);

        canvas.drawArc(innerRect, startAngle, 180, false, innerProgressPaint);
        canvas.drawArc(outerRect, startAngle, 180, false, outerProgressPaint);

        // Draw start and end lines
        drawStartAndEndLines(canvas, centerX, centerY, radius);

        // Draw indicators and labels
        drawIndicators(canvas, centerX, centerY, radius);

        // value and target labels
        drawTextLabels(canvas);
        // target circle
        drawTargetPointer(canvas);
    }

    private void drawTargetPointer(Canvas canvas) {
        // Calculate dimensions
        float availableWidth = getWidth() - 2 * viewPadding;
        float availableHeight = getHeight() - 2 * viewPadding;
        float radius = (availableWidth / 2f) - innerPadding;
        float centerX = viewPadding + availableWidth / 2f;
        float centerY = viewPadding + availableHeight - innerPadding;

        // Ensure target is within range
        float clampedTarget = Math.max(minValue, Math.min(targetValue, maxValue));

        // Calculate angle for target (180° to 0° range)
        float targetAngle = startAngle + (180 * (clampedTarget - minValue) / (maxValue - minValue));

        // Calculate position between inner and outer arcs
        float innerRadius = radius - iDialWidth;
        float outerRadius = radius + iDialWidth;
        float targetRadius = (innerRadius + outerRadius) / 2; // Middle point

        // Calculate target circle position
        float targetX = centerX + targetRadius * (float) Math.cos(Math.toRadians(targetAngle));
        float targetY = centerY + targetRadius * (float) Math.sin(Math.toRadians(targetAngle));

        // Draw the target circle
        targetPaint.setStyle(Paint.Style.FILL);
        targetPaint.setColor(Color.RED);
        canvas.drawCircle(targetX, targetY, 10, targetPaint); // 10 is the radius of the target circle

    }

    private void drawTextLabels(Canvas canvas) {
        // Create text paints
        TextPaint valuePaint = new TextPaint();
        valuePaint.setTextSize(48);
        valuePaint.setColor(Color.BLACK);
        valuePaint.setAntiAlias(true);
        valuePaint.setTextAlign(Paint.Align.CENTER);

        TextPaint targetPaint = new TextPaint();
        targetPaint.setTextSize(48);
        targetPaint.setColor(Color.RED); // Different color for target
        targetPaint.setAntiAlias(true);
        targetPaint.setTextAlign(Paint.Align.CENTER);

        // Prepare text values
        String valueText = formatWithSuffix(animatedValue, true);
        String targetText = formatWithSuffix(targetValue, true);

        // Calculate text bounds
        Rect valueBounds = new Rect();
        valuePaint.getTextBounds(valueText, 0, valueText.length(), valueBounds);

        Rect targetBounds = new Rect();
        targetPaint.getTextBounds(targetText, 0, targetText.length(), targetBounds);

        // Calculate vertical positions
        float baseY = ((float) getHeight() / 2) + 100; // Your original position
        // Target below value with 20px gap
        float valueY = baseY - valueBounds.height() - 20;

        // Draw target text (above)
        canvas.drawText(targetText, (float) getWidth() / 2, baseY, targetPaint);

        // Draw value text (below)
        canvas.drawText(valueText, (float) getWidth() / 2, valueY, valuePaint);
    }

    private void drawStartAndEndLines(Canvas canvas, float centerX, float centerY, float radius) {
        // Start line
        float startInnerX = centerX + (radius - iDialWidth) * (float) Math.cos(Math.toRadians(startAngle));
        float startInnerY = centerY + (radius - iDialWidth) * (float) Math.sin(Math.toRadians(startAngle));
        canvas.drawLine(startInnerX, startInnerY, 150, startInnerY, startLinePaint);

        // End line
        float endAngle = 0f;
        float endInnerX = centerX + (radius - iDialWidth) * (float) Math.cos(Math.toRadians(endAngle));
        float endInnerY = centerY + (radius - iDialWidth) * (float) Math.sin(Math.toRadians(endAngle));
        canvas.drawLine(endInnerX, endInnerY, getWidth()-150, endInnerY, endLinePaint);
    }

    private void drawIndicators(Canvas canvas, float centerX, float centerY, float radius) {
        if (numberOfIndicators <= 1) return;

        float interval = (maxValue - minValue) / (numberOfIndicators - 1);
        float tickLength = 6;

        for (int i = 0; i < numberOfIndicators; i++) {
            float indicatorValue = minValue + interval * i;
            float angle = startAngle + (180 * (indicatorValue - minValue) / (maxValue - minValue));

            // Draw tick mark
            float startX = centerX + (radius + 26) * (float) Math.cos(Math.toRadians(angle));
            float startY = centerY + (radius + 26) * (float) Math.sin(Math.toRadians(angle));
            float endX = centerX + (radius + 20) * (float) Math.cos(Math.toRadians(angle));
            float endY = centerY + (radius + 20) * (float) Math.sin(Math.toRadians(angle));

            if (i == 1)
            {
                canvas.drawLine(startX-20,startY-20,endX-39,endY-41, indicatorPaint);
            }
            else if (i == 2)
            {
                canvas.drawLine(startX, startY - 45, endX, endY - 35, indicatorPaint);
            }
            else if (i == 3)
            {
                canvas.drawLine(startX+17,startY-23,endX+32,endY-38, indicatorPaint);
            }
            // Draw label
            String text = formatWithSuffix(indicatorValue, false);
            TextPaint textPaint = new TextPaint();
            textPaint.setTextSize(36);
            textPaint.setColor(Color.BLACK);

            float textWidth = textPaint.measureText(text);
            float labelRadius = radius + 50 + textWidth / 2;
            float labelX = centerX + labelRadius * (float) Math.cos(Math.toRadians(angle));
            float labelY = centerY + labelRadius * (float) Math.sin(Math.toRadians(angle));

            if(i == 0 || i == numberOfIndicators-1)
            {
                if(i == 0)
                {
                    canvas.drawText(text, (labelX - 50) - (textWidth / 2), labelY, textPaint);
                }
                else
                {
                    canvas.drawText(text, (labelX + 50) - (textWidth / 2), labelY, textPaint);
                }

            }
            else
            {
                canvas.drawText(text, labelX - textWidth / 2, labelY+10, textPaint);
            }


        }
    }



    private void updateChart() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, value);
        animator.setDuration(1500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animatedValue = (float) animation.getAnimatedValue();
//            pointerAngle = startAngle + (180 * (animatedValue - minValue) / (maxValue - minValue));
            invalidate();
        });
        animator.start();
    }

    private String formatWithSuffix(float value, boolean useComma) {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(0);
        formatter.setGroupingUsed(useComma);
        return formatter.format(value) + " " + (clBaseBulletKPIValueDTO.getSuffix() != null ? clBaseBulletKPIValueDTO.getSuffix() : "");
    }

    private float angle(float value) {
        // Implement your angle calculation based on your value range and visual representation.
        // This is a placeholder; you'll need to adapt it.
        float range = maxValue - minValue;
        float normalizedValue = (value - minValue) / range;
        return (float) (Math.PI * normalizedValue); // Assuming 180 degrees representation
    }

}
