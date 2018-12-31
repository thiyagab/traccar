package org.traccar.api.resource;

import org.traccar.Context;
import org.traccar.api.BaseObjectResource;
import org.traccar.database.TrackDeviceManager;
import org.traccar.helper.LogAction;
import org.traccar.model.Device;
import org.traccar.model.TrackDevice;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Path("trackdevices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TrackDeviceResource extends BaseObjectResource<TrackDevice> {


    public TrackDeviceResource(Class<TrackDevice> baseClass) {
        super(baseClass);
    }

    public TrackDeviceResource() {
        super(TrackDevice.class);
    }


    @GET
    public Collection<TrackDevice> get(
            @QueryParam("all") boolean all, @QueryParam("userId") long userId,
            @QueryParam("uniqueId") List<String> uniqueIds,
            @QueryParam("id") List<Long> deviceIds) throws SQLException {
        TrackDeviceManager trackDeviceManager = Context.getTrackDeviceManager();
        Collection<TrackDevice> result = null;
        if (all) {
            if (Context.getPermissionsManager().getUserAdmin(getUserId())) {
                result = trackDeviceManager.getAllValues();
            }
        } else if (userId!=0) {
            result = trackDeviceManager.getUserTrackedDevices(userId);
        } else {
            if(uniqueIds!=null && !uniqueIds.isEmpty())
                result=trackDeviceManager.getItems(uniqueIds.stream().map(Long::valueOf).collect(Collectors.toSet()));
            else if(deviceIds!=null && !deviceIds.isEmpty()) {
                for (Long deviceId : deviceIds) {
                    Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
                }
                result = trackDeviceManager.getDeviceTrackers(deviceIds);
            }
        }
        return result;
    }


}
