/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////example//////////////////////////////////////////////////////
//		String res = "";
//		try
//		{
//			 InputStream fin = this.getBaseContext().getAssets().open("example.txt");;
//			 int length = fin.available();
//			 byte [] buffer = new byte[length];
//			 fin.read(buffer);
//			 res = EncodingUtils.getString(buffer, "UTF-8");
//			 fin.close(); 
//		}catch(Exception e)
//		{
//			e.printStackTrace();
//		} 
//		PlayListUtil.readPlayList(res);
//
//
//
////////////////////////////////////////////////////////////////////////////////////////////////
package org.mortbay.ijetty.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EncodingUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.ijetty.AppConstants;
import org.mortbay.ijetty.IJetty;
import org.mortbay.ijetty.network.IRequestListener;
import org.mortbay.ijetty.network.InterfaceOp;
import org.mortbay.ijetty.network.NetworkUtil;

import android.os.SystemClock;
import android.util.Log;
import android.webkit.WebSettings;

public class PlayListUtil
{
    private final static String TAG = "PlayListUtil";
    public static String playListVersion = "";
    public static boolean isPlayListChanged = false;
    public static boolean isPlayListFileSynced = true;
    public static boolean isApkGetNeedConfirm = false;
    public static List<DownloadFile> mListDownloadFiles = new ArrayList<DownloadFile>();

    //把字符串分成zone、set、time三部分
    public static String playListFilePath = "file:///android_asset/example.txt";
    private static String mStrPlayList, mStrNewPlayList;
    private static String mStrFileList;

    private static String mStrZoneDefine;
    private static List<PlayZone> mListPlayZone = new ArrayList<PlayZone>();

    private static String mStrSetDefine; //会有多个SET域
    private static List<String> mListSetDefine = new ArrayList<String>();
    private static List<PlaySet> mListPlaySets = new ArrayList<PlaySet>();

    private static String mStrTimeDefine;//会有多个PLAY域
    private static List<String> mListPlayDefineList = new ArrayList<String>();
    private static List<PlayTime> mListPlayTimes = new ArrayList<PlayTime>();

    public static void getPlayList()
    {
        InterfaceOp.protoPlayListGet(new IRequestListener()
        {
            public void onError(Exception e)
            {
                LogUtil.log("getPlayList onError   " + e.getMessage());
            }

            public void onComplete(boolean isError, String errMsg, JSONObject respObj)
            {
                //                        LogUtil.log("getPlayList onComplete =====>isError: "
                //                                        + isError + "  respObj:" + respObj);
                if (respObj.optString("result","") == "false")
                {
                    Log.e("====smallstar=====",respObj.optString("error",""));
                    return;
                }
                //========================playlist==========================
                if (respObj.optString("playlists","") == "")
                {
                    mStrNewPlayList = "var programs = " + "{}" + ";";
                }
                else
                {
                    mStrNewPlayList = "var programs = " + respObj.optString("playlists","") + ";";
                }
                mStrPlayList = mStrNewPlayList;
                writeFile(IJetty.__JETTY_DIR + "/" + IJetty.__WEBAPP_DIR + "/console/demo/data/programs.js",mStrNewPlayList);
                //========================filelist==========================
                mStrFileList = respObj.optString("files","");
                Log.w("smallstar", mStrFileList);
                try
                {
                    JSONArray jsonar = new JSONArray(mStrFileList);
                    for (int i = 0; i < jsonar.length(); i++)
                    {
                        JSONObject oj = jsonar.getJSONObject(i);
                        DownloadFile downloadFile = new DownloadFile();
                        Log.v("=====smallstar====", oj.getString("url"));
                        downloadFile.url = oj.getString("url");
                        downloadFile.fileName = downloadFile.url.substring(downloadFile.url.lastIndexOf("/") + 1);
                        downloadFile.time = oj.getString("time");
                        downloadFile.size = oj.getString("size");
                        downloadFile.folder = oj.getString("folder");
                        mListDownloadFiles.add(downloadFile);
                    }
                }
                catch (JSONException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                isPlayListChanged = false;
                isPlayListFileSynced = false;
                //IJetty.getInstance().clearFormData();
                SystemClock.sleep(10 * 1000);
                //IJetty.getInstance().mWebView.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK);
                //清除缓存，重新加载
                IJetty.getInstance().mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);//LOAD_CACHE_ELSE_NETWORK//LOAD_NO_CACHE
                IJetty.getInstance().mWebView.clearHistory();
                IJetty.getInstance().mWebView.clearFormData();
                IJetty.getInstance().mWebView.clearCache(true);
                IJetty.getInstance().mWebView.loadUrl(AppConstants.CLIENT_DEFAULT2_PLAYURL);
                //IJetty.getInstance().mWebView.reload();
            }
        });
    }

