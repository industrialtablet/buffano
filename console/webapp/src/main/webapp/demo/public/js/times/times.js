// JavaScript Document

(function($) {
    function initArray() {
        for(i=0;i<initArray.arguments.length;i++) {
            this[i] = initArray.arguments[i];
        }
    }
	$.fn.times = function(options) {
		$.fn.times.defaults = {
			'obj':{},
            'isnMonths' : [],
            'isnDays' : [],
			'css':'time_1',
            /*时间部件宽度*/
            'width' : 256,
            /*时间部件高度*/
            'height': 120,
			'option':{},
			/* 配置 */
			'config': {},
            getFullYear : function(d) {
                yr = d.getYear();
                if(yr<1000) {
                    yr += 1900;
                }
                return yr;
            },
            setClock: function(){
                var Digital = new Date();
                var hours   = Digital.getHours();
                var minutes = Digital.getMinutes();
                var seconds = Digital.getSeconds();
                if(minutes<=9)
                    minutes = "0" + minutes;
                if(seconds<=9)
                    seconds = "0" + seconds;
                var clock = '';
                switch(opts.css) {
                    case 'time_3':
                        clock = hours+'<span>:</span>'+minutes+'<span class="time_3-small">'+seconds+'</span>';
                        break;
                    default :
                        clock = "" + hours + ":" + minutes + ":" + seconds + "";
                }
                $("#clock").html(clock);
                setTimeout(opts.setClock,1000);
            },
			/* 样式1 */
			'time_1': function() {
                var today = new Date();
				var html = '<div class="'+opts.css+'" style="width:'+opts.width+'px; height:'+opts.height+'px; ">'+opts.getFullYear(today)+"."+opts.isnMonths[today.getMonth()]+"."
                    +today.getDate()+"<br/>"+opts.isnDays[today.getDay()]+"<br/><span id='clock'></span>"
                    +""+'</div>';
				return html;
			}, 
			/* 样式2 */
			'time_2': function() {
                var today = new Date();
                var html = '<div class="'+opts.css+'" style="width:'+opts.width+'px; height:'+opts.height+'px; ">' +
                    '<span class="hour" id="clock"></span><br/>'+opts.isnDays[today.getDay()]+'&nbsp;'+opts.getFullYear(today)+"."+opts.isnMonths[today.getMonth()]+"."
                    +today.getDate()+"<br/>"
                    +""+'</div>';
				return html;
			},
            /* 样式3 */
            'time_3': function() {
                var today = new Date();
                var html = '<div class="'+opts.css+'" style="width:'+opts.width+'px; height:'+opts.height+'px; "><div class="time_3-box">' +
                    '<p class="time_3-time" id="clock"></p><p class="time_3-datebox">' +
                    '<span class="time_3-week">'+opts.isnDays[today.getDay()]+'</span><span class="time_3-date">'
                    +opts.getFullYear(today)+'年'+opts.isnMonths[today.getMonth()]+'月'+today.getDate()+'日</span></p></div></div>';
                return html;
            }
		};
		
		/*继承传入的设置参数*/
		var opts = $.extend({}, $.fn.times.defaults, options);
		
		return this.each(function() {
			opts.obj = $(this);
            opts.isnMonths = new initArray("01","02","03","04","05","06","07","08","09","10","11","12");
            opts.isnDays   = new initArray("星期日","星期一","星期二","星期三","星期四","星期五","星期六","星期日");
            var html = '';
            switch(opts.css) {
                case 'time_1':
                    html = opts.time_1();
                    break;
                case 'time_2':
                    html = opts.time_2();
                    break;
                case 'time_3':
                    html = opts.time_3();
                    break;
            }
            $(opts.obj).html(html);
            opts.setClock();
		});
	}
})(jQuery);