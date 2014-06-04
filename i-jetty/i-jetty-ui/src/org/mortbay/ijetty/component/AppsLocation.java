/**
 * @Title: AppsLocation.java
 * @Package com.mylayout.app.component
 * @Description: TODO
 * Copyright: Copyright (c) 2012 
 * Company:湖南潇峰云集科技有限公司
 * 
 * @author Comsys-ningh
 * @date 2013-7-5 下午2:59:34
 * @version V1.0
 */  	

    
package org.mortbay.ijetty.component;

import java.io.Serializable;
import java.util.Map;


	/**
 * Copyright (c) 2012,湖南潇峰云集科技有限公司
 * All rights reserved.
 *
 * 文件名称： AppsLocation.java
 * 文件标识：见配置管理计划书
 * 摘    要：简要描述本文件的内容
 *
 * 当前版本：1.1
 * 作    者：输入作者（或修改者）名字          ---> ningh
 * 完成日期：2013-7-5
 *
 * 取代版本：1.0
 * 原作者  ：输入原作者（或修改者）名字  ---> ningh
 * 完成日期：2013-7-5 
 */

public class AppsLocation implements Serializable{
	public int mRows;
	public int mCold;
	public Map<String, Point> mApps;
}
