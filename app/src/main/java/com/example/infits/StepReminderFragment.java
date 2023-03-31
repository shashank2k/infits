package com.example.infits;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

public class StepReminderFragment extends Fragment {

    ImageView imgBack;
    TextView time, timeAmPm, remindOnceTime, remindOnceAmPm;
    CheckBox checkBox;
    Button dismiss;

    AlarmManager alarmManager;

    PendingIntent stepReceiverPendingIntent;

    long remindOnceTimeInMillis = 0L;

    public StepReminderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step_reminder, container, false);

        hooks(view);

        imgBack.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_stepReminderFragment_to_stepTrackerFragment));

        time.setOnClickListener(v -> showTimePicker());

        timeAmPm.setOnClickListener(v -> showTimePicker());

        remindOnceTime.setOnClickListener(v -> showTimePickerOnce());

        remindOnceAmPm.setOnClickListener(v -> showTimePickerOnce());

        checkBox.setOnCheckedChangeListener(((compoundButton, b) -> {
            if(b) {
                setAlarm(remindOnceTimeInMillis);
            } else {
                cancelOnceAlarm();
            }
        }));

        dismiss.setOnClickListener(v -> {
            dismissAlarm();
        });

        return view;
    }

    private void dismissAlarm() {
        if(alarmManager == null) {
            alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
        }

        alarmManager.cancel(stepReceiverPendingIntent);
        Toast.makeText(requireContext(), "Alarm Dismissed", Toast.LENGTH_LONG).show();
    }

    private void cancelOnceAlarm() {
        Intent stepReceiverIntent = new Intent(requireContext(), NotificationReceiver.class);
        PendingIntent stepReceiverPendingIntent = PendingIntent.getBroadcast(requireContext(), 0, stepReceiverIntent, PendingIntent.FLAG_IMMUTABLE);

        if(alarmManager == null) {
            alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
        }

        alarmManager.cancel(stepReceiverPendingIntent);
    }

    private void showTimePickerOnce() {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTitleText("SELECT REMINDER TIMING")
                .setHour(12)
                .setMinute(10)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setTheme(R.style.TimePickerStep)
                .build();

        timePicker.show(requireActivity().getSupportFragmentManager(), "Reminder");

        timePicker.addOnPositiveButtonClickListener(view -> {
            int pickedHour = timePicker.getHour();
            int pickedMinute = timePicker.getMinute();

            long millisInHour = 60 * 60 * 1000;
            long millisInMinute = 60 * 1000;
            remindOnceTimeInMillis = pickedHour * millisInHour + pickedMinute * millisInMinute;

            setTextFieldsOnce(pickedHour, pickedMinute);
        });
    }

    private void showTimePicker() {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTitleText("SELECT REMINDER TIMING")
                .setHour(12)
                .setMinute(10)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setTheme(R.style.TimePickerStep)
                .build();

        timePicker.show(requireActivity().getSupportFragmentManager(), "Reminder");

        timePicker.addOnPositiveButtonClickListener(view -> {
            int pickedHour = timePicker.getHour();
            int pickedMinute = timePicker.getMinute();

            long millisInHour = 60 * 60 * 1000;
            long millisInMinute = 60 * 1000;
            long timeInMillis = pickedHour * millisInHour + pickedMinute * millisInMinute;

            setTextFields(pickedHour, pickedMinute);
            setAlarm(timeInMillis);
        });
    }

    private void setTextFieldsOnce(int pickedHour, int pickedMinute) {
        String timeText = "", amPm = "AM";

        if(pickedHour > 12) {
            pickedHour -= 12;
            amPm = "PM";
        }

        timeText = pickedHour + ":";
        if(pickedMinute < 10) {
            timeText += "0" + pickedMinute;
        } else {
            timeText += pickedMinute;
        }

        remindOnceTime.setText(timeText);
        remindOnceAmPm.setText(amPm);
    }

    private void setTextFields(int pickedHour, int pickedMinute) {
        String timeText = "", amPm = "AM";

        if(pickedHour > 12) {
            pickedHour -= 12;
            amPm = "PM";
        }

        timeText = pickedHour + ":";
        if(pickedMinute < 10) {
            timeText += "0" + pickedMinute;
        } else {
            timeText += pickedMinute;
        }

        time.setText(timeText);
        timeAmPm.setText(amPm);
    }

    private void setAlarm(long time) {
        createNotificationChannel();

        AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);

        Intent stepReceiverIntent = new Intent(requireActivity(), NotificationReceiver.class);
        stepReceiverIntent.putExtra("tracker", "step");

        stepReceiverPendingIntent = PendingIntent.getBroadcast(
                requireActivity(), 0, stepReceiverIntent, PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.set(AlarmManager.RTC_WAKEUP, time, stepReceiverPendingIntent);
        Log.d("setAlarm", "alarm set");
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("StepChannelId", "Step Reminder", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = requireActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void hooks(View view) {
        imgBack = view.findViewById(R.id.img_back);
        time = view.findViewById(R.id.time);
        timeAmPm = view.findViewById(R.id.timeAmPm);
        remindOnceTime = view.findViewById(R.id.remind_once_time);
        remindOnceAmPm = view.findViewById(R.id.remind_once_time_am_pm);
        checkBox = view.findViewById(R.id.checkbox);
        dismiss = view.findViewById(R.id.dismiss);
    }
}