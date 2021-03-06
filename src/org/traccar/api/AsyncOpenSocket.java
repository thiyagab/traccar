/*
 * Copyright 2015 - 2016 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.database.ConnectionManager;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.model.Position;
import org.traccar.model.TrackDevice;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.*;

public class AsyncOpenSocket extends WebSocketAdapter implements ConnectionManager.UpdateListener  {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncOpenSocket.class);


    TrackDevice trackdevice;
    private static final String KEY_POSITIONS = "positions";


    public AsyncOpenSocket(long trackerid) throws SQLException {
        trackdevice=Context.getDataManager().getObject(TrackDevice.class,trackerid);
        if(trackdevice==null){
            //TODO heights of laziness and Worst to throw sqlexception
            throw new SQLDataException("Object not found");
        }
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        if(validateAndSendPosition(
                Context.getDeviceManager().getLastPosition(trackdevice.getDeviceId()))) {
            Context.getConnectionManager().addTimedListener(trackdevice.getId(), this);
        }
        sendDummyDataPeriodically();
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        Context.getConnectionManager().removeTimedListener(trackdevice.getId(), this);
    }

    @Override
    public void onUpdateDevice(Device device) {
       //DO Nothing
    }

    @Override
    public void onUpdatePosition(Position position) {
        if(position.getDeviceId()==trackdevice.getDeviceId()) {
            if(!validateAndSendPosition(position)){

                Context.getConnectionManager().removeTimedListener(trackdevice.getId(), this);
            }
        }
    }

    private boolean validateAndSendPosition(Position position) {
        Map<String, Collection<?>> data = new HashMap<>();
        boolean inTimeWindow=validateTimeWindow();
        if (inTimeWindow) {
            data.put(KEY_POSITIONS, Collections.singletonList(position));
        }else{
            data.put("error",Collections.singletonList("Expired"));
//                Context.getConnectionManager().removeListener(trackdevice.getId(), this);
        }
        sendData(data);
        return inTimeWindow;
    }

    private boolean validateTimeWindow() {
        Date now = new Date();
        return now.after(trackdevice.getStartTime())
                && now.before(trackdevice.getEndTime());
    }

    @Override
    public void onUpdateEvent(Event event) {
        //Do Nothing
    }

    private void sendData(Map<String, Collection<?>> data) {
        if (!data.isEmpty() && isConnected()) {
            try {
                getRemote().sendString(Context.getObjectMapper().writeValueAsString(data), null);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Socket JSON formatting error", e);
            }
        }
    }


    static double latitude=12.97;
    static double longitude=80.21;

    public void sendDummyDataPeriodically(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                dummyData();
            }
        },5000,5000);
    }

    public void dummyData(){
        Position position = new Position();
        position.setLatitude(latitude*=1.00001);
        position.setLongitude(longitude);
        Map<String, Collection<?>> data = new HashMap<>();
        data.put(KEY_POSITIONS, Collections.singletonList(position));
        sendData(data);
    }
}
