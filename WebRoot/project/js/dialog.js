var jy = function(id) {return document.getElementById(id);};
function ShowDialog(id,vTopOffset)
{
   if(typeof arguments[1] == "undefined")
     vTopOffset = 90;
     
   var bb=(document.compatMode && document.compatMode!="BackCompat") ? document.documentElement : document.body;
   $("overlay").style.width = Math.max(parseInt(bb.scrollWidth),parseInt(bb.offsetWidth))+"px";
   $("overlay").style.height = Math.max(parseInt(bb.scrollHeight),parseInt(bb.offsetHeight))+"px";

   $("overlay").style.display = "block";
   jy(id).style.display = 'block';

   jy(id).style.left = ((bb.offsetWidth - jy(id).offsetWidth)/2)+"px";
   jy(id).style.top  = (vTopOffset + bb.scrollTop)+"px";
}

function HideDialog(id)
{
   $("overlay").style.display = 'none';
   jy(id).style.display = 'none';
   location.reload();
}