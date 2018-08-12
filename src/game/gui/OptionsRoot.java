package game.gui;

import java.util.function.Supplier;
import util.vectors.Vec2d;
import world.World;

public class OptionsRoot extends GUIRoot {

    public OptionsRoot(GUIManager manager) {
        super(manager);

        GUIText title = new GUIText("Options");
        title.offset = new Vec2d(0, 250);
        title.scale = 3;

        Supplier<String> renderDistText = () -> "Render distance: " + World.RENDER_DISTANCE + "  (" + World.RENDER_DISTANCE * World.CHUNK_SIZE + " blocks)";
        GUIText renderDist = new GUIText(renderDistText.get());
        renderDist.offset = new Vec2d(-300, 0);
        renderDist.centered = false;

        GUIButton renderDistPlus = new GUIButton("+", () -> {
            World.RENDER_DISTANCE += 1;
            renderDist.setText(renderDistText.get());
        });
        renderDistPlus.offset = new Vec2d(250, 0);
        renderDistPlus.size = new Vec2d(40, 40);

        GUIButton renderDistMinus = new GUIButton("-", () -> {
            if (World.RENDER_DISTANCE > 1) {
                World.RENDER_DISTANCE -= 1;
                renderDist.setText(renderDistText.get());
            }
        });
        renderDistMinus.offset = new Vec2d(300, 0);
        renderDistMinus.size = new Vec2d(40, 40);

        GUIButton back = new GUIButton("Back", () -> {
            manager.setRoot(manager.menuRoot);
        });
        back.offset = new Vec2d(0, -275);
        back.size = new Vec2d(800, 100);

        add(title, renderDist, renderDistPlus, renderDistMinus, back);
    }
}
