<?
   include_once("../header.php");
   include_once("inc/utility_file.php");
   ob_clean();

   $query = "SELECT * from EMAIL,EMAIL_BODY where EMAIL_BODY.BODY_ID=EMAIL.BODY_ID and EMAIL_ID='$EMAIL_ID' and TO_ID='$LOGIN_USER_ID' and (EMAIL.DELETE_FLAG='' or EMAIL.DELETE_FLAG='0' or EMAIL.DELETE_FLAG='2')";
   $cursor= exequery($connection,$query);
   if($ROW=mysql_fetch_array($cursor))
   {
      $FROM_ID=$ROW["FROM_ID"];
      $SUBJECT=$ROW["SUBJECT"];
      $CONTENT=$ROW["COMPRESS_CONTENT"] == "" ? $ROW["CONTENT"] : @gzuncompress($ROW["COMPRESS_CONTENT"]);
      $SEND_TIME=$ROW["SEND_TIME"];
      $IMPORTANT=$ROW["IMPORTANT"];
      $ATTACHMENT_ID=$ROW["ATTACHMENT_ID"];
      $ATTACHMENT_NAME=$ROW["ATTACHMENT_NAME"];
      $READ_FLAG=$ROW["READ_FLAG"];
   
      $SUBJECT=htmlspecialchars($SUBJECT);
      $CONTENT=stripslashes($CONTENT);
   
      $IS_WEBMAIL=$ROW["IS_WEBMAIL"];
   
      if($IMPORTANT=="0" || $IMPORTANT=="")
      $IMPORTANT_DESC="";
      else if($IMPORTANT=="1")
      $IMPORTANT_DESC="<font color='red'>"._("重要")."</font>";
      else if($IMPORTANT=="2")
      $IMPORTANT_DESC="<font color='red'>"._("非常重要")."</font>";
   
      $query1 = "SELECT UID,USER_NAME from USER where USER_ID='$FROM_ID'";
      $cursor1= exequery($connection,$query1);
      if($ROW=mysql_fetch_array($cursor1))
      {
         $UID=$ROW["UID"];
         $FROM_NAME=$ROW["USER_NAME"];
      }
      else
         $FROM_NAME=$FROM_ID;
         
      //2012/4/12 2:30:45 lp 增加已阅状态更新
      if($READ_FLAG == 0)
      {
         $query = "update EMAIL set READ_FLAG = 1 where EMAIL_ID='$EMAIL_ID'";
         exequery($connection,$query);    
      }
   }
   else
   {
      exit;
   }
?>
      <div class="container">
         <div id="replyTo" style="display:none;">
            <em userid="<?=$FROM_ID?>" uid="<?=$UID?>"><?=$FROM_NAME?></em>   
         </div>
         <div class="read_detail email_from"><span class="grapc"><?=_("发件人：")?></span><?=$FROM_NAME?></div>
         <?
            if($IS_WEBMAIL!="0")
            {
         ?>
         <div class="read_detail"><span class="grapc"><?=_("收件人：")?></span><?=$FROM_NAME?></div>
         <?
            }
         ?>
         <div class="read_detail">
            <strong><?=$SUBJECT?></strong> <?=$IMPORTANT_DESC?>
            <span class="fix_detail grapc"><?=date("Y"._("年")."n"._("月")."j"._("日")." H:i",$SEND_TIME)?></span>
         </div>
         <div class="read_content"><?=$CONTENT?></div>
         <?
         if($ATTACHMENT_ID != "" && $ATTACHMENT_NAME != "")
         {
         ?>
            <div class="read_attach"><?=attach_link_pda($ATTACHMENT_ID,$ATTACHMENT_NAME,$P,'',1,1,1)?></div>
         <?
         }
         ?>
      </div>
<script type="text/javascript">
$(document).ready(function(){
   oImg = $(".read_content img");
   oImg.each(function(){
      $(this).wrap("<div class='img_wrap'></div>");
      preLoadImage($(this).parent(".img_wrap") ,$(this).attr("src"));
   });
   
   function preLoadImage(obj, url)
   {
      //创建一个Image对象，实现图片的预下载
      var img = new Image();
      img.src = url;
      obj.html("<?=_('图片加载中...')?>");
      
      // 如果图片已经存在于浏览器缓存，直接调用回调函数
      if(img.complete)
      { 
         obj.empty().append(img);
         return; //直接返回，不用再处理onload事件
      }
      img.onload = function () 
      {
         obj.empty().append(img);
         oiScroll_2.refresh();
      };
      img.onerror = function()
      {
         obj.html('<?=_("图片加载失败！")?>');
      };
   }
});
</script>