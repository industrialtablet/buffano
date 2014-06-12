/**
 * User: chenhua
 * Date: 14-3-29
 * Time: 上午11:05
 * To change this app use File | Settings | File app.
 */

/**
 * 外部链接播放接口
 *
 * @param options
 */
var JavaLinks =  (function ($) {
    return {
        /*是否开启接口*/
        enable : false,

        initialize : function(enable) {
            JavaLinks.enable = enable;
        },

        playUrlAssist : function(urlPlayListJson) {
            window.ia.playUrlAssist(urlPlayListJson);
        }
    }
})(jQuery);


/*
* @requires  JavaLinks, jquery
* 外部链接推送插件
* @return mix
*/
var links = (function($) {
    return {
        /*初始化*/
        initialize: function() {},
        /*读取播放列表*/
        getPlayList : function(data) {
            var playList = Array();
            if($.isEmptyObject(data)) {
                return false;
            }

            $.each(data, function(index,item) {
                playList.push({
                    "url" : item.play_list,
                    "time_start" : item.time_start,
                    "time_end" : item.time_end
                });
            });
            return JSON.stringify(playList);
        },
        /*推送数据*/
        push: function(data) {
            if($.isEmptyObject(data)) {
                Utils.debug("link节目当前没有播放的内容！");
            } else {
                var jsonStr = links.getPlayList(data);
                Utils.debug(jsonStr);
                JavaLinks.playUrlAssist(jsonStr);
            }
            return false;
        }
    }
})(jQuery);

