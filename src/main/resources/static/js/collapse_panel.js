function collapseAction(panel) {
    var $this = $(panel);
    if ($this.hasClass('collapsed')) {
        $this.find('i').removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
    } else {
        $this.find('i').removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-up');
    }
}
