package org.traccar.model;

import java.util.Date;

public class TrackDevice extends ExtendedModel {

    private long deviceId;
    private Date startTime;
    private Date endTime;
//    private long userId;


    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

//    public void setUserId(long userId) {
//        this.userId = userId;
//    }
//
//    public long getUserId() {
//        return userId;
//    }
}
