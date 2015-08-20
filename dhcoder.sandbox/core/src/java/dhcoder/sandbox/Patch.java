package dhcoder.sandbox;

import com.badlogic.gdx.physics.box2d.*;

import static com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public final class Patch {


    private final Body myBody;

    public Patch(World world, float x, float y, float hW, float hH) {
        PolygonShape box = new PolygonShape();
        box.setAsBox(hW, hH);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 1f;
        fixtureDef.filter.categoryBits = Layers.PATCH;
        fixtureDef.filter.maskBits = Layers.PLAYER;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        bodyDef.position.set(x, y);
        myBody = world.createBody(bodyDef);
        myBody.createFixture(fixtureDef);
        box.dispose();
    }

    public Body getBody() {
        return myBody;
    }
}
