package dhcoder.sandbox;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import dhcoder.libgdx.physics.PhysicsConstants;
import dhcoder.support.annotations.NotNull;
import dhcoder.support.math.Angle;

import static com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public final class Player {


    private final Angle myHeading = Angle.fromDegrees(0f);
    private final Body myBody;

    public Player(World world, float radius) {

        CircleShape circle = new CircleShape();
        circle.setRadius(radius);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1f;
        fixtureDef.filter.categoryBits = Layers.PLAYER;
        fixtureDef.filter.maskBits = Layers.BULLET | Layers.WALL | Layers.PATCH;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        myBody = world.createBody(bodyDef);
        myBody.createFixture(fixtureDef);
        circle.dispose();
    }

    @NotNull
    public Vector2 getPosition() {
        return myBody.getWorldCenter();
    }

    @NotNull
    public Angle getHeading() {
        myHeading.setRadians(myBody.getAngle());
        return myHeading;
    }

    public void setHeading(@NotNull Angle heading) {
        myHeading.set(heading);
        myBody.setTransform(myBody.getPosition(), heading.getRadians());
    }

    public void setVelocity(@NotNull Vector2 force) {
        if (force.isZero()) {
            myBody.setLinearDamping(PhysicsConstants.DAMPING_FAST_STOP);
            return;
        }

        myBody.setLinearDamping(0f);
        myBody.setLinearVelocity(force);
    }

    public void reset() {
        myBody.setTransform(Vector2.Zero, 0f);
    }
//
//    public void render(@NotNull ShapeRenderer shapeRenderer) {
//        float triangleSize = myRadius / 4f;
//        float halfTriangleSize = triangleSize / 2f;
//        Vector2 pos = getPosition();
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        shapeRenderer.translate(pos.x, pos.y, 0f);
//        shapeRenderer.rotate(0f, 0f, 1f, getHeading().getDegrees());
//        shapeRenderer.setColor(Color.BLUE);
//        shapeRenderer.circle(0, 0, myRadius);
//        shapeRenderer.setColor(Color.RED);
//        shapeRenderer.triangle(
//            myRadius - halfTriangleSize, triangleSize,
//            myRadius, 0,
//            myRadius - halfTriangleSize, -triangleSize);
//        shapeRenderer.identity();
//        shapeRenderer.end();
//    }
}
