package ejunkins.rovercontroller;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.AttributeSet;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import static ejunkins.rovercontroller.R.attr.height;

/**
 * Joystick class for sending control signals to the raspberry pi
 */
public class ControlStick extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private float centerX;
    private float centerY;
    private float baseRadius;
    private float hatRadius;
    public JoystickListener joystickCallback;

    public ControlStick(Context context){
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    public ControlStick(Context context, AttributeSet attributes, int style){
        super(context,attributes,style);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    public ControlStick(Context context, AttributeSet attributes){
        super(context, attributes);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    /**
     * When the Joystick is first created draws the initial joysticks
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder){
        setupDimensions();
        drawJoystick(centerX,centerY);
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int forsat, int width, int height){
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
    }
    /**
     * Gets the dimensions of the space the joystick is allocated to
     */
    private void setupDimensions(){
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        baseRadius = Math.min(getWidth(), getHeight()) *4/11;
        hatRadius = Math.min(getWidth(), getHeight()) / 4;
    }

    /**
     * Draws the joysticks at their updated x and y location based on touch data
     * @param newX X location of touch
     * @param newY Y location of touch
     */

    @SuppressLint("NewApi")
    private void drawJoystick(float newX, float newY){
        /*
        int alpha = 255;
        int red = 100;
        int blue = 100;
        int green = 100;
        */

        int ratio = 5;
        if(getHolder().getSurface().isValid()) {
            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint colors = new Paint();
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            // Used to make 3-D effect on joystick
            float hypotenuse = (float) Math.sqrt(Math.pow(newX-centerX,2) + Math.pow(newY - centerY,2));
            float sin = (newY - centerY)/hypotenuse;
            float cos = (newX - centerX)/hypotenuse;

            Paint paint = new Paint();
            int r = Math.min(getWidth(),getHeight()); // r as in polar
            //RectF borderRect = new RectF(centerX - getHeight() / 6, centerY + getWidth() / 3, centerX + getHeight() / 6, centerY - getWidth() / 3);

            makeJoystickBase(paint, colors, myCanvas, r);
            makeJoystickStem(paint, colors, myCanvas, r);
            makeJoystickHat(colors, myCanvas, new JoystickHatFloats(newX, newY, hypotenuse, sin, cos, ratio));
        }
    }

    /**
     * Gets the location of the touch and converts them to locations that make sense to draw on based
     * on constraints of joystick
     * @param view
     * @param myEvent Touch event
     * @return
     */
    @Override
    public boolean onTouch(View view, MotionEvent myEvent){
        if (view.equals(this)) {
            if (myEvent.getAction() != myEvent.ACTION_UP) {
                int height = Math.min(getWidth(),getHeight())/3;
                float displacement = (float) Math.sqrt(Math.pow(myEvent.getY() - centerY, 2));
                if (displacement < height) {
                    drawJoystick(centerX, myEvent.getY());
                    joystickCallback.onJoystickMoved((myEvent.getX() - centerX)/height, (myEvent.getY() - centerY)/height, getId());
                }
                else{
                    float ratio = height/displacement;
                    float constrainedX = centerX + (myEvent.getX()-centerX)*ratio;
                    float constrainedY = centerY + (myEvent.getY()-centerY)*ratio;
                    drawJoystick(centerX,constrainedY);
                    joystickCallback.onJoystickMoved((constrainedX-centerX)/height, (constrainedY-centerY)/height, getId());
                }
            } else {
                drawJoystick(centerX, centerY);
                joystickCallback.onJoystickMoved(0,0,getId());
            }
        }
        return true;
    }

    /**
     * Listens for changes in each joystick based on its' ID
     */
    public interface JoystickListener {
        void onJoystickMoved(float xPercent, float yPercent, int id);
    }

    // ########################################
    // BEGIN drawJoystick Helper Methods
    // ########################################


    private RectF makeJoystickRectF(int x_rDisplacement, int y_rDisplacement, int shift) {
        return new RectF(centerX -  x_rDisplacement + shift, centerY + y_rDisplacement,
                         centerX + x_rDisplacement - shift, centerY - y_rDisplacement);
    }

    private void setJoystickHatColor(int drawIteration, Paint colors) {
        int tenPercHatRadius = (int) (hatRadius/10);
        int halfHatRadius = (int) (hatRadius/2);
        int twoThirdHatRadius = (int) (hatRadius*2/3);

        if (drawIteration <= tenPercHatRadius){
            colors.setARGB(255,0,0,52);

        } else if (drawIteration > tenPercHatRadius &&
                   drawIteration <= halfHatRadius ){
            colors.setARGB(255,0,0,52+2*i);

        } else if (drawIteration > halfHatRadius &&
                   drawIteration < twoThirdHatRadius){
            colors.setARGB(255,0,0,0);

        } else if (drawIteration >= twoThirdHatRadius){
            colors.setARGB(255,0,0,255) ;
        }
    }


    // TODO: Add documentation
    private void makeJoystickBase(Paint paint, Paint colors, Canvas myCanvas, int r) {
        paint.setColor(Color.TRANSPARENT);
        paint.setStyle(Paint.Style.FILL);

        int x_rDisplacement = r / 6,
            y_rDisplacement = r * (4/10); // Why not (2/5) instead?

        for (int shift = 1; shift <= 100; shift++) {
            int blueVal = shift;
            if (i > 50){
                blueVal = 2 * shift;
            }

            colors.setARGB(255, 0, 0, blueVal);
            RectF borderRect = makeJoystickRectF(x_rDisplacement, y_rDisplacement, shift);
            myCanvas.drawRoundRect(borderRect,50,50, colors);
        }
    }

    private void makeJoystickStem(Paint paint, Paint colors, Canvas myCanvas, int r) {
        //colors.setARGB(255,150,150,150);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(15);
        paint.setStyle(Paint.Style.STROKE);
        myCanvas.drawCircle(centerX, centerY, r * (4/9), paint);

        int ovalDisplacement = r / 4;
        // Make background oval
        colors.setARGB(255,255,255,255);
        myCanvas.drawOval(centerX - ovalDisplacement, centerY + ovalDisplacement,
                          centerX + ovalDisplacement, centerY - ovalDisplacement, colors);

        // Fill in over background oval
        for (int i = 2; i <= 100; i++) {
            colors.setARGB(50, i, i, i * 2);
            myCanvas.drawOval(centerX - ovalDisplacement, centerY + ovalDisplacement,
                              centerX + ovalDisplacement, centerY - ovalDisplacement, colors);
        }
    }

    private void makeJoystickHat(Paint colors, Canvas myCanvas, JoystickHatFloats hatFloats) {
        float newX = hatFloats.newX(),
            newY = hatFloats.newY(),
            hypotenuse = hatFloats.hypotenuse(),
            sin = hatFloats.sin(),
            cos = hatFloats.cos(),
            ratio = hatFloats.ratio();

        for (int i =1; i <= (int) (baseRadius/ratio); i++){
            colors.setARGB(255/i,0,0,0);
            myCanvas.drawCircle(newX - (cos * hypotenuse) * (ratio / baseRadius) * i,
                                newY - (sin * hypotenuse) * (ratio / baseRadius) * i,
                                i * (hatRadius * ratio / baseRadius),
                                colors);
        }

        colors.setARGB(255,0,0,0);
        myCanvas.drawCircle(newX, newY, hatRadius + (int) 0.2* hatRadius , colors);

        for(int drawIteration =0; drawIteration <= (int)( hatRadius); drawIteration++) {
            setJoystickHatColor(drawIteration, colors);
            myCanvas.drawCircle(newX, newY, hatRadius - (float) drawIteration * 2/3, colors);
        }

        getHolder().unlockCanvasAndPost(myCanvas);
    }

    // ########################################
    // END drawJoystick Helper Methods
    // ########################################

    /**
     * Immutable type to contain float parameters for ControlStick::drawJoyStick
     */
    private class JoystickHatFloats {
        private float newX, newY, hypotenuse, sin, cos, ratio;

        JoystickHatFloats(float newX, float newY, float hypotenuse, float sin, float cos, float ratio) {
            this.newX = newX;
            this.newY = newY;
            this.hypotenuse = hypotenuse;
            this.sin = sin;
            this.cos = cos;
            this.ratio = ratio;
        }

        float newX() {
            return newX;
        }

        float newY() {
            return newY;
        }

        float hypotenuse() {
            return hypotenuse;
        }

        float sin() {
            return sin;
        }

        float cos() {
            return cos;
        }

        float ratio() {
            return ratio;
        }
    }
}
