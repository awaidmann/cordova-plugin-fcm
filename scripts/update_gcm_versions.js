var GMS_VERSION = '9.2.0'

module.exports = function(ctx) {
  if (ctx.opts.platforms.indexOf('android') < 0) {
    return
  }

  var fs = ctx.requireCordovaModule('fs')
  var path = ctx.requireCordovaModule('path')

  var projectProps = path.join(ctx.opts.projectRoot, 'platforms/android/project.properties')
  fs.readFile(projectProps, {encoding: 'utf8'}, (err, props) => {
    if (err) { return }
    if (props) {
      fs.open(projectProps, 'w', (err, fd) => {
          props.split('\n').forEach( dep => {
            if (dep.includes('=com.google.android.gms:') && dep.endsWith(':+')) {
              dep = dep.replace(':+', ':' + GMS_VERSION)
            }
            fs.write(fd, dep + '\n')
          })
          fs.close(fd)
      })
    }
  })
}
