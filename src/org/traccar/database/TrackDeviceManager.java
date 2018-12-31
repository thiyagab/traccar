package org.traccar.database;

import org.traccar.Context;
import org.traccar.model.TrackDevice;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class TrackDeviceManager extends BaseObjectManager<TrackDevice> implements ManagableObjects  {

    private Map<Long, Set<TrackDevice>> deviceTrackerMap= new HashMap<>();

    public TrackDeviceManager(DataManager dataManager) {
        super(dataManager,TrackDevice.class);
    }

    @Override
    public Set<Long> getUserItems(long userId) {
        if (Context.getPermissionsManager() != null) {
            Set<Long> result = new HashSet<>();
            for (long deviceId : Context.getPermissionsManager().getDevicePermissions(userId)) {
                getAllValues().parallelStream()
                        .filter(trackDevice -> trackDevice.getDeviceId()==deviceId)
                        .forEach(trackDevice -> result.add(trackDevice.getId()));
            }
            return result;
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public Set<Long> getManagedItems(long userId) {
        Set<Long> result = new HashSet<>();
        result.addAll(getUserItems(userId));
        for (long managedUserId : Context.getUsersManager().getUserItems(userId)) {
            result.addAll(getUserItems(managedUserId));
        }
        return result;
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
        //TODO remove from database
        removeDeviceTrackers(deviceId);

    }

}
