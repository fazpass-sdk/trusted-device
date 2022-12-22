package com.fazpass.trusted_device;

import static com.fazpass.trusted_device.BASE.USER_PIN;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.function.Function;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class Notification {
    protected static final String NOTIFICATION_DEVICE = "device";
    protected static final String NOTIFICATION_STATUS = "status";
    protected static final String CROSS_DEVICE_CHANNEL = "cross_device_channel";
    protected static final String NOTIFICATION_ID = "notification_id";
    protected static final String NOTIFICATION_TOKEN = "notification_token";
    protected static final String NOTIFICATION_REQ_ID = "notification_request_id";

    protected static boolean IS_REQUIRE_PIN = false;

    /**
     * Dialog builder class for Fazpass.launchedFromNotification dialogBuilder parameter.
     */
    public static class DialogBuilder {
        private final Context context;
        private View contentView;
        private Function<String, String> textMessage;
        private int textMessageId;
        private int positiveButtonId;
        private int negativeButtonId;
        private int inputId;

        /**
         * Default constructor for dialog builder.
         * <p>
         * Before passing this instance as an argument, every field must be set by:<p>
         * <code>
         *     setContentView(View)
         *     setTextMessage(device -> String)
         *     setTextMessageId(int)
         *     setInputId(int)
         *     setPositiveButtonId(int)
         *     setNegativeButtonId(int)
         * </code>
         */
        DialogBuilder(Context context) {
            this.context = context;
        }

        public DialogBuilder setContentView(View contentView) {
            this.contentView = contentView;
            return this;
        }

        public DialogBuilder setTextMessage(Function<String, String> textMessage) {
            this.textMessage = textMessage;
            return this;
        }

        public DialogBuilder setTextMessageId(int textMessageId) {
            this.textMessageId = textMessageId;
            return this;
        }

        public DialogBuilder setPositiveButtonId(int positiveButtonId) {
            this.positiveButtonId = positiveButtonId;
            return this;
        }

        public DialogBuilder setNegativeButtonId(int negativeButtonId) {
            this.negativeButtonId = negativeButtonId;
            return this;
        }

        public DialogBuilder setInputId(int inputId) {
            this.inputId = inputId;
            return this;
        }

        AlertDialog build(String notificationId, String notificationToken, String device, int requestId) {
            if (contentView==null||inputId==0||positiveButtonId==0||negativeButtonId==0)
                throw new NullPointerException("Fazpass Notification: every field must be set");
            String cryptPin = Storage.readDataLocal(context, USER_PIN);

            final EditText input = contentView.findViewById(inputId);
            final Button positiveButton = contentView.findViewById(positiveButtonId);
            final Button negativeButton = contentView.findViewById(negativeButtonId);
            final TextView textMessageView = contentView.findViewById(textMessageId);
            textMessageView.setText(textMessage.apply(device));
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(contentView)
                    .create();

            if (Notification.IS_REQUIRE_PIN) {
                input.setVisibility(View.VISIBLE);
            }
            positiveButton.setOnClickListener(v -> {
                if (Notification.IS_REQUIRE_PIN) {
                    String inputtedPin = input.getText().toString();

                    if (inputtedPin.equals("")) {
                        Toast.makeText(context, "PIN should be filled.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BCrypt.Result result = BCrypt.verifyer().verify(inputtedPin.toCharArray(), cryptPin);
                    if (result.verified) {
                        onConfirmation(context,"com.fazpass.trusted_device.CONFIRM_STATUS", notificationId, requestId, device, notificationToken);
                    } else {
                        Toast.makeText(context, "PIN doesn't match.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    onConfirmation(context, "com.fazpass.trusted_device.CONFIRM_STATUS", notificationId, requestId, device, notificationToken);
                }
                dialog.dismiss();
            });
            negativeButton.setOnClickListener(v -> {
                Notification.onConfirmation(context,"com.fazpass.trusted_device.DECLINE_STATUS", notificationId, requestId, device, notificationToken);
                dialog.dismiss();
            });
            return dialog;
        }
    }

    protected static void onConfirmation(Context context, String action, String notificationId, int requestId, String device, String notificationToken) {
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.setAction(action);
        intent.putExtra(Notification.NOTIFICATION_ID, notificationId);
        intent.putExtra(Notification.NOTIFICATION_REQ_ID, requestId);
        intent.putExtra(Notification.NOTIFICATION_DEVICE, device);
        intent.putExtra(Notification.NOTIFICATION_TOKEN, notificationToken);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}
