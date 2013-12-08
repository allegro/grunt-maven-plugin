module.exports = {
    copy: {
        targetGrunt: { // copy from webapp static src to target-grunt
            files: [ { expand: true, cwd: '../src/main/webapp/static/', src: './**', dest: './' } ]
        },
        dist: { // copy results of Grunt compilation to target-grunt/dist
            files: [ { expand: true, src: ['app.min.js', 'js/**', '!js/test/**'], dest: 'dist/' } ]
        },
        targetMaven: { // copy dist files to exploded WAR target
            files: [ { expand: true, cwd: './dist', src: ['./**'], dest: '../target/<war_name>/static/' } ]
        }
    },
    watch: {
        maven: { // observe files in webapp static src
            files: '../src/main/webapp/static/**',
                tasks: ['copy:targetGrunt', 'default']
        }
    },
};