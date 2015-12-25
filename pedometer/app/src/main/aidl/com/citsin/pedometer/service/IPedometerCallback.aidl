// IPedometerCallback.aidl
package com.citsin.pedometer.service;


// Declare any non-default types here with import statements

interface IPedometerCallback {

    void timeChanged(long time);

    void valueChanged(int step,long time,float distance,float calories,float speed);
}
