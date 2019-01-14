package org.traccar.database;

import org.traccar.Context;
import org.traccar.model.TrackDevice;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TrackDeviceManager extends SimpleObjectManager<TrackDevice> implements ManagableObjects  {

    //Map to store trackdevice objects for given device, the default items map store the objectid->objects
    private Map<Long, Set<TrackDevice>> deviceTrackerMap;

    public TrackDeviceManager(DataManager dataManager) {
        super(dataManager,TrackDevice.class);
    }


    public Collection<TrackDevice> getUserTrackedDevices(long userId){
        return getItems(getUserItems(userId));
    }



    public Collection<TrackDevice> getDeviceTrackers(long deviceId){
        return getAllValues().parallelStream()
                .filter(trackDevice -> trackDevice.getDeviceId()==deviceId).collect(Collectors.toList());
    }

    public Collection<TrackDevice> getDeviceTrackers(List<Long> deviceIds){
        Set<TrackDevice> totalCollection= new HashSet<>();
        for (Long deviceId : deviceIds) {
            if(deviceTrackerMap.get(deviceId)!=null)
                totalCollection.addAll(deviceTrackerMap.get(deviceId));
        }

        return totalCollection;
    }


    @Override
    public void refreshItems() {
        if(deviceTrackerMap==null){
            deviceTrackerMap= new ConcurrentHashMap<>();
        }
        super.refreshItems();
    }

    protected void addNewItem(TrackDevice item) {
        super.addNewItem(item);
        Set<TrackDevice> devices=deviceTrackerMap.get(item.getDeviceId());
        if(devices==null){
            devices=new HashSet<>();
        }
        devices.add(item);
        deviceTrackerMap.put(item.getDeviceId(),devices);

    }



    protected void updateCachedItem(TrackDevice item) {
        addNewItem(item);
    }


    protected void removeCachedDeviceTrackers(long deviceId) {
        deviceTrackerMap.remove(deviceId);
    }

    public void removeDeviceTrackers(long deviceId) throws SQLException {
        //TODO remove from database or check if it will be removed by cascade if device is deletedi


    }

}
