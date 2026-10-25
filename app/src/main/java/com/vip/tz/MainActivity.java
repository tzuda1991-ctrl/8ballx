package com.vip.tz;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private MyOverlayView overlayView;
    private boolean isOverlayActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Simple UI Button just to trigger the overlay start
        setContentView(R.layout.activity_main);
        
        Button btnStart = findViewById(R.id.btn_start);
        btnStart.setText("Enable & Start Line");
        btnStart.setOnClickListener(v -> {
            if (!isAccessibilityEnabled()) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } else {
                startOverlay();
            }
        });
    }

    private void startOverlay() {
        if (overlayView == null) {
            overlayView = new MyOverlayView(this);
        }
        
        // Create Window Manager for Overlay
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            -3 // PixelFormat.TRANSLUCENT
        );
        
        params.alpha = 0.8f; // Slightly transparent so you can see game
        
        getWindowManager().addView(overlayView, params);
        isOverlayActive = true;
    }

    private boolean isAccessibilityEnabled() {
        String enabledServices = Settings.Secure.getString(
            getContentResolver(), 
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        
        if (enabledServices == null) return false;
        
        // Check if our service is enabled
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
                        // Optional: You can add a tap here to shoot if needed
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
                // Draw a small dot at center if no touch
                canvas.drawCircle(getWidth()/2, getHeight()/2, 5, paint);
            } else {
                canvas.drawPath(path, paint);
            }
        }
    }
}
