package org.mortbay.ijetty.component;

import org.mortbay.ijetty.AppConstants;
import org.mortbay.ijetty.util.LogUtil;

import android.graphics.Rect;

public class Polygon {
	public static final double SIN_30 = Math.sin(Math.PI / 6);
	public static final double SIN_60 = Math.sin(Math.PI / 3);
	
	private Point[] points = new Point[6];
	private double a = 0.0D;
	private int w, h;

	public Point[] getPoints() {
		return points;
	}

	public Polygon() {
		this.a = AppConstants.a;
		this.w = AppConstants.w;
		this.h = AppConstants.h;
		for (int i = 0; i < points.length; i++)
			points[i] = new Point(0, 0);

		points[0].x = (int) (a * SIN_30);
		points[1].x = 0;
		points[2].x = (int) (a * SIN_30);
		points[3].x = (int) (a + a * SIN_30);
		points[4].x = (int) (a + 2 * a * SIN_30);
		points[5].x = (int) (a + a * SIN_30);

		points[0].y = 0;
		points[1].y = (int) (a * SIN_60);
		points[2].y = (int) (2 * a * SIN_60);
		points[3].y = (int) (2 * a * SIN_60);
		points[4].y = (int) (a * SIN_60);
		points[5].y = 0;
	}

	/**
	 * 获得Logo图片矩形区域
	 * 
	 * @return 矩形区域
	 */

	public Rect getLogoRect() {
		return new Rect((int) (a * SIN_30 / 2), (int) (a * SIN_60 / 2),
				(int) ((a * SIN_30 / 2) + (((double) w) / 2)), (int) ((a
						* SIN_60 / 2) + (((double) h) / 2)));
	}

	/**
	 * 通过宽高获得边长
	 * 
	 * @param w
	 *            宽度
	 * @param h
	 *            高度
	 * @return 边长
	 */
	public static double getPreferredSideLen(int w, int h) {
		double width = (double) w;
		double height = (double) h;
		return Math.min(width / (1 + 2 * SIN_30), height / (2 * SIN_60));

	}

	/**
	 * 通过宽度获得边长
	 * 
	 * @param w
	 *            宽度
	 * @return 边长
	 */
	public static double getPreferredSideLen(int w) {
		double width = (double) w;
		return (width / (1 + 2 * SIN_30));

	}

	/**
	 * 通过高度获得宽度
	 * 
	 * @param h
	 *            高度
	 * @return 宽度
	 */
	public static int getPreferredWidth(int h) {
		double height = (double) h;
		double width = (height * (1 + 2 * SIN_30)) / (2 * SIN_60);
		return (int) width;
	}

	/**
	 * 通过宽度获得高度
	 * 
	 * @param w
	 *            宽度
	 * @return 高度
	 */
	public static int getPreferredHeight(int w) {
		double width = (double) w;
		double height = (2 * width * SIN_60) / ((1 + 2 * SIN_30));
		return (int) height;
	}

	/**
	 * 判断某个点是否在三角形区域内
	 * 
	 * @param a
	 *            三角形一点
	 * @param b
	 *            三角形一点
	 * @param c
	 *            三角形一点
	 * @param p
	 *            待判断的点
	 * @return 点是否在三角形内
	 */
	public static boolean checkPointInTriangle(Point a, Point b, Point c,
			Point p) {
		double abc = triangleArea(a, b, c);
		double abp = triangleArea(a, b, p);
		double acp = triangleArea(a, c, p);
		double bcp = triangleArea(b, c, p);
		Boolean retFlag = false;
		if (abc == abp + acp + bcp) {

			retFlag = true;
		} else {
			retFlag = false;
		}
		return retFlag;
	}

	/**
	 * 求三角形的面积
	 * 
	 * @param a
	 *            三角形一点
	 * @param b
	 *            三角形一点
	 * @param c
	 *            三角形一点
	 * @return 三角形面积
	 */
	private static double triangleArea(Point a, Point b, Point c) {
		double result = Math.abs((a.x * b.y + b.x * c.y + c.x * a.y - b.x * a.y
				- c.x * b.y - a.x * c.y) / 2.0D);
		return result;
	}

	/**
	 * 判断点是否在矩形区域内
	 * 
	 * @param a
	 *            矩形左上角坐标
	 * @param b
	 *            矩形右下角坐标
	 * @param p
	 *            待判断的点
	 * @return 点是否在矩形内
	 */
	private static boolean checkPointInRectangle(Point a, Point b, Point p) {
		int x = p.x;
		int y = p.y;
		Boolean retFlag = false;
		if (x >= a.x && x <= b.x) {
			if (y >= a.y && y <= b.y)
				retFlag = true;
		}
		return retFlag;
	}

	/**
	 * 判断点是否在当前对象所在六边形内
	 * 
	 * @param p
	 *            待判断的点
	 * @return 点是否在六边形内
	 */
	public boolean checkPointInHexagon(Point p) {
		int x = p.x, y = p.y;

		if (x < 0 || x > w)
			return false;
		if (y < 0 || y > h)
			return false;

		if (x <= points[0].x)
			return checkPointInTriangle(points[0], points[1], points[2], p);
		else if (x <= points[3].x)
			return checkPointInRectangle(points[0], points[3], p);
		else if (x <= points[4].x)
			return checkPointInTriangle(points[3], points[4], points[5], p);
		return false;
	}

	public int getW() {
		return w;
	}

	public int getH() {
		return h;
	}

	/**
	 * 获得六边形左上角的点坐标
	 * 
	 * @param w
	 *            宽度
	 * @param h
	 *            高度
	 * @param a
	 *            边长
	 * @param row
	 *            行
	 * @param col
	 *            列
	 * @return 六边形左上角点的坐标
	 */
	public static Point getLocationPoint(int w, int h, double a, int row,
			int col) {
		Point p = new Point();
		int x = 0, y = 0;
		boolean vFlag = false;
		vFlag = (col>0);
		if (vFlag) {
			if ((col % 2) == 0) {
				x = (int) ((w + a) * (col / 2));
				y = row * h - h / 2;
			} else {
				x = (int) (((w + a) * (col / 2) + (a * SIN_30 + a)));
				y = (2 * row + 1) * h / 2 - h / 2;
			}
		}else{
			col = Math.abs(col);
			if ((col % 2) == 0) {
				x = (int) ((w + a) * (col / 2));
				y = row * h - h / 2;
			} else {
				x = (int) (((w + a) * (col / 2) + (a * SIN_30 + a)));
				y = (2 * row + 1) * h / 2 - h / 2;
			}
			x=-x; 
		}
		x-=(w+a);
		p.x = x;
		p.y = y;
		return p;
	}

	/**
	 * 通过所在行列获得ID
	 * 
	 * @param row
	 *            行
	 * @param col
	 *            列
	 * @return 唯一ID
	 */
	public static int getIDByRowCol(int row, int col) {
		return (1000000000 + 100000 * col + row);
	}
	/**
	 * 通过高度初始化六边形的长，宽，边长
	 * @param height 指定的高度
	 */
	
	public static void initSize(int height) {
		
		AppConstants.w = Polygon.getPreferredWidth(height);
		AppConstants.a = Polygon.getPreferredSideLen(AppConstants.w);
		AppConstants.h = height;
		LogUtil.log("application initSize " + AppConstants.a + "  "
				+ AppConstants.w + "  " + AppConstants.h);
	}
}