    public static class DownloadFile
    {
        String url;
        String fileName;
        String time;
        String size;
        String folder;
    }

    //确认APK下载完
    public static void confirmGetPlayListFiles()
    {
        InterfaceOp.protoPlayListConfirm(new IRequestListener()
        {
            public void onError(Exception e)
            {
                LogUtil.log("confirmGetPlayListFiles onError   " + e.getMessage());
            }

            public void onComplete(boolean isError, String errMsg, JSONObject respObj)
            {
                if (respObj.optString("result","").equals("false"))
                {
                    Log.e("====smallstar=====",respObj.optString("error",""));
                    return;
                }
            }
        });
    }

    public static boolean syncPlayListFiles2()
    {
        boolean filesDownloadFinished = false;
        File uploadDir = new File(IJetty.__JETTY_DIR + "/" + IJetty.__WEBAPP_DIR + "/" + IJetty.__UPLOAD_DIR);
        if (!uploadDir.exists())
        {
            boolean made = uploadDir.mkdirs();
            Log.i(TAG,"Made " + uploadDir + ": " + made);
        }
        if (mListDownloadFiles.isEmpty())
        {
            filesDownloadFinished = true;
            return filesDownloadFinished;
        }
        int i = 0;
        for (DownloadFile downloadFile : mListDownloadFiles)
        {
            //判断文件夹是否存在
            File downloadFolder = new File(IJetty.__JETTY_DIR + "/" + IJetty.__WEBAPP_DIR + "/" + IJetty.__UPLOAD_DIR + "/" + downloadFile.folder);
            if (!downloadFolder.exists())
            {
                boolean made = downloadFolder.mkdirs();
                Log.i(TAG,"Made " + downloadFolder + ": " + made);
            }
            //判断文件是否存在
            File file = new File(IJetty.__JETTY_DIR + "/" + IJetty.__WEBAPP_DIR + "/" + IJetty.__UPLOAD_DIR + "/" + downloadFile.folder + "/"
                    + downloadFile.fileName);
            if (!file.exists())
            {//文件不存在下载
                NetworkUtil.requestDownload(downloadFile.url,IJetty.__JETTY_DIR + "/" + IJetty.__WEBAPP_DIR + "/" + IJetty.__UPLOAD_DIR + "/"
                        + downloadFile.folder);
            }
            else
            {//文件存在，从mListDownloadFiles移除
                mListDownloadFiles.remove(i);
                break;
            }
            i++;
        }
        return filesDownloadFinished;
    }

    public static boolean syncPlayListFiles()
    {
        //获取本地所有文件总大小，判断是否有文件需要删除
        //获取所有本地资源文件列表
        //获取所有播单资源列表
        //取播单资源文件，在本地资源里表里面查找判断是否存在
        //不存在则添加到下载列表里面去
        List<String> localFilesNameList = FileUtil.getSrcFileNameList();
        boolean filesAllEqure = true;
        for (final PlaySet set : mListPlaySets)
        {
            for (final PlaySetUnit setUnit : set.listPlaySetUnits)
            {
                //setUnit.fileName实际上是一个link
                String tempFileName = setUnit.fileName.substring(setUnit.fileName.lastIndexOf("/") + 1);
                if (localFilesNameList.indexOf(tempFileName) < 0)
                {
                    //文件不存在就申请下载
                    NetworkUtil.requestDownload(setUnit.fileName);
                    filesAllEqure = false;
                }
            }
        }

        if (filesAllEqure)
        {
            InterfaceOp.protoPlayListConfirm(new IRequestListener()
            {
                public void onError(Exception e)
                {
                    LogUtil.log("protoPlayListConfirm onError: " + e.getMessage());
                }

                public void onComplete(boolean isError, String errMsg, JSONObject respObj)
                {
                    //Log.e("smallstar", "heartbeat onComplete=>isError: " + isError + "  respObj:" + respObj);
                    if (isError || (respObj == null))
                    {
                        //节目单推送完成确认失败，强制重新同步
                        PlayListUtil.isPlayListFileSynced = false;
                        return;
                    }
                    String result = respObj.optString("result","");
                    Log.w("protoPlayListConfirm return",result);
                    if (!result.equals("true"))
                    {
                        //节目单推送完成确认失败，强制重新同步
                        PlayListUtil.isPlayListFileSynced = false;
                    }
                }
            });
        }
        return filesAllEqure;
    }

