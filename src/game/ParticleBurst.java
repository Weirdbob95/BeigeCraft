package game;

import behaviors.AccelerationBehavior;
import behaviors.LifetimeBehavior;
import behaviors.PositionBehavior;
import behaviors.VelocityBehavior;
import engine.Behavior;
import graphics.Model;
import java.util.LinkedList;
import java.util.List;
import util.math.MathUtils;
import util.math.Quaternion;
import util.math.Vec3d;
import util.math.Vec4d;

public class ParticleBurst extends Behavior {

    private static final Model SINGLE_VOXEL = Model.load("singlevoxel.vox");

    public final PositionBehavior position = require(PositionBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final AccelerationBehavior acceleration = require(AccelerationBehavior.class);
    public final LifetimeBehavior lifetime = require(LifetimeBehavior.class);

    public Vec4d color = new Vec4d(1, 1, 1, 1);
    private double maxLifetime;
    private final List<Vec3d> particles = new LinkedList();

    @Override
    public void createInner() {
        acceleration.acceleration = new Vec3d(0, 0, -20);
    }

    @Override
    public void render() {
        for (Vec3d p : particles) {
            Vec3d pos = position.position.add(p.mul(maxLifetime - lifetime.lifetime));
            SINGLE_VOXEL.render(pos, Quaternion.IDENTITY, 1 / 16., SINGLE_VOXEL.center(), color);
        }
    }

    public void setLifetime(double maxLifetime) {
        this.maxLifetime = maxLifetime;
        lifetime.lifetime = maxLifetime;
    }

    public void spawn(int numParticles, double minParticleVelocity, double maxParticleVelocity) {
        for (int i = 0; i < numParticles; i++) {
            Vec3d dir = MathUtils.randomInSphere();
            double speed = MathUtils.lerp(minParticleVelocity, maxParticleVelocity, Math.random());
            particles.add(dir.mul(speed));
        }
    }
}
