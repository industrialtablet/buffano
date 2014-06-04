/**
 * @Title: Point.java
 * @Package com.mylayout.app.component
 * @Description: TODO
 * Copyright: Copyright (c) 2012 
 * Company:湖南潇峰云集科技有限公司
 * 
 * @author Comsys-ningh
 * @date 2013-7-8 下午3:58:14
 * @version V1.0
 */  	

    
package org.mortbay.ijetty.component;

	/**
 * Copyright (c) 2012,湖南潇峰云集科技有限公司
 * All rights reserved.
 *
 * 文件名称： Point.java
 * 文件标识：见配置管理计划书
 * 摘    要：简要描述本文件的内容
 *
 * 当前版本：1.1
 * 作    者：输入作者（或修改者）名字          ---> ningh
 * 完成日期：2013-7-8
 *
 * 取代版本：1.0
 * 原作者  ：输入原作者（或修改者）名字  ---> ningh
 * 完成日期：2013-7-8 
 */

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Point holds two integer coordinates
 */
public class Point implements Parcelable, Serializable {
    public int x;
    public int y;

    public Point() {}

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point src) {
        this.x = src.x;
        this.y = src.y;
    }

    /**
     * Set the point's x and y coordinates
     */
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Negate the point's coordinates
     */
    public final void negate() {
        x = -x;
        y = -y;
    }

    /**
     * Offset the point's coordinates by dx, dy
     */
    public final void offset(int dx, int dy) {
        x += dx;
        y += dy;
    }

    /**
     * Returns true if the point's coordinates equal (x,y)
     */
    public final boolean equals(int x, int y) {
        return this.x == x && this.y == y;
    }

    @Override public boolean equals(Object o) {
        if (o instanceof Point) {
            Point p = (Point) o;
            return this.x == p.x && this.y == p.y;
        }
        return false;
    }

    @Override public int hashCode() {
        return x * 32713 + y;
    }

    @Override public String toString() {
        return "Point(" + x + ", " + y+ ")";
    }

    /**
     * Parcelable interface methods
     */
//    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write this point to the specified parcel. To restore a point from
     * a parcel, use readFromParcel()
     * @param out The parcel to write the point's coordinates into
     */
//    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(x);
        out.writeInt(y);
    }

    public static final Parcelable.Creator<Point> CREATOR = new Parcelable.Creator<Point>() {
        /**
         * Return a new point from the data in the specified parcel.
         */
        public Point createFromParcel(Parcel in) {
            Point r = new Point();
            r.readFromParcel(in);
            return r;
        }

        /**
         * Return an array of rectangles of the specified size.
         */
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };

    /**
     * Set the point's coordinates from the data stored in the specified
     * parcel. To write a point to a parcel, call writeToParcel().
     *
     * @param in The parcel to read the point's coordinates from
     */
    public void readFromParcel(Parcel in) {
        x = in.readInt();
        y = in.readInt();
    }
}

