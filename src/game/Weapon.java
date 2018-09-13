package game;

import graphics.Model;
import util.vectors.Vec3d;

public class Weapon {

    public static final Weapon SWORD = new Weapon();
    public static final Weapon DAGGER = new Weapon();
    public static final Weapon FIST = new Weapon();
    public static final Weapon HAMMER = new Weapon();
    public static final Weapon SPEAR = new Weapon();

    public Model model;
    public Vec3d modelTip;
    public double ext1, ext2;
    public double slashDuration;
    public double weight;
    public double slashiness;

    static {
        // Sword
        SWORD.model = Model.load("sword.vox");
        SWORD.modelTip = new Vec3d(16, 16, 32);
        SWORD.ext1 = 2.5;
        SWORD.ext2 = 5;
        SWORD.slashDuration = .2;
        SWORD.weight = .5;
        SWORD.slashiness = 2;
        // Dagger
        DAGGER.model = Model.load("dagger.vox");
        DAGGER.modelTip = new Vec3d(4, 4, 16);
        DAGGER.ext1 = 1.5;
        DAGGER.ext2 = 3;
        DAGGER.slashDuration = .15;
        DAGGER.weight = .25;
        DAGGER.slashiness = 1;
        // Fist
        FIST.model = Model.load("fist.vox");
        FIST.modelTip = new Vec3d(-8, 2, 4);
        FIST.ext1 = 1;
        FIST.ext2 = 2.5;
        FIST.slashDuration = .15;
        FIST.weight = 2;
        FIST.slashiness = .2;
        // Hammer
        HAMMER.model = Model.load("hammer.vox");
        HAMMER.modelTip = new Vec3d(8, 8, 32);
        HAMMER.ext1 = 1.5;
        HAMMER.ext2 = 3;
        HAMMER.slashDuration = .8;
        HAMMER.weight = 2.5;
        HAMMER.slashiness = 5;
        // Spear
        SPEAR.model = Model.load("spear.vox");
        SPEAR.modelTip = new Vec3d(4, 4, 64);
        SPEAR.ext1 = 3.5;
        SPEAR.ext2 = 6;
        SPEAR.slashDuration = .25;
        SPEAR.weight = 1;
        SPEAR.slashiness = .1;
    }
}
