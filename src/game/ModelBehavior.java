package game;

import behaviors.PositionBehavior;
import engine.Behavior;
import graphics.Model;

public class ModelBehavior extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);

    public Model model;
    public double rotation = 0;
    public double scale = 1 / 16.;

    @Override
    public void render() {
        model.render(position.position, rotation, scale, model.size().mul(.5));
    }
}
