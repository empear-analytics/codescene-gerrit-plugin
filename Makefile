build:
	rm -rf target
	rm -f tsconfig.tsbuildinfo
	tsc --build --verbose
	mvn compile
	esbuild target/web/plugin.js --bundle --outfile=target/classes/static/codescene.js
	mvn package
	mv target/codescene-gerrit-plugin*.jar target/codescene.jar