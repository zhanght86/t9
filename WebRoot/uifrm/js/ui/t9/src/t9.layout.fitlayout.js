(function($) {
  var opts = {
    el: $('<div></div>'),
    items: [],
    draggable: {
      containment: 'parent'
    },
    resizable: {},
    position: 'center',
    droppableCls: 'free-droppable',
    cmpCls: 'jq-freelayout',
    droppableCmps: []
  };
  
  T9.createComponent.call(T9.layouts, "FitLayout", opts, T9.layouts.AutoLayout.prototype, {
    doItem: function(e) {
      if (T9.isCmp(e)) {
        e.setStyle({
          width: '100%',
          zoom: 1
        });
        if (e.setHeight) {
          e.setHeight(this.el.height() - (e.weltHeight || 75));
        }
      }
    },
    receiveItem: function(e) {
      T9.layouts.AutoLayout.prototype.receiveItem.apply(this, arguments);
      if (e.setHeight) {
        e.setHeight(this.el.height() - (e.weltHeight || 75));
      }
    }
  });
})(jQuery);