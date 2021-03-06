package com.lichao.jbox.widget;

import android.util.Log;
import android.view.View;

import com.lichao.jbox.R;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import java.util.Random;

/**
 * 物体模拟碰撞类
 * Created by Administrator on 2017-11-13.
 */

public class JboxImpl {
    private static final String TAG = JboxImpl.class.getSimpleName();

    private World mWorld; //模拟世界
    private float dt = 1f/60f; //模拟世界的频率
    private int mVelocityIterations = 5; //速率迭代器
    private int mPositionIterations = 20; //迭代次数

    private int mWidth,mHeight;

    private float mDensity = 0.5f;//物理事件密度
    private float mRatio = 50;//坐标映射比例
    private final Random mRandom = new Random();

    public JboxImpl(float density) {
        mDensity = density;
    }

    /**
     * 设置大小
     * @param width
     * @param height
     */
    public void setWorldSize(int width, int height) {
        mHeight = height;
        mWidth = width;
    }

    public void startWorld() {
        if (mWorld != null) {
            mWorld.step(dt, mVelocityIterations, mPositionIterations);
        }
    }

    /**
     * 创建模拟事件
     */
    public void createWorld() {
        if (mWorld == null) {
            mWorld = new World(new Vec2(0, 10.0f));//Vec2 二维向量,设置起始点
            updateVerticalBounds();
            updateHorizontalBounds();
        }
    }

    /**
     * 创建钢体
     * @param view
     */
    public void createBody(View view) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.setType(BodyType.DYNAMIC);

        bodyDef.position.set(switchPositionToBody(view.getX() + (view.getWidth() / 2)),
                switchPositionToBody(view.getY() + (view.getHeight() / 2)));

        Shape shape = null;
        Boolean isCircle = (Boolean) view.getTag(R.id.lc_view_circle_tag);
        if (isCircle != null && isCircle) {
            shape = createCircleShape(switchPositionToBody(view.getWidth() / 2));
        } else {
            Log.i(TAG,"createBody view tag is not circle!!!");
            return;
        }

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.setShape(shape);
        fixtureDef.friction = 0.8f;//摩擦系数
        fixtureDef.density = mDensity;
        fixtureDef.restitution = 0.5f;//补偿系数

        Body body = mWorld.createBody(bodyDef);
        body.createFixture(fixtureDef);
        view.setTag(R.id.lc_view_body_tag, body);

        body.setLinearVelocity(new Vec2(mRandom.nextFloat(), mRandom.nextFloat()));
    }

    /**
     * 创建圆形
     * @param radius
     * @return
     */
    private Shape createCircleShape(float radius) {
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(radius);
        return circleShape;
    }

    /**
     * 碰撞垂直边界
     */
    private void updateVerticalBounds() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.STATIC; //定义静止的刚体

        PolygonShape box = new PolygonShape(); //定义的形状
        float boxWidth = switchPositionToBody(mWidth);
        float boxHeight = switchPositionToBody(mRatio);
        box.setAsBox(boxWidth, boxHeight); //确定为矩形

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = mDensity;
        fixtureDef.friction = 0.8f;//摩擦系数
        fixtureDef.restitution = 0.5f; //补偿系数

        bodyDef.position.set(0, -boxHeight);
        Body topBody = mWorld.createBody(bodyDef); //创建一个真实的上边 body
        topBody.createFixture(fixtureDef);

        bodyDef.position.set(0, switchPositionToBody(mHeight) + boxHeight);
        Body bottomBody = mWorld.createBody(bodyDef);
        bottomBody.createFixture(fixtureDef);
    }

    /**
     * 碰撞水平边界
     */
    private void updateHorizontalBounds() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.STATIC; //定义静止的刚体

        PolygonShape box = new PolygonShape(); //定义的形状
        float boxWidth = switchPositionToBody(mRatio);
        float boxHeight = switchPositionToBody(mHeight);
        box.setAsBox(boxWidth, boxHeight); //确定为矩形

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = mDensity;
        fixtureDef.friction = 0.8f;//摩擦系数
        fixtureDef.restitution = 0.5f; //补偿系数

        bodyDef.position.set(-boxWidth, 0);
        Body leftBody = mWorld.createBody(bodyDef); //创建一个真实的上边 body
        leftBody.createFixture(fixtureDef);

        bodyDef.position.set(switchPositionToBody(mWidth) + boxWidth, 0);
        Body rightBody = mWorld.createBody(bodyDef);
        rightBody.createFixture(fixtureDef);
    }

    /**
     * view坐标映射为物理的坐标
     * @param viewPosition
     * @return
     */
    private float switchPositionToBody(float viewPosition) {
        return viewPosition / mRatio;
    }

    /**
     * 物理的坐标映射为iew坐标映射
     * @param bodyPosition
     * @return
     */
    private float switchPositionToView(float bodyPosition) {
        return bodyPosition * mRatio;
    }

    public boolean isBodyView(View view) {
        Body body = (Body) view.getTag(R.id.lc_view_body_tag);
        return body != null;
    }

    public float getViewX(View view) {
        Body body = (Body) view.getTag(R.id.lc_view_body_tag);
        if (body != null) {
            return switchPositionToView(body.getPosition().x ) - (view.getWidth() / 2);
        }
        return 0;
    }

    public float getViewY(View view) {
        Body body = (Body) view.getTag(R.id.lc_view_body_tag);
        if (body != null) {
            return switchPositionToView(body.getPosition().y ) - (view.getHeight() / 2);
        }
        return 0;
    }

    public float getViewRotaion(View view) {
        Body body = (Body) view.getTag(R.id.lc_view_body_tag);
        if (body != null) {
            float angle = body.getAngle();
            return  (angle / 3.14f * 180f) % 360;
        }
        return 0;
    }

    public void applyLinearImpulse(float x, float y, View view) {
        Body body = (Body) view.getTag(R.id.lc_view_body_tag);
        Vec2 impluse = new Vec2(x,y);
        body.applyLinearImpulse(impluse, body.getPosition(), true); //给body做线性运动 true 运动完之后停止
    }

}
