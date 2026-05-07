package com.zack.starttimesender;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.InputType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String PREFS = "start_time_sender_prefs";
    private static final String DATA_KEY = "people_json";
    private static final int BG = Color.rgb(21, 16, 13);
    private static final int PANEL = Color.rgb(47, 33, 26);
    private static final int INPUT = Color.rgb(17, 12, 10);
    private static final int TEXT = Color.rgb(255, 247, 237);
    private static final int MUTED = Color.rgb(200, 183, 167);
    private static final int ACCENT = Color.rgb(243, 165, 31);
    private static final int GREEN = Color.rgb(34, 197, 94);

    private EditText messageBox;
    private EditText startTimeBox;
    private EditText noteBox;
    private TextView sentBadge;
    private LinearLayout rowOneList;
    private LinearLayout rowTwoList;
    private final List<Person> people = new ArrayList<>();

    static class Person {
        String name;
        String phone;
        int row;
        boolean sent;
        Person(String name, String phone, int row, boolean sent) {
            this.name = name;
            this.phone = phone;
            this.row = row;
            this.sent = sent;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadData();
        buildScreen();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(14), dp(14), dp(26));
        scroll.addView(root);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText("Start Time Sender");
        title.setTextColor(TEXT);
        title.setTextSize(26);
        title.setTypeface(null, 1);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        sentBadge = new TextView(this);
        sentBadge.setTextColor(ACCENT);
        sentBadge.setTextSize(14);
        sentBadge.setTypeface(null, 1);
        header.addView(sentBadge);
        root.addView(header);

        TextView subtitle = new TextView(this);
        subtitle.setText("Build tomorrow's start-time message, send each text, and track who is complete.");
        subtitle.setTextColor(MUTED);
        subtitle.setPadding(0, dp(4), 0, dp(12));
        root.addView(subtitle);

        LinearLayout card = card();
        TextView label = label("CUSTOM MESSAGE");
        card.addView(label);
        messageBox = editText("Hey guys, great job today. Tomorrow ST is 4:30 AM. Please be on time.", true);
        card.addView(messageBox);

        LinearLayout messageInputs = new LinearLayout(this);
        messageInputs.setOrientation(LinearLayout.HORIZONTAL);
        messageInputs.setPadding(0, dp(8), 0, 0);
        startTimeBox = editText("4:30 AM", false);
        noteBox = editText("Great job today", false);
        messageInputs.addView(startTimeBox, new LinearLayout.LayoutParams(0, -2, 1));
        messageInputs.addView(noteBox, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(messageInputs);

        LinearLayout quick = new LinearLayout(this);
        quick.setOrientation(LinearLayout.HORIZONTAL);
        quick.setPadding(0, dp(8), 0, 0);
        addQuickTime(quick, "4:30 AM");
        addQuickTime(quick, "4:50 AM");
        addQuickTime(quick, "5:00 AM");
        addQuickTime(quick, "5:15 AM");
        card.addView(quick);

        LinearLayout toolRow = new LinearLayout(this);
        toolRow.setOrientation(LinearLayout.HORIZONTAL);
        toolRow.setPadding(0, dp(8), 0, 0);
        Button build = button("Build Message", ACCENT, Color.rgb(27,17,11));
        build.setOnClickListener(v -> buildMessage());
        Button copy = button("Copy", Color.rgb(58,42,33), TEXT);
        copy.setOnClickListener(v -> {
            android.content.ClipboardManager cm = (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(android.content.ClipData.newPlainText("Start Time", messageBox.getText().toString()));
            saveData();
        });
        toolRow.addView(build, new LinearLayout.LayoutParams(0, dp(48), 1));
        toolRow.addView(copy, new LinearLayout.LayoutParams(0, dp(48), 1));
        card.addView(toolRow);
        root.addView(card);

        LinearLayout rowOneCard = card();
        rowOneCard.addView(sectionTitle("Row 1"));
        rowOneList = new LinearLayout(this);
        rowOneList.setOrientation(LinearLayout.VERTICAL);
        rowOneCard.addView(rowOneList);
        Button add1 = button("+ Add Person", Color.rgb(58,42,33), TEXT);
        add1.setOnClickListener(v -> { people.add(new Person("", "", 1, false)); saveData(); renderPeople(); });
        rowOneCard.addView(add1);
        root.addView(rowOneCard);

        LinearLayout rowTwoCard = card();
        rowTwoCard.addView(sectionTitle("Row 2"));
        rowTwoList = new LinearLayout(this);
        rowTwoList.setOrientation(LinearLayout.VERTICAL);
        rowTwoCard.addView(rowTwoList);
        Button add2 = button("+ Add Person", Color.rgb(58,42,33), TEXT);
        add2.setOnClickListener(v -> { people.add(new Person("", "", 2, false)); saveData(); renderPeople(); });
        rowTwoCard.addView(add2);
        root.addView(rowTwoCard);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setPadding(0, dp(4), 0, 0);
        Button reset = button("Reset Checks", Color.rgb(90,36,36), Color.rgb(255,220,220));
        reset.setOnClickListener(v -> { for (Person p : people) p.sent = false; saveData(); renderPeople(); });
        Button sendAll = button("Send All List", Color.rgb(23,53,31), Color.rgb(210,255,220));
        sendAll.setOnClickListener(v -> sendAll());
        bottom.addView(reset, new LinearLayout.LayoutParams(0, dp(52), 1));
        bottom.addView(sendAll, new LinearLayout.LayoutParams(0, dp(52), 1));
        root.addView(bottom);

        setContentView(scroll);
        renderPeople();
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackgroundColor(PANEL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(lp);
        return card;
    }

    private TextView label(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(ACCENT);
        t.setTypeface(null, 1);
        t.setPadding(0, 0, 0, dp(6));
        return t;
    }

    private TextView sectionTitle(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(TEXT);
        t.setTextSize(20);
        t.setTypeface(null, 1);
        t.setPadding(0, 0, 0, dp(8));
        return t;
    }

    private EditText editText(String hint, boolean multi) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(MUTED);
        e.setTextColor(TEXT);
        e.setTextSize(16);
        e.setSingleLine(!multi);
        e.setMinLines(multi ? 3 : 1);
        e.setBackgroundColor(INPUT);
        e.setPadding(dp(10), dp(8), dp(10), dp(8));
        e.setInputType(multi ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE : InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dp(3), dp(3), dp(3), dp(3));
        e.setLayoutParams(lp);
        return e;
    }

    private Button button(String text, int bg, int fg) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(fg);
        b.setTextSize(14);
        b.setTypeface(null, 1);
        b.setBackgroundColor(bg);
        return b;
    }

    private void addQuickTime(LinearLayout row, String time) {
        Button b = button(time.replace(" AM", ""), Color.rgb(58,42,33), TEXT);
        b.setOnClickListener(v -> { startTimeBox.setText(time); buildMessage(); });
        row.addView(b, new LinearLayout.LayoutParams(0, dp(44), 1));
    }

    private void renderPeople() {
        rowOneList.removeAllViews();
        rowTwoList.removeAllViews();
        for (Person p : people) {
            View personView = personView(p);
            if (p.row == 2) rowTwoList.addView(personView); else rowOneList.addView(personView);
        }
        updateBadge();
    }

    private View personView(Person p) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, 0, 0, dp(10));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        EditText name = editText("Name", false);
        name.setText(p.name);
        EditText phone = editText("Phone", false);
        phone.setText(p.phone);
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        top.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(phone, new LinearLayout.LayoutParams(0, -2, 1));
        box.addView(top);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button send = button("Send", Color.rgb(31,59,36), Color.rgb(217,255,226));
        TextView check = new TextView(this);
        check.setText(p.sent ? "✓" : "");
        check.setTextColor(p.sent ? GREEN : MUTED);
        check.setTextSize(26);
        check.setGravity(Gravity.CENTER);
        check.setTypeface(null, 1);
        Button remove = button("×", Color.rgb(58,42,33), TEXT);
        actions.addView(send, new LinearLayout.LayoutParams(0, dp(48), 1));
        actions.addView(check, new LinearLayout.LayoutParams(dp(52), dp(48)));
        actions.addView(remove, new LinearLayout.LayoutParams(dp(52), dp(48)));
        box.addView(actions);

        name.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) { p.name = name.getText().toString(); saveData(); } });
        phone.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) { p.phone = phone.getText().toString(); saveData(); } });
        send.setOnClickListener(v -> {
            p.name = name.getText().toString();
            p.phone = phone.getText().toString();
            sendPerson(p);
        });
        remove.setOnClickListener(v -> { people.remove(p); saveData(); renderPeople(); });
        return box;
    }

    private void buildMessage() {
        String st = startTimeBox.getText().toString().trim();
        if (st.isEmpty()) st = "4:30 AM";
        String note = noteBox.getText().toString().trim();
        String msg = note.isEmpty()
                ? "Hey guys, tomorrow ST is " + st + ". Please be on time."
                : "Hey guys, " + note + ". Tomorrow ST is " + st + ". Please be on time.";
        messageBox.setText(msg);
        saveData();
    }

    private void sendPerson(Person p) {
        saveFromBoxes();
        String phone = cleanPhone(p.phone);
        String message = messageBox.getText().toString();
        if (phone.isEmpty() || message.trim().isEmpty()) return;
        p.sent = true;
        saveData();
        renderPeople();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + Uri.encode(phone)));
        intent.putExtra("sms_body", message);
        startActivity(intent);
    }

    private void sendAll() {
        saveFromBoxes();
        StringBuilder phones = new StringBuilder();
        for (Person p : people) {
            String phone = cleanPhone(p.phone);
            if (!phone.isEmpty()) {
                if (phones.length() > 0) phones.append(';');
                phones.append(phone);
                p.sent = true;
            }
        }
        saveData();
        renderPeople();
        if (phones.length() == 0 || messageBox.getText().toString().trim().isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + Uri.encode(phones.toString())));
        intent.putExtra("sms_body", messageBox.getText().toString());
        startActivity(intent);
    }

    private String cleanPhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9+]", "");
    }

    private void saveFromBoxes() {
        getPreferences(MODE_PRIVATE).edit()
                .putString("message", messageBox.getText().toString())
                .putString("startTime", startTimeBox.getText().toString())
                .putString("note", noteBox.getText().toString())
                .apply();
    }

    private void loadData() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String raw = sp.getString(DATA_KEY, "");
        if (!raw.isEmpty()) {
            try {
                JSONArray arr = new JSONArray(raw);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    people.add(new Person(o.optString("name"), o.optString("phone"), o.optInt("row", 1), o.optBoolean("sent", false)));
                }
                return;
            } catch (Exception ignored) {}
        }
        String[] row1 = {"Phillips","Ellis","May","Cory","Cavanaugh","Hershal","Maria","Bass","Jeremy","Sliger","Noah","Seiber","Samples","Sharkey","Capps","Moore"};
        for (String n : row1) people.add(new Person(n, "", 1, false));
        people.add(new Person("McPherson", "", 2, false));
        people.add(new Person("Alisha", "", 2, false));
    }

    private void saveData() {
        saveFromBoxes();
        try {
            JSONArray arr = new JSONArray();
            for (Person p : people) {
                JSONObject o = new JSONObject();
                o.put("name", p.name);
                o.put("phone", p.phone);
                o.put("row", p.row);
                o.put("sent", p.sent);
                arr.put(o);
            }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(DATA_KEY, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private void updateBadge() {
        int sent = 0;
        for (Person p : people) if (p.sent) sent++;
        sentBadge.setText(sent + "/" + people.size() + " sent");
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
