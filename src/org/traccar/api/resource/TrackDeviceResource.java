package org.traccar.api.resource;

import org.traccar.Context;
import org.traccar.api.BaseObjectResource;
import org.traccar.helper.LogAction;
import org.traccar.model.Device;
import org.traccar.model.ManagedUser;
import org.traccar.model.TrackDevice;
import org.traccar.model.User;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Date;

@Path("trackdevice")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TrackDeviceResource extends BaseObjectResource<TrackDevice> {


    public TrackDeviceResource(Class<TrackDevice> baseClass) {
        super(baseClass);
    }

    @Override
    @PermitAll
    @POST
    public Response add(TrackDevice entity) throws SQLException {

        LogAction.create(getUserId(), entity);
        if (Context.getPermissionsManager().getUserManager(getUserId())) {

            Context.getDataManager().addObject(entity);

        }
        Context.getUsersManager().refreshUserItems();
        return Response.ok(entity).build();
    }
}
