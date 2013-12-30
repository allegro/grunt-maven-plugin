
var path = require('path');

module.exports = function(grunt) {

    var mavenTasksPath = path.resolve('maven-tasks');

    var mavenProperties = readJSONWithCustomOverride(grunt, path.join(mavenTasksPath, 'maven-inner-properties.json'), 'maven-custom-inner-properties.json');

    grunt.registerTask('maven', 'grunt-maven workflow task.', function() {
        var config = grunt.config.get('maven');

        var copyGruntTargetToDist = {
        files: [{
                expand: true,
                src: config.dist.src,
                dest: config.dist.dest
            }]
        };

        var copyDistToMavenTarget = {
            files: [{
                expand: true,
                cwd: config.dist.dest,
                src: ['./**'],
                dest: path.join(mavenProperties.targetPath, config.warName, mavenProperties.jsSourceDirectory)
            }]
        };

        grunt.config.set('copy.mavenGruntTargetToDist', copyGruntTargetToDist);
        grunt.config.set('copy.mavenDistToMavenTarget', copyDistToMavenTarget);

        grunt.task.run(['copy:mavenGruntTargetToDist', 'copy:mavenDistToMavenTarget']);
    });

    grunt.registerTask('maven-watch', 'grunt-maven workflow task.', function() {
        var config = grunt.config.get('maven');
        var watchedFiles = path.join(mavenProperties.projectRootPath, mavenProperties.sourceDirectory, mavenProperties.jsSourceDirectory) + "/**";

        grunt.config.set('watch.maven', {
            files: watchedFiles,
            tasks: ['maven-watch-config', 'copy:mavenSrcToGruntTarget'].concat(config.watch.tasks)
        });

        grunt.task.run(['watch:maven']);
    });

    grunt.registerTask('maven-watch-config', '', function() {
        var config = grunt.config.get('maven');

        var copySrcToGruntTargetFiles = Array.isArray(config.maven.src) ? config.maven.src : [config.maven.src];
        for(var i = 0; i < mavenProperties.filteredFiles.length; ++i) {
            copySrcToGruntTargetFiles.push('!./' + mavenProperties.filteredFiles[i]);
        }

        var copySrcToGruntTarget = {
            files: [{
                expand: true,
                cwd: path.join(mavenProperties.projectRootPath, mavenProperties.sourceDirectory, mavenProperties.jsSourceDirectory),
                src: copySrcToGruntTargetFiles,
                dest: './'
            }]
        };

        grunt.config.set('copy.mavenSrcToGruntTarget', copySrcToGruntTarget);
    });

};

function readJSONWithCustomOverride(grunt, path, overridePath) {
    var properties = grunt.file.readJSON(path);
    if(grunt.file.exists(overridePath)) {
        var overridingProperties = grunt.file.readJSON(overridePath);
        for(var attr in overridingProperties) {
            properties[attr] = overridingProperties[attr];
        }
    }

    return properties;
}