    public static boolean readPlayList(String playList)
    {
        mStrPlayList = playList;
        Log.w(TAG,mStrPlayList);
        if (!initDomain(mStrPlayList))
        {
            return false;
        }
        initPlayZones(mStrZoneDefine);
        initSetDefine(mListSetDefine);
        initTimeDefine(mListPlayDefineList);
        return true;
    }

    /**
     * init Domains in playList: mStrZoneDefine, mStrSetDefine, mStrTimeDefine
     * 
     * @param playList
     *            //完整的播放清单字符串
     * @return
     * */
    private static boolean initDomain(String playList)
    {
        //START_ZONES开头
        playList = StringUtils.replaceBlank(playList);
        if (!playList.startsWith("START_ZONES"))
        {
            Log.e(TAG,"playList String not start with START_ZONES");
            return false;
        }

        int Index = playList.indexOf("END_ZONES");
        if (Index < 0)
        {
            Log.e(TAG,"playList String have no END_ZONES");
            return false;
        }
        else
        {
            mStrZoneDefine = playList.substring(0,Index + "END_ZONES".length());
        }
        playList = playList.substring(Index + "END_ZONES".length(),playList.length());

        if (!playList.startsWith("DEFINE_SET"))
        {
            Log.e(TAG,"playList Second String not start with START_ZONES");
            return false;
        }
        Index = playList.lastIndexOf("END_SET");
        if (Index < 0)
        {
            Log.e(TAG,"playList String have no END_SET");
            return false;
        }
        else
        {
            mStrSetDefine = playList.substring(0,Index + "END_SET".length());
            //获取set字符串列表
            String temp = mStrSetDefine;
            String subTempStr;
            for (int i = temp.indexOf("END_SET"); i > 0; i = temp.indexOf("END_SET"))
            {
                subTempStr = temp.substring(0,i + "END_SET".length());
                temp = temp.substring(i + "END_SET".length());
                mListSetDefine.add(subTempStr);
            }
        }

        playList = playList.substring(Index + "END_SET".length(),playList.length());
        if (!playList.startsWith("PLAY"))
        {
            Log.e(TAG,"playList Second String not start with START_ZONES");
            return false;
        }
        else
        {
            mStrTimeDefine = playList;
            //获取play字符串列表
            String temp = mStrTimeDefine;
            String subTempStr;
            for (int i = temp.indexOf(")"); i > 0; i = temp.indexOf(")"))
            {
                subTempStr = temp.substring(0,i + ")".length());
                temp = temp.substring(i + ")".length());
                mListPlayDefineList.add(subTempStr);
            }
        }
        return true;
    }

    private static void initPlayZones(String zoneDefine)
    {
        int index;
        //去掉START_ZONES与END_ZONES
        if (!zoneDefine.startsWith("START_ZONES"))
        {
            Log.e(TAG,"zoneDefine String not start with START_ZONES");
            return;
        }
        String temp = zoneDefine.substring("START_ZONES".length());
        index = temp.indexOf("END_ZONES");
        temp = temp.substring(0,index);
        String zoneName, PointValues;
        int pointValues[] = new int[4];
        for (index = temp.indexOf("="); index > 0; index = temp.indexOf("="))
        {
            PlayZone playZone = new PlayZone();
            zoneName = temp.substring(0,index);
            PointValues = temp.substring(index + 1,temp.indexOf(")") + 1);
            temp = temp.substring(temp.indexOf(")") + 1,temp.length());
            playZone.zoneName = zoneName;
            PointValues = PointValues.substring(1);//remove (
            PointValues = PointValues.substring(0,PointValues.length() - 1);//remove)
            for (int i = PointValues.indexOf(','), j = 0; i > 0; i = PointValues.indexOf(','))
            {
                String value = PointValues.substring(0,i);
                PointValues = PointValues.substring(i + 1);
                pointValues[j] = Integer.parseInt(value);
                //Log.w(TAG, String.valueOf(pointValues[j]));
                j++;
                if (j == 3)
                {
                    pointValues[j] = Integer.parseInt(PointValues);
                }
            }
            playZone.zoneValue = pointValues;
            mListPlayZone.add(playZone);
        }
    }

