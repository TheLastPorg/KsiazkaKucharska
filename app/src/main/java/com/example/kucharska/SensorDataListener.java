package com.example.kucharska;

public interface SensorDataListener {
    void OnResume();

    void OnPause();

    void onColorsChanged(int textColor, int backgroundColor);
    void onHintColorChanged(int hintColor);
}

