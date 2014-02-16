module.exports = function(grunt) {
	// load all grunt tasks
	require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

	// Do grunt-related things in here
	grunt.initConfig({
		// The grunt-maven-plugin provides the following Maven properties in grunt-maven.json:
		// 	 .finalName: 			${finalName},
		//   .filesToWatch: 		${sourceDirectory}/${jsSourceDirectory} default: src/main/webapp/ static /**
		//   .directoryToWatch: 	${sourceDirectory}/${jsSourceDirectory} default: src/main/webapp/ static
		//   .projectRootPath: 		${baseDir},								/
		//   .gruntBuildDirectory: 	${gruntBuildDirectory}",				/target-grunt
		//   .targetPath: 			${project.build.directory}",			/target
		//   .sourceDirectory: 		${sourceDirectory}",					default: src/main/webapp/
		//   .jsSourceDirectory: 	${jsSourceDirectory}",					/static
		//   .filteredFiles": 		${filteredResources} as an array
		gruntMavenProperties: grunt.file.readJSON('grunt-maven.json'),

		// https://github.com/allegro/grunt-maven-npm
		// copy raw sources from Maven webapp sources to target-grunt
		mavenPrepare: {
			options: {
				resources: ['images/**', 'less/**', 'js/**']
			},
			dev: {
			},
			prod: {
				resources: ['**', '!dev/**']
			}
		},
		mavenDist: {
			options: {
				warName: 'war',
				deliverables: ['**', '!non-deliverable.js'],
				gruntDistDir: 'dist'
			},
			dev: {
				warName: 'war-dev'
			}
		}
	});

	grunt.registerTask('default', ['mavenPrepare', 'mavenDist']);
};