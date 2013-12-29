
var path = require('path');

module.exports = function(grunt) {

    var mavenTasksPath = path.resolve('maven-tasks');

    var mavenProperties = grunt.file.readJSON(path.join(mavenTasksPath, 'maven-inner-properties.json'));
    var workflowProperties = grunt.file.readJSON('maven-workflow-properties.json');

    var copySrcToGruntTargetFiles = ['./**'];
    for(var i = 0; i < mavenProperties.filteredFiles.length; ++i) {
        copySrcToGruntTargetFiles.push('!./' + mavenProperties.filteredFiles[i]);
    }

    var copySrcToGruntTaget = {
        files: [{
                expand: true,
                cwd: path.join(mavenProperties.projectRootPath, mavenProperties.sourceDirectory, mavenProperties.jsSourceDirectory),
                src: copySrcToGruntTargetFiles,
                dest: './'
            }]
    };

    var copyGruntTargetToDist = {
        files: [{
                expand: true,
                src: workflowProperties.distFilePatterns,
                dest: workflowProperties.distDirectory
            }]
    };

    var copyDistToMavenTarget = {
        files: [{
                expand: true,
                cwd: workflowProperties.distDirectory,
                src: ['./**'],
                dest: path.join(mavenProperties.targetPath, workflowProperties.warName, mavenProperties.jsSourceDirectory)
            }]
    };


    var watchedFiles = path.join(mavenProperties.projectRootPath, mavenProperties.sourceDirectory, mavenProperties.jsSourceDirectory) + "/**";

    grunt.config.set('copy.mavenSrcToGruntTaget', copySrcToGruntTaget);
    grunt.config.set('watch.maven.files', watchedFiles);

    grunt.registerTask('maven', 'grunt-maven workflow task.', function() {

        grunt.config.set('copy.mavenGruntTargetToDist', copyGruntTargetToDist);
        grunt.config.set('copy.mavenDistToMavenTarget', copyDistToMavenTarget);

        grunt.task.run(['copy:mavenGruntTargetToDist', 'copy:mavenDistToMavenTarget']);
    });

    grunt.registerTask('maven-watch', 'grunt-maven workflow task.', function() {
        grunt.task.run(['copy:mavenSrcToGruntTaget']);
    });

};
