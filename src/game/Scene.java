package game;

import object.*;

import java.util.ArrayList;

public class Scene {

    public ArrayList<Model> models = new ArrayList<>();
    public ArrayList<Light> lights = new ArrayList<>();
    public Sky sky;
    public Terrain terrain;

    public float fogDensity;
    public float fogGradient;



}
