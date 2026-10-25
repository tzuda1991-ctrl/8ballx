package com.vip.tz;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private MyOverlayView overlayView;
    private boolean isOverlayActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Programmatic UI (Kisi XML layout file ki zaroorat nahi!)
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50, 50, 50, 50);

        Button btnStart = new Button(this);
        btnStart.setText("Enable & Start Line");
        btnStart.setOnClickListener(v -> {
            // 1. Pehle Overlay Permission check karein
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please allow Overlay Permission", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return;
            }

            // 2. Phir Accessibility check karein
            if (!isAccessibilityEnabled()) {
                Toast.makeText(this, "Please enable Accessibility Service", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } else {
                startOverlay();
            }
        });

        layout.addView(btnStart);
        setContentView(layout);
    }

    private void startOverlay() {
        if (isOverlayActive) {
            Toast.makeText(this, "Overlay is already running!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (overlayView == null) {
            overlayView = new MyOverlayView(this);
        }

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        params.alpha = 0.8f;

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm != null) {
            wm.addView(overlayView, params);
            isOverlayActive = true;
            Toast.makeText(this, "Line Overlay Started!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isAccessibilityEnabled() {
        String enabledServices = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        if (enabledServices == null) return false;

        String myServiceName = getPackageName() + "/com.vip.tz.MyAccessibilityService";
        return enabledServices.contains(myServiceName);
    }

    // Custom View for Drawing Long Lines
    private static class MyOverlayView extends View {
        Paint paint = new Paint();
        Path path = new Path();
        boolean drawing = false;

        public MyOverlayView(android.content.Context context) {
            super(context);
            initPaint();
        }

        private void initPaint() {
            paint.setColor(Color.parseColor("#00FF00")); // Green Color for Line
            paint.setStrokeWidth(8);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);

            setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        drawing = true;
                        path.reset();
                        path.moveTo(event.getX(), event.getY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (drawing) {
                            path.lineTo(event.getX(), event.getY());
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        drawing = false;
                        break;
                }
                invalidate();
                return true;
            });
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (path.isEmpty()) {
                canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, 5, paint);
            } else {
                canvas.drawPath(path, paint);
            }
        }
    }
}
