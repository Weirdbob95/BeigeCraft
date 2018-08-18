package game.spells.shapes;

import game.spells.SpellInfo;
import game.spells.TypeDefinitions.SpellShapeInitial;
import util.vectors.Vec3d;

public class S_Self extends SpellShapeInitial {

    @Override
    public void cast(SpellInfo info, Vec3d goal) {
        hit(info);
    }
}
