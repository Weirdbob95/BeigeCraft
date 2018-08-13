package game.gui;

import engine.Core;
import util.vectors.Vec2d;

public class MenuRoot extends GUIRoot {

    public MenuRoot(GUIManager manager) {
        super(manager);

        GUIText title = new GUIText("Menu");
        title.offset = new Vec2d(0, 250);
        title.scale = 3;

        GUIButton resume = new GUIButton("Resume", () -> {
            manager.setRoot(null);
        });
        resume.offset = new Vec2d(0, 100);
        resume.size = new Vec2d(800, 100);

        GUIButton quests = new GUIButton("Quests", () -> {
            // TODO
        });
        quests.offset = new Vec2d(0, -25);
        quests.size = new Vec2d(800, 100);

        GUIButton options = new GUIButton("Options", () -> {
            manager.setRoot(manager.optionsRoot);
        });
        options.offset = new Vec2d(0, -150);
        options.size = new Vec2d(800, 100);

        GUIButton quit = new GUIButton("Exit game", () -> {
            Core.stopGame();
        });
        quit.offset = new Vec2d(0, -275);
        quit.size = new Vec2d(800, 100);

        add(title, resume, quests, options, quit);
    }
}
