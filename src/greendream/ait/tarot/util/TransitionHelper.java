package greendream.ait.tarot.util;

import android.app.Activity;
import android.content.Intent;
import greendream.ait.tarot.R;

public class TransitionHelper {
    public static void transitionTo(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public static void transitionTo(Activity activity, Class<?> cls) {
        activity.startActivity(new Intent(activity, cls));
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    
    public static void finish(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
