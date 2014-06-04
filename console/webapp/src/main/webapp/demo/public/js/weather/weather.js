// JavaScript Document

(function($) {
	$.fn.weather = function(options) {
		$.fn.weather.defaults = {
			'obj':{},
			'css':'style_1',
			'option':{},
			/* 配置 */
			'config': {
				lang : {
					day : '白天',
					night : '夜晚',
					temp : '°C',
					wind : '级风',
				},
				convert : function(sky){
					var weatherInfo = {
							cloudy 		: ['多云','多云转阴','晴转多云','阴转多云'],
							overcast	: ['阴','雾','沙尘暴','浮尘','扬沙','强沙尘暴'],
							rainy		: ['多云转小雨','小雨转多云','小雨','中雨','大雨','暴雨','大暴雨','特大暴雨','冻雨','小雨转中雨','中雨转大雨','大雨转暴雨','暴雨转大暴雨','大暴雨转特大暴雨','阵雨','雷阵雨','雷阵雨伴有冰雹'],
							sleet		: ['雨夹雪'],
							snow		: ['阵雪','小雪','中雪','大雪','暴雪','小雪转中雪','中雪转大雪','大雪转暴雪','中雪转小雪','大雪转中雪','暴雪转大雪'],
							sunshine	: ['晴']
						},
						weather = '',
						state = '';
					for( state in weatherInfo ){
						if( $.inArray( sky , weatherInfo[state] ) > -1 ){
							weather = state;
							break;
						}
					} 
					return weather || state || 'sunshine'  ;
				}
			}, 
			/* 载入数据 */
			'include': function(d,g) {
				function o(){}
				function j(b,c,a){/\.css$/.test(b)?(a=f.createElement(p),a.href=b,a.rel="stylesheet",a.type="text/css",e.appendChild(a),c()):(k++,a=f.createElement(q),a.onload=function(){r(a,c)},a.onreadystatechange=function(){/loaded|complete/.test(this.readyState)&&r(a,c)},a.async=!0,a.src=b,e.insertBefore(a,e.firstChild))}
				function r(b,c){t(c);l[b.src.split("/").pop()]=1;b.onload=b.onreadystatechange=null}
				function t(b){function c(){!--k&&g()}
				b.length?b(c):(b(),c())};function s(b){var c,a;c=b.length;for(a=[];c--;a.unshift(b[c]));return a}
				var f=document,e=f.getElementsByTagName("head")[0],l={},k=0,h=[],q="script",p="link",m;!d.pop&&(d=[d]);g=g||o;(function c(a,i,e,n){if(!f.body)return setTimeout(c,1);h=[].concat(s(f.getElementsByTagName(q)),s(f.getElementsByTagName(p)));for(a=h.length;a--;)(m=h[a].src||h[a].href)&&(l[m.split("/").pop()]=m);for(a=d.length;a--;)n=o,e=!1,d[a].pop?(i=d[a][0],n=d[a][1],e=d[a][2]):i=d[a],l[i.split("/").pop()]||j(i,n,e);!k&&g()})()
			}, 
			/* 样式1 */
			'style_1': function() {
				var html = '<div class="weather_1_icon weather_1_'+opts.option.css+'">&nbsp;</div>';
				html += '<div style="float:left;"><h2>'+opts.option.city+'</h2><br />'+opts.option.sky+'</div>';
				return html;
			}, 
			/* 样式2 */
			'style_2': function() {
				var hours = new Date().getHours();
				hours_css = 'daytime';
				if(hours > 6 && hours < 18) {
					hours_css = 'daytime';
				} else {
					hours_css = 'night';
				}
				 
				var html = '<div class="weather_2_'+hours_css+'">';
				html += '<div class="weather_2_box_a"><div class="weather_2_icon weather_2_'+opts.option.css+'"></div>';
				html += '<div class="weather_2_temp">'+opts.option.temp+'℃</div></div>';
				html += '<div class="weather_2_tips">'+opts.option.sky+'<br>北风(微风)</div>';
				html += '</div>';
				return html;
			}
		};
		
		/*继承传入的设置参数*/
		var opts = $.extend({}, $.fn.weather.defaults, options);
		
		return this.each(function() {
			opts.obj = $(this);
			opts.include(['http://php.weather.sina.com.cn/iframe/index/w_cl.php?code=js&day=0&city=&dfc=1&charset=utf-8'], function(){
				var city = '', weatherData = '', dataInfo = '';
				for( city in window.SWther.w ){					
					dataInfo = SWther.w[city][0];
					weatherData = {
						city : city ,
						date : SWther.add.now.split(' ')[0] || '',
						day_weather: dataInfo.s1,
						night_weather :dataInfo.s2,
						day_temp: dataInfo.t1,
						night_temp: dataInfo.t2,
						day_wind:dataInfo.p1,
						night_wind: dataInfo.p2
					};
				}
				var is_day = new Date().getHours() > 18;
				opts.option.city = weatherData.city;
				opts.option.sky = is_day ? weatherData.day_weather : weatherData.night_weather;
				opts.option.temp = is_day ? weatherData.day_temp : weatherData.night_temp;
				opts.option.wind = is_day ? weatherData.day_wind : weatherData.night_wind;
				opts.option.css = opts.config.convert( opts.option.sky ) ;
				/*for(var i in opts.option) {
					alert(i+'='+opts.option[i]);
				}*/
				var html = '';
				/*alert(opts.css);*/
				switch(opts.css) {
					case 'style_1':
						html = opts.style_1();
						break;
					case 'style_2':
						html = opts.style_2();
						break;
				}
				$(opts.obj).html(html);
			});
		});
	}
})(jQuery);