    private static void initSetDefine(List<String> setList)
    {
        String setTemp;
        for (int i = 0; i < setList.size(); i++)
        {
            setTemp = setList.get(i);
            //DEFINE_SET END_SET
            setTemp = setTemp.substring("DEFINE_SET".length(),setTemp.indexOf("END_SET"));
            PlaySet playSet = new PlaySet();
            playSet.setName = setTemp.substring("(".length(),setTemp.indexOf(")"));
            Log.w(TAG,playSet.setName);
            setTemp = setTemp.substring(setTemp.indexOf(")") + 1);
            String strPlaySetUnit;
            String strKeys[] = new String[5];
            for (int j = setTemp.indexOf(';'); j > 0; j = setTemp.indexOf(';'))
            {
                strPlaySetUnit = setTemp.substring(0,j);
                setTemp = setTemp.substring(j + 1);
                //				Log.w(TAG, strPlaySetUnit);
                for (int k = strPlaySetUnit.indexOf(','), l = 0; k > 0; k = strPlaySetUnit.indexOf(','))
                {
                    strKeys[l] = strPlaySetUnit.substring(0,k);
                    strPlaySetUnit = strPlaySetUnit.substring(k + 1);
                    l++;
                    if (l == 4)
                    {
                        strKeys[l] = strPlaySetUnit;
                    }
                }
                PlaySetUnit playSetUnit = new PlaySetUnit();
                playSetUnit.fileName = strKeys[0];
                playSetUnit.playRegions = strKeys[1];
                playSetUnit.playMode = strKeys[2];
                playSetUnit.playDuration = strKeys[3];
                playSetUnit.playTransition = strKeys[4];
                //				Log.w(TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++");
                //				Log.w(TAG, playSetUnit.fileName + playSetUnit.playRegions + playSetUnit.playMode + 
                //						playSetUnit.playDuration + playSetUnit.playTransition);
                //				Log.w(TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++");
                playSet.listPlaySetUnits.add(playSetUnit);
            }
            mListPlaySets.add(playSet);
        }
    }

    private static void initTimeDefine(List<String> timeList)
    {
        String timeTemp;
        for (int i = 0; i < timeList.size(); i++)
        {
            PlayTime playTime = new PlayTime();
            timeTemp = timeList.get(i);
            timeTemp = timeTemp.substring(timeTemp.indexOf("(") + 1,timeTemp.indexOf(")"));
            //Log.w(TAG, timeTemp);
            String subTimeTemp1, subTimeTemp2;
            subTimeTemp1 = timeTemp.substring(0,timeTemp.indexOf('/') - 3);
            subTimeTemp2 = timeTemp.substring(timeTemp.indexOf('/') - 2);
            //Log.w(TAG, subTimeTemp1);
            playTime.playContent = subTimeTemp1.substring(0,subTimeTemp1.indexOf(','));
            playTime.days = subTimeTemp1.substring(subTimeTemp1.indexOf('=') + 1);
            //Log.w(TAG, subTimeTemp2);
            String strKeys[] = new String[5];
            for (int j = subTimeTemp2.indexOf(','), k = 0; j > 0; j = subTimeTemp2.indexOf(','))
            {
                strKeys[k] = subTimeTemp2.substring(0,j);
                subTimeTemp2 = subTimeTemp2.substring(j + 1);
                k++;
                if (k == 4)
                {
                    strKeys[k] = subTimeTemp2;
                }
            }
            playTime.startDate = strKeys[0];
            playTime.endDate = strKeys[1];
            playTime.startTime = strKeys[2];
            playTime.endTime = strKeys[3];
            playTime.endMode = strKeys[4];
            //			Log.w(TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++");
            //			Log.w(TAG, playTime.playContent + playTime.days + playTime.startDate + playTime.endDate + playTime.startTime+
            //					playTime.endTime + playTime.endMode);
            //			Log.w(TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++");
            mListPlayTimes.add(playTime);
        }
    }

