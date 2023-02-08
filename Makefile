
libs: package-libs
docs:
	echo 'No Docs'

package-libs:
	VERSION=${VERSION} gradle clean build publishToMavenLocal publish
