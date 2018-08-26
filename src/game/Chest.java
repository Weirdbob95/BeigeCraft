package game;

import behaviors.ModelBehavior;
import engine.Behavior;
import graphics.Model;

public class Chest extends Behavior {

    public final ModelBehavior model = require(ModelBehavior.class);

    @Override
    public void createInner() {
        model.model = Model.load("chest.vox");
    }
}