    public static class PlayZone
    {
        String zoneName;
        int zoneValue[];//xStart, yStart, xEnd, yEnd;
    }

    public static class PlaySet
    {
        public String setName;
        List<PlaySetUnit> listPlaySetUnits = new ArrayList<PlaySetUnit>();
    }

    public static class PlaySetUnit
    {
        String fileName;
        String playRegions;
        String playMode;//S = Stretch (fill in region)
        String playDuration;//seconds, duration (0)= length of video
        //I = transition is INSTANT,
        //U= transition is scroll UP,
        //L = transition is scroll LEFT
        //F = transition is FADE IN
        //R = transition is RANDOM
        //T = transition is TWIST
        String playTransition;
    }

    public static class PlayTime
    {
        String playContent;
        String days;
        String startDate;
        String endDate;
        String startTime;
        String endTime;
        String endMode;
    }

    public static String playZones2Json()
    {
        String JsonString;
        PlayZone playZone;
        JsonString = "'ZONES':{";
        for (int i = 0; i < mListPlayZone.size(); i++)
        {
            playZone = mListPlayZone.get(i);
            JsonString = JsonString + "'" + playZone.zoneName + "':[" + "'" + String.valueOf(playZone.zoneValue[0]) + "'," + "'"
                    + String.valueOf(playZone.zoneValue[1]) + "'," + "'" + String.valueOf(playZone.zoneValue[2]) + "'," + "'"
                    + String.valueOf(playZone.zoneValue[3]) + "'";
            if ((i + 1) == mListPlayZone.size())
                JsonString += "]";
            else
                JsonString += "],";
        }
        JsonString += "}";
        return JsonString;
    }

    public static String setDefine2Json()
    {
        String JsonString;
        PlaySet playSet;
        PlaySetUnit playSetUnit;
        //Log.w(TAG, String.valueOf(mListPlaySets.size()));
        JsonString = "'DEFINE_SET':{";
        for (int i = 0; i < mListPlaySets.size(); i++)
        {
            playSet = mListPlaySets.get(i);
            JsonString = JsonString + "'" + playSet.setName + "':{";
            for (int j = 0; j < playSet.listPlaySetUnits.size(); j++)
            {
                playSetUnit = playSet.listPlaySetUnits.get(j);
                JsonString = JsonString + "[" + "'" + playSetUnit.fileName + "'," + "'" + playSetUnit.playRegions + "'," + "'" + playSetUnit.playMode + "',"
                        + "'" + playSetUnit.playDuration + "'," + "'" + playSetUnit.playTransition + "'";
                if ((j + 1) == playSet.listPlaySetUnits.size())
                    JsonString += "]";
                else
                    JsonString += "],";
            }
            if ((i + 1) == mListPlaySets.size())
                JsonString += "}";
            else
                JsonString += "},";
        }
        JsonString += "}";
        return JsonString;
    }

    public static String playTime2Json()
    {
        String JsonString;
        PlayTime playTime;
        JsonString = "'PLAY':{";
        for (int i = 0; i < mListPlayTimes.size(); i++)
        {
            playTime = mListPlayTimes.get(i);
            JsonString = JsonString + "[" + "'" + playTime.playContent + "'," + "'" + playTime.days + "'," + "'" + playTime.startDate + "'," + "'"
                    + playTime.endDate + "'," + "'" + playTime.startTime + "'," + "'" + playTime.endTime + "'";
            if ((i + 1) == mListPlayTimes.size())
                JsonString += "]";
            else
                JsonString += "],";
        }
        JsonString += "}";
        return JsonString;
    }

    public static void writePlayList2JsonFile()
    {
        Log.w(TAG,playZones2Json());
        Log.w(TAG,"********************************************************");
        Log.w(TAG,setDefine2Json());
        Log.w(TAG,"********************************************************");
        Log.w(TAG,playTime2Json());
    }

    public static void writeFile(String fileName, String message)
    {
        try
        {
            FileOutputStream overWrite = new FileOutputStream(fileName,false);
            OutputStreamWriter osw = new OutputStreamWriter(overWrite,"UTF-8");
            osw.write(message);
            osw.flush();
            overWrite.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
