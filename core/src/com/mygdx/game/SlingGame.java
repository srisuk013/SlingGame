package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public  class SlingGame extends ScreenAdapter implements InputProcessor {
	private OrthographicCamera camera;
    private Viewport viewport;
	private World world;
	private Box2DDebugRenderer b2dr;
    private ShapeRenderer renderer;

	private static final float PPM = 32;
	
	private Vector2 anchor;
    private Vector2 firingPosition;
    private float distance, angle;
	
    public SlingGame() {
		camera = new OrthographicCamera();
//		camera.setToOrtho(false, 800/PPM, 480/PPM);
        viewport = new ExtendViewport(800/PPM, 480/PPM, camera);
		b2dr = new Box2DDebugRenderer();
        renderer = new ShapeRenderer();

		anchor = new Vector2(100, 120);
		firingPosition = anchor.cpy();	        

        // 1. Create World.
		world = new World(new Vector2(0, -9.81f), true);
	}
    
	@Override
	public void show() {
        Gdx.input.setInputProcessor(this);

        createGround();
        createBox(620, 60, 5, 30);
        createBox(650, 85, 40, 5);
        createBox(680, 60, 5, 30);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		renderer.setProjectionMatrix(camera.combined);
		renderer.setColor(Color.ORANGE);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
			renderer.circle(anchor.x/PPM, anchor.y/PPM, 5/PPM);
			renderer.setColor(Color.WHITE);
			renderer.circle(firingPosition.x/PPM, firingPosition.y/PPM, 5/PPM);
			renderer.line(firingPosition,anchor);
		renderer.end();

		b2dr.render(world, camera.combined);
		camera.update();
		
		world.step(1/60f, 8, 3);
	}

    public void createBall(){
        Body body;
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(new Vector2(anchor.x/PPM, anchor.y/PPM));
        body = world.createBody(bdef);

        CircleShape shape = new CircleShape();
        shape.setRadius(10/PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1;

        body.createFixture(fdef);

        shape.dispose();

        float velX = 30 * MathUtils.cos(angle) * distance/PPM;
        float velY = 30 * MathUtils.sin(angle) * distance/PPM;

        body.setLinearVelocity(velX, velY);

//        deadBodies.add(body);
    }

    public void createGround(){
        Body body;
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(new Vector2(400/PPM, 10/PPM));
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(500/PPM, 10/PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1;

        body.createFixture(fdef);
        shape.dispose();
    }

    public void createBox(float x, float y, float w, float h){
        Body body;
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(new Vector2(x/PPM, y/PPM));
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w/PPM, h/PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1;

        body.createFixture(fdef);
        shape.dispose();

//        deadBodies.add(body);
    }

    private float angleBetweenTwoPoints(){
        float angle = MathUtils.atan2(anchor.y - firingPosition.y, anchor.x - firingPosition.x);
        angle %= MathUtils.PI2;
        if(angle < 0) {
            angle += MathUtils.PI2;
        }

        return angle;
    }

    private float distanceBetweenTwoPoints(){
        return (float)Math.sqrt((anchor.x-firingPosition.x)*(anchor.x-firingPosition.x) + (anchor.y-firingPosition.y)*(anchor.y-firingPosition.y));
    }

    private void calculateAngleAndDistanceForBall(int screenX, int screenY){
        firingPosition.set(screenX, screenY);
        viewport.unproject(firingPosition);

        distance = distanceBetweenTwoPoints();
        angle = angleBetweenTwoPoints();

        if(distance > 100) {
            distance = 100;
        }
        firingPosition.set(anchor.x + distance*-MathUtils.cos(angle), (anchor.y + distance*-MathUtils.sin(angle)));
    }
    
	public boolean keyDown(int keycode) {
		return false;
	}

	public boolean keyUp(int keycode) {
		return false;
	}

	public boolean keyTyped(char character) {
		return false;
	}

	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        calculateAngleAndDistanceForBall(screenX, screenY);
        return true;
	}

	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		createBall();
        firingPosition.set(anchor.cpy());
        return true;
	}

	public boolean touchDragged(int screenX, int screenY, int pointer) {
        calculateAngleAndDistanceForBall(screenX, screenY);
        return true;
	}

	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    public boolean scrolled(int amount) {
		return false;
	}
	
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void dispose() {
        renderer.dispose();
    }

}