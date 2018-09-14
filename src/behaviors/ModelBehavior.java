package behaviors;

import engine.Behavior;
import graphics.Model;
import util.math.Vec4d;

public class ModelBehavior extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);

    public Model model;
    public double rotation = 0;
    public double scale = 1 / 16.;
    public Vec4d color = new Vec4d(1, 1, 1, 1);

    @Override
    public void render() {
        model.render(position.position, rotation, 0, scale, model.size().mul(.5), color);
    }
}
