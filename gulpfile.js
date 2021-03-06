var gulp = require('gulp');
var uglify = require('gulp-uglify');
var concat = require('gulp-concat');
var ngAnnotate = require('gulp-ng-annotate');
var sourcemaps = require('gulp-sourcemaps');

gulp.task('js', function () {
gulp.src(['./public/javascript/**/app.js', './public/javascript/**/*.js'])
    .pipe(sourcemaps.init({loadMaps: true}))
    .pipe(concat('javascripts.js'))
    .pipe(ngAnnotate())
    .on('error', swallowError)
    .pipe(uglify())
    .pipe(sourcemaps.write())
    .pipe(gulp.dest('./public/dist'))
})

gulp.task('watch', function () {
    gulp.watch('./public/javascript/**/*.js', ['js'])
});

gulp.task('default', ['js']);

function swallowError(error) {
    console.log(error.toString())

    this.emit('end');
}
