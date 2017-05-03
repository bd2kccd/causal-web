function toHumanReadableSize(fileSize) {
    var unit = 1000;
    if (fileSize < unit) {
        return fileSize + ' B';
    }
    var size = ['kB', 'MB', 'GB', 'TB', 'PB', 'EB'];
    var exp = Math.floor(Math.log(fileSize) / Math.log(unit));

    return Number(fileSize / Math.pow(unit, exp)).toFixed(2) + ' ' + size[exp - 1];
}