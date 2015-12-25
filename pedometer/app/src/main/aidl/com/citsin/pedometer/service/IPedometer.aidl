// IPedometer.aidl
package com.citsin.pedometer.service;

import com.citsin.pedometer.service.IPedometerCallback;

interface IPedometer {
    void registerCallback(IPedometerCallback cb);

     /**
      * Remove a previously registered callback interface.
      */
    void unregisterCallback(IPedometerCallback cb);

    void start();

    void stop();

    void pause();

    int getRunningType();
}
