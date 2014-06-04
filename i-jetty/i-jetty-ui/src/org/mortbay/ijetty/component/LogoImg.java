package org.mortbay.ijetty.component;

import android.graphics.drawable.Drawable;

public class LogoImg {
	private int mCol, mRow;
	private int mId;
	private Drawable mBackgroundImg;
	private String mPackgeName; 
	private String mAppName; 
	
	public LogoImg() {

	}

	public LogoImg(int row, int col, Drawable backgroundImg) {
		super();
		this.mCol = col;
		this.mRow = row;
		this.mBackgroundImg = backgroundImg;
		mId = Polygon.getIDByRowCol(row, col);
	}

	public void setRowCol(int row, int col) {
		this.mCol = col;
		this.mRow = row;
		this.mId = Polygon.getIDByRowCol(row, col);
	}
	
	public void setRowCol(Point pPoint) {
		this.mCol = pPoint.y;
		this.mRow = pPoint.x;
		this.mId = Polygon.getIDByRowCol(mRow, mCol);
	}

	public int getId() {
		return mId;
	}

	public int getCol() {
		return mCol;
	}

	public int getRow() {
		return mRow;
	}

	public Drawable getBackgroundImg() {
		return mBackgroundImg;
	}

	public void setBackgroundImg(Drawable backgroundImg) {
		this.mBackgroundImg = backgroundImg;
	}

	public String getmPackgeName() {
		return mPackgeName;
	}

	public void setmPackgeName(String mPackgeName) {
		this.mPackgeName = mPackgeName;
	}

	public String getmAppName() {
		return mAppName;
	}

	public void setmAppName(String mAppName) {
		this.mAppName = mAppName;
	}
}
