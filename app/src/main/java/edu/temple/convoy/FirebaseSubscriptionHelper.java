package edu.temple.convoy;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import static android.content.ContentValues.TAG;

public class  FirebaseSubscriptionHelper {

    public static void subscribe(Context context, String convoyNo){
        FirebaseMessaging.getInstance().subscribeToTopic(convoyNo)
                .addOnCompleteListener(task -> {
                    String msg = context.getString(R.string.msg_subscribed);
                    if (!task.isSuccessful()) {
                        msg = context.getString(R.string.msg_subscribe_failed);
                    }
                    Log.d(TAG, msg);
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                });
    }
}
