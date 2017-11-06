package newbilius.com.online_comics_reader.UI;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
    private IOnSwipeAction onSwipeAction;

    public SwipeGestureListener(IOnSwipeAction onSwipeAction) {
        this.onSwipeAction = onSwipeAction;
    }

    @Override
    public boolean onFling(MotionEvent motionEvent1, MotionEvent motionEvent2, float v, float v1) {
        float sensitivity = 80;
        float yMotion = Math.abs((motionEvent2.getY() - motionEvent1.getY()));
        float xMotion = Math.abs((motionEvent1.getX() - motionEvent2.getX()));
        if (yMotion > xMotion)
            return false;

        if ((motionEvent1.getX() - motionEvent2.getX()) > sensitivity) {
            onSwipeAction.FromRightToLeft();
            return true;
        } else if ((motionEvent2.getX() - motionEvent1.getX()) > sensitivity) {
            onSwipeAction.FromLeftToRight();
            return true;
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

}
