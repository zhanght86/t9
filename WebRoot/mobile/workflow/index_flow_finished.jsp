<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/mobile/workflow/header.jsp" %>
<script type="text/javascript">
    mui.init({keyEventBind: { backbutton: true }});
    mui.back = function(){
        closeWin();
    }
    var p = "<%=request.getSession().getId()%>";
    
    function closeWin(){
    	var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        parent.layer.close(index);
    }
</script>
<style>
    .mui-card .mui-control-content {
        padding: 10px;
    }
    .mui-control-content {
        height:500px;
    }
    .active span{
        font-weight:bold;
        font-size: 18px;
    }
    .mui-table-view-cell p{
        font-size: 16px;
        padding-top:5px; 
    }
    .mui-bar-nav ~ .mui-content .mui-pull-top-pocket{
        top:0px;
    }
</style>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">已办流程</h1>
</header>
<div class="mui-content">
    <div id="item1" class="mui-control-content mui-scroll-wrapper mui-active">
        <div class="mui-scroll">
        <ul class="mui-table-view">
        </ul>
        </div>
    </div>
</div>
</body>
</html>
<script>
    (function($) {
        $('#item1').scroll({
            indicators: true //是否显示滚动条
        });
        jQuery(".mui-control-content").css("height", (document.documentElement.clientHeight-50) + "px")
        
        $(document).on("tap", ".mui-table-view-cell",function(){
            var q_run_id = this.getAttribute("q_run_id");
            var q_flow_id = this.getAttribute("q_flow_id");
            var q_prcs_id = this.getAttribute("q_prcs_id");
            var q_flow_prcs = this.getAttribute("q_flow_prcs");
            openFlowDetail(q_run_id, q_flow_id, q_prcs_id, q_flow_prcs, 3, p);
        });
    })(mui);
    
    function createFragment(types) {
        var fragment = document.createDocumentFragment();
        var tempData;
        var url = "<%=contextPath%>/t9/mobile/workflow/act/T9PdaWorkflowIndexAct/dataYB.act";
        if(types == 'down'){//下拉
            $.ajax({url,  type: "GET",async: false, data:{'A':'down', 'STYPE':stype, "sessionid":p},
                success: function(data){
                    if(data && data != "NONEWDATA") {
                        tempData = data;
                    }else{
                        //jQuery("#total_item"+index).html("0");
                        mui.toast(nonewdata);
                    }
                }
            });
        }else{//上拉
            var lastedId =  jQuery("#item1").find("li:last").attr("q_id");
            if(!lastedId) return null;
            $.ajax({url,  type: "GET",async: false, data:{'A':'up', 'STYPE':stype, "sessionid":p, "LASTEDID": lastedId},
                success: function(data){
                    if(data && data != "NONEWDATA") {
                        tempData = data;
                    }else{
                        mui.toast(nonewdata);
                    }
                }
            });
        }
        if(tempData != null){
            var json_temp = eval(tempData);
            var json = eval(json_temp[0].list);
            //jQuery("#total_item"+index).html(json_temp[0].total_items);
            var li;
            var temp;
            for (var i = 0; i < json.length; i++) {
                temp = json[i];
                li = document.createElement('li');
                li.className = 'mui-table-view-cell '+temp.CLASS;
                li.setAttribute("q_id", temp.SEQ_ID);
                li.setAttribute("q_run_id", temp.RUN_ID);
                li.setAttribute("q_flow_id", temp.FLOW_ID);
                li.setAttribute("q_prcs_id", temp.PRCS_ID);
                li.setAttribute("q_flow_prcs", temp.FLOW_PRCS);
                li.setAttribute("q_op_flag", temp.OP_FLAG);
                li.innerHTML = "<span>"+temp.RUN_NAME+"</span>";
                li.innerHTML += "<p>["+temp.FLOW_NAME+"]&nbsp;&nbsp;"+temp.PRCS_NAME+" "+ temp.OP_FLAG_DESC +"</p>";
                li.innerHTML += "<p>[发起人："+temp.userName+"]&nbsp;&nbsp;"+temp.state+"</p>";
                fragment.appendChild(li);
            }
        }
        return fragment;
    }
    mui.init({
        pullRefresh: {
            container: '#item1',
            down: {
                contentrefresh: '正在加载...',
                callback: function() {
                    var self = this;
                    setTimeout(function() {
                        var ul = self.element.querySelector('.mui-table-view');
                        while(ul.hasChildNodes()) {//当table下还存在子节点时 循环继续
                            ul.removeChild(ul.firstChild);
                        }
                        var frg = createFragment('down');
                        if(frg){
                            ul.insertBefore(frg, ul.firstChild);
                        }
                        mui('#item1').pullRefresh().endPulldownToRefresh();
                    }, 1000);
                }
            },
            up: {
                contentrefresh: '正在加载...',
                callback: function() {
                    setTimeout(function() {
                        var ul = document.body.querySelector('.mui-table-view');
                        var frg = createFragment('up');
                        if(frg){
                            ul.appendChild(frg);
                        }
                        mui('#item1').pullRefresh().endPullupToRefresh();
                    }, 1000);
                }
            }
        },
        keyEventBind: { backbutton: true }
    });
    
    (function($) {
        //阻尼系数
        var deceleration = mui.os.ios?0.003:0.0009;
        $('.mui-scroll-wrapper').scroll({
            bounce: false,
            indicators: true, //是否显示滚动条
            deceleration:deceleration
        });
       // mui('#item1').pullRefresh().pulldownLoading();
    })(mui);
</script>
<script>
    jQuery(function(){
    	setTimeout(function() {
    		mui('#item1').pullRefresh().pulldownLoading();
    	}, 300);
    	//mui('#item1').pullRefresh().pulldownLoading();
    });
</script>