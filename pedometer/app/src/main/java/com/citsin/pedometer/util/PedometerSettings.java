/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.citsin.pedometer.util;

import android.content.SharedPreferences;


public class PedometerSettings {

    SharedPreferences mSettings;

    public PedometerSettings(SharedPreferences settings) {
        mSettings = settings;
    }


    public float getStepLength() {
        try {
            return Float.valueOf(mSettings.getString("step_length", "20").trim());
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public float getBodyWeight() {
        try {
            return Float.valueOf(mSettings.getString("body_weight", "50").trim());
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public boolean isRunning() {
        return mSettings.getString("exercise_type", "running").equals("running");
    }


}
