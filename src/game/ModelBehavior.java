package game;

import behaviors.PositionBehavior;
import engine.Behavior;
import graphics.Model;
import opengl.ShaderProgram;
import util.Resources;

public class ModelBehavior extends Behavior {

    public static final ShaderProgram MODEL_SHADER = Resources.loadShaderProgram("model");

    public final PositionBehavior position = require(PositionBehavior.class);

    public Model model;
    public double rotation = 0;
    public double scale = 1 / 16.;

    @Override
    public void render() {
        model.render(position.position, rotation, scale, model.size().mul(.5));
    }
}
