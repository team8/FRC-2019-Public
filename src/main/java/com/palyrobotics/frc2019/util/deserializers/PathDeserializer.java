package com.palyrobotics.frc2019.util.deserializers;

import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.Deserializers;
import org.codehaus.jackson.map.deser.StdDeserializer;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.impl.TypeDeserializerBase;
import org.codehaus.jackson.type.JavaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PathDeserializer extends StdDeserializer<Path> {

    public PathDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Path deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        //pathList.add(new Path.Waypoint(new Translation2d(0,0),0));
        JSONArray pathArray = new JSONArray();

        JsonToken temp = jp.nextValue();
        String jsonString = "";
        int waypointNum = 0;
        while(temp != null){
            if(jp.getCurrentName() != null){
                if(jp.getCurrentName().contains("Waypoint")){
                    String[] split = jp.getCurrentName().split(" ");
                    System.out.println(split[0] + split[1]);
                    waypointNum = Integer.valueOf(split[1]);
                }

            }
            jsonString += (jp.getText()+ "#" + jp.getCurrentName() + "#");

            temp = jp.nextValue();
        }
        List<Path.Waypoint> pathList = new ArrayList<>(3);

        for(var i = 0;i < waypointNum + 1;i++){
            pathList.add(i, new Path.Waypoint(new Translation2d(0,0),0));
        }
       // pathList.set(0, new Path.Waypoint(new Translation2d(50,0),0));
        System.out.println(jsonString);
        String[] jsonSplit = jsonString.split("#");
        int currentWaypoint = 0;
        for(var i = 0;i < jsonSplit.length;i++){
            if(jsonSplit[i].contains("Waypoint")){
                currentWaypoint = Integer.parseInt(jsonSplit[i].split(" ")[1]);

            }
            //setting x to the target and keeping other values;
            if(jsonSplit[i].equals("x")){
                double targetX = Double.parseDouble(jsonSplit[i-1]);
               pathList.set(currentWaypoint, new Path.Waypoint(new Translation2d(targetX, pathList.get(currentWaypoint).position.getY()), pathList.get(currentWaypoint).speed));
            }
            //same for y
            if(jsonSplit[i].equals("y")){
                double targetY = Double.parseDouble(jsonSplit[i-1]);
                pathList.set(currentWaypoint, new Path.Waypoint(new Translation2d(pathList.get(currentWaypoint).position.getX(), targetY), pathList.get(currentWaypoint).speed));
            }
            //same for speed
            if(jsonSplit[i].equals("speed")){
                double targetSpeed = Double.parseDouble(jsonSplit[i-1]);
                pathList.set(currentWaypoint, new Path.Waypoint(new Translation2d(pathList.get(currentWaypoint).position.getX(), pathList.get(currentWaypoint).position.getY()), targetSpeed));
            }
        }

//        for(var i = 0;i < jp.getTextLength();i++){
//            jp.nextValue();
//
//
//
//            //System.out.println(jp.getTextCharacters());
//        }
//        jp.nextValue();
//        System.out.println(jp.getTextCharacters());
//        jp.nextValue();
//        System.out.println(jp.getTextCharacters());
//        jp.nextValue();
//        System.out.println(jp.getTextCharacters());
//        jp.nextValue();
//        System.out.println(jp.getTextCharacters());
//        jp.nextValue();
//        System.out.println(jp.getTextCharacters());
        //JsonToken temp = ctxt.getParser().nextValue();
//        while(temp != null){
//            System.out.println(jp.getTextCharacters());
//            temp = ctxt.getParser().nextValue();
//
//
//
//        }
        return new Path(pathList);
    }
}
