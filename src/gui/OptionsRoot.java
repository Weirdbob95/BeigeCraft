package gui;

import static game.Settings.RENDER_DISTANCE;
import static game.Settings.SHOW_DEBUG_HUD;
import java.util.function.Supplier;
import util.vectors.Vec2d;
import world.World;

public class OptionsRoot extends GUIRoot {

    public OptionsRoot(GUIManager manager) {
        super(manager);

        GUIText title = new GUIText("Options");
        title.offset = new Vec2d(0, 250);
        title.scale = 3;

        Supplier<String> renderDistText = () -> "Render distance: " + RENDER_DISTANCE + "  (" + RENDER_DISTANCE * World.CHUNK_SIZE + " blocks)";
        GUIText renderDist = new GUIText(renderDistText.get());
        renderDist.offset = new Vec2d(-300, -25);
        renderDist.centered = false;

        GUIButton renderDistPlus = new GUIButton("+", () -> {
            RENDER_DISTANCE += 1;
            renderDist.setText(renderDistText.get());
        });
        renderDistPlus.offset = new Vec2d(250, -25);
        renderDistPlus.size = new Vec2d(40, 40);

        GUIButton renderDistMinus = new GUIButton("-", () -> {
            if (RENDER_DISTANCE > 1) {
                RENDER_DISTANCE -= 1;
                renderDist.setText(renderDistText.get());
            }
        });
        renderDistMinus.offset = new Vec2d(300, -25);
        renderDistMinus.size = new Vec2d(40, 40);

        Supplier<String> showDebugInfo = () -> "Show debug info: " + (SHOW_DEBUG_HUD ? "On" : "Off");
        GUIButton showPos = new GUIButton(showDebugInfo.get());
        showPos.onClick = () -> {
            SHOW_DEBUG_HUD = !SHOW_DEBUG_HUD;
            showPos.text.setText(showDebugInfo.get());
        };
        showPos.offset = new Vec2d(0, -150);
        showPos.size = new Vec2d(800, 100);

        GUIButton back = new GUIButton("Back", () -> {
            manager.setRoot(manager.menuRoot);
        });
        back.offset = new Vec2d(0, -275);
        back.size = new Vec2d(800, 100);

        add(title, renderDist, renderDistPlus, renderDistMinus, showPos, back);
    }
